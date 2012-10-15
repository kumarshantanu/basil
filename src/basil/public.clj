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
  (:use
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
  (alter-var-root #'vars/x-char?  (constantly char?))
  (alter-var-root #'vars/re-quote (constantly #(if (string? %)
                                                 (re-pattern (Pattern/quote %))
                                                 %))))


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
    (group/make-cached-group
      (group/make-group
        #(let [rname (cnam %)
               rfile (io/as-file rname)]
           [(apply parse-compile-resource rname rfile args) nil]))
      #(System/currentTimeMillis)
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
    (group/make-cached-group
      (group/make-group
        #(let [rname (cnam %)
               r-url  (as-url rname)]
           [(apply parse-compile-resource rname r-url args) nil]))
      #(System/currentTimeMillis)
      cache-millis)))


(defn clojure-core-publics
  "Return the public vars in clojure.core as a map, suitable for use as context."
  []
  (reduce (fn [m [n v]]
            (merge m {(keyword n) (deref v)}))
          {} (filter first (ns-publics 'clojure.core))))
