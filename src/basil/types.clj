(ns basil.types
  "Typed abstractions"
  (:require [basil.vars :as vars]))


;; [Text]
;;
;; --> `Text` is a chunk of text with a known location (consisting
;;     of row, column, stream position - all of them 1-based.)
;; --> Attributes :slot? and :error? specify whether it is a slot
;;     or an error message respectively.
;; --> A `slot` is the portion of text in a `template` that should
;;     be parsed and rendered dynamically using attributes external
;;     to the template.


(defrecord Text [text slot? error? row col pos])


(defn textable?
  "Return true if `s` can be turned into a Text, false otherwise"
  [s]
  (or (string? s)
      (and (coll? s)
           (every? vars/*char?* s))))


(defn text?
  "Return true if `s` is a Text instance, false otherwise"
  [s] (instance? Text s))


(defn make-static-text
  [text row col pos] {:pre [(textable? text)
                            (number? row)
                            (number? col)
                            (number? pos)]}
  (Text. text false false row col pos))


(defn make-slot-text
  [text row col pos] {:pre [(textable? text)
                            (number? row)
                            (number? col)
                            (number? pos)]}
  (Text. text true false row col pos))


(defn make-error-text
  ([text row col pos]
    {:pre [(textable? text)
           (number? row)
           (number? col)
           (number? pos)]}
    (Text. text true true row col pos))
  ([msg text]
    {:pre [(textable? msg)
           (text? text)]}
    (Text. msg true true (:row text) (:col text) (:pos text))))


(defn static-text? [s] (and (text? s) (not (:slot? s)) (not (:error? s))))
(defn slot-text?   [s] (and (text? s) (:slot? s)       (not (:error? s))))
(defn error-text?  [s] (and (text? s) (:error? s)))


(defn as-string
  "Return String representation of `s`"
  [s]
  (if (text? s) (:text s)
    (str s)))


;; ===== Template =====


(defrecord Template [content name])


(defn make-raw-template
  [content name] {:pre [(string? content)]}
  (Template. content name))


(defn raw-template?
  "Return true if `t` is a raw-template (i.e. String content), false otherwise"
  [t]
  (and (instance? Template t)
       (string? (:content t))))


(defn make-parsed-template
  [text-seq name] {:pre [(coll? text-seq)
                         (every? text? text-seq)]}
  (Template. text-seq name))


(defn parsed-template?
  "Return true if `t` s a parsed-template (i.e. collection of Text instances),
  false otherwise"
  [t]
  (and (instance? Template t)
       (coll? (:content t))
       (every? text? (:content t))))


(defn make-compiled-template
  [fn-seq name] {:pre [(coll? fn-seq)
                       (every? fn? fn-seq)]}
  (Template. fn-seq name))


(defn compiled-template?
  "Return true if `t` is a compiled template (i.e. collection of functions),
  false otherwise"
  [t]
  (and (instance? Template t)
       (coll? (:content t))
       (every? fn? (:content t))))


;; ===== Slot Tokens =====


(defrecord SlotToken [value literal? text])


(defn slot-token?
  "Return true if `s` is an instance of SlotToken, false otherwise"
  [s]
  (instance? SlotToken s))


(defn make-slot-symbol
  "Return a SlotToken instance that depicts a slot-symbol"
  [s ^Text text] {:pre [(slot-text? text)]}
  (SlotToken. s false text))


(defn slot-symbol?
  [s]
  (and (slot-token? s) (not (:literal? s))))


(defn make-slot-literal
  "Return a SlotToken instance that depicts a slot-literal"
  [s ^Text text] {:pre [(slot-text? text)]}
  (SlotToken. s true text))


(defn slot-literal?
  "Return true if `s` is a slot literal, false otherwise"
  [s]
  (and (slot-token? s) (:literal? s)))