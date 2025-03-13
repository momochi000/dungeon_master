(ns dungeon-master.repositories.world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens])

  (:require [dungeon-master.config :refer [database-url]]
            [dungeon-master.llm.embedding :refer [get-embedding]]
            [dungeon-master.repositories.util :refer [create-node
                                                      create-node-with-embedding
                                                      create-relationship-statement
                                                      ;;run-cypher-stmt-with-data
                                                      run-cypher-stmt-with-data-no-return]]
            [cheshire.core :as json]))


;; NOTE: Using declare here because clojure must define functions in the order
;; in which they're used. However, this function is supposed to be private. It
;; feels weird to have private functions declared BEFORE public ones, but
;; that's the way it's supposed to be in clojure. I did this to get around
;; that, but i don't believe it is idiomatic clojure (or lisp)
;;
;; I've left this here for self-documentation, but going forward i'll do it the "right"
;; way and declare private functions before the public ones
(declare create-relationship-from-string)

(defn- insert-or-update-entity-node
  "Wrapper function to handle putting entity-data into the database
  this also converts the description into a vector and puts that into the node data"
  [entity-data db-session]

  ;;(println "DEBUG: in insert-or-update-entity-node. if statement upcoming")
  ;;(println (p/pprint entity-data))

  (if (get entity-data "description")
    (let [embedding-response (get-embedding (get entity-data "description"))
          embedding-vector (:embedding embedding-response)
          entity-data-with-embedding (merge {"vector" embedding-vector} entity-data)]
      ;;(println "DEBUG: inserting vector with embedding entity-data is ===========>")
      ;;(println (p/pprint entity-data-with-embedding) )
      (create-node-with-embedding entity-data-with-embedding db-session))

    (create-node entity-data db-session)))


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
                   (insert-or-update-entity-node entity-data session))
                 entities))

        (doall (map
                 (fn [relationship-data]
                   (create-relationship-from-string relationship-data session))
                 relationships))))))


;; DEPRECATED
;; I can instead create either type of node with a single statement in `util.create-node`
;;(defn create-person-node
;;  "create a person node"
;;  [node-data driver-session]
;;  (let [cypher-string "MERGE (p:Person {name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)"]
;;    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

;; DEPRECATED
;;(defn create-place-node
;;  "create a place node"
;;  [node-data driver-session]
;;  (let [cypher-string "MERGE (p:Place {name_id: $id}) ON CREATE SET p.name = $name, p.description = $description RETURN (p)" ]
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
;;    (run-cypher-stmt-with-data cypher-string node-data driver-session)))

(defn- decompose-relationship-string
  "convert string of form
  node_1_id|RELATIONSHIP_TYPE|node_2_id
  to a seq of (node-1 relationship-type node-2) each of which are strings"
  [input]
  (clojure.string/split input #"\|"))


(defn create-relationship-from-string
  "relate two nodes given the input string of the format
   node_1_id|RELATIONSHIP_TYPE|node_2_id"
  [input driver-session]
  (let [[cypher-query cypher-params] (apply create-relationship-statement (decompose-relationship-string input))]
    (run-cypher-stmt-with-data-no-return cypher-query cypher-params driver-session)))


;; TESTING SECTION

;;(import '[org.neo4j.driver GraphDatabase])
;;(import '[org.neo4j.driver AuthTokens])
;;(import '[org.neo4j.driver Values])
;;(import '[org.neo4j.driver TransactionWork])
;;(require '[clojure.pprint :as p])
;;(require '[dungeon-master.config :refer [database-url]])
;;(require '[dungeon-master.llm.embedding :refer [get-embedding]])
;;(require '[dungeon-master.repositories.util :refer [create-node
;;                                                      create-node-with-embedding]])


;;(with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;  (with-open [session (.session driver)]
;;    (insert-or-update-entity-node
;;      {"label" "Person" "name" "Conan" "id" "conan" "description" "A barbarian of Cimmeria, destined to be king."}
;;      session)))

;;(with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;  (with-open [session (.session driver)]
;;    (insert-or-update-entity-node
;;      {"label" "Person" "name" "Grog Strongjaw" "id" "grogStrongjaw" "description" "A goliath barbarian of the mountain tribes. currently attempting to grow a beard through magical means."}
;;      session)))

;;(if "foo"
;;  "yes"
;;  "no")
;;
;;(def entity-data {"label" "Person",
;; "id" "lordDhelt"
;; "name" "Lord Dhelt"
;; "description"
;; "A nobleman in the tavern seeking assistance with a delicate matter involving a stolen family heirloom."})
;;
;;(println ("description" entity-data))
;;(println (get entity-data "description"))
;;(println (get entity-data "ducks"))
;;
;;(if ("description" entity-data)
;;  "yes"
;;  "no")
