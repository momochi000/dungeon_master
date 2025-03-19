(ns dungeon-master.repositories.embedding
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver TransactionWork])
  (:require [dungeon-master.config :refer [database-url]]
            [dungeon-master.repositories.util :refer [run-cypher-stmt-with-data run-cypher-read-many-results-with-params]]
            ))


(defn add-embedding
  "Given a node's name-id and a vector to attach, attach the vector to the node"
  [name-id embedding-vector]

    (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
      (with-open [session (.session driver)]
        (let [cypher-string "MATCH (n:KnowledgeObject {name_id: $name_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"]

        (run-cypher-stmt-with-data
          cypher-string
          {"name_id" name-id "vector" embedding-vector}
          session))
  )))


(defn find-knn-nodes-query
  "given an embedding vector, return the k most similar nodes (along with their similarity scores)"
  [embedding-vector k]

  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [cypher-string "MATCH (n:KnowledgeObject) CALL db.index.vector.queryNodes('knowledge-embeddings', $numNodes, $vector) YIELD node AS nodeMatch, score RETURN nodeMatch, score"]
        (run-cypher-read-many-results-with-params
          cypher-string
          {"vector" embedding-vector
           "numNodes" k}
          session))
      )))

(defn- query-result-to-node
  [query-result]
  (let [node (-> query-result (.get "nodeMatch") .asNode)]

    {:id (.id node)
     :name-id (-> node (.get "name_id") .asString)
     :description (-> node (.get "description") .asString) ;(.get node "description")
     :name (-> node (.get "name") .asString) ;(.get node "name")
     })
  )

(defn find-knn
  "Find and return the k nearest node-ids to the given embedding vector
  The resulting structure looks like:
  ```
  { :node {:id foo :name-id bar :description ... }
    :score .994 }
  ```
  "
  [embedding-vector k]
  (let [query-results (find-knn-nodes-query embedding-vector k)]
    (map
      (fn [r]
        {:node (query-result-to-node r)
         :score (-> r (.get "score") .asFloat)})
      (take k query-results))))


;; TESTING SECTION

;;(import '[org.neo4j.driver GraphDatabase]
;;           '[org.neo4j.driver AuthTokens]
;;           '[org.neo4j.driver TransactionWork])
;;(require '[dungeon-master.config :refer [database-url]]
;;            '[dungeon-master.repositories.util :refer [run-cypher-stmt-with-data run-cypher-read-many-results-with-params]]
;;            )
;;(def query-output
;;  (let [e (get-embedding "a test description")
;;        out (find-knn-nodes-query (:embedding e) 2) ]
;;    out
;;    ))
;;(query-result-to-node (first query-output))
;;
;;(def temp
;;  (let [e (get-embedding "a test description")
;;        out (find-knn (:embedding e) 2) ]
;;    out
;;    ))
