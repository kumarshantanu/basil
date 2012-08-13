(ns basil.ambiguous
  (:refer-clojure :exclude [char?])
  (:require [clojure.core :as core])
  (:import (java.util.regex Pattern)))


(defn char?
  [x]
  (core/char? x))


(defn re-quote
  [x]
  (re-pattern (Pattern/quote x)))