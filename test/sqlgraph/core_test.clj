(ns sqlgraph.core-test
  (:require [clojure.test :refer :all]
            [sqlgraph.core :refer :all]))

(deftest select-parse-test
  (is (= (parse-expr "SELECT * from MY_TABLE")
         {:produces [] :consumes ["my_table"]}))
  (is (= (parse-expr "select * from my_table")
         {:produces [] :consumes ["my_table"]})))
