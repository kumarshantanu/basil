(ns basil.group
  (:require [basil.error  :as error]
            [basil.render :as render]
            [basil.slot   :as slot]
            [basil.types  :as types]
            [basil.util   :as util]
            [basil.vars   :as vars]))


(defprotocol TemplateGroup
  "Template Group functions"
  (obtain  [this name] "Return a vector [template error-text] by name"))


(defn template-group?
  "Return true if `group` is a valid template group, false otherwise"
  [group]
  (satisfies? TemplateGroup group))


(def no-text (types/make-static-text "" 0 0 0))


(defn no-such-name
  "Return a No Such Resource error"
  [name]
  (error/render-error
    (types/make-error-text
      (format "No such template name/key: '%s'" name)
      (or vars/*slot-text* no-text))))


(defn make-group
  "Make a template group based on the `f-obtain` function that accepts
  template-key as argument; returns template if exists, or nil"
  [f-obtain] {:post [(template-group? %)]
              :pre  [(fn? f-obtain)]}
  (reify TemplateGroup
    (obtain [this name] (f-obtain name))))


(defn make-group-union
  "Given a collection of groups create a union-group that finds template from
  any of them in a sequential order."
  [coll] {:post [(template-group? %)]
          :pre  [(seq coll)
                 (every? template-group? coll)]}
  (let [find-template (fn [needle group]
                        (let [[template _] (obtain group needle)]
                          (when template
                            [template nil])))]
    (make-group (fn [name]
                  (or (some (partial find-template name) coll)
                      [nil (no-such-name name)])))))


(defn make-group-from-map
  "Given a map of template-name to template-body, convert into a template-group
  and return the same."
  [group] {:post [(template-group? %)]
           :pre  [(map? group)
                  (every? types/compiled-template? (vals group))]}
  (make-group #(if (contains? group %)
                 [(get group %) nil]
                 [nil (no-such-name %)])))


(defn make-cached-group
  "Given f-now        (no-arg fn that returns current time in millis),
         f-obtain     (as in basil.group/make-group),
     and cache-millis (long)
  create a cached group that re-obtains template only after cache-millis or
  more milliseconds have elapsed since last obtain."
  [f-now f-obtain cache-millis]
  (let [is-cache? (zero? cache-millis)
        last-read (ref {})
        find-last #(get @last-read %)
        kill-last #(dosync (alter last-read dissoc %))
        save-last (fn [k v] (dosync (alter last-read assoc k [(f-now) v])))]
    (make-group (fn [name]
                  (if-let [cache-value (let [[t v] (find-last name)]
                                         (and is-cache? t
                                              (< (- (f-now) t) cache-millis)
                                              v))]
                    cache-value
                    (let [[t e] (f-obtain name)]
                      (if e (kill-last name)
                        (save-last name [t e]))
                      [t e]))))))


(def ^{:dynamic true
       :doc "Group of templates paired by template-name and compiled-text"}
  *template-group* (make-group-from-map {}))


(defn render-by-name*
  "Find template by name from specified/current template group and render it"
  ([template-group template-name]
    {:pre [(template-group? template-group)
           (not (nil? template-name))]}
    (let [[compiled-template error-text] (->> (obtain template-group template-name)
                                              (util/verify util/pair?))]
      (if error-text
        (:text error-text)
        (render/render-template* compiled-template))))
  ([template-name]
    {:pre [(template-group? *template-group*)]}
    (render-by-name* *template-group* template-name)))