(ns dungeon-master.llm.embedding
  (:require [wkok.openai-clojure.api :as api]))


(def default-embedding-model "text-embedding-3-small")
(defn get-embedding
  "return embedding values for a given string
  this can take either a single string or a list of strings
  "
  [input-text]
  (let [api-response (api/create-embedding {:model default-embedding-model
                                            :input input-text})]
    ;; The api-response is of the form:
    ;; {:object list, :data [{:object embedding, :index 0, :embedding [-0.00893185 ...] , :model text-embedding-3-small, :usage {:prompt_tokens 2, :total_tokens 2}} }
    (if (coll? input-text)
      (input-to-embedding
        input-text
        (response-to-embedding-list api-response))

      ;; TODO: Clean this up
      ;; if we're calling embed with 1 string, then we expect only 1 vector
      {:input-text input-text :embedding ((first (api-response :data)) :embedding)}
      )))

(defn- input-to-embedding [inputs embeddings]
  (map
    (fn [i e] {:input-text i :embedding e})
    inputs embeddings))

(defn- response-to-embedding-list [api-response]
  (map
    (fn [curr-embedding-response] (curr-embedding-response :embedding))
    (api-response :data)))






;; TESTING SECTION

(require '[wkok.openai-clojure.api :as api])

;;(get-embedding ["ducks"])
;;(get-embedding ["ducks" "and" "geese are the most hazardous"])

(defn test-add-vector
  []
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [embedding (get-embedding "sir dunksalot : a test description")
            ;cypher-string "MATCH (n:KnowledgeObject {id: $node_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"
            cypher-string "MATCH (n:Person {name_id: $name_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"
            ;cypher-string "MATCH (n:Person {name_id: $name_id}) RETURN n"
            ]


        ;;(println "DEBUG: got embedding for the test vector ----> " embedding)
        (run-cypher-stmt-with-data
          cypher-string
          {"name_id" "dunkey1235" "vector" embedding}
          session)

        )

      )))
;(test-add-vector)

;; LEFT OFF: I think the problem here is i can only use the vector search on a node of type(label) KnowledgeObject (since that's where the index is placed) I need to re-create my test node with that label
(defn test-query-with-embedding
  []
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [embedding (get-embedding "sir dunksalot : a test description")
            ;cypher-string "MATCH (n:KnowledgeObject {id: $node_id}) CALL db.create.setNodeVectorProperty(n, 'embedding', $vector) RETURN n"
            cypher-string "MATCH (n:Person) CALL db.index.vector.queryNodes('knowledge-embeddings', 2, $vector) YIELD node AS similarEmbedding, score RETURN similarEmbedding"
            ;cypher-string "MATCH (n:Person {name_id: $name_id}) RETURN n"
            ]

        (run-cypher-read-many-results-with-params
          cypher-string
          {"vector" embedding}
          session)
  ))))
(def temp (test-query-with-embedding))


;;(.get temp "similarEmbedding")

(map
  (fn [r] (.asString (.get (.get r "similarEmbedding") "name_id")))
  temp)

(require '[dungeon-master.repositories.util :refer [run-cypher-read-many-results-with-params]])
