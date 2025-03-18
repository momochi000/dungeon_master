(ns dungeon-master.game.data.entities
  (:require [dungeon-master.game.state :refer [get-messages]]
            [dungeon-master.llm.gpt :refer [extract-entities-prompt]]
            )
  )

(defn compile-context-for-entity-extraction
  "prepare the prompt and context needed to call the LLM for entity extraction"
  [game-state]

  (let [prompt extract-entities-prompt
        messages (get-messages game-state)]

    (cons
      {:role "system" :content prompt}
      messages)))
