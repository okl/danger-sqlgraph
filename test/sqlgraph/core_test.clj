(ns sqlgraph.core-test
  (:require [clojure.test :refer :all]
            [sqlgraph.core :refer :all]))

(deftest select-parse-test
  (is (= (parse-expr "SELECT * from MY_TABLE")
         {:produces #{} :consumes #{"my_table"} :destroys #{}}))
  (is (= (parse-expr "select * from my_table")
         {:produces #{} :consumes #{"my_table"} :destroys #{}}))
  (is (= (parse-expr "SELECT blah as 89_foo from MY_TABLE")
         {:produces #{} :consumes #{"my_table"} :destroys #{}}))
  (is (= (parse-expr "SELECT now() - interval 1 day")
         {:produces #{} :consumes #{} :destroys #{}}))
  (is (parse-expr "SELECT foo as 'bar bar'"))
  (is (parse-expr "SELECT foo 'bar bar'"))
  (is (parse-expr "select 1 # this is a comment"))
  (is (parse-expr "select * from atable join othertable where 1 = 1"))
  (is (parse-expr "select a.`some col` from sometable a"))
  (is (parse-expr "select case when id = @prev then @count := @count + 1 else @count := 1 and @prev := id end"))
  (is (parse-expr "select * from ms.mt join ms.mt2 use index (idx) on mt.thing = mt2.thing2"))
  (is (= (parse-expr "select * from mytab; select * from mytab2")
         {:produces #{} :consumes #{"mytab" "mytab2"} :destroys #{}}))
  (is (parse-expr "select \"column\" as somehting")))

(deftest create-parse-test
  (is (= (parse-expr "create table schema.mytable (a int, b int, c varchar(20))")
         {:produces #{"schema.mytable"} :consumes #{} :destroys #{}}))
  (is (= (parse-expr "create table schema.mytable as select * from schema.mytable_old")
         {:produces #{"schema.mytable"} :consumes #{"schema.mytable_old"} :destroys #{}}))
  (is (= (parse-expr "create table ms.mt (a int) engine=innodb")
         {:produces #{"ms.mt"} :consumes #{} :destroys #{}}))
  (is (parse-expr "create table ms.mt select * from ms.mt2"))
  (is (parse-expr "create table ms.mt (a int(11), b int, c int, primary key (a, b, c))"))
  (is (parse-expr "create table ms.mt (a tinyint(1))"))
  (is (parse-expr "create table ms.mt (a varchar(25) default null, b varchar(20) NOT NULL)"))
  (is (parse-expr "create table ms.mt (a varchar(25) COMMENT 'this does some stuff')")))


(deftest insert-parse-test
  (is (= (parse-expr "insert into schema.mytable (a, b, c) select A, B, C from schema.mytable_old")
         {:produces #{} :consumes #{"schema.mytable" "schema.mytable_old"} :destroys #{}}))
  (is (= (parse-expr "insert into schema.mytable VALUES (10);")
         {:produces #{} :consumes #{"schema.mytable"} :destroys #{}}))
  (is (= (parse-expr "insert into schema.mytable VALUES (10), (20);")
         {:produces #{} :consumes #{"schema.mytable"} :destroys #{}}))
  (is (parse-expr "insert into schema.mytable (col1, col2) select 1, 2 on duplicate key update col1 = col1 + 1;"))
  (is (parse-expr "insert ignore into schema.mytable (col1, col2) select 1, 2"))
  (is (parse-expr "insert schema.mytable(a) select 1")))

(deftest drop-parse-test
  (is (= (parse-expr "drop table myschema.mytable")
         {:produces #{} :consumes #{} :destroys #{"myschema.mytable"}}))
  (is (= (parse-expr "drop table if exists myschema.mytable")
         {:produces #{} :consumes #{} :destroys #{"myschema.mytable"}})))

(deftest update-parse-test
  (is (= (parse-expr "update analytics.yoda y set y.id = y.blah")
         {:produces #{} :consumes #{"analytics.yoda"} :destroys #{}}))
  (is (= (parse-expr "update analytics.yoda y, foo.bar f set y.thingy = f.thingy where f.id = y.id")
         {:produces #{} :consumes #{"analytics.yoda" "foo.bar"} :destroys #{}}))
  (is (= (parse-expr "update table_name t1 join table_name2 t2 on t1.id = t2.id set t1.thingy = t2.thingy, t1.otherthingy = t2.otherthingy")
         {:produces #{} :consumes #{"table_name" "table_name2"} :destroys #{}}))
  (is (parse-expr "update table_name t set t.`some colOHBS*%*` = 15")))

(deftest delete-parse-test
  (is (= (parse-expr "delete from ms.mt where something = selse")
         {:produces #{} :consumes #{"ms.mt"} :destroys #{}})))

(deftest alter-table-test
  (is (= (parse-expr "alter table myschema.mytable add primary key (mycol1, mycol2);")
         {:produces #{} :consumes #{"myschema.mytable"} :destroys #{}}))
  (is (= (parse-expr "alter table ms.mt add unique index idx(somecol)")
         {:produces #{} :consumes #{"ms.mt"} :destroys #{}}))
  (is (parse-expr "alter table ms.mt modify foo varchar(200)"))
  (is (parse-expr "alter table ms.mt add primary key fookey (foo)")))

(deftest rename-parse-test
  (is (= (parse-expr "RENAME TABLE ms.mt TO ms2.mt2")
         {:produces #{"ms2.mt2"} :consumes #{"ms.mt"} :destroys #{}})))

(deftest do-nothing-parse-test
  (is (= (parse-expr ";")
         nil)))

(deftest truncate-table-test
  (is (= (parse-expr "truncate table ms.mt")
         {:produces #{} :consumes #{"ms.mt"} :destroys #{}})))

(deftest nothing-test
  (is (= (parse-expr "")
         {:produces #{} :consumes #{} :destroys #{}})))

(deftest view-test
  (is (= (parse-expr "CREATE VIEW ms.mv as select * from something")
         {:produces #{"ms.mv"} :consumes #{"something"} :destroys #{}}))
  (is (= (parse-expr "DROP VIEW if exists ms.mv")
         {:produces #{} :consumes #{} :destroys #{"ms.mv"}})))

(deftest create-index-test
  (is (= (parse-expr "CREATE INDEX idx ON ms.mt (col1, col2) using btree")
         {:produces #{} :consumes #{"ms.mt"} :destroys #{}})))
