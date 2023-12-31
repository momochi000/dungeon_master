(ns dungeon-master.fixtures.test-world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           ;;[org.neo4j.driver Values parameters]
           [org.neo4j.driver TransactionWork])
  (:require [cheshire.core :as json]))


(def fixture-json-string
"{
    \"entities\": [
        {\"label\":\"Person\",\"id\":\"coran\",\"name\":\"Coran\",\"description\":\"The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate\"},
        {\"label\":\"Person\",\"id\":\"lordDhelt\",\"name\":\"Lord Dhelt\",\"description\":\"A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help\"},
        {\"label\":\"Place\",\"id\":\"blushingMermaidTavern\",\"name\":\"Blushing Mermaid Tavern\",\"description\":\"The tavern in Baldur's Gate known for a warm atmosphere and busy clientele\"},
        {\"label\":\"Place\",\"id\":\"baldursGate\",\"name\":\"Baldur's Gate\",\"description\":\"The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran\"}
    ],
    \"relationships\": [
        \"coran|AT|blushingMermaidTavern\",
        \"lordDhelt|AT|blushingMermaidTavern\",
        \"blushingMermaidTavern|IN|baldursGate\"
    ]
}")

;; These are the statements we want to run.
;;-- CREATE CONSTRAINT IF NOT EXISTS FOR (p:Person) REQUIRE (p.name) IS UNIQUE;
;;CREATE INDEX IF NOT EXISTS FOR (p:Person) ON (p.name);
;;-- CREATE CONSTRAINT IF NOT EXISTS FOR (p:Place) REQUIRE (p.name) IS UNIQUE;
;;CREATE INDEX IF NOT EXISTS FOR (p:Place) ON (p.name);

;;CREATE (coran:Person {name:'Coran', description:'The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate'})
;;CREATE (lordDhelt:Person {name:'Lord Dhelt', description:'A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help'})
;;CREATE (blushingMermaidTavern:Place {name:'Blushing Mermaid Tavern', description:'The tavern in Baldur's Gate known for a warm atmosphere and busy clientele'})
;;CREATE (baldursGate:Place {name:'Baldur\'s Gate', description:'The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran'})
;;CREATE
;;  (coran)-[:AT]->(blushingMermaidTavern),
;;  (lordDhelt)-[:AT]->(blushingMermaidTavern),
;;  (blushingMermaidTavern)-[:IN]->(baldursGate)


;; TODO: update this to take in or read the config for the graph database
(defn insert-test-world-state
  "insert some fixed data into the graph db"
  []
  (with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
      (with-open [session (.session driver)]
        (let [result (.run session "CREATE (n) RETURN n")]
      (println (.single result))))))


(defn create-person-node
  "create a person node"
  [node-data session-driver]
  (.writeTransaction
    session-driver
    (reify TransactionWork (execute [this tx]
                             (let [result
                                   (.run tx
                                         "MERGE (p:Person {name: $name, description: $description}) RETURN (p)"
                                         node-data)]
                               (.single result))))))

;; next steps
;; TODO: now put the above together:
;;      parse the json string
;;      loop over entities
;;      for each entity, call the right function: create person node or create place node
;;        need to figure out how to do that cleanly in clojure
;;        it looks like maybe cond-> is able to do it?
;; TODO: , create the relationships as defined in the input json



;; testing this out

;; From within the compose cluster, use graphdb as the hostname of the neo4j instance
;; but running in my repl, i can access it as localhost
;;(defn test-session
;;  "simply providing a session to test individual queries, takes in the function requring the session"
;;  [query-func data]
;;  ;;(with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (query-func data session))))


;;(import '(org.neo4j.driver TransactionWork))
;;(import '(org.neo4j.driver GraphDatabase))
;;(import '(org.neo4j.driver AuthTokens))
;;
;;
;;(def sample-person
;;  {:id "coran" :name "Coran" :description "some test description"})
;;
;;(print (sample-person :id))
;;
;;(def sample-person
;;  {"id" "coran" "name" "Coran" "description" "some test description"})
;;
;;(print (sample-person "id"))
;;
;;
;;(test-session create-person-node sample-person)


;; parse the fixture json into a clojure map
;; the true here says make the keys into symbols. we might not want this though.
;; (json/parse-string fixture-json-string true)
