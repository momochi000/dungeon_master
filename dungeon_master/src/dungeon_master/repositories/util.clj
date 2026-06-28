(ns dungeon-master.repositories.util
  (:require [clojure.string :as str]
            [dungeon-master.config :refer [database-path]])
  (:import [com.ladybugdb Database Connection Value]
           [java.util ArrayList HashMap]))

(def embedding-dim 1536)

(def ^:private supported-node-labels
  #{"Person" "Place"})

(def ^:private relationship-type->table-name
  {"AT" "AtRel"
   "HAS" "HasRel"
   "IN" "InRel"
   "KNOWS" "KnowsRel"})

(def ^:private graph-schema-statements
  [(format "CREATE NODE TABLE IF NOT EXISTS KnowledgeObject(name_id STRING PRIMARY KEY, label STRING, name STRING, description STRING, embedding FLOAT[%d]);"
           embedding-dim)
   "CREATE NODE TABLE IF NOT EXISTS GameState(save_file_name STRING PRIMARY KEY, data STRING);"
   "CREATE REL TABLE InRel(FROM KnowledgeObject TO KnowledgeObject);"
   "CREATE REL TABLE AtRel(FROM KnowledgeObject TO KnowledgeObject);"
   "CREATE REL TABLE KnowsRel(FROM KnowledgeObject TO KnowledgeObject);"
   "CREATE REL TABLE HasRel(FROM KnowledgeObject TO KnowledgeObject);"])

(def ^:private query-param-pattern #"\$([A-Za-z][A-Za-z0-9_]*|\d+)")

(defn- ex-message* [^Throwable e]
  (or (.getMessage e) (str e)))

(defn- already-exists-error? [message]
  (boolean (re-find #"(?i)already exists" (or message ""))))

(defn- lookup-param [params token]
  (or (get params token)
      (get params (keyword token))
      (get params (symbol token))))

(defn- ordered-distinct [xs]
  (reduce (fn [acc x]
            (if (some #{x} acc)
              acc
              (conj acc x)))
          []
          xs))

(defn- positionalize-params
  [query params]
  (let [tokens (ordered-distinct (map second (re-seq query-param-pattern query)))]
    (if (or (empty? tokens)
            (every? #(re-matches #"\d+" %) tokens))
      {:query query
       :params (into {}
                     (map (fn [token]
                            [token (lookup-param params token)]))
                     tokens)}
      (let [token->index (zipmap tokens (map #(str (inc %)) (range)))
            rewritten-query (str/replace query
                                         query-param-pattern
                                         (fn [[_ token]]
                                           (str "$" (token->index token))))]
        {:query rewritten-query
         :params (into {}
                       (map (fn [[token index]]
                              [index (lookup-param params token)]))
                       token->index)}))))

(defn- ->java-object [value]
  (cond
    (nil? value) nil
    (string? value) value
    (keyword? value) (name value)
    (symbol? value) (name value)
    (map? value) (let [m (HashMap.)]
                   (doseq [[k v] value]
                     (.put m (str k) (->java-object v)))
                   m)
    (sequential? value) (let [xs (ArrayList.)]
                          (doseq [item value]
                            (.add xs (->java-object item)))
                          xs)
    :else value))

(defn- ->ladybug-value [value]
  (if (instance? Value value)
    value
    (if (nil? value)
      (Value/createNull)
      (Value. (->java-object value)))))

(defn- ->java-params [params]
  (let [m (HashMap.)]
    (doseq [[k v] params]
      (.put m (str k) (->ladybug-value v)))
    m))

(defn- result-error! [query message]
  (throw (ex-info (str "Ladybug query failed: " message)
                  {:query query
                   :message message})))

(defn- assert-success! [query result]
  (when-not (.isSuccess result)
    (result-error! query (.getErrorMessage result)))
  result)

(defn- read-value [value]
  (cond
    (nil? value) nil
    (.isNull value) nil
    :else
    (let [raw (.getValue value)]
      (cond
        (instance? java.util.List raw) (vec raw)
        (instance? java.util.Map raw) (into {} raw)
        (nil? raw) (str value)
        :else raw))))

(defn- query-result->rows [result]
  (let [num-cols (.getNumColumns result)
        col-names (mapv #(.getColumnName result %) (range num-cols))]
    (loop [rows []]
      (if (.hasNext result)
        (let [tuple (.getNext result)
              row (reduce (fn [acc idx]
                            (assoc acc
                                   (nth col-names idx)
                                   (read-value (.getValue tuple idx))))
                          {}
                          (range num-cols))]
          (recur (conj rows row)))
        rows))))

(defn- execute-query!
  ([query conn]
   (let [result (.query conn query)]
     (assert-success! query result)))
  ([query params conn]
   (if (empty? params)
     (execute-query! query conn)
     (let [{:keys [query params]} (positionalize-params query params)
           prepared (.prepare conn query)]
       (when-not (.isSuccess prepared)
         (result-error! query (.getErrorMessage prepared)))
       (let [result (.execute conn prepared (->java-params params))]
         (assert-success! query result))))))

(defn- run-schema-statement!
  [statement conn]
  (try
    (with-open [result (execute-query! statement conn)]
      :ok)
    (catch Exception e
      (if (already-exists-error? (ex-message* e))
        :ok
        (throw e)))))

(defn ensure-graph-schema!
  [conn]
  (doseq [statement graph-schema-statements]
    (run-schema-statement! statement conn))
  :ok)

(defmacro with-connection
  [[conn-sym] & body]
  `(with-open [db# (Database. database-path)
               ~conn-sym (Connection. db#)]
     (ensure-graph-schema! ~conn-sym)
     ~@body))

(defn clear-db
  "Delete all entities and relationships in the database."
  []
  (with-connection [conn]
    (with-open [result (execute-query! "MATCH (n) DETACH DELETE n;" conn)]
      :ok)))

(defn run-cypher-stmt-with-data
  "Run a statement with params and return the first row as a Clojure map."
  [cypher-statement node-data conn]
  (with-open [result (execute-query! cypher-statement node-data conn)]
    (first (query-result->rows result))))

(defn run-cypher-read-one-result-no-params
  [cypher-statement conn]
  (with-open [result (execute-query! cypher-statement conn)]
    (first (query-result->rows result))))

(defn run-cypher-read-many-results-with-params
  [cypher-statement params conn]
  (with-open [result (execute-query! cypher-statement params conn)]
    (vec (query-result->rows result))))

(defn run-cypher-stmt-with-data-no-return
  "Run a statement with params and ignore any returned rows."
  [cypher-statement node-data conn]
  (with-open [result (execute-query! cypher-statement node-data conn)]
    :ok))

(defn- assert-supported-node-label! [label]
  (when-not (contains? supported-node-labels label)
    (throw (ex-info (str "Unsupported node label: " label)
                    {:label label
                     :supported-labels supported-node-labels}))))
  label)

(defn- relationship-type->rel-table [relationship-type]
  (or (get relationship-type->table-name relationship-type)
      (throw (ex-info (str "Unsupported relationship type: " relationship-type)
                      {:relationship-type relationship-type
                       :supported-relationship-types (keys relationship-type->table-name)}))))

(defn create-node
  "Create or update a graph node in Ladybug.
  Required keys: \"label\", \"id\". Optional keys: \"name\", \"description\"."
  [node-data conn]
  (let [label (assert-supported-node-label! (get node-data "label"))
        sanitized-node-data (merge {"name" nil "description" nil} (assoc node-data "label" label))
        cypher-string
        (str "MERGE (n:KnowledgeObject {name_id: $id}) "
             "ON CREATE SET n.label = $label, n.name = $name, n.description = $description "
             "ON MATCH SET n.label = COALESCE($label, n.label), "
             "n.name = COALESCE($name, n.name), "
             "n.description = COALESCE($description, n.description) "
             "RETURN n.name_id AS name_id, n.label AS label, n.name AS name, n.description AS description")]
    (run-cypher-stmt-with-data cypher-string sanitized-node-data conn)))

(defn create-node-with-embedding
  "Create or update a graph node in Ladybug and also store its embedding."
  [node-data conn]
  (let [label (assert-supported-node-label! (get node-data "label"))
        sanitized-node-data (merge {"name" nil "description" nil "vector" nil} (assoc node-data "label" label))
        cypher-string
        (str "MERGE (n:KnowledgeObject {name_id: $id}) "
             "ON CREATE SET n.label = $label, n.name = $name, n.description = $description, n.embedding = $vector "
             "ON MATCH SET n.label = COALESCE($label, n.label), "
             "n.name = COALESCE($name, n.name), "
             "n.description = COALESCE($description, n.description), "
             "n.embedding = COALESCE($vector, n.embedding) "
             "RETURN n.name_id AS name_id, n.label AS label, n.name AS name, n.description AS description")]
    (run-cypher-stmt-with-data cypher-string sanitized-node-data conn)))

(defn create-relationship-statement
  "Build a Ladybug Cypher statement for relating two nodes by name_id."
  [first-node-id relationship-type second-node-id]
  (let [rel-table (relationship-type->rel-table relationship-type)
        cypher-stmt (format
                      (str "MATCH (n1), (n2) "
                           "WHERE n1.name_id = $start_node_id AND n2.name_id = $end_node_id "
                           "MERGE (n1)-[:%s]->(n2)")
                      rel-table)
        cypher-params {"start_node_id" first-node-id
                       "end_node_id" second-node-id}]
    [cypher-stmt cypher-params]))
