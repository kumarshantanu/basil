(ns basil.lib
  "Library of filter/handler functions"
  (:require [basil.group    :as group]
            [clojure.string :as str]))


(defn clojure-core-publics
  "Return the public vars in clojure.core as a map, suitable for use as locals."
  []
  (reduce (fn [m [n v]]
            (merge m {(keyword n) (deref v)}))
          {} (filter first (ns-publics 'clojure.core))))


(defn for-each
  ([locals k-bindings f args]
    {:pre [(map? locals)
           (coll? k-bindings)
           (not (map? k-bindings))
           (even? (count k-bindings))
           (not (nil? f))
           (or (nil? args) (coll? args))]}
    (let [pairs (partition 2 k-bindings)
          [[k s] & more] pairs]
      (if (seq more)
        (apply concat (for [each s]
                        (apply for-each (merge locals {k each}) more f args)))
        (for [each s]
          (->> args
               (map #(get (merge locals {k each}) % %))
               (apply f))))))
  ([k-bindings f args]
    (for-each {} k-bindings f args)))


(defn format-rows
  "Given a collection of `rows` (each row will be turned into a string) and
  pairs of alternating head/tail tokens (repeated to match the `rows` count),
  concatenate `rows` each prefixed with head and suffixed with tail."
  [rows decors] {:pre [(coll? decors)
                       (not (empty? decors))
                       (or (nil? rows)
                           (coll? rows))]}
  (reduce str (map (fn [[head tail] text]
                     (format "%s%s%s" head text tail))
                   (partition 2 (flatten (repeat decors)))
                   rows)))


(defn serial-rows
  [rows serial decors-format] {:pre [(coll? decors-format)
                                        (not (empty? decors-format))]}
  (format-rows rows
               (map (fn [n [head-format tail-format]]
                      [(format head-format n) (format tail-format n)])
                    serial (partition 2 (flatten (repeat decors-format))))))


(def ^{:doc "Default handler functions"}
  default-handlers
  {:default     str
   :when        (fn [test f & more] (when     test (apply f more)))
   :when-not    (fn [test f & more] (when-not test (apply f more)))
   :for-each    (fn [k-bindings f & more] (for-each k-bindings f more))
   :format-rows (fn [rows decor & more]
                  (format-rows rows (into [decor] more)))
   :serial-rows (fn [rows serial decor-format & more]
                  (serial-rows rows serial
                               (into [decor-format] more)))
   :str         str
   :str-join    str/join
   :str-newline (partial str/join "\n")
   :html-tr     (fn [rows] (format-rows rows "<tr>" "</tr>\n"))
   :html-li     (fn [rows] (format-rows rows "<li>" "</li>\n"))
   :include     group/render-by-name*})


(def ^{:doc "Default model"}
  default-model
  {:one-onward  (iterate inc 1)
   :zero-onward (iterate inc 0)
   :all-odd     (filter odd?  (iterate inc 1))
   :all-even    (filter even? (iterate inc 0))
   :pos-even    (filter even? (iterate inc 2))})