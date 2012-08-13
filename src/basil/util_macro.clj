(ns basil.util-macro)


;; ===== Diagnostics and debugging =====


(defmacro verify
  "Assert that (pred x) returns logical true, then returns x. `pred` may be a
  fn, or something that can be invoked with one argument."
  [pred x]
  `(do (assert (~pred ~x))
     ~x))
