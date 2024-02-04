(ns dungeon-master.repositories.util
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver TransactionWork]))

(defn clear-db
  "delete all the entities and relationships in the database, never use this in production..."
  []
  (let [cypher-string "MATCH (n) DETACH DELETE n" ]
    (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
      (with-open [session (.session driver)]
        (.writeTransaction driver-session
                           (reify TransactionWork (execute [this tx]
                                                    (let [result
                                                          (.run tx
                                                                cypher-string)]
                                                      (.single result)))))))))
