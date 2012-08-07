(ns basil.lib-test
  (:require [basil.core  :as core]
            [basil.lib   :as lib]
            [basil.types :as types]
            [basil.util  :as util])
  (:use [basil.testvars;*CLJSBUILD-REMOVE*;-cljs
         :only [;*CLJSBUILD-REMOVE*;RuntimeException
                slot-compiler]])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [clojure.test;*CLJSBUILD-REMOVE*;-cljs
     ]))


(defn render
  [template t-name locals]
  (core/parse-compile-render
    slot-compiler template t-name locals))


(def users ["foo" "bar"])


(deftest test-formatting-fns
  (testing
    "format-rows"
    (is (= "hfoothbart"
           (lib/format-rows users ["h" "t"])))
    (is (= "hfoothbart"
           (render "<%(format-rows users [\"h\" \"t\"])%>"
                   "format-rows"
                   [{:users users}]))))
  (testing
    "serial-decors"
    (is (= [["hfoo" "t"] ["hbar" "t"]]
           (lib/serial-decors users ["h%s" "t"])))
    (is (= "hfoo\nt\nhbar\nt"
           (render "<%(serial-decors users [\"h%s\" \"t\"])%>"
                   "serial-decors"
                   [{:users ["foo" "bar"]}]))))
  (testing
    "format-rows with serial-decors"
    (is (= "hfoofoothbarbart"
           (lib/format-rows users (lib/serial-decors users ["h%s" "t"]))))
    (is (= "hfoothbart"
           (render "<%(format-rows users (serial-decors users [\"h\" \"t\"]))%>"
                   "serial-decors"
                   [{:users users}])))))


(defn ;;^:export
       test-ns-hook []
  (test-formatting-fns))