(ns dungeon-master.fixtures.test-world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver Values]
           [org.neo4j.driver TransactionWork])
  (:require [cheshire.core :as json]
            [dungeon-master.repositories.world-state 
             :refer [create-node 
                     create-relationship-from-string]]))

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
;;  (coran)-[:IN]->(blushingMermaidTavern),
;;  (lordDhelt)-[:IN]->(blushingMermaidTavern),
;;  (blushingMermaidTavern)-[:IN]->(baldursGate)


(def fixture-json-string
"{
    \"entities\": [
        {\"label\":\"Person\",\"id\":\"coran\",\"name\":\"Coran\",\"description\":\"The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate\"},
        {\"label\":\"Person\",\"id\":\"lordDhelt\",\"name\":\"Lord Dhelt\",\"description\":\"A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help\"},
        {\"label\":\"Place\",\"id\":\"blushingMermaidTavern\",\"name\":\"Blushing Mermaid Tavern\",\"description\":\"The tavern in Baldur's Gate known for a warm atmosphere and busy clientele\"},
        {\"label\":\"Place\",\"id\":\"baldursGate\",\"name\":\"Baldur's Gate\",\"description\":\"The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran\"}
    ],
    \"relationships\": [
        \"coran|IN|blushingMermaidTavern\",
        \"lordDhelt|IN|blushingMermaidTavern\",
        \"blushingMermaidTavern|IN|baldursGate\"
    ]
}")


;; TODO: update this to take in or read the config for the graph database
(defn insert-test-world-state
  "insert some fixed data into the graph db"
  []
  (with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
  ;;(with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
      (with-open [session (.session driver)]
        (let [world-data (json/parse-string fixture-json-string)
              entities (world-data "entities")
              relationships (world-data "relationships") ]

          (doall (map
                   (fn [entity-data]
                     (create-node entity-data session))
                   entities))

          (doall (map
                   (fn [relationship-data]
                     (create-relationship-from-string relationship-data session))
                   relationships))))))


;; TODO: use Values/parameters rather than just clojure maps to pass the args
  ;; couldn't get this to work. complains of 6 params being passed to Values.parameters
  ;; other means of calling `parameters` seemed to complain of things relating
  ;; to clojure data types not matching up with java datatypes



;; testing this out

;; Run these in repl or editor to have access to the imports needed in this namespace
;;(import '(org.neo4j.driver TransactionWork))
;;(import '(org.neo4j.driver GraphDatabase))
;;(import '(org.neo4j.driver AuthTokens))
;;(import '(org.neo4j.driver Values))
;;(require '[cheshire.core :as json])

;;(insert-test-world-state)


;;(defn test-create-relationship
;;  "insert some fixed data into the graph db"
;;  []
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;      (with-open [session (.session driver)]
;;        (create-relationship-from-string  "coran|IN|blushingMermaidTavern" session))))

;; From within the compose cluster, use graphdb as the hostname of the neo4j instance
;; but running in my repl, i can access it as localhost
;;(defn test-session
;;  "simply providing a session to test individual queries, takes in the function requring the session"
;;  [query-func data]
;;  ;;(with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (query-func data session))))


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


;;(def people
;;  '(
;;    {:name "john" :description "a person"}
;;    {:name "tony" :description "a person"}
;;    {:name "amy" :description "a person"}
;;    ))
;;
;;
;;(defn create-node
;;  [node-data driver-session]
;;  (.writeTransaction
;;    driver-session
;;    (reify TransactionWork (execute [this tx]
;;                             (let [result
;;                                   (.run tx
;;                                         "MERGE (p:Person {name: $name, description: $description}) RETURN (p)"
;;                                         node-data)]
;;                               (.single result)
;;                               )))))
;;
;;(defn test-insert-records
;;  "insert some fixed data into the graph db"
;;  []
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (create-node (first people) session))))
;;
;;      ;;(map
;;      ;;  (fn [entity-data] (create-node entity-data session))
;;      ;;  people))))
;;
;;(test-insert-records)
