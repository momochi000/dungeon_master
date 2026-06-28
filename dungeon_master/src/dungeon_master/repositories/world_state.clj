(ns dungeon-master.repositories.world-state
  (:require [cheshire.core :as json]
            [dungeon-master.llm.embedding :refer [get-embedding]]
            [dungeon-master.repositories.util :refer [with-connection
                                                      create-node
                                                      create-node-with-embedding
                                                      create-relationship-statement
                                                      run-cypher-stmt-with-data-no-return]]))

(declare create-relationship-from-object)

(defn entity-name-to-node-id
  "Given an entity name, return a normalized node id.
  This removes digits and symbols and PascalCases the result."
  [entity-name]
  (-> entity-name
      (clojure.string/replace #"[\'\"\!\@\#\$\%\^\&\*\(\)]" "")
      (clojure.string/replace #"\d" "")
      (clojure.string/replace #"\b." #(clojure.string/upper-case %1))
      (clojure.string/replace #" " "")))

(defn- insert-or-update-entity-node
  "Insert entity-data into Ladybug, optionally attaching an embedding."
  [entity-data conn]
  (let [entity-name (get entity-data "name")
        node-id (entity-name-to-node-id entity-name)
        entity-data-with-id (merge entity-data {"id" node-id})]
    (if-let [description (get entity-data-with-id "description")]
      (let [embedding-response (get-embedding description)
            embedding-vector (:embedding embedding-response)
            final-entity-data (merge entity-data-with-id {"vector" embedding-vector})]
        (create-node-with-embedding final-entity-data conn))
      (create-node entity-data-with-id conn))))

(defn update-db-world-state
  "Given a map of entities and relationships, insert or update the world state.
  The map is expected to use string keys."
  [entities-map]
  (with-connection [conn]
    (let [entities (entities-map "entities")
          relationships (entities-map "relationships")]
      (doseq [entity-data entities]
        (insert-or-update-entity-node entity-data conn))
      (doseq [relationship-data relationships]
        (create-relationship-from-object relationship-data conn)))))

(defn- decompose-relationship-object
  "Convert
  {\"from_entity_name\": \"node1 name\", \"relationship_type\": \"RELATIONSHIP_TYPE\", \"to_entity_name\": \"node2 name\"}
  into [node-1-id relationship-type node-2-id]."
  [input]
  [(entity-name-to-node-id (get input "from_entity_name"))
   (get input "relationship_type")
   (entity-name-to-node-id (get input "to_entity_name"))])

(defn create-relationship-from-object
  "Relate two nodes given a relationship object."
  [input conn]
  (let [[cypher-query cypher-params]
        (apply create-relationship-statement (decompose-relationship-object input))]
    (run-cypher-stmt-with-data-no-return cypher-query cypher-params conn)))
