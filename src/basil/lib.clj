(ns basil.lib
  "Library of filter/handler functions"
  (:require [basil.group    :as group]
            [basil.util     :as util]
            [basil.vars     :as vars]
            [clojure.string :as str]))


(defn auto-str
  "Automaticlly convert `x` to appropriate representation of string."
  [x]
  (cond (string? x) x
        (coll? x)   (->> (map auto-str x)
                      (str/join \newline))
        (seq? x)    (->> (map auto-str x)
                      (str/join \newline))
        :otherwise  (str x)))


(defn partiar
  "Like `clojure.core/partial`, but applies cached aruments *after* the new
  arguments."
  [f & args]
  (fn [& fargs]
    (apply f (concat fargs args))))


(defn entities
  "Given a map `m` and a string `x`, replace every character in `x` by looking
  up in `m`, combine the result as a string and return it."
  [m x] {:pre [(map? m)
               (string? x)]}
  (->> (seq x)
    (map #(get m (str %) %))
    (util/redstr)))


(def html-entities-map
  {"&" "&amp;"    ;Ampersand	0026
   "<" "&lt;"     ;Less than	003C
   ">" "&gt;"     ;Greater than	003E
   ;; ""  "&nbsp;"   ;Non-breaking space	00A0 (non-effective, key is empty)
   "¢" "&cent;"   ;Cent	00A2
   "£" "&pound;"  ;Pound	00A3
   "©" "&copy;"   ;Copyright	00A9
   "«" "&laquo;"  ;Left-pointing double angle quotation mark	00AB
   "®" "&reg;"    ;Registered trademark	00AE
   "°" "&deg;"    ;Degree	00B0
   "·" "&middot;" ;Middle dot	00B7
   "»" "&raquo;"  ;Right-pointing double angle quotation mark	00BB
   "½" "&frac12;" ;Vulgar fraction one half	00BD
   "×" "&times;"  ;Multiplication	00D7
   "÷" "&divide;" ;Division	00F7
   "–" "&ndash;"  ;En dash	2013
   "—" "&mdash;"  ;Em dash	2014
   "‘" "&lsquo;"  ;Left single quotation mark	2018
   "’" "&rsquo;"  ;Right single quotation mark	2019
   "“" "&ldquo;"  ;Left double quotation mark	201C
   "”" "&rdquo;"  ;Right double quotation mark	201D
   "•" "&bull;"   ;Bullet	2022
   "…" "&hellip;" ;Horizontal ellipsis	2026
   "€" "&euro;"   ;Euro	20AC
   "™" "&trade;"  ;Trademark	2122
   "←" "&larr;"   ;Leftwards arrow	2190
   "↑" "&uarr;"   ;Upwards arrow	2191
   "→" "&rarr;"   ;Rightwards arrow	2192
   "↓" "&darr;"   ;Downwards arrow	2193
   })


(def html-entities (merge html-entities-map
                          (reduce (fn [m [k v]]
                                    (merge m {k v}))
                                  {} html-entities-map)))


(def nbsp-entities (merge html-entities
                          {" " "&nbsp;"} ;; in CLJS, chars are string (dup keys)
                          {(first " ") "&nbsp;"}))


(defn html-safe
  "Given a string `x`, return HTML-safe version of `x`."
  [x] {:pre [(string? x)]}
  (entities html-entities x))


(defn html-nbsp
  "Given a string `x`, return HTML-safe version of `x` such that spaces are
  converted to &nbsp;."
  [x] {:pre [(string? x)]}
  (entities nbsp-entities x))


(defn for-each
  "Given a map `locals`, a binding vector (keyword to sequable) `k-bindings`,
  function `f` and a collection of arguments `args`,
  * iterate through the sequable elements, re/binding corresponding keyword to
    the element in each pass
  * substitute in `args` the keywords bound in `k-bindings`
  * apply function `f` to modified `args`
  Example:
  (for-each [:a [1 2]
             :b [10 20]]
    str \":a = \" :a \", :b = \" :b)"
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
  "Given a collection `rows` (each row will be turned into a string) and
  pairs of alternating head/tail tokens (repeated to match the `rows` count),
  concatenate `rows` each prefixed with head and suffixed with tail."
  [rows decors] {:pre [(or (seq? decors)
                           (coll? decors))
                       (or (nil? rows)
                           (seq? rows)
                           (coll? rows))]}
  (reduce str (map (fn [[head tail] text]
                     (format "%s%s%s" head text tail))
                   (partition 2 (flatten (repeat decors)))
                   rows)))


(defn serial-decors
  "Given a collection of series data (eg. a range of numbers) and
  pairs of alternating head/tail 'format' tokens (repeated to `serial` count),
  return `decors` (see `format-rows` function) by applying `(format fmt each)`
  where `fmt` is either `head` or `tail` (from `decors-format`) and `each` is
  the element from `serial` during iteration.
  Example:
  (format-rows rows (serial-decors (iterate inc 1) [\"id=%d\" \"\n\"]))"
  [serial decors-format] {:pre [(or (seq? serial)
                                    (coll? serial))
                                (seq serial)
                                (or (coll? decors-format)
                                    (seq? decors-format))
                                (seq decors-format)]}
  (map (fn [n [head-format tail-format]]
         [(format head-format n) (format tail-format n)])
       serial (partition 2 (flatten (repeat decors-format)))))


(defn mapseq->keys
  "Given a sequence of maps `mapseq`, return the keys by looking at the first
  map element in the sequence."
  [mapseq]
  (keys (first mapseq)))


(defn mapseq->valrows
  "Given a sequence of maps `mapseq` (and optionally a sequence of keys `ks`),
  return a sequence of value sequences."
  ([mapseq]
    (mapseq->valrows mapseq (mapseq->keys mapseq)))
  ([mapseq ks]
    (map #(map % ks) mapseq)))


(defn html-th
  [rows]
  (format-rows rows ["<th>" "</th>\n"]))


(defn html-td
  [rows]
  (format-rows rows ["<td>" "</td>\n"]))


(defn html-tr
  ([rows]
    (html-tr rows html-td))
  ([rows html-td] {:pre [(fn? html-td)]}
    (format-rows (map html-td rows) ["<tr>\n" "</tr>\n"])))


(defn include
  "Find specified template by name in the current group and render it. Use the
  locals if specified."
  ([template-name]
    (binding [vars/*template-name* template-name]
      (group/render-by-name* template-name)))
  ([template-name & locals]
    {:pre [(every? map? locals)]}
    (binding [vars/*template-name* template-name
              vars/*locals-coll*   (concat locals vars/*locals-coll*)]
      (group/render-by-name* template-name))))


(def ^{:doc "Default filter functions"}
  default-handlers
  {;; generic string conversion
   :apply         apply
   :auto-str      auto-str
   :default       auto-str
   :seq           seq
   :str           str
   :str-join      str/join
   :str-br        (partial str/join "<br/>\n")
   :str-newline   (partial str/join "\n")
   ;; currying and HoF
   :identity      identity
   :partial       partial
   :partiar       partiar
   ;; conditionals and iteration (for the lack of macros)
   :when          (fn [test f & more] (when     test (apply f more)))
   :when-not      (fn [test f & more] (when-not test (apply f more)))
   :for-each      (fn [k-bindings f & more] (for-each k-bindings f more))
   :with-locals   (fn [locals f & args] {:pre [(every? map? locals)
                                               (not (nil? f))]}
                    (binding [vars/*locals-coll* (concat locals vars/*locals-coll*)]
                      (apply f args)))
   ;; formatting
   :format-rows   format-rows     ;; args -- [rows decors]
   :serial-decors serial-decors   ;; args -- [serial decor-format-coll]
   :mapseq->keys  mapseq->keys    ;; args -- [mapseq]
   :mapseq->rows  mapseq->valrows ;; args -- [mapseq] [mapseq ks]
   :html-tr       html-tr         ;; args -- [rows] [rows html-td]
   :html-th       html-th         ;; args -- [rows]
   :html-td       html-td         ;; args -- [rows]
   :html-li       (fn [rows] (format-rows rows ["<li>" "</li>\n"]))
   :html-option   (fn [rows] (format-rows rows ["<option>" "</option>"]))
   :html-option-v (fn [vals rows] (->> ["<option value='%s'>" "</option>"]
                                       (serial-decors vals)
                                       (format-rows rows)))
   ;; HTML-escaping
   :html-safe     html-safe
   :html-nbsp     html-nbsp
   ;; including other templates
   :include       include})


(def ^{:doc "Default model"}
  default-model
  {:one-onward  (iterate inc 1)
   :zero-onward (iterate inc 0)
   :all-odd     (filter odd?  (iterate inc 1))
   :all-even    (filter even? (iterate inc 0))
   :pos-even    (filter even? (iterate inc 2))})
