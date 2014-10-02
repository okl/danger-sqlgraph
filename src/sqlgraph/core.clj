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

(def sql-stmt "SELECT * FROM YOURMOM;")

(def words (atom ""))

(defn make-listener []
  (proxy [okl.sqlgraph.SQLParserBaseListener] []
    (enterTable_name [^SQLParser$SqlContext ctx]
      (swap! words #(str % "<TNAME>" (.getText ctx))))
    (exitTable_name [^SQLParser$SqlContext ctx]
      (swap! words #(str % "</TNAME>")))))

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
