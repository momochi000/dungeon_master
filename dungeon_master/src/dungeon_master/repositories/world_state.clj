(ns dungeon-master.repositories.world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens])
  (:require [dungeon-master.config :refer [database-url]]
            [dungeon-master.repositories.util :refer [create-node
                                                      create-relationship-statement
                                                      run-cypher-stmt-with-data
                                                      run-cypher-stmt-with-data-no-return]]
            [cheshire.core :as json]))


(declare create-relationship-from-string)


;; TODO: get the database url using some sort of application configuration
(defn update-db-world-state
  "Given a map of entities and relationships, make appropriate insert or update
  statements into the graph db. Due to the interop with neo4j, the map expects
  keys as strings."
  [entities-map]
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
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


(defn create-person-node
  "create a person node"
  [node-data driver-session]
  (let [cypher-string "MERGE (p:Person {name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)"]
    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

(defn create-place-node
  "create a place node"
  [node-data driver-session]
  (let [cypher-string "MERGE (p:Place {name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)" ]
        ;; in this commented code i was trying to use [org.neo4j.driver Values]
        ;; which should be a type that the cypher statement accepts for it's parameters
        ;; i.e. "MERGE (p) {name: $name_param}" would provide some guarantees
        ;; around name_param if was given in a Values object, however, I
        ;; couldn't get this to work. But i think this is the "right" way to do
        ;; it, just need to figure it out
        ;;parameters (Values/parameters
        ;;             ;;(into {} (map (fn [[k v]] [(name k) v]) node-data))
        ;;             "id" ("id" node-data)
        ;;             "name" ("name" node-data)
        ;;             "description" ("description" node-data)) ]
    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

(declare decompose-relationship-string)

(defn create-relationship-from-string
  "relate two nodes given the input string of the format
   node_1_id|RELATIONSHIP_TYPE|node_2_id"
  [input driver-session]
  (let [[cypher-query cypher-params] (apply create-relationship-statement (decompose-relationship-string input))]
    (run-cypher-stmt-with-data-no-return cypher-query cypher-params driver-session)))

(defn decompose-relationship-string
  "convert string of form
  node_1_id|RELATIONSHIP_TYPE|node_2_id
  to a seq of (node-1 relationship-type node-2) each of which are strings"
  [input]
  (clojure.string/split input #"\|"))


;; TESTING SECTION

;;(import '[org.neo4j.driver GraphDatabase])
;;(import '[org.neo4j.driver AuthTokens])
;;(import '[org.neo4j.driver Values])
;;(import '[org.neo4j.driver TransactionWork])
