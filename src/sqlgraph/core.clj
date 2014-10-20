(ns sqlgraph.core
  (:require [clojure.string :refer [lower-case]]
            [clojure.set :refer [union]])
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
               "rename from" :consumes
               "rename to" :produces
               "index" :consumes
               "truncate" :consumes
               "delete" :consumes
               "replace" :consumes
               "call" :consumes
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
  (swap! results (fn [a] {:produces #{} :consumes #{} :destroys #{}}))
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
    (enterFrom_table_name [^SQLParser$SqlContext ctx]
      (enter-state "rename from"))
    (exitFrom_table_name [^SQLParser$SqlContext ctx]
      (exit-state "rename from"))
    (enterTo_table_name [^SQLParser$SqlContext ctx]
      (enter-state "rename to"))
    (exitTo_table_name [^SQLParser$SqlContext ctx]
      (exit-state "rename to"))
    (enterIndex_statement [^SQLParser$SqlContext ctx]
      (enter-state "index"))
    (exitIndex_statement [^SQLParser$SqlContext ctx]
      (exit-state "index"))
    (enterTruncate_table_statement [^SQLParser$SqlContext ctx]
      (enter-state "truncate"))
    (exitTruncate_table_statement [^SQLParser$SqlContext ctx]
      (exit-state "truncate"))
    (enterDelete_statement [^SQLParser$SqlContext ctx]
      (enter-state "delete"))
    (exitDelete_statement [^SQLParser$SqlContext ctx]
      (exit-state "delete"))
    (enterCreate_view_statement [^SQLParser$SqlContext ctx]
      (enter-state "create"))
    (exitCreate_view_statement [^SQLParser$SqlContext ctx]
      (exit-state "create"))
    (enterDrop_view_statement [^SQLParser$SqlContext ctx]
      (enter-state "drop"))
    (exitDrop_view_statement [^SQLParser$SqlContext ctx]
      (exit-state "drop"))
    (enterReplace_statement [^SQLParser$SqlContext ctx]
      (enter-state "replace"))
    (exitReplace_statement [^SQLParser$SqlContext ctx]
      (exit-state "replace"))
    (enterCall_statement [^SQLParser$SqlContext ctx]
      (enter-state "call"))
    (exitCall_statement [^SQLParser$SqlContext ctx]
      (exit-state "call"))
    (enterTable_name [^SQLParser$SqlContext ctx]
      (add-table (.getText ctx)))))

(defn parse-expr
  ([s]
     (if (not (= s ";"))
       (try
         (let [lexer (SQLLexer. (ANTLRInputStream. s))
               tokens (CommonTokenStream. lexer)
               error-strategy (BailErrorStrategy. )
               parser (doto (SQLParser. tokens)
                        (.setErrorHandler error-strategy))
               ctx  (.sql parser)
               walker (ParseTreeWalker.)
               my-listener (make-listener)]
           (.walk walker my-listener ctx)
           @results)
         (catch Exception ex
           (binding [*out* *err*]
             (println (str "Error parsing \"" s "\""))
             (println (.getMessage ex)))))))
  ([s & args]
     (let [all-output (map parse-expr (concat [s] args))
           produces (apply union (map :produces all-output))
           consumes (apply union (map :consumes all-output))
           destroys (apply union (map :destroys all-output))]
       {:produces produces :consumes consumes :destroys destroys})))
