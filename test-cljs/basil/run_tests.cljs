(ns basil.run-tests
  (:require [basil.core-test :as core-test]))


(defn ^:export run
  []
  (.log js/console "Running CLJS tests.")
  (binding [*print-fn* (fn [& args] (doseq [each args] (.log js/console each)))]
    (core-test/test-ns-hook)))