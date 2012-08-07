(ns basil.core-test
  (:require [basil.core  :as core]
            [basil.types :as types]
            [basil.util  :as util])
  (:use [basil.testvars;*CLJSBUILD-REMOVE*;-cljs
         :only [;*CLJSBUILD-REMOVE*;RuntimeException
                slot-compiler]])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [clojure.test;*CLJSBUILD-REMOVE*;-cljs
     ]))


(defn run-testcases
  [& cases]
  (doseq [{:keys [name templt model handlers render]} cases]
    (is (= render (core/parse-compile-render
                    slot-compiler templt name (filter identity
                                                      [model handlers])))
        name)))


(deftest test-static-templates
  (run-testcases
   ;; no escapes
   {:name   "Empty"
    :templt ""                 :render ""}
   {:name   "Incomplete slot"
    :templt "Hello <% "        :render "Hello ERROR: [Parse] Invalid/incomplete slot ' ' in 'Incomplete slot' at row 1, col 9 (pos 9)"}
   {:name   "No slots"
    :templt "Hello World"      :render "Hello World"}
   ;; escapes
   {:name   "Escaped backslash"
    :templt "Hello \\\\ World" :render "Hello \\ World"}
   {:name   "Escaped slot-begin"
    :templt "Hello \\<% World" :render "Hello <% World"}
   {:name   "Escaped slot-end"
    :templt "Hello \\%> World" :render "Hello %> World"}
   ;; non-escapes
   {:name   "Backslash should not escape regular characters"
    :templt "Hello \\World"    :render "Hello \\World"}
   {:name   "Backslash should not escape html tag beginning"
    :templt "Hello \\<World>"  :render "Hello \\<World>"}
   {:name   "Backslash should not escape html tag end"
    :templt "Hello <World\\>x" :render "Hello <World\\>x"}
   ;; Trailing escapes
   {:name   "Trailing backslash"
    :templt "Hello World\\"    :render "Hello World\\"}
   {:name   "Trailing escaped backslash"
    :templt "Hello World\\\\"  :render "Hello World\\"}
   {:name   "Trailing escaped slot-begin"
    :templt "Hello\\<%"        :render "Hello<%"}
   {:name   "Trailing escaped slot-end"
    :templt "Hello\\%>"        :render "Hello%>"}
   ;; Trailing non-escapes
   {:name   "Trailing Backslash should not escape regular characters"
    :templt "Hello Worl\\d"    :render "Hello Worl\\d"}
   {:name   "Trailing Backslash should not escape html tag beginning"
    :templt "Hello \\<"        :render "Hello \\<"}
   {:name   "Trailing Backslash should not escape html tag end"
    :templt "Hello <World\\>"  :render "Hello <World\\>"}))


(deftest test-template-empty-slot
  (testing
    "Empty slot"
    (is (thrown? RuntimeException ;#"EOF while reading"
                 ;(run-testcases
                 ;  ;; empty slot
                 ;  {:name   "Empty slot"
                 ;   :templt "<%%>" :render ""})
                 (let [name   "Empty slot"
                       templt "<%%>"]
                   (core/parse-compile-render slot-compiler templt name))))))


(deftest test-slot-with-literals
  (testing
    "Slot with literals"
    (run-testcases
      {:name   "--Only a slot with one string literal"
       :templt "<% \"foobar\"  %>"
       :render "foobar"}
      {:name   "--Only a slot with one keyword literal"
       :templt "<% :foobar  %>"
       :render ":foobar"}
      {:name   "--Only a slot with one keyword literal of length one"
       :templt "<% :f %>"
       :render ":f"}
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
      {:name   "--Only a slot with one integer literal"
       :templt "<% 3456  %>"
       :render "3456"}
      {:name   "--Only a slot with one negative integer literal"
       :templt "<% -3456  %>"
       :render "-3456"}
      {:name   "--Only a slot with one decimal number literal"
       :templt "<% 345.67  %>"
       :render "345.67"}
      {:name   "--Only a slot with one negative decimal number literal"
       :templt "<% -567.89  %>"
       :render "-567.89"}
      {:name   "--Only a slot with boolean literals"
       :templt "<% true false %>"
       :render "true"}
      {:name   "--Only a slot with one nil literal"
       :templt "<% nil  %>"
       :render ""})))


(deftest test-static-with-slot
  (testing
    "Static and slot"
    (run-testcases
      {:name   "--Content and one slot with one literal"
       :templt "Hello <% \"World\" %>"
       :render "Hello World"}
      {:name   "--Only 2 slots with string literals"
       :templt "<% \"Hello\" %><% \"World\" %>"
       :render "HelloWorld"}
      {:name   "--Slot that renders slot-markers"
       :templt "<% \"\\<%test\\%>\" %>"
       :render "<%test%>"})))


(deftest test-slot-with-attributes
  (testing
    "Slot with attributes"
    (run-testcases
      {:name   "--Only a slot with one attribute"
       :templt "<% hello %>"
       :model {:hello "Namaste"}
       :render "Namaste"}
      {:name   "Content and one slot with one attribute"
       :templt "Hello <% world %>"
       :model {:world "World"}
       :render "Hello World"}
      {:name   "--Only 2 slots with attributes"
       :templt "<% hello %><% world %>"
       :model {:hello "Hello"
               :world "World"}
       :render "HelloWorld"})))


(deftest test-slot-with-handler-calls
  (testing
    "Slot with handler calls"
    (run-testcases
      {:name     "No-arg handler"
       :templt   "<% (foo) %>"
       :handlers {:foo (constantly "FOO")}
       :render   "FOO"}
      {:name     "Single literal-arg handler"
       :templt   "<% (foo :bar) %>"
       :handlers {:foo (partial str "FOO")}
       :render   "FOO:bar"}
      {:name     "Single attribute-arg handler"
       :templt   "<% (foo bar) %>"
       :model    {:bar "BAR"}
       :handlers {:foo (partial str "FOO")}
       :render   "FOOBAR"}
      {:name     "Two attribute-args handler"
       :templt   "<% (foo bar baz) %>"
       :model    {:bar "BAR" :baz "BAZ"}
       :handlers {:foo (partial str "FOO")}
       :render   "FOOBARBAZ"}
      {:name     "Two attribute/literal-args handler"
       :templt   "<% (foo bar :baz) %>"
       :model    {:bar "BAR"}
       :handlers {:foo (partial str "FOO")}
       :render   "FOOBAR:baz"})))


(deftest test-parse-compile
  (let [t "Hello <% beautiful %> World"
        p (core/parse-template t)
        c (core/compile-template slot-compiler p)]
    (is (types/parsed-template? p))
    (is (types/compiled-template? c))))


(deftest test-missing-locals
  (testing
    "missing locals"
    (run-testcases
      {:name "missing lone symbol"
       :templt "<% foo %>"
       :model {}
       :handlers {}
       :render "ERROR: [Render] No such local-key 'foo' in 'missing lone symbol' at row 1, col 3 (pos 3)"}
      {:name "missing fn symbol"
       :templt "<% (foo) %>"
       :model {}
       :handlers {}
       :render "ERROR: [Render] No such local-key 'foo' in 'missing lone symbol' at row 1, col 3 (pos 3)"})))


(defn run-groupcases
  [& cases]
  (doseq [{:keys [name model handlers render group]} cases]
    (let [tgp (core/compile-template-group slot-compiler group)]
      (is (= render (core/render-by-name tgp name (filter identity
                                                          [model handlers])))
          name))))


(deftest test-template-group
  (testing
    "Template groups"
    (let [group {:a "Static"
                 :b "<% \"Dynamic\" %>"
                 :c "<% (include :a) %>"
                 :d "<% (include :b) %>"
                 :e "<% (include :c) %><% (include :d) %>"}]
      (run-groupcases
        {:name   :a
         :render "Static"
         :group  group}
        {:name   :b
         :render "Dynamic"
         :group  group}
        {:name   :c
         :render "Static"
         :group  group}
        {:name   :d
         :render "Dynamic"
         :group  group}
        {:name   :e
         :render "StaticDynamic"
         :group  group})))
  (testing
    "Missing templates in groups"
    (let [tgp (core/compile-template-group slot-compiler {:a "Static"
                                                          :b "<% (include :c) %>"})]
      (is (= "ERROR: [Render] No such template name/key: ':c' in ':c' at row 0, col 0 (pos 0)"
             (core/render-by-name tgp :c [])))
      (is (= "ERROR: [Render] No such template name/key: ':c' in ':b' at row 1, col 3 (pos 3)"
             (core/render-by-name tgp :b []))))))


(defn ;;^:export
       test-ns-hook []
  (test-static-templates)
  (test-template-empty-slot)
  (test-slot-with-literals)
  (test-static-with-slot)
  (test-slot-with-attributes)
  (test-slot-with-handler-calls)
  (test-parse-compile)
  (test-missing-locals)
  (test-template-group))