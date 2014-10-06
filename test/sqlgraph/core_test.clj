(ns sqlgraph.core-test
  (:require [clojure.test :refer :all]
            [sqlgraph.core :refer :all]))

(deftest select-parse-test
  (is (= (parse-expr "SELECT * from MY_TABLE")
         {:produces [] :consumes ["my_table"] :destroys []}))
  (is (= (parse-expr "select * from my_table")
         {:produces [] :consumes ["my_table"] :destroys []})))

(deftest create-parse-test
  (is (= (parse-expr "create table schema.mytable (a int, b int, c varchar(20))")
         {:produces ["schema.mytable"] :consumes [] :destroys []}))
  (is (= (parse-expr "create table schema.mytable as select * from schema.mytable_old")
         {:produces ["schema.mytable"] :consumes ["schema.mytable_old"] :destroys []})))

(deftest insert-parse-test
  (is (= (parse-expr "insert into schema.mytable (a, b, c) select A, B, C from schema.mytable_old")
         {:produces [] :consumes ["schema.mytable" "schema.mytable_old"] :destroys []})))

(deftest drop-parse-test
  (is (= (parse-expr "drop table myschema.mytable")
         {:produces [] :consumes [] :destroys ["myschema.mytable"]}))
  (is (= (parse-expr "drop table if exists myschema.mytable")
         {:produces [] :consumes [] :destroys ["myschema.mytable"]})))
