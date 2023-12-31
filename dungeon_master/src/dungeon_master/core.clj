(ns dungeon-master.core
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens])
  (:gen-class))

;(defn -main
;  "I don't do a whole lot ... yet."
;  [& args]
;  (println "Hello, World!"))

(defn -main
  "create a node without properties"
  [& args]
  (with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [result (.run session "CREATE (n) RETURN n")]
        (println (.single result))))))
