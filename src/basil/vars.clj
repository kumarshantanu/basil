(ns basil.vars)


;; "Freestanding dynamic vars"
;; Docstring not allowed in ns macro in ClojureScript yet
;; http://dev.clojure.org/jira/browse/CLJS-86


(def ^{:dynamic true
       :doc     "Name of the template; used for generating error messages"}
  *template-name* "noname")


(def ^{:dynamic true
       :doc     "Tab width to be considered while parsing template"}
  *tab-width* 1)


(def ^{:dynamic true
       :doc     "The slot-text (Text instance) currently being rendered"}
  *slot-text* nil)


(def ^{:dynamic true
       :doc     "Collections of locals for the current rendering"}
  *locals-coll* [])