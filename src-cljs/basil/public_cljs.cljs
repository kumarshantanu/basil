(ns basil.public-cljs
  (:require [cljs.reader :as reader]
            [basil.core  :as core]
            [basil.error :as error]
            [basil.slot  :as slot]
            [basil.types :as types]
            [basil.vars  :as vars])
  (:use-macros ;;--not used in CLJS file--;*CLJSBUILD-REMOVE*;-macros
    [basil.core-macro :only [defn-binding]]))


(def slot-compiler (slot/make-slot-compiler reader/read-string))


(defn err-handler
  "Error handler for ClojureScript that throws exception instead of error messages."
  [text] {:pre [(types/error-text? text)]}
  (throw (js/Error ^String (str (:text text)))))


(defn init-vars
  []
  (set! vars/*char?*    string?)
  (set! vars/*re-quote* re-pattern))


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
