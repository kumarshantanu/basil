(ns basil.lib-test
  (:require [basil.public;*CLJSBUILD-REMOVE*;-cljs
                          :as public]
            [basil.lib    :as lib]
            [basil.types  :as types]
            [basil.util   :as util])
  (:use [basil.testvars;*CLJSBUILD-REMOVE*;-cljs
         :only [;*CLJSBUILD-REMOVE*;RuntimeException
                slot-compiler]])
  (:use;*CLJSBUILD-REMOVE*;-macros
    [clojure.test;*CLJSBUILD-REMOVE*;-cljs
     ;*CLJSBUILD-REMOVE*;:only [deftest testing is thrown? thrown-with-msg?]
     ]))


(deftest test-currying
  (testing
    "partiar ('partial' with reversed argument blocks)"
    (let [r (lib/partiar str 1 2 3)]
      (is (= "789123" (r 7 8 9))))))


(defn render
  [template t-name locals]
  (public/parse-compile-render
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


(def table-data
  [(array-map :name "Abdul" :age 34)
   (array-map :name "Saira" :age 41)])


(deftest test-html-table
  (testing
    "table columns"
    (is (= "<td>foo</td>\n<td>bar</td>\n"
           (render "<% (html-td users) %>"
                   "html-td"
                   [{:users users}])))
    (is (= "<th>foo</th>\n<th>bar</th>\n"
           (render "<% (html-th users) %>"
                   "html-th"
                   [{:users users}]))))
  (testing
    "table data transformation"
    (is (= ":name\n:age"
           (render "<% (mapseq->keys table-data) %>"
                   "mapseq->keys"
                   [{:table-data table-data}])))
    (is (= "Abdul\n34\nSaira\n41"
           (render "<% (mapseq->rows table-data) %>"
                   "mapseq->rows"
                   [{:table-data table-data}]))))
  (testing
    "table rows"
    (is (= "<tr>\n<td>Abdul</td>\n<td>34</td>\n</tr>\n<tr>\n<td>Saira</td>\n<td>41</td>\n</tr>\n"
           (render "<% (html-tr (mapseq->rows table-data)) %>"
                   "html-tr"
                   [{:table-data table-data}])))))


(defn test-ns-hook []
  (test-currying)
  (test-formatting-fns)
  (test-html-table))