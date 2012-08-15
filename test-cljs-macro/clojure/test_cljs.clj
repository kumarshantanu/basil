;*CLJSBUILD-MACRO-FILE*;

(ns clojure.test-cljs
  (:use [basil.util :only [*try-catch*]]))


(defmacro deftest
  [the-name & body]
  `(defn ~the-name
     []
     (do (println "Running test" ~(str the-name))
       ~@body)))


(defmacro testing
  [msg & body]
  `(do (println "Testing" ~msg)
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
  `(try-catch #(do ~@body)
              (constantly true)))


(defmacro thrown-with-msg?
  "Rebind *try-catch* before calling this, so that the effect is same as:
  `(try ~@body
     (catch ~'js/Error err#
       (boolean (re-find ~msg-regex (.message err#)))))"
  [ex-class msg-regex & body]
  `(try-catch #(do ~@body)
              (fn [err#] (boolean (re-find ~msg-regex (.message err#))))))