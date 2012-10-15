(ns basil.group
  (:require [basil.error  :as error]
            [basil.render :as render]
            [basil.slot   :as slot]
            [basil.types  :as types]
            [basil.util   :as util]
            [basil.vars   :as vars])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [basil.util-macro :only [verify]]))


(defprotocol TemplateGroup
  "Template Group functions"
  (obtain  [this name] "Return a vector [template error-text] by name"))


(defn template-group?
  "Return true if `group` is a valid template group, false otherwise"
  [group]
  (satisfies? TemplateGroup group))


(defn get-template
  [group name] {:pre (template-group? group)}
  (obtain group name))


(def no-text (types/make-static-text "" 0 0 0))


(defn no-such-name
  "Return a No Such Resource error"
  [name]
  (error/render-error
    (types/make-error-text
      (format "No such template name/key: '%s'" (str name))
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
  "Given group        template group
         f-now        (no-arg fn that returns current time in millis),
     and cache-millis (long)
  create a cached group that re-obtains template only after cache-millis or
  more milliseconds have elapsed since last obtain."
  [group f-now cache-millis] {:pre [(template-group? group)
                                    (fn? f-now)
                                    (number? cache-millis)]}
  (let [last-read (atom [(f-now) {}])  ;; flush-ts , name => [timestamp value]
        update-ca (fn [[flush-ts last-map] name]
                    (let [now (f-now)
                          [flush-ts last-map]
                          (if (< (- now flush-ts) cache-millis)
                            [flush-ts last-map]
                            [now (reduce (fn [m [k [t v]]]
                                           (merge m (when (< (- now t)
                                                             cache-millis)
                                                      {k [t v]})))
                                         {} last-map)])]
                      [flush-ts (if (let [[ts value] (get last-map name)]
                                      (and ts (< (- now ts) cache-millis) value))
                                  last-map
                                  (assoc last-map
                                         name [now (get-template group name)]))]))]
    (if (not (pos? cache-millis))
      group
      (make-group (fn [name] (-> last-read
                               (swap! update-ca name)
                               (get-in [1 name 1])))))))


(def ^{:dynamic true
       :doc "Group of templates paired by template-name and compiled-text"}
  *template-group* (make-group-from-map {}))


(defn render-by-name*
  "Find template by name from specified/current template group and render it"
  ([template-group template-name]
    {:pre [(template-group? template-group)
           (not (nil? template-name))]}
    (let [[compiled-template error-text] (->> (obtain template-group template-name)
                                              (verify util/pair?))]
      (if error-text
        (:text error-text)
        (binding [vars/*template-name* template-name]
          (render/render-template* compiled-template)))))
  ([template-name]
    {:pre [(template-group? *template-group*)]}
    (render-by-name* *template-group* template-name)))