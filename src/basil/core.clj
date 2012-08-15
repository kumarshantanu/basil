(ns basil.core
  (:require [clojure.string :as str]
            [basil.error    :as error]
            [basil.group    :as group]
            [basil.lib      :as lib]
            [basil.render   :as render]
            [basil.slot     :as slot]
            [basil.types    :as types]
            [basil.util     :as util]
            [basil.vars     :as vars]))


(defn rowcolpos
  "Given `row`, `col`, `pos` and `chr` return a vector containing the new
  [row col pos] position. Row, column and position are 1-based and NOT 0-based.
  Horizontal tab is considered 1 character unless overriden via *tab-width*."
  [row col pos chr] {:post [(vector? %)
                            (let [[r c p] %]
                              (and (number? r)
                                   (number? c)
                                   (number? p)))]
                     :pre [(number? row)
                           (number? col)
                           (number? pos)
                           (or (nil? chr)
                               (vars/*char?* chr))]}
  (cond
    (nil? chr)       [row       col                            pos]
    (= chr \newline) [(inc row) 1                              (inc pos)]
    (= chr \return)  [row       1                              (inc pos)]
    (= chr \tab)     [row      (+ col (or vars/*tab-width* 1)) (inc pos)]
    :otherwise       [row      (inc col)                       (inc pos)]))


;; ===== Template parsing =====


(def ^{:doc "Default escape character to be used for parsing the template"}
      default-escape-char \\)


(defn parse-slot
  "Parse given `template-string` assuming it is already in a slot, and
  return a 2-element vector containing:
  1. slot-text (or parse-error message) as a Text instance
  2. remaining template-string (static-text) as a Text instance"
  [text escape-char] {:post [(vector? %)
                             (let [[st rt] %
                                   s (or (types/slot-text? st)
                                         (types/error-text? st))
                                   r (types/static-text? rt)]
                               (and s r))]}
  (loop [slot   []
         buffer []
         templt (seq (:text text))
         rownow (:row text)
         colnow (:col text)
         posnow (:pos text)]
    (let [E     escape-char
          each  (first templt)
          buf=  #(= buffer %)
          [row
           col
           pos] (rowcolpos rownow colnow posnow each)]
      (cond
       (empty? templt)   [(error/parse-error
                            (types/make-error-text
                              (format "Invalid/incomplete slot '%s'"
                                      (util/redstr (concat slot buffer)))
                              (:row text) (:col text) (:pos text)))
                          (types/make-static-text "" rownow colnow posnow)]
       (and (buf= [\%])
            (= each \>)) [(types/make-slot-text
                            (str/trim (util/redstr slot))
                            (:row text) (:col text) (:pos text))
                          (types/make-static-text (rest templt) row col pos)]
       :otherwise
       (let [pass  (fn ([]     [(conj slot each) []])
                     ([v & vs] [(apply conj slot v vs) []]))
             buff  (fn ([]     [slot [each]])
                     ([v & vs] [slot (into [v] vs)]))
             [s b] (cond
                     (empty?
                       buffer)     (if (#{E \%} each) (buff) (pass))
                     (buf= [E])    (cond
                                     (= each E)      (pass)
                                     (#{\< \%} each) (buff E each)
                                     :otherwise      (pass E each))
                     (buf= [E \<]) (if (= each \%) (pass \< \%) (pass E \< each))
                     (buf= [E \%]) (if (= each \>) (pass \% \>) (pass E \% each))
                     (buf= [\%])   (pass \% each)
                     :otherwise    (pass))]
         (recur s b (rest templt) row col pos))))))


(defn parse-static
  "Given a character sequence, parse and return a Template instance with
  :content being a lazy sequence of alternating static and slot Text instances"
  [template escape-char] {:post [(types/parsed-template? %)]
                          :pre  [(types/raw-template? template)]}
  (let [E escape-char
        f (fn parse [text] {:pre [(types/text? text)]}
            (loop [result []
                   buffer []
                   templt (:text text)
                   rownow (:row text)
                   colnow (:col text)
                   posnow (:pos text)]
              (if (empty? templt)
                (cons (types/make-static-text (util/redstr (concat result buffer))
                                              (:row text) (:col text) (:pos text))
                      (lazy-seq))
                (let [each  (first templt)
                      [row
                       col
                       pos] (rowcolpos rownow colnow posnow each)
                      buf=  #(= buffer %)
                      pass  (fn ([]     [true (conj result each) []])
                              ([v & vs] [true (apply conj result v vs) []]))
                      buff  (fn ([]     [true result [each]])
                              ([v & vs] [true result (into [v] vs)]))
                      slot  #(let [[st rt] (parse-slot
                                             (types/make-static-text (rest templt)
                                                                     row col pos)
                                             escape-char)]
                               [false (cons  ; parse slot, continue (no recur)
                                        (types/make-static-text
                                          (util/redstr result)
                                          (:row text) (:col text) (:pos text))
                                        (cons st (lazy-seq (parse rt))))])
                      [recur? & v]
                      (cond
                        (empty?
                          buffer)     (if (#{E \<} each) (buff) (pass))
                        (buf= [E])    (cond
                                        (= each E)      (pass)
                                        (#{\< \%} each) (buff E each)
                                        :otherwise      (pass E each))
                        (buf= [E \%]) (if (= each \>) (pass \% \>) (pass E  \% each))
                        (buf= [E \<]) (if (= each \%) (pass \< \%) (pass E  \< each))
                        (buf= [\<])   (if (= each \%) (slot)       (pass \< each))
                        :otherwise    (pass))]
                  (if recur?
                    (let [[s b t] v] (if (nil? t)
                                       (recur s b (rest templt) row col pos)
                                       (recur s b t row col pos)))
                    (first v))))))]
    (types/make-parsed-template
      (f (types/make-static-text (:content template) 1 1 1)) (:name template))))


(defn parse-template
  "Parse template and return a sequence of (alternating static and slot/error)
  Text instances.
  Note: basil.error/*error* must be already rebound before this call."
  ([template-string]
    (parse-template template-string (str (gensym))))
  ([template-string template-name]
    (parse-template template-string template-name default-escape-char))
  ([template-string template-name escape-char]
    {:post [(types/parsed-template? %)]
     :pre  [(string? template-string)
            (not (nil? template-name))]}
    (binding [vars/*template-name* (or template-name vars/*template-name*)]
      (parse-static (types/make-raw-template template-string template-name)
                    escape-char))))


;; ===== Template Compilation =====


(defn compile-text
  "Given a (static, slot or error) text, return a function that will return the
  string in a rendered form. `slot-reader` parses/compiles a slot-text into a
  function that renders it as string at runtime."
  [slot-reader text] {:post [(fn? %)]
                      :pre  [(types/text? text)
                             (fn? slot-reader)]}
  (let [t (:text text)]
    (if (types/slot-text? text)
      (slot-reader text)
      (constantly t))))


(defn compile-template
  "Compile a parsed template into a ready-to-render format.
  Note: basil.error/*error* must be already rebound before this call."
  ([slot-reader parsed-template]
    (compile-template slot-reader parsed-template
                      (or (:name parsed-template) vars/*template-name*)))
  ([slot-reader parsed-template template-name]
    {:post [(types/compiled-template? %)
            (= (:name parsed-template) (:name %))]
     :pre  [(types/parsed-template? parsed-template)
            (fn? slot-reader)]}
    (binding [vars/*template-name* template-name]
      (types/make-compiled-template
        (doall (map (partial compile-text slot-reader)
                    (:content parsed-template)))
        (:name parsed-template)))))


(defn parse-compile
  "Convenience function to parse and compile a template in one step.
  Note: basil.error/*error* must be already rebound before this call."
  [slot-reader ^String template ^String template-name]
  (->> (parse-template template template-name)
       (compile-template slot-reader)))


(defn compile-template-group
  "Given a map of template-name to template-body, convert into a template-group
  and return the same. You can pass the same optional args that you pass when
  parsing and compiling templates.
  Note: basil.error/*error* must be already rebound before this call."
  [slot-reader group] {:post [(group/template-group? %)]
                       :pre  [(map? group)]}
  (let [cgp (reduce
              (fn [m n]
                (assoc m n
                       (let [b (get group n)]
                         (cond
                           (types/compiled-template? b) b
                           (types/parsed-template? b) (compile-template
                                                        slot-reader b)
                           :otherwise                 (parse-compile
                                                        slot-reader b n)))))
              {} (keys group))]
    (group/make-group-from-map cgp)))


;; ===== Template Rendering =====


(defn add-default-locals
  "Append default locals to the locals collection."
  [locals-coll]
  (concat locals-coll [lib/default-model lib/default-handlers]))


(defn ^String render-template
  "Render given `compiled-template` using `locals-coll`.
  Note: basil.error/*error* must be already rebound before this call."
  [compiled-template locals-coll]
  {:post [(string? %)]
   :pre  [(types/compiled-template? compiled-template)
          (slot/locals-coll? locals-coll)]}
  (binding [vars/*template-name* (:name compiled-template)
            vars/*locals-coll*   (add-default-locals locals-coll)]
    (render/render-template* compiled-template)))


(defn render-by-name
  "Given a template group, find the compiled template by name and render it.
  Note: basil.error/*error* must be already rebound before this call."
  [template-group template-name locals-coll]
  {:post [(string? %)]
   :pre  [(group/template-group? template-group)
          (slot/locals-coll? locals-coll)]}
  (binding [vars/*template-name*   template-name
            vars/*locals-coll*     (add-default-locals locals-coll)
            group/*template-group* template-group]
    (group/render-by-name* template-name)))


(defn parse-compile-render
  "Convenience function to parse, compile and render a template in one step.
  Note: basil.error/*error* must be already rebound before this call."
  [slot-reader ^String template ^String template-name locals-coll]
  (-> (parse-compile slot-reader template template-name)
      (render-template locals-coll)))