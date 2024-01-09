(ns dungeon-master.game.turn
  (:require [dungeon-master.llm.gpt :refer [run-completion]]
            [dungeon-master.game-state :refer [generate-prompt]]))

(defn run-turn
  "Execute a single game turn given some user input
   This is where the magic (will) happens"
  [game-state user-input]
  (-> game-state
      (add-user-input-to-interaction-history user-input)
      call-gpt)
  ;; decide needed context from input
  ;; search for context
  ;; formulate prompt
  ;; send prompt to llm
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
                     (generate-prompt game-state))
        result-message (-> gpt-result :choices first :message)]

    (assoc
      game-state
      :interaction-history
      (conj (:interaction-history game-state) result-message))))


;; TESTING SECTION
;;(require '[dungeon-master.game-state :as gs])
;;(require '[dungeon-master.llm.gpt :refer [run-completion]])
;;(require '[dungeon-master.game.prompt :refer [generate-prompt]])
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
