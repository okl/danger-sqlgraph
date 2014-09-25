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
            SQLParserBaseListener
            SQLHelloListener]))

(def sql-stmt "SELECT * FROM YOURMOM;")

(def words (atom ""))

(defn make-listener []
  (proxy [okl.sqlgraph.SQLParserBaseListener] []
    (enterSql [^SQLParser$SqlContext ctx]
      (swap! words (fn [w] (str w "hello!"))))
    (exitSql [^SQLParser$SqlContext ctx]
      (swap! words #(str % "bye!")))))

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
