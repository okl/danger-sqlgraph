(ns sqlgraph.core
  (:import [org.antlr.v4.runtime ANTLRFileStream]
           [org.antlr.v4.runtime
            ANTLRInputStream CommonTokenStream CharStream]
           [org.antlr.v4.runtime.tree
            ParseTree ParseTreeWalker]))

(def sql-stmt "Select * from yourmom")



(.ready sql-reader)
(.close sql-reader)
(let [foo (ANTLRInputStream. sql-stmt)]
  (SQLLexer. foo))

(instance? CharStream (ANTLRInputStream. sql-stmt) )




(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
