(ns basil.run-tests
  (:require [basil.core-test :as core-test]
            [basil.util      :as util]))


(defn ^:export run
  []
  (.log js/console "Running CLJS tests.")
  (binding [*print-fn* (fn [& args] (doseq [each args]
                                      (.log js/console each)))
            util/*try-catch* (fn [try-f catch-f] (try (try-f)
                                                   (catch js/Error err
                                                     (catch-f err))))]
    (core-test/test-ns-hook)))