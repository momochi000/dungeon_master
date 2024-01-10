(ns dungeon-master.llm.gpt
  (:require [wkok.openai-clojure.api :as api]))

(def default-model "gpt-3.5-turbo")
(def dm-system-prompt
  "You are an experienced dungeon master who loves running all kinds of paper and pencil campaigns for your players. Your goals are to: 1. Ensure your players are having fun, 2. Enforce a consistent world with fixed rules and consistent characters")



;; example return value and shape of `run-completion`
;;{:id "chatcmpl-8fHPpRYsZ9kEbGQQjAWXKcn85nGK7",
;; :object "chat.completion",
;; :created 1704849085,
;; :model "gpt-3.5-turbo-0613",
;; :choices
;; [{:index 0,
;;   :message
;;   {:role "assistant",
;;    :content
;;    "the response message"},
;;   :logprobs nil,
;;   :finish_reason "stop"}],
;; :usage
;; {:prompt_tokens 768, :completion_tokens 258, :total_tokens 1026},
;; :system_fingerprint nil}
(defn run-completion
  "accepts"
  [messages]
  (api/create-chat-completion {:model default-model
                               :messages messages }))

(defn get-result-message
  "when gpt responds from run-completion the result is a map of a certain
  format. This function extracts the first message. This returns a map of the
  form `{:role \"assistant\" :content \"gpt response.. some content\"}`"
  [gpt-response]
  (-> gpt-response :choices first :message))

(defn get-result-content
  [gpt-response]
  (:content (get-result-message gpt-response)))

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
