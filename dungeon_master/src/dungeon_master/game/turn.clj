(ns dungeon-master.game.turn
  (:require [dungeon-master.llm.gpt
             :refer [run-completion
                     get-result-message
                     get-result-content
                     generate-dm-prompt]]
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
  ;; update game state
  ;; update world state
      update-world-state)
  ;; present response to user
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



(defn update-world-state
  "given the latest message from the dungeon master, extract any new entities and/or
  relationships and update the graph db accordingly"
  [game-state]

  (let [extracted-entity-response (extract-entities game-state)
        extract-entities-json (get-result-tool-arguments extracted-entity-response)
        extract-entities-map (json/parse-string extract-entities-json) ]

    (update-db-world-state extract-entities-map)
    game-state))


;; TESTING SECTION
;;(require '[dungeon-master.game-state :as gs])
;;(require '[dungeon-master.llm.gpt :refer [run-completion generate-dm-prompt]])
;;(require '[cheshire.core :as json])
(require '[dungeon-master.repositories.world-state :refer [update-db-world-state]])
(require '[dungeon-master.llm.gpt :refer [generate-dm-prompt get-result-message get-result-content get-result-tool-calls get-result-tool-arguments run-completion]])
;;(require '[ dungeon-master.game.data :refer [extract-entities]])

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
