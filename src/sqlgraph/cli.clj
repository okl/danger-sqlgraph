(ns sqlgraph.cli
  (:require [sqlgraph.core :refer [parse-expr]])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :as string]))


(def cli-opts
  [["-f" "--file FILE" "File to load SQL from"
    :validate [#(.exists (as-file %)) "Unable to find file"]]])

(defn- usage [options-summary]
  (->> ["This will process SQL for used/produced tables"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn- error-msg [errors summary]
  (string/join "\n" (concat errors [(usage summary)])))

(defn- process-stmt [stmt]
  (println (parse-expr (str (string/trim stmt) ";"))))

(defn- process-file [file-contents]
  "Process each statement of the file separately"
  (doall (map process-stmt (string/split file-contents #";"))))

(defn -main [& args]
  (let [opts-out (parse-opts args cli-opts)]
    (if (:errors opts-out)
      (do
        (println (error-msg (:errors opts-out) (:summary opts-out)))
        (System/exit 1)))
    (if (:file (:options opts-out))
      (process-file (slurp (:file (:options opts-out))))
      (println (parse-expr (string/join "\n" (:arguments opts-out)))))))
