(ns dungeon-master.repositories.embedding
  (:require [dungeon-master.repositories.util :refer [with-connection
                                                      run-cypher-read-many-results-with-params
                                                      run-cypher-stmt-with-data-no-return]]))

(def ^:private vector-index-statements
  ["INSTALL VECTOR;"
   "LOAD VECTOR;"
   "CALL CREATE_VECTOR_INDEX('KnowledgeObject', 'knowledge_object_embedding_index', 'embedding', metric := 'cosine');"])

(defn- ex-message* [^Throwable e]
  (or (.getMessage e) (str e)))

(defn- ignorable-vector-error? [message]
  (boolean (re-find #"(?i)(already exists|already installed|already loaded)" (or message ""))))

(defn- ensure-vector-search-ready!
  [conn]
  (doseq [statement vector-index-statements]
    (try
      (run-cypher-stmt-with-data-no-return statement {} conn)
      (catch Exception e
        (when-not (ignorable-vector-error? (ex-message* e))
          (throw e))))))

(defn add-embedding
  "Given a node's name-id and a vector to attach, attach the vector to the node."
  [name-id embedding-vector]
  (with-connection [conn]
    (run-cypher-stmt-with-data-no-return
      (str "MATCH (n:KnowledgeObject) "
           "WHERE n.name_id = $name_id "
           "SET n.embedding = $vector")
      {"name_id" name-id
       "vector" embedding-vector}
      conn)))

(defn find-knn-nodes-query
  "Given an embedding vector, return the k most similar nodes (along with their distances)."
  [embedding-vector k]
  (with-connection [conn]
    (ensure-vector-search-ready! conn)
    (let [query
          (str "CALL QUERY_VECTOR_INDEX('KnowledgeObject', 'knowledge_object_embedding_index', $vector, $numNodes) "
               "RETURN node.name_id AS name_id, node.description AS description, "
               "node.name AS name, node.label AS label, distance")
          params {"vector" embedding-vector
                  "numNodes" k}]
      (->> (run-cypher-read-many-results-with-params query params conn)
           (sort-by #(double (or (get % "distance") Double/POSITIVE_INFINITY)))
           (take k)
           vec))))

(defn- query-result-to-node
  [query-result]
  {:name-id (get query-result "name_id")
   :description (get query-result "description")
   :name (get query-result "name")
   :label (get query-result "label")})

(defn find-knn
  "Find and return the k nearest nodes to the given embedding vector.
  The resulting structure looks like:
  {:node {:name-id ... :description ... :name ... :label ...}
   :score ...
   :distance ...}"
  [embedding-vector k]
  (map (fn [r]
         (let [distance (double (or (get r "distance") 1.0))]
           {:node (query-result-to-node r)
            :score (- 1.0 distance)
            :distance distance}))
       (find-knn-nodes-query embedding-vector k)))
