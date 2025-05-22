(ns dungeon-master.game.turn
  (:require
    [dungeon-master.llm.gpt :refer [run-completion
                                    get-result-message
                                    get-result-tool-arguments
                                    generate-system-prompt]]
    [dungeon-master.game.data :refer [extract-entities]]
    [dungeon-master.game.data.entities :refer [compile-context-for-entity-extraction]]
    [dungeon-master.game.prompt :refer [generate-dm-prompts]]
    [dungeon-master.game.state :refer [add-user-input-to-interaction-history
                                       add-user-input-to-working-memory
                                       update-knowledge-context-to-working-memory
                                       ]]
    [dungeon-master.game.knowledge :refer [find-k-similar-descriptions]]
    [dungeon-master.repositories.world-state :refer [update-db-world-state]]
    [cheshire.core :as json]
    [clojure.tools.logging :as logging]
    ))

(declare update-world-state)
(declare call-gpt)

(defn- store-user-input
  "add user input into the game state"
  [game-state user-input]
  (-> game-state
      (add-user-input-to-interaction-history user-input)
      (add-user-input-to-working-memory user-input)))

(defn obtain-context
  "Given the current state of the game, do we need to query the knowledge base for more context that the DM needs to play their part this turn? If yes, then make that query and stuff it into working memory. Return the new game state including changes to working memory"
  [game-state]

  ;; IN PROGRESS: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ;; query knowledgebase based on user input
  ;; add that context into the working memory of game state
  ;; return new game state


  (let [latest-input (get-in game-state [:working-memory :current-input])
        related-nodes (find-k-similar-descriptions latest-input 4)]

    ;; Both of these log messages are meant  to be examples for logging
    ;;(logging/log :debug "DEBUG: obtaining context from users input. nodes queried -=================> ")
    ;;(logging/debug (p/pprint related-nodes))

    (update-knowledge-context-to-working-memory game-state related-nodes))
  )

(defn run-turn
  "Execute a single game turn given some user input
   This is where the magic happens"
  [game-state user-input]
  ;;(println "DEBUG: in run-turn, the input is " user-input)
  (-> game-state
      (store-user-input user-input)
      ;; decide needed context from input
      ;; search for context
      obtain-context
      ;; formulate prompt
      ;; send prompt to llm
      call-gpt

      ;; update game state
      update-world-state)
  )

  ;;(assoc game-state :interaction-history (conj (:interaction-history game-state) {:role "user" :content user-input})))


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

  (let [extracted-entity-response (extract-entities (compile-context-for-entity-extraction game-state))
        extract-entities-json (get-result-tool-arguments extracted-entity-response)
        extract-entities-map (json/parse-string extract-entities-json) ]

    ;;(println (p/pprint extracted-entity-response))

    (update-db-world-state extract-entities-map)
    game-state))


;; TESTING SECTION
;;(require '[clojure.pprint :as p])
;;(require '[dungeon-master.game.state :as gs])
;;(require '[dungeon-master.llm.gpt :refer [run-completion generate-dm-prompt]])
;;(require '[cheshire.core :as json])
;;(require '[dungeon-master.repositories.world-state :refer [update-db-world-state]])
;;(require '[dungeon-master.llm.gpt :refer [generate-dm-prompt get-result-message get-result-content get-result-tool-calls get-result-tool-arguments run-completion]])
;;(require '[ dungeon-master.game.data :refer [extract-entities]])
;;(require '[dungeon-master.fixtures.test-world-state :refer :all])

;;(test-game-state)
;;(run-turn (test-game-state) "I lean back in my chair and take a sip of my mead. Lord Dhelt, you've done yourself a great service coming in here today and meeting me. Please, ease your burdens some and tell me more about these 'delicate matters'.")
