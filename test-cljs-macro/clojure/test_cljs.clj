;*CLJSBUILD-MACRO-FILE*;

(ns clojure.test-cljs
  (:use [basil.util :only [*try-catch*]]))


(defmacro deftest
  [the-name & body]
  `(defn ~the-name
     []
     (do (println (str "Running test " ~(str the-name)))
       ~@body)))


(defmacro testing
  [msg & body]
  `(do (println (str "Testing " ~msg))
     ~@body))


(defmacro is
  [& body]
  `(assert ~@body))


(defmacro thrown?
  "Rebind *try-catch* before calling this, so that the effect is same as:
  `(try ~@body
     (catch ~'js/Error err#
       true))"
  [ex-class & body]
  `(*try-catch* #(do ~@body false)
                (constantly true)))


(defmacro thrown-with-msg?
  "Rebind *try-catch* before calling this, so that the effect is same as:
  `(try ~@body
     (catch ~'js/Error err#
       (boolean (re-find ~msg-regex (.message err#)))))"
  [ex-class msg-regex & body]
  `(*try-catch* #(do ~@body false)
                (fn [err#]
                  (or (if (string? ~msg-regex)
                        (= ~msg-regex (.-message err#))
                        (boolean (re-find ~msg-regex (.-message err#))))
                      (println (str "Expected message:"
                                    (pr-str ~msg-regex)
                                    ", Actual mesage: "
                                    (pr-str (.-message err#))))))))