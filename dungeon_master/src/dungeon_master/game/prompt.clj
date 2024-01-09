(ns dungeon-master.game.prompt
  (:require [dungeon-master.llm.gpt]
            [dungeon-master.util]))

(def default-history-length 8)

(defn generate-prompt
  "based on the current game state, generate the necessary prompt to send to the llm
   currently this depends on the current game mode and interaction history. For example, it depends on the player having entered some input, however, in the future, it may develop a different prompt if the user has not entered anything. For example, when starting a new session."
  [game-state]

  (cons
    {:role "system" :content dm-system-prompt}
    (last-n-elements (:interaction-history game-state) default-history-length)))

;; TESTING SECTION

;;(use 'dungeon-master.util)
;;(require '[dungeon-master.game-state :refer :all])
;;(require '[dungeon-master.llm.gpt :as gpt])
;;(require '[dungeon-master.llm.gpt :refer :all])
;;(require '[dungeon-master.llm.gpt :refer [dm-system-prompt]])
;;
;;(generate-prompt
;;  (assoc (blank-game-state) :interaction-history
;;         [{:role "assistant" :content "computer said this"}
;;          {:role "user" :content "then I said this"}
;;          {:role "assistant" :content "then it said this"}
;;          {:role  "assistant" :content "lastly i said this"}]))
;;
;;(def foo (assoc (blank-game-state) :interaction-history
;;                [{:role "assistant" :content "computer said this"}
;;                 {:role "user" :content "then I said this"}
;;                 {:role "assistant" :content "then it said this"}
;;                 {:role "user" :content "lastly i said this"}]))
;;
;;(cons
;;    {:role "system" :content dm-system-prompt}
;;    (last-n-elements (:interaction-history foo) 8))
;;
;;(ns-aliases *ns*)
;;(dm-system-prompt)
;;
;;
