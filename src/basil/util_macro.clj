;*CLJSBUILD-MACRO-FILE*;

(ns basil.util-macro)


;; ===== Diagnostics and debugging =====


(defmacro verify
  "Assert that (pred x) returns logical true, then returns x. `pred` may be a
  fn, or something that can be invoked with one argument."
  [pred x]
  `(do (assert (~pred ~x))
     ~x))


(defmacro defn-binding
  [binding-vec f-name] {:pre [(vector? binding-vec)]}
  (let [q-name (symbol (str "core/" (name f-name)))
        f-doc  (str "See basil.core/" (name f-name))]
    `(defn ^:export ~f-name ~f-doc [& args#]
       (binding ~binding-vec (apply ~q-name args#)))))
