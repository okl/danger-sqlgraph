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

(def results (atom {:produces [] :consumes []}))
(def state (atom nil))

(defn add-table [table-name]
  (let [mode (if (= "query" (first @state)) :consumes :produces)]
    (swap! results #(assoc % mode (conj (mode %) table-name)))))

(defn enter-state [new-state]
  (swap! state #(conj % new-state)))

(defn exit-state [exit-state]
  (let [current-state (first @state)]
    (if (= current-state exit-state)
      (swap! state #(rest %))
      (throw (IllegalStateException.
              (str "Can't pop " exit-state " from " @state))))))


(defn make-listener []
  (swap! results (fn [a] {:produces [] :consumes []}))
  (swap! state (fn [a] nil))
  (proxy [okl.sqlgraph.SQLParserBaseListener] []
    (enterCreate_table_statement [^SQLParser$SqlContext ctx]
      (enter-state "create"))
    (exitCreate_table_statement [^SQLParser$SqlContext ctx]
      (exit-state "create"))
    (enterQuery_expression [^SQLParser$SqlContext ctx]
      (enter-state "query"))
    (exitQuery_expression [^SQLParser$SqlContext ctx]
      (exit-state "query"))
    (enterInsert_statement [^SQLParser$SqlContext ctx]
      (enter-state "insert"))
    (exitInsert_statement [^SQLParser$SqlContext ctx]
      (exit-state "insert"))
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
    @results))
