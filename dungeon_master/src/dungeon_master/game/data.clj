(ns dungeon-master.game.data
  (:require [cheshire.core :as json]
            [dungeon-master.repositories.util :refer [clear-db]]
            [dungeon-master.game-state :refer [get-last-message]]
            [dungeon-master.llm.gpt :refer [run-function-completion extract-entities-prompt]]
            [dungeon-master.fixtures.test-world-state :refer [fixture-json-string
                                                              test-game-state
                                                              insert-test-world-state]]

            ))


;;(require '[dungeon-master.game-state :refer [get-last-message]])
;;(require '[ dungeon-master.llm.gpt :refer [run-function-completion extract-entities-prompt]])

(defn extract-entities
  "obtain entities from the last message in the interaction history.
  Sends a request to the llm asking it to identify entities and their
  relationships and returns a json string representing them."
  [game-state]
  (let [last-message (get-last-message game-state)
        completion-messages [{:role "system" :content extract-entities-prompt}
                             {:role "user" :content last-message}]]

    (run-function-completion completion-messages :extract-entities)))

;; this is what i can use to start playtesting the game
(defn initialize-strawman-state
  "For now, create an expected version of game state. Just use the fixture we set up in test_world_state"
  []
  ;; clear the database
  (println "DEBUG: initialize-strawman-state: clearing the db")
  (clear-db)

  ;; insert the strawman data into the db
  (println "DEBUG: initialize-strawman-state: inserting strawman data into the db")
  (insert-test-world-state (json/parse-string fixture-json-string))
  ;; return the dummy game state
  (println "DEBUG: initialize-strawman-state: returning the dummy game state")
  (test-game-state))
