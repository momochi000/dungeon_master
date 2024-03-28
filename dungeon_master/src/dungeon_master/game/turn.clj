(ns dungeon-master.game.turn
  (:require
    [dungeon-master.llm.gpt :refer [run-completion
                                    get-result-message
                                    get-result-tool-arguments
                                    generate-system-prompt]]
    [dungeon-master.game.data :refer [extract-entities]]
    [dungeon-master.game.prompt :refer [generate-dm-prompts]]
    [dungeon-master.repositories.world-state :refer [update-db-world-state]]
    [cheshire.core :as json]
    ))

(declare update-world-state)
(declare call-gpt)
(declare add-user-input-to-interaction-history)

(defn run-turn
  "Execute a single game turn given some user input
   This is where the magic (will) happens"
  [game-state user-input]
  ;;(println "DEBUG: in run-turn, the input is " user-input)
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
  )

;;(defn- add-user-input-to-interaction-history
(defn add-user-input-to-interaction-history ;; making this public while debugging
  [game-state user-input]
  (assoc game-state :interaction-history (conj (:interaction-history game-state) {:role "user" :content user-input})))

(defn call-gpt
  [game-state]
  (let [system-prompt (generate-system-prompt
                        (:interaction-history game-state)
                        (clojure.string/join ". " (generate-dm-prompts game-state)))
        gpt-result (run-completion system-prompt)
        result-message (get-result-message gpt-result)]

    (assoc
      game-state
      :interaction-history
      (conj (:interaction-history game-state) result-message))))

;; TODO: this should happen asynchronously.
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
;;(require '[dungeon-master.repositories.world-state :refer [update-db-world-state]])
;;(require '[dungeon-master.llm.gpt :refer [generate-dm-prompt get-result-message get-result-content get-result-tool-calls get-result-tool-arguments run-completion]])
;;(require '[ dungeon-master.game.data :refer [extract-entities]])
;;(require '[dungeon-master.fixtures.test-world-state :refer :all])



;;(test-game-state)
;;(run-turn (test-game-state) "I lean back in my chair and take a sip of my mead. Lord Dhelt, you've done yourself a great service coming in here today and meeting me. Please, ease your burdens some and tell me more about these 'delicate matters'.")

