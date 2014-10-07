(ns sqlgraph.core
  (:require [clojure.string :refer [lower-case]])
  (:import [org.antlr.v4.runtime ANTLRFileStream]
           [org.antlr.v4.runtime
            ANTLRInputStream
            CommonTokenStream
            CharStream
            BailErrorStrategy]
           [org.antlr.v4.runtime.tree
            ParseTree ParseTreeWalker]
           [org.antlr.v4.runtime.misc
            ParseCancellationException])
  (:import [okl.sqlgraph
            SQLLexer
            SQLParser
            SQLParser$Select_listContext
            SQLParser$SqlContext
            SQLParserBaseListener]))

(def results (atom {}))
(def state (atom nil))

(defn- add-table [table-name]
  (let [mode (case (first @state)
               "query" :consumes
               "insert" :consumes
               "create" :produces
               "drop" :destroys
               "update" :consumes
               "alter" :consumes
               (throw (IllegalStateException.
                       (str "Unknown state " (first @state)))))
        lower-table (lower-case table-name)]
    (swap! results #(assoc % mode (conj (mode %) lower-table)))))

(defn- enter-state [new-state]
  (swap! state #(conj % new-state)))

(defn- exit-state [exit-state]
  (let [current-state (first @state)]
    (if (= current-state exit-state)
      (swap! state #(rest %))
      (throw (IllegalStateException.
              (str "Can't pop " exit-state " from " @state))))))


(defn- make-listener []
  (swap! results (fn [a] {:produces [] :consumes [] :destroys []}))
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
    (enterDrop_table_statement [^SQLParser$SqlContext ctx]
      (enter-state "drop"))
    (exitDrop_table_statement [^SQLParser$SqlContext ctx]
      (exit-state "drop"))
    (enterUpdate_statement [^SQLParser$SqlContext ctx]
      (enter-state "update"))
    (exitUpdate_statement [^SQLParser$SqlContext ctx]
      (exit-state "update"))
    (enterAlter_table_statement [^SQLParser$SqlContext ctx]
      (enter-state "alter"))
    (exitAlter_table_statement [^SQLParser$SqlContext ctx]
      (exit-state "alter"))
    (enterTable_name [^SQLParser$SqlContext ctx]
      (add-table (.getText ctx)))))

(defn parse-expr [s]
  (try
    (let [lexer (SQLLexer. (ANTLRInputStream. s))
          tokens (CommonTokenStream. lexer)
          error-strategy (BailErrorStrategy. )
          parser (doto (SQLParser. tokens) (.setErrorHandler error-strategy))
          ctx  (.sql parser)
          walker (ParseTreeWalker.)
          my-listener (make-listener)]
      (.walk walker my-listener ctx)
      @results)
    (catch ParseCancellationException ex
      (println (str "Error parsing \"" s "\"")))))
