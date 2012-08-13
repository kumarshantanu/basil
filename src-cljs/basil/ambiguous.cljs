(ns basil.ambiguous)


(defn char?
  [x]
  (string? x))


(defn re-quote
  [x]
  (re-pattern x))