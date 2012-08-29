(ns basil.slot
  (:require [basil.error   :as error]
            [basil.types   :as types]
            [basil.util    :as util]
            [basil.vars    :as vars]
            [quiddity.core :as quid]))


(defn context-coll?
  [x]
  (and (some #(% x) [list? vector? set? seq? nil?])
       (every? map? x)))

(defn make-quiddity-err-handler
  [slot-text]
  (fn [msg]
    (error/render-error
      (types/make-error-text
        (str msg)
        (:row slot-text) (:col slot-text) (:pos slot-text)))))


(defn eval-entire-slot
  "Evaluate entire slot and return the value (string)."
  [sexp slot-text context-coll]
  (binding [vars/*slot-text*     slot-text
            quid/*error-handler* (make-quiddity-err-handler slot-text)]
    (let [to-str (quid/env-get :default context-coll)]
      (to-str (quid/evaluate sexp context-coll)))))


(defn make-slot-compiler*
  "Given an S-Expression reader, return a slot compiler that can parse/compile
  a slot-text into a function that renders it. You may not have to call this
  directly; invoke JVM or CLJS specific version instead."
  [reader]
  (fn [slot-text] {:pre [(types/slot-text? slot-text)]}
    (let [sexp (reader (:text slot-text))]
      (partial eval-entire-slot sexp slot-text))))


(def make-slot-compiler (memoize make-slot-compiler*))


;; slot-compiler -- parses/compiles a slot-text into a function that renders it


;See: https://github.com/brentonashworth/one/blob/master/src/lib/clj/one/test.clj