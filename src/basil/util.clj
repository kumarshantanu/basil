(ns basil.util
  (:require [clojure.string :as str])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [basil.util-macro :only [verify]]))


;; ===== Diagnostics and debugging =====


(defn echo
  "Print msg, x and return x."
  ([msg x]
    (print msg "") (println x)
    (flush)
    x)
  ([x]
    (println x)
    (flush)
    x))


(def ^{:dynamic true
       :doc
       "Handler fn that takes 2 args `try-f` (fn that takes no arg) and `catch-f`
  (fn that takes the exception object as only arg.) You must rebind `try-catch`
  in CLJS as follows:

  (binding [*try-catch* (fn [try-f catch-f]
                          (try (try-f)
                            (catch js/Error err#
                              (catch-f err#)))]
    ...)"}
      *try-catch*
  (fn [try-f catch-f] (assert (not "Rebind *try-catch* as per documentation"))))


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
  (apply str "" coll))


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
