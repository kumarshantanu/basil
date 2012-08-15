(ns basil.public
  (:require [clojure.java.io :as io]
            [clojure.string  :as str]
            [basil.core  :as core]
            [basil.error :as error]
            [basil.group :as group]
            [basil.slot  :as slot]
            [basil.types :as types]
            [basil.util  :as util]
            [basil.vars  :as vars])
  (:use ;;--not used--;*CLJSBUILD-REMOVE*;-macros
    [basil.core-macro :only [defn-binding]])
  (:import (java.io  File)
           (java.net URL)
           (java.util.regex Pattern)))


(def slot-compiler (slot/make-slot-compiler read-string))


(defn err-handler
  "Error handler for the JVM that throws exception instead of error messages."
  [text] {:pre [(types/error-text? text)]}
  (throw (RuntimeException. ^String (str (:text text)))))


(defn init-vars
  []
  (alter-var-root #'vars/*char?*    (constantly char?))
  (alter-var-root #'vars/*re-quote* (constantly #(re-pattern (Pattern/quote %)))))


;; Initialize platform-specific vars so that we don't need to rebind them
(init-vars)


;; ----- Public functions from basil.core


(defn-binding [error/*error* err-handler] parse-template)
(defn-binding [error/*error* err-handler] compile-template)
(defn-binding [error/*error* err-handler] parse-compile)
(defn-binding [error/*error* err-handler] compile-template-group)
(defn-binding [error/*error* err-handler] render-template)
(defn-binding [error/*error* err-handler] render-by-name)
(defn-binding [error/*error* err-handler] parse-compile-render)


;; ----- End of public functions from basil.core


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
    (group/make-group (fn [name]
                        (if-let [cache-value (let [[t v] (find-last name)]
                                               (and is-cache? t
                                                    (< (- (f-now) t) cache-millis)
                                                    v))]
                          cache-value
                          (let [[t e] (f-obtain name)]
                            (if e (kill-last name)
                              (save-last name [t e]))
                            [t e]))))))


(defn parse-compile-resource
  "Parse and compile the template after loading it"
  [resource-name resource-ptr & args] {:post [(types/compiled-template? %)]
                                       :pre  [(string? resource-name)
                                              (not (nil? resource-ptr))]}
  (apply core/parse-compile slot-compiler (slurp resource-ptr) resource-name
         args))


(defn make-group-from-directory
  "Create a template group from a filesystem directory.
  Optional arguments:
   prefix (string)     this text is prefixed to the template name before lookup
   cache-millis (long) how long to cache template (default: 0 disables caching)"
  [& {:keys [prefix cache-millis]
      :or {prefix       ""
           cache-millis 0}
      :as opt}]
  {:pre [(string? prefix)
         (and (number? cache-millis)
              (not (neg? cache-millis)))]}
  (let [cnam (partial str prefix)
        args (flatten (seq (dissoc opt :cache-millis)))]
    (make-cached-group
      #(System/currentTimeMillis)
      #(let [rname (cnam %)
             rfile (io/as-file rname)]
         [(apply parse-compile-resource rname rfile args) nil])
      cache-millis)))


(defn make-group-from-classpath
  "Create a template group from the classpath.
  Optional arguments:
   prefix (string)     this text is prefixed to the template name before lookup
   as-url (function)   accepts template name, returns a URL suitable for slurp
   cache-millis (long) how long to cache template (default: 0 disables caching)"
  [& {:keys [prefix cache-millis as-url]
      :or {prefix       ""
           cache-millis 0  ; 0 = no-caching
           as-url       io/resource}
      :as opt}]
  {:pre [(string? prefix)
         (and (number? cache-millis)
              (not (neg? cache-millis)))
         (fn? as-url)]}
  (let [nols (fn [^String s] ; Remove leading slashes
               (util/redstr (drop-while #(= % \/) s)))
        cnam (partial (comp nols str) prefix)
        args (flatten (seq (dissoc opt :prefix :cache-millis :as-url)))]
    (make-cached-group
      #(System/currentTimeMillis)
      #(let [rname (cnam %)
             r-url  (as-url rname)]
         [(apply parse-compile-resource rname r-url args) nil])
      cache-millis)))


(defn clojure-core-publics
  "Return the public vars in clojure.core as a map, suitable for use as locals."
  []
  (reduce (fn [m [n v]]
            (merge m {(keyword n) (deref v)}))
          {} (filter first (ns-publics 'clojure.core))))
