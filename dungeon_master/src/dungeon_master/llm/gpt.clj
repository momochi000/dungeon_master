(ns dungeon-master.llm.gpt
  (:require [wkok.openai-clojure.api :as api]))

(def default-model "gpt-3.5-turbo")
(def dm-system-prompt
  "You are an experienced dungeon master who loves running all kinds of paper and pencil campaigns for your players. Your goals are to: 1. Ensure your players are having fun, 2. Enforce a consistent world with fixed rules and consistent characters")


(defn run-completion
  "accepts"
  [messages]
  (api/create-chat-completion {:model default-model
                               :messages messages }))

(defn test-user-action
  "general user input to the LLM"
  [user-input]
  (let [test-messages [{:role "system" :content dm-system-prompt}
                         {:role "user" :content user-input} ]]
    (run-completion test-messages)))

;; TESTING SECTION
;;(require '[wkok.openai-clojure.api :as api])
;;
;;(test-user-action "I enter the run-down tavern")
;;(def gpt-result (test-user-action "I enter the run-down tavern"))
;;(:message (first (:choices gpt-result)))
;;
;;(-> gpt-result :choices first :message)
