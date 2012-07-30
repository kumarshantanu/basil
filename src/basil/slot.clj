(ns basil.slot
  (:require [basil.error :as error]
            [basil.types :as types]
            [basil.util  :as util]
            [basil.vars  :as vars]))


;; slot-compiler -- parses/compiles a slot-text into a function that renders it

(defn locals-coll?
  [x]
  (and (some #(% x) [list? vector? set? seq? nil?])
       (every? map? x)))

(defn local-val
  [k slot-text locals-coll] {:pre [(symbol? k)
                                   (types/slot-text? slot-text)
                                   (locals-coll? locals-coll)]}
  (let [r (some (fn [coll]
                  (cond
                    (contains? coll k)            [(get coll k)]
                    (and (symbol? k)
                         (contains? coll
                                    (keyword k))) [(get coll (keyword k))]))
                locals-coll)]
    (if r (first r)
      (:text (error/render-error
               (types/make-error-text
                 (format "No such local-key '%s'" k)
                 (:row slot-text) (:col slot-text) (:pos slot-text)))))))


(declare eval-slot)


(defn local-val-coll
  "Given an S-expression that is a collection, and a collection of locals"
  [sexp slot-text locals-coll] {:pre [(coll? sexp)
                                      (types/slot-text? slot-text)
                                      (locals-coll? locals-coll)]}
  (let [coll-fn (cond
                  (vector? sexp) vec
                  (set? sexp)    set
                  :otherwise     list*)]
    (coll-fn (map #(eval-slot % slot-text locals-coll) sexp))))


(defn eval-slot
  [sexp slot-text locals-coll] {:pre [(types/slot-text? slot-text)
                                      (locals-coll? locals-coll)]}
  (binding [vars/*slot-text* slot-text]
    (cond
      ;; symbol, hence lookup in locals
      (symbol? sexp)    (local-val sexp slot-text locals-coll)
      ;; function call
      (and (list? sexp)
           (seq sexp))  (let [rexp (local-val-coll sexp slot-text locals-coll)]
                          (apply (first rexp) (rest rexp)))
      ;; plain old collection
      (coll? sexp)      (local-val-coll sexp slot-text locals-coll)
      ;; primitive
      :otherwise        sexp)))


(defn make-slot-compiler*
  "Given an S-Expression reader, return a slot compiler that can parse/compile
  a slot-text into a function that renders it. You may not have to call this
  directly; invoke JVM or CLJS specific version instead."
  [reader]
  (fn [slot-text] {:pre [(types/slot-text? slot-text)]}
    (let [sexp (reader (:text slot-text))]
      (partial eval-slot sexp slot-text))))


(def make-slot-compiler (memoize make-slot-compiler*))


;(defn read-template
;  "
;  See: https://github.com/brentonashworth/one/blob/master/src/lib/clj/one/test.clj"
;  [template-body template-name slot-reader
;   & {:keys [err-handler escape-char]
;      :or {err-handler error/*error*
;           escape-char default-escape-char}}])
;
;(defn eval-template
;  [template & locals])