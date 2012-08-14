(ns basil.public-cljs
  (:require [cljs.reader :as reader]
            [basil.slot  :as slot]
            [basil.types :as types])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [basil.util-macro :only [defn-binding]]))


(def slot-compiler (slot/make-slot-compiler reader/read-string))


(defn err-handler
  "Error handler for ClojureScript that throws exception instead of error messages."
  [text] {:pre [(types/error-text? text)]}
  (throw (js/Error ^String (str (:text text)))))


;; ----- Public functions from basil.core


(defn-binding [error/*error* err-handler] parse-template)
(defn-binding [error/*error* err-handler] compile-template)
(defn-binding [error/*error* err-handler] parse-compile)
(defn-binding [error/*error* err-handler] compile-template-group)
(defn-binding [error/*error* err-handler] render-template)
(defn-binding [error/*error* err-handler] render-by-name)
(defn-binding [error/*error* err-handler] parse-compile-render)


;; ----- End of public functions from basil.core
