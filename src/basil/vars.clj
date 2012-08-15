(ns basil.vars
  "Freestanding dynamic vars")


;; ----- Template related -----


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


;; ----- Platform specific -----


(def ^{:dynamic true
       :doc "In JavaScript, every char is a string"}
      *char?* (fn [x] (assert (not "You must override *char?*"))))


(def ^{:dynamic true
       :doc "Equivalent of Pattern/quote"}
      *re-quote* (fn [x] (assert (not "You must override *re-quote*"))))


(def ^{:dynamic true
       :doc ""}
      *error* (fn [x] (assert (not "You must override *error*"))))