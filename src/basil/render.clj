(ns basil.render
  (:require [basil.types :as types]
            [basil.util  :as util]
            [basil.vars  :as vars]))


(defn render-template*
  [compiled-template]
  {:pre [(types/compiled-template? compiled-template)]}
  (util/redstr (map #(% vars/*locals-coll*) (:content compiled-template))))
