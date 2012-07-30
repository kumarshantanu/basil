;*CLJSBUILD-MACRO-FILE*;

(ns clojure.test-cljs)


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
  [ex-class & body]
  `(try ~@body
     (catch err#
       true)))


(defmacro thrown-with-msg?
  [ex-class msg-regex & body]
  `(try ~@body
     (catch err#
       (boolean (re-find ~msg-regex (.message err#))))))