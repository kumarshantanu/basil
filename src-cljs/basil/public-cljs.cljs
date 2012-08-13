(ns basil.public-cljs
  (:require [cljs.reader :as reader]
            [basil.slot  :as slot]
            [basil.types :as types]))


(def slot-compiler (slot/make-slot-compiler reader/read-string))


(defn err-handler
  "Error handler for ClojureScript that throws exception instead of error messages."
  [text] {:pre [(types/error-text? text)]}
  (throw (js/Error ^String (str (:text text)))))
