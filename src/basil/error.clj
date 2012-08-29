(ns basil.error
  (:require [basil.types    :as types]
            [basil.util     :as util]
            [basil.vars     :as vars]))


;; === Rendering error messages ===


(def ^{:dynamic true
       :doc
       "A function accepting just one parameter (a raw error Text instance)
  that either throws a platform-specific exception or returns a Text
  instance with appropriatly rendered message (eg. HTML for a webpage)"}
  *error* (fn [& args]
            (assert (not "*error* rebound to error-handler"))))


(defn common-error
  [^String err-type text] {:pre [(types/error-text? text)]}
  "Return/throw an error"
  (*error*
    (assoc text :text
           (format "[%s] %s in '%s' at row %d, col %d (pos %d)"
                   err-type
                   (:text text) (str vars/*template-name*)
                   (:row text) (:col text) (:pos text)))))


(defn parse-error   [text] (common-error "Parse" text))
(defn compile-error [text] (common-error "Compile" text))
(defn render-error  [text] (common-error "Render" text))
