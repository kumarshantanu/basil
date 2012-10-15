(ns basil.run-tests
  (:require [basil.core-test         :as core-test]
            [basil.group-test        :as group-test]
            [basil.lib-test          :as lib-test]
            [basil.util              :as util]
            [clip-test.testutil-cljs :as tu]))


(defn ^:export run
  []
  (core-test/test-ns-hook)
  (group-test/test-ns-hook)
  (lib-test/test-ns-hook)
  (tu/print-test-summary))