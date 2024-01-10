(ns dungeon-master.repositories.world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver Values]
           [org.neo4j.driver TransactionWork])
  (:require [cheshire.core :as json]))


(defn update-db-world-state
  "Given a map of entities and relationships, make appropriate insert or update
  statements into the graph db. Due to the interop with neo4j, the map expects
  keys as strings."
  [entities-map]
  (with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
    ;;(with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [entities (entities-map "entities")
            relationships (entities-map "relationships") ]

        (doall (map
                 (fn [entity-data]
                   (create-node entity-data session))
                 entities))

        (doall (map
                 (fn [relationship-data]
                   (create-relationship-from-string relationship-data session))
                 relationships))))))




(defn create-node
  [node-data driver-session]
  (case (node-data "label")
    "Place" (create-place-node node-data driver-session)
    "Person" (create-person-node node-data driver-session)))

(defn create-person-node
  "create a person node"
  [node-data driver-session]

  (let [cypher-string "MERGE (p:Person {name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)"]
    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

(defn create-place-node
  "create a place node"
  [node-data driver-session]

  ;;(println "DEBUG: called create-place-node")
  (let [cypher-string "MERGE (p:Place {name_id: $id, name: $name, description: $description}) RETURN (p)" ]
        ;;parameters (Values/parameters
        ;;             ;;(into {} (map (fn [[k v]] [(name k) v]) node-data))
        ;;             "id" ("id" node-data)
        ;;             "name" ("name" node-data)
        ;;             "description" ("description" node-data)) ]
    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

(defn create-relationship-from-string
  "relate two nodes given the input string of the format
   node_1_id|RELATIONSHIP_TYPE|node_2_id"
  [input driver-session]
  (let [[cypher-query cypher-params] (apply create-relationship-statement (decompose-relationship-string input))]
    (run-cypher-stmt-with-data-no-return cypher-query cypher-params driver-session)))

(defn create-relationship-statement
  "relate two nodes with each other"
  [first-node-id relationship-type second-node-id]
  (let [cypher-stmt (case relationship-type
                      "IN" "MATCH (n1) WHERE n1.name_id = $start_node_id
                           MATCH (n2) WHERE n2.name_id = $end_node_id
                           MERGE (n1)-[:IN]->(n2)")
        cypher-params {"start_node_id" first-node-id "end_node_id" second-node-id} ]
    [cypher-stmt cypher-params]))

(defn decompose-relationship-string
  "convert string of form
  node_1_id|RELATIONSHIP_TYPE|node_2_id
  to a seq of (node-1 relationship-type node-2) each of which are strings"
  [input]
  (clojure.string/split input #"\|"))


(defn run-cypher-stmt-with-data
  "Run a cypher statement along with data to fill cypher placeholders"
  [cypher-statement node-data driver-session]

  (.writeTransaction
    driver-session
    (reify TransactionWork (execute [this tx]
                             (let [result
                                   (.run tx
                                         cypher-statement
                                         node-data)]
                               (.single result))))))

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