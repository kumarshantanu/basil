(ns basil.run-tests
  (:require [basil.core-test :as core-test]))


(defn ^:export run
  []
  (.log js/console
    (pr-str (core-test/test-ns-hook))))