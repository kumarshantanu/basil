(ns basil.core-macro
  (:require [basil.core :as core]))


(defmacro defn-binding
  [binding-vec f-name] {:pre [(vector? binding-vec)]}
  (let [q-name (symbol (str "core/" (name f-name)))
        f-doc  (str "See basil.core/" (name f-name))]
    `(defn ^:export ~f-name ~f-doc [& args#]
       (binding ~binding-vec (apply ~q-name args#)))))
