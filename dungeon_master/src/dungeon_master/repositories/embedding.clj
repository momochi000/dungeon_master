(ns dungeon-master.repositories.embedding
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver TransactionWork])
  (:require [dungeon-master.config :refer [database-url]]
            [dungeon-master.repositories.util :refer [run-cypher-stmt-with-data]]
            ))


(defn add-embedding
  "Given a node's name-id and a vector to attach, attach the vector to the node"
  [name-id embedding-vector]

    (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
      (with-open [session (.session driver)]
        (let [cypher-string "MATCH (n:Person {name_id: $name_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"]

        (run-cypher-stmt-with-data
          cypher-string
          {"name_id" name-id "vector" embedding-vector}
          session))
  )))

(defn find-knn
  "Find and return the k nearest node-ids to the given embedding vector"
  [embedding-vector k]

  (let [query-result (find-knn-nodes-query embedding-vector k)
        nodes (query-result-to-nodes query-result) ]

    nodes
    ))

(defn- find-knn-nodes-query
  [embedding-vector k]

  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [cypher-string "MATCH (n:KnowledgeObject) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding, score RETURN similarEmbedding"])
      (run-cypher-stmt-with-data
        cypher-string
        {"vector" embedding}
        session)
      ))
  )

(defn- query-result-to-nodes
  [query-result]
  (map fn [r] (.get r "similarEmbedding")))
