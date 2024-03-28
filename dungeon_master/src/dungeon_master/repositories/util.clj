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

;; TODO: distinguish reads from writes
(defn run-cypher-stmt-with-data
  "Run a cypher statement along with data to fill cypher placeholders
  Data is a map with strings a keys which correspond to the placeholders in cypher"
  [cypher-statement node-data driver-session]

  (.writeTransaction
    driver-session
    (reify TransactionWork (execute [this tx]
                             (let [result
                                   (.run tx
                                         cypher-statement
                                         node-data)]
                               (.single result))))))

(defn run-cypher-stmt
  [cypher-statement driver-session]
  (.readTransaction
    driver-session
    (reify TransactionWork (execute [this tx]
                             (let [result
                                   (.run tx
                                         cypher-statement)]
                               (.single result)))))
  )

(defn run-cypher-stmt-with-data-no-return
  "Run a cypher statement along with data to fill cypher placeholders"
  [cypher-statement node-data driver-session]
  (.writeTransaction
    driver-session
    (reify TransactionWork (execute [this tx]
                             (let [result
                                   (.run tx
                                         cypher-statement
                                         node-data)])))))


(defn create-node
  [node-data driver-session]
  (println "DEBUG: in create-node, node-data is ----> " node-data)

  ;; This is a bit iffy. It's interpolating the label key of node-data into the query.
  ;; I should implement some controls on what can be passed in here as node types
  ;; interpolating into raw database command is risky
  (let [cypher-string
        (str
          "MERGE (p:"
          (node-data "label")
          "{name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)" )
        ]
    (run-cypher-stmt-with-data cypher-string node-data driver-session))

  ;;(case (node-data "label")
  ;;  "Place" (create-place-node node-data driver-session)
  ;;  "Person" (create-person-node node-data driver-session))
  )

(defn create-relationship-statement
  "relate two nodes with each other"
  [first-node-id relationship-type second-node-id]
  (let [cypher-stmt (format
                      "MATCH (n1) WHERE n1.name_id = $start_node_id
                      MATCH (n2) WHERE n2.name_id = $end_node_id
                      MERGE (n1)-[:%s]->(n2)"
                      relationship-type)
        cypher-params {"start_node_id" first-node-id "end_node_id" second-node-id} ]

    [cypher-stmt cypher-params]))

