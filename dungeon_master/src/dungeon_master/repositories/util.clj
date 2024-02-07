(ns dungeon-master.repositories.util
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver TransactionWork])
  (:require [dungeon-master.config :refer [database-url]])
  )

(defn clear-db
  "delete all the entities and relationships in the database, never use this in production..."
  []
  (println "DEBUG: in clear-db database-url is" database-url)
  (let [cypher-string "MATCH (n) DETACH DELETE n" ]
    (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
      (with-open [session (.session driver)]
        (.writeTransaction session
                           (reify TransactionWork (execute [this tx]
                                                    (let [result
                                                          (.run tx cypher-string)]
                                                      "success"))))))))
