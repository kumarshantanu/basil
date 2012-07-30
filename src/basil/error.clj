(ns basil.error
  (:require [basil.types    :as types]
            [basil.util     :as util]
            [basil.vars     :as vars]))


;; === Rendering error messages ===


(defn text-error
  "Render error message as regular text"
  [text] {:post [(types/error-text? %)]
          :pre  [(types/error-text? text)]}
  (assoc text :text (str "ERROR: " (:text text))))


(defn html-error
  "Render error message as HTML snippet"
  [text] {:post [(types/error-text? %)]
          :pre  [(types/error-text? text)]}
  (assoc
    text :text
    (format "<pre style='color: white; background-color: red'>ERROR: %s</pre>"
            (util/html-entities (:text text)))))


(defn js-alert-error
  "Render error message as a JavaScript alert"
  [text] {:post [(types/error-text? %)]
          :pre  [(types/error-text? text)]}
  (assoc
    text :text
    (format "<script language='javascript'>alert(\"ERROR: %s\");</script>"
            (reduce str (map #(if (#{\' \"} %) (str \\ %) %) (:text text))))))


(def ^{:dynamic true
       :doc     "A function accepting just one parameter (a raw error Text
  instance) and returns a Text instance with correct rendering
  of the message. You may like to override this to raise a
  platform-specific exception or to render for a specific
  environment (eg. web.)"}
  *error* text-error)


(defn common-error
  [^String err-type text] {:pre [(types/error-text? text)]}
  "Return/throw an error"
  (*error*
    (assoc text :text
           (format "[%s] %s in '%s' at row %d, col %d (pos %d)"
                   err-type
                   (:text text) vars/*template-name*
                   (:row text) (:col text) (:pos text)))))


(defn parse-error   [text] (common-error "Parse" text))
(defn compile-error [text] (common-error "Compile" text))
(defn render-error  [text] (common-error "Render" text))


