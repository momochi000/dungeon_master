(ns dungeon-master.game.data
  (:require [dungeon-master.game-state :refer [get-last-message]]
            [dungeon-master.llm.gpt :refer [run-function-completion extract-entities-prompt]]))


(require '[dungeon-master.game-state :refer [get-last-message]])
(require '[ dungeon-master.llm.gpt :refer [run-function-completion extract-entities-prompt]])

(defn extract-entities
  "obtain entities from the last message in the interaction history.
  Sends a request to the llm asking it to identify entities and their
  relationships and returns a json string representing them."
  [game-state]
  (let [last-message (get-last-message game-state)
        completion-messages [{:role "system" :content extract-entities-prompt}
                             {:role "user" :content last-message}]]

    (run-function-completion completion-messages :extract-entities)))
