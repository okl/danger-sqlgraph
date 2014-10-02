(ns sqlgraph.core
  (:import [org.antlr.v4.runtime ANTLRFileStream]
           [org.antlr.v4.runtime
            ANTLRInputStream
            CommonTokenStream
            CharStream]
           [org.antlr.v4.runtime.tree
            ParseTree ParseTreeWalker])
  (:import [okl.sqlgraph
            SQLLexer
            SQLParser
            SQLParser$Select_listContext
            SQLParser$SqlContext
            SQLParserBaseListener]))

(def sql-stmt "CREATE TABLE IQ.TEST AS SELECT * FROM SCHEMA.YOURMOM Q LEFT JOIN SCHEMA.MYMOM T ON Q.FY = T.FY;")

(def results (atom {:produces [] :consumes []}))
(def state (atom nil))

(defn add-table [table-name]
  (let [mode (if (= "query" (first @state)) :consumes :produces)]
    (swap! results #(assoc % mode (conj (mode %) table-name)))))

(defn make-listener []
  (proxy [okl.sqlgraph.SQLParserBaseListener] []
    (enterCreate_table_statement [^SQLParser$SqlContext ctx]
      (swap! state #(conj % "create"))
      (println state))
    (exitCreate_table_statement [^SQLParser$SqlContext ctx]
      (swap! state #(rest %))
      (println state))
    (enterQuery_expression [^SQLParser$SqlContext ctx]
      (swap! state #(conj % "query"))
      (println state))
    (exitQuery_expression [^SQLParser$SqlContext ctx]
      (swap! state #(rest %))
      (println state))
    (enterTable_name [^SQLParser$SqlContext ctx]
      (add-table (.getText ctx)))))

(defn parse-expr [s]
  (let [lexer (SQLLexer. (ANTLRInputStream. s))
        tokens (CommonTokenStream. lexer)
        parser (SQLParser. tokens)
        ctx  (.sql parser)
        walker (ParseTreeWalker.)
        my-listener (make-listener)]
    (.walk walker my-listener ctx)
    my-listener))

;; (.getWords (parse-expr sql-stmt))
 (parse-expr sql-stmt)
