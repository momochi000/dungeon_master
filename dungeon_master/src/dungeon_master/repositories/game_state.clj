(ns dungeon-master.repositories.game-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens])
  (:require [cheshire.core :as json]
            [dungeon-master.config :refer [database-url]]
            [dungeon-master.repositories.util :refer [create-node
                                                      create-relationship-statement
                                                      run-cypher-stmt
                                                      run-cypher-stmt-with-data
                                                      run-cypher-stmt-with-data-no-return]]
            ))


(defn save-state
  "dump the current game state into the database for reload later"
  [game-state]
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [json-string (json/generate-string game-state)
            cypher-string "CREATE (gs:GameState {data: $data})"]

        (run-cypher-stmt-with-data-no-return cypher-string {"data" json-string} session)
        ))))

(defn load-state
  "load the game state from the database" []
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
    (with-open [session (.session driver)]
      (let [cypher-string "MATCH (gamestate:GameState) RETURN gamestate.data"
            cypher-result (run-cypher-stmt cypher-string session)
            game-state-json (.asString (.get cypher-result "gamestate.data"))]

        (json/parse-string game-state-json true)
        ))))


;; For testing


;;(import '[org.neo4j.driver GraphDatabase]
;;        '[org.neo4j.driver AuthTokens]
;;        '[org.neo4j.driver TransactionWork]
;;        '[org.neo4j.driver InternalRecord]
;;        )
;;(require '[cheshire.core :as json])
;;(require '[dungeon-master.config :refer [database-url]]
;;         '[dungeon-master.repositories.util :refer [create-node
;;                                                      create-relationship-statement
;;                                                      run-cypher-stmt
;;                                                      run-cypher-stmt-with-data
;;                                                      run-cypher-stmt-with-data-no-return]]
;;         )

;;(require '[dungeon-master.fixtures.test-world-state :refer [test-game-state]])
;;
;;(save-state test-game-state)
;;
;;(with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;  ;;(let [json-string (json/generate-string test-game-state)
;;  (let [json-string (json/generate-string (:interaction-history test-game-state))
;;        cypher-string "CREATE (GameState {data: $data})"]
;;
;;    (.withParameters (.executableQuery driver cypher-string) {"data" json-string})
;;    ))
;;
;;
;;
;;
;;(defn load-state
;;  "load the game state from the database" []
;;  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (let [cypher-string "MATCH (gamestate:GameState) RETURN gamestate.data"
;;            run-cypher-stmt (.readTransaction
;;                              session
;;                              (reify TransactionWork (execute [this tx]
;;                                                       (let [result
;;                                                             (.run tx
;;                                                                   cypher-string)]
;;                                                         (.single result)))))
;;            game-state-json run-cypher-stmt]
;;
;;        (println "DEBUG: game-state-json class is --> " (class game-state-json))
;;        (println "DEBUG: game-state-json is --> " game-state-json)
;;        (println "DEBUG: calling .get on game-state-json is --> " (.get game-state-json "data"))
;;        ;;(println "DEBUG: calling .get on game-state-json is --> " (.get game-state-json))
;;
;;        ;;(json/parse-string game-state-json true)
;;        game-state-json
;;        ))))
