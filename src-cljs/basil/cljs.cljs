(ns basil.cljs
  (:require [cljs.reader :as reader]
            [basil.slot  :as slot]))


(def slot-compiler (slot/make-slot-compiler reader/read-string))