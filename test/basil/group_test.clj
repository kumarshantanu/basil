(ns basil.group-test
  (:require [basil.public;*CLJSBUILD-REMOVE*;-cljs
                          :as public]
            [basil.error  :as error]
            [basil.group  :as group]
            [basil.slot   :as slot]
            [basil.types  :as types]
            [basil.util   :as util])
  (:use [basil.testvars;*CLJSBUILD-REMOVE*;-cljs
         :only [slot-compiler]]
        [clip-test.testutil;*CLJSBUILD-REMOVE*;-cljs
         :only [;*CLJSBUILD-REMOVE*;RuntimeException
                read-str re-quote throw-msg try-catch error-msg millis-now sleep]])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [clip-test.core;*CLJSBUILD-REMOVE*;-cljs
     :only [deftest testing is
            ;*CLJSBUILD-REMOVE*;thrown? thrown-with-msg?
            ]]))


(deftest test-make-group
  (let [f (fn [name] [:foo nil])
        g (group/make-group f)]
    (is (group/template-group? g) "create template group")
    (is (= [:foo nil] (group/get-template g :bar)) "lookup works")))


(deftest test-make-group-union
  (let [f1 (fn [name] (get {:foo [:foo nil]} name))
        f2 (fn [name] (get {:bar [:bar nil]} name))
        g (->> [f1 f2]
            (map group/make-group)
            group/make-group-union)]
    (is (group/template-group? g) "create group union")
    (is (= [:foo nil] (group/get-template g :foo)) "lookup on group-1 works")
    (is (= [:bar nil] (group/get-template g :bar)) "lookup on group-2 works")))


(deftest test-make-group-from-map
  (let [t (public/parse-compile (slot/make-slot-compiler read-str) "foo" :foo)
        m {:foo t}
        g (group/make-group-from-map m)]
    (is (group/template-group? g) "create group from map")
    (is (= [t nil] (group/get-template g :foo)) "lookup on existing key works")
    (is (thrown-with-msg? RuntimeException (re-quote "[Render] No such template name/key: ':bar' in 'noname' at row 0, col 0 (pos 0)")
                          (binding [error/*error* #(throw-msg (str (:text %)))]
                            (group/get-template g :bar))) "lookup on absent key works")))


(deftest test-make-cached-group
  (let [a (atom {:foo :foo})
        g (group/make-group #(do [(get @a %) nil]))
        c (group/make-cached-group g millis-now 1000)]
    (is (= [:foo nil] (group/get-template g :foo)) "get ordinary")
    (is (= [:foo nil] (group/get-template c :foo)) "get uncached")
    (reset! a {:foo :bar})
    (is (= [:bar nil] (group/get-template g :foo)) "get ordinary, updated")
    (is (= [:foo nil] (group/get-template c :foo)) "get cached")
    (sleep 1000)
    (is (= [:bar nil] (group/get-template g :foo)) "get ordinary, last updated")
    (is (= [:bar nil] (group/get-template c :foo)) "get cache-updated")))


(defn test-ns-hook []
  (test-make-group)
  (test-make-group-union)
  (test-make-group-from-map)
  (test-make-cached-group))