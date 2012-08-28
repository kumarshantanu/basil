(ns basil.run-tests
  (:require [basil.core-test         :as core-test]
            [basil.util              :as util]
            [mini-test.testutil-cljs :as tu]))


(defn ^:export run
  []
  (core-test/test-ns-hook)
  (tu/print-test-summary))