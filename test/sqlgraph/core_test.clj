(ns sqlgraph.core-test
  (:require [clojure.test :refer :all]
            [sqlgraph.core :refer :all]))

(deftest select-parse-test
  (is (= (parse-expr "SELECT * from MY_TABLE")
         {:produces [] :consumes ["my_table"] :destroys []}))
  (is (= (parse-expr "select * from my_table")
         {:produces [] :consumes ["my_table"] :destroys []}))
  (is (= (parse-expr "SELECT blah as 89_foo from MY_TABLE")
         {:produces [] :consumes ["my_table"] :destroys []}))
  (is (= (parse-expr "SELECT now() - interval 1 day")
         {:produces [] :consumes [] :destroys []}))
  (is (parse-expr "SELECT foo as 'bar bar'"))
  (is (parse-expr "SELECT foo 'bar bar'")))

(deftest create-parse-test
  (is (= (parse-expr "create table schema.mytable (a int, b int, c varchar(20))")
         {:produces ["schema.mytable"] :consumes [] :destroys []}))
  (is (= (parse-expr "create table schema.mytable as select * from schema.mytable_old")
         {:produces ["schema.mytable"] :consumes ["schema.mytable_old"] :destroys []})))


(deftest insert-parse-test
  (is (= (parse-expr "insert into schema.mytable (a, b, c) select A, B, C from schema.mytable_old")
         {:produces [] :consumes ["schema.mytable" "schema.mytable_old"] :destroys []}))
  (is (= (parse-expr "insert into schema.mytable VALUES (10);")
         {:produces [] :consumes["schema.mytable"] :destroys []}))
  (is (= (parse-expr "insert into schema.mytable VALUES (10), (20);")
         {:produces [] :consumes["schema.mytable"] :destroys []})))

(deftest drop-parse-test
  (is (= (parse-expr "drop table myschema.mytable")
         {:produces [] :consumes [] :destroys ["myschema.mytable"]}))
  (is (= (parse-expr "drop table if exists myschema.mytable")
         {:produces [] :consumes [] :destroys ["myschema.mytable"]})))

(deftest update-parse-test
  (is (= (parse-expr "update analytics.yoda y set y.id = y.blah")
         {:produces [] :consumes ["analytics.yoda"] :destroys []}))
  (is (= (parse-expr "update analytics.yoda y, foo.bar f set y.thingy = f.thingy where f.id = y.id")
         {:produces [] :consumes ["analytics.yoda" "foo.bar"] :destroys []})))

(deftest alter-table-test
  (is (= (parse-expr "alter table myschema.mytable add primary key (mycol1, mycol2);")
         {:produces [] :consumes ["myschema.mytable"] :destroys []})))

(deftest rename-parse-test
  (is (= (parse-expr "RENAME TABLE ms.mt TO ms2.mt2")
         {:produces ["ms2.mt2"] :consumes ["ms.mt"] :destroys []})))

(deftest do-nothing-parse-test
  (is (= (parse-expr ";")
         nil)))
