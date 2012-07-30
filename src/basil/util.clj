(ns basil.util
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]))


;; ===== Diagnostics and debugging =====


(defmacro echo
  "Print x and return it."
  [msg x]
  `(let [line# (->> (Throwable.)
                 (.getStackTrace)
                 (filter #(.startsWith (.getClassName %) "basil."))
                 first)]
     (print (format "\n[ECHO] %s:%d%s "
                    (.getFileName line#) (.getLineNumber line#) ~msg))
     (pp/pprint ~x)
     (flush)
     ~x))


(defmacro verify
  "Assert that (pred x) returns logical true, then returns x. `pred` may be a
  fn, or something that can be invoked with one argument."
  [pred x]
  `(do
     (try (assert (~pred ~x))
       (catch AssertionError e#
         (throw (AssertionError.
                  (str (.getMessage e#) " - found (" (type ~x) ")\n"
                       (with-out-str
                         (try (pp/pprint ~x)
                           (catch Exception _# (println ~x)))))))))
     ~x))


;; ===== Tuple utilities =====


(defn n-tuple?
  [n x] {:pre [(number? n)]}
  (and (vector? x)
       (= n (count x))))


(defn pair?
  [x]
  (n-tuple? 2 x))


(defn as-pair
  [x]
  (if (pair? x) x
    (do
      (verify #(not (vector? %)) x)  ;; ensure not already a tuple
      [x nil])))


;; ===== String utilities =====


(defn redstr
  "Shortcut for (reduce str \"\" coll)"
  [coll] {:post [(string? %)]
          :pre  [(or (coll? coll)
                     (seq? coll))]}
  (reduce str "" coll))


(defn drop-last-while
  "Repeatedly drop the last element of `coll` as long as `pred` returns true
  when applied to each element."
  [pred coll]
  (cond (empty? coll) nil
        (pred (last coll)) (recur pred (drop-last coll))
        :else coll))

(def ^{:doc "Whitespace chars; can also be used as a function"}
  whitespace-chars #{\tab \newline \formfeed \return \space})

(defn trim
  [s]
  (apply str (drop-while whitespace-chars (drop-last-while whitespace-chars s))))


;(println (format "*%s*" (trim nil)))
;(println (format "*%s*" (trim "  Hello World")))
;(println (format "*%s*" (trim "Hello World  ")))
;(println (format "*%s*" (trim " Hello World ")))
;(println (format "*%s*" (trim "")))


;; ===== HTML Entities =====


(def html-xlat {;; \space "&nbsp;"  ; not required, since we use the <pre/> tag
                \< "&lt;"
                \> "&gt;"
                \& "&amp;"
                ;; \' "&apos;"  ; doesn't work on Internet Explorer
                \" "&quot;"})

(defn html-entities
  "Convert `text` (string or sequence of chars) to HTML entities and return it"
  [text]
  (redstr (map #(or (html-xlat %) %)  text)))
