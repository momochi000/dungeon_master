(ns dungeon-master.llm.embedding
  (:require [wkok.openai-clojure.api :as api]
            [dungeon-master.config :refer [openai-api-key]]))


(def default-embedding-model "text-embedding-3-small")

(defn- input-to-embedding [inputs embeddings]
  (map
    (fn [i e] {:input-text i :embedding e})
    inputs embeddings))

(defn- response-to-embedding-list [api-response]
  (map
    (fn [curr-embedding-response] (curr-embedding-response :embedding))
    (api-response :data)))


(defn get-embedding
  "return embedding values for a given string
  this can take either a single string or a list of strings
  returns a hash map of the form:
  {:input-text input-text
  :embedding [0.2239 0.0001278 0.238 ...]}
  "
  [input-text]
  (let [api-response (api/create-embedding {:model default-embedding-model
                                            :input input-text}
                                           {:api-key openai-api-key})]
    ;; The api-response is of the form:
    ;; {:object list, :data [{:object embedding, :index 0, :embedding [-0.00893185 ...] , :model text-embedding-3-small, :usage {:prompt_tokens 2, :total_tokens 2}} }
    (if (coll? input-text)
      (input-to-embedding
        input-text
        (response-to-embedding-list api-response))
      {:input-text input-text :embedding ((first (api-response :data)) :embedding)}
      )))





;; TESTING SECTION

;;(require '[wkok.openai-clojure.api :as api]
;;         '[dungeon-master.config :refer [database-url]]
;;         '[dungeon-master.repositories.util :refer [run-cypher-stmt-with-data
;;                                                    run-cypher-read-many-results-with-params
;;                                                    ]]
;;         :reload
;;         )
;;(import '[org.neo4j.driver GraphDatabase]
;;           '[org.neo4j.driver AuthTokens]
;;           '[org.neo4j.driver TransactionWork])

;; (get-embedding ["ducks"])
;;(get-embedding ["ducks" "and" "geese are the most hazardous"])

;;(defn test-add-vector
;;  "Test adding a vector onto an existing node"
;;  []
;;  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (let [embedding (get-embedding "sir dunksalot : a test description")
;;            cypher-string "MATCH (n:KnowledgeObject {id: $node_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"
;;            ]
;;
;;        ;(println "DEBUG: got embedding for the test vector ----> " embedding)
;;        (run-cypher-stmt-with-data
;;          cypher-string
;;          {"name_id" "dunkey1235" "vector" embedding}
;;          session)
;;
;;        )
;;
;;      )))
;;(test-add-vector)
;;
;;(defn test-query-with-embedding
;;  []
;;  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (let [embedding (get-embedding "sir dunksalot : a test description")
;;            ;cypher-string "MATCH (n:KnowledgeObject {id: $node_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"
;;
;;            ;cypher-string "MATCH (n:KnowledgeObject) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding, score RETURN similarEmbedding.name AS name, score"
;;            cypher-string "MATCH (n:KnowledgeObject) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS nodeMatch, score RETURN nodeMatch AS nodeMatch, score"
;;            ;cypher-string "MATCH (n:KnowledgeObject) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding RETURN similarEmbedding.name AS name"
;;
;;            ;cypher-string "MATCH (n:Person) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding, score RETURN similarEmbedding, score"
;;            ;cypher-string "MATCH (n:Person) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding, score RETURN similarEmbedding, score"
;;            ;cypher-string "MATCH (n:Person {name_id: $name_id}) RETURN n"
;;            ]
;;
;;        (run-cypher-read-many-results-with-params
;;          cypher-string
;;          {"vector" (:embedding embedding)}
;;          session)
;;  ))))
;;
;;(def temp (test-query-with-embedding))
;;(.get (first temp) "score")
