(let [antlr-src "antlr/src"]
  (defproject sqlgraph "0.1.0-SNAPSHOT"
    :description "FIXME: write description"
    :url "http://example.com/FIXME"
    :license {:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
    :dependencies [[org.clojure/clojure "1.5.1"]
                   [org.antlr/antlr4 "4.3"]]
    :java-source-paths [~antlr-src]
    :plugins [[org.clojars.rferraz/lein-antlr "0.2.2-SNAPSHOT"]]
    :hooks [leiningen.antlr]
    ;; `lein-antlr` plugin configuraiton
    :antlr-src-dir "antlr/grammar"
    :antlr-dest-dir ~antlr-src))
