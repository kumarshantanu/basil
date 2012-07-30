(ns basil.ambiguous
  (:refer-clojure :exclude [char?])
  (:require [clojure.core :as core]))


(defn char?
  [x]
  (core/char? x))