(ns basil.core-jvm-test
  (:require [basil.public    :as public]
            [basil.types     :as types]
            [basil.util      :as util]
            [basil.vars      :as vars]
            [basil.core-test :as core-test])
  (:use [basil.testvars :only [slot-compiler]]
        [clip-test.testutil :only [read-str re-quote throw-msg try-catch error-msg]])
  (:use [clip-test.core :only [deftest testing is]]))


(deftest test-slot-with-jvm-number-literals
  (testing
    "Slot with JVM number literals"
    (core-test/run-testcases
      {:name   "--Only a slot with one octal number literal"
       :templt "<% 03456  %>"
       :render "1838"}
      {:name   "--Only a slot with one negative octal number literal"
       :templt "<% -03456  %>"
       :render "-1838"}
      {:name   "--Only a slot with one hex number literal"
       :templt "<% 0x2F  %>"
       :render "47"}
      {:name   "--Only a slot with one negative hex number literal"
       :templt "<% -0x2F  %>"
       :render "-47"}
      {:name   "--Only a slot with one radix-notation number literal"
       :templt "<% 32r2pu2v  %>"
       :render "2947167"}
      {:name   "--Only a slot with one radix-notation number literal"
       :templt "<% -32r2pu2v  %>"
       :render "-2947167"}
      )))


(defn test-ns-hook
  []
  (test-slot-with-jvm-number-literals))