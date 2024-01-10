(ns dungeon-master.game.turn
  (:require [dungeon-master.llm.gpt
             :refer [run-completion
                     get-result-message
                     get-result-content]]
            [dungeon-master.game-state :refer [generate-dm-prompt]]
            [dungeon-master.game.data :refer [extract-entities]]
            [dungeon-master.repositories.world-state :refer [update-db-world-state]]))

(defn run-turn
  "Execute a single game turn given some user input
   This is where the magic (will) happens"
  [game-state user-input]
  (-> game-state
      (add-user-input-to-interaction-history user-input)
  ;; decide needed context from input
  ;; search for context
  ;; formulate prompt
  ;; send prompt to llm
      call-gpt
      update-world-state)
  ;; update game state
  ;; present response to user
  ;; update world state
  )

(defn add-user-input-to-interaction-history
  [game-state user-input]
  (assoc game-state :interaction-history (conj (:interaction-history game-state) {:role "user" :content user-input})))


(defn call-gpt
  [game-state]
  (let [gpt-result (run-completion
                     (generate-dm-prompt game-state))
        result-message (get-result-message gpt-result)]

    (assoc
      game-state
      :interaction-history
      (conj (:interaction-history game-state) result-message))))



;;(require '[dungeon-master.llm.gpt :refer [run-completion get-result-message get-result-content]])
;;(require '[dungeon-master.repositories.world-state :refer [update-db-world-state]])
;;(require '[cheshire.core :as json])


(defn update-world-state
  "given the latest message from the dungeon master, extract any new entities and/or
  relationships and update the graph db accordingly"
  [game-state]

  (let [extracted-entity-response (extract-entities game-state)
        entity-message (get-result-message extracted-entity-response)
        entity-json (get-result-content extracted-entity-response)
        ;; LEFT OFF ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ;; there may be something wrong with the json parse here, need to debug
        ;; Looks like sometimes the returned json is not correct, it's missing the outer brackets {}
        ;;entity-map (json/parse-string entity-json)]
        ]

    (println "DEBUG: got result from gpt extracting entities from response ==============>")
    (println entity-message)
    (println "DEBUG: let me specifically get the content ==============>")
    (println entity-json)
    ;;(println "DEBUG: checking the result of parsing the thing ======================================>")
    ;;(println (json/parse-string entity-json))
    ;;(println "======================================XXXXXXXXXX")

    (update-db-world-state (json/parse-string entity-json))

    ;;(update-db-world-state entity-map)

    ;;extracted-entity-response
    ;;game-state

    entity-json
    ))


;; TESTING SECTION
;;(require '[dungeon-master.game-state :as gs])
;;(require '[dungeon-master.llm.gpt :refer [run-completion]])
;;(require '[dungeon-master.game.prompt :refer [generate-dm-prompt]])
;;
;;(add-to-interaction-history (gs/blank-game-state) "hello")
;;(ns-aliases *ns*)
;;(ns-publics gs)
;;
;;(def blank (gs/blank-game-state))
;;(def test-input "something")
;;
;;(assoc blank :interaction-history (conj (:interaction-history blank) test-input))
;;
;;(add-user-input-to-interaction-history blank "hello")
;;
;;(run-turn blank "I enter the run-down tavern")
