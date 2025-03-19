(ns dungeon-master.llm.gpt
  (:require [wkok.openai-clojure.api :as api]
            [dungeon-master.util :refer [last-n-elements]]
            [dungeon-master.config :refer [openai-api-key]]
            ))

(def default-model "gpt-3.5-turbo")
(def default-history-length 8)

(def extract-entities-prompt
  "From the given text, please extract the following Entities & relationships described in the mentioned format
1. First, look for these Entity types in the text and generate as comma-separated format similar to entity type.
2. Only extract entities from the LAST user message. Use the prior messages/conversation as context to understand places/people/characters/objects/names/etc referenced in that last message.
    Entity Types:
    label:'Person',name:string,description:string // A person who appears in the text. `id` property is the name of the person, in camel-case; for example, \"michaelClark\", or \"emmaMartinez\". 'name' is the person's name, as spelled in the text if available; for example, \"Michael Clark\" or \"Emma Martinez\". 'description' is a description of the person from the context of the text.
    label:'Place',name:string,description:string // A location or place. It could be a building, a business, a city, a neighborhood in a city etc.; 'id' property should be the name of the place, camel-case. if no name is available, make up a placeholder name based on how it is described or referred. Name is the name of the place, if unnamed, then omit this property. Description is a description of the place or location.
3. Next generate each relationships as triples of head, relationship, and tail. To refer the head and tail entity, use their respective `name` property. You will have to generate as many relationships as needed as defined below:
    Relationship types:
  {from_entity_name: \"Person A\", relationship_type: \"AT\", to_entity_name: \"Place A\"}
  {from_entity_name: \"Person A\", relationship_type: \"KNOWS\", to_entity_name: \"Person B\"}
  {from_entity_name: \"Person A\", relationship_type: \"KNOWS\", to_entity_name: \"Place A\"}
  {from_entity_name: \"Place A\", relationship_type: \"IN\", to_entity_name: \"Place B\"}

The output should look like :
%%%%%%%%%%BEGIN EXAMPLE OUTPUT%%%%%%%%%%%%%%
{
    \"entities\": [
      {\"label\": \"Person\", \"name\": \"Thorfinn\", \"description\": \"A young warrior on a quest to avenge his father. Wields twin daggers.\"},
      {\"label\": \"Place\", \"name\": \"Neverwinter\", \"description\": \"A city in the cold northern region.\"}
    ],
    \"relationships\": [{\"from_entity_name\": \"Thorfinn\", \"relationship_type\": \"AT\", \"to_entity_name\": \"Neverwinter\"}]
}
%%%%%%%%%%END EXAMPLE OUTPUT%%%%%%%%%%%%%%
Please ensure the output is valid json")

;; NOTE: old definition for reference. This is deprecated and should be removed at some later time
;;(def extract-entities-prompt
;;  "From the given text extract the following Entities & relationships described in the mentioned format
;;0. ALWAYS FINISH THE OUTPUT. Never send partial responses
;;1. First, look for these Entity types in the text and generate as comma-separated format similar to entity type.
;;   `id` property of each entity must be alphanumeric and must be unique among the entities. You will be referring this property to define the relationship between entities. Do not create new entity types that aren't mentioned below. You will have to generate as many entities as needed as per the types below:
;;    Entity Types:
;;    label:'Person',id:string,name:string,description:string // A person who appears in the text. `id` property is the name of the person, in camel-case; for example, \"michaelClark\", or \"emmaMartinez\". 'name' is the person's name, as spelled in the text if available; for example, \"Michael Clark\" or \"Emma Martinez\". 'description' is a description of the person from the context of the text.
;;    label:'Place',id:string,name:string,description:string // A location or place. It could be a building, a business, a city, a neighborhood in a city etc.; 'id' property should be the name of the place, camel-case. if no name is available, make up a placeholder name based on how it is described or referred. Name is the name of the place, if unnamed, then omit this property. Description is a description of the place or location.
;;3. Next generate each relationships as triples of head, relationship and tail. To refer the head and tail entity, use their respective `id` property. Relationship property should be mentioned within brackets as comma-separated. They should follow these relationship types below. You will have to generate as many relationships as needed as defined below:
;;    Relationship types:
;;    personid|AT|placeid
;;    personid|KNOWS|personid
;;    personid|KNOWS|placeid
;;    placeid|IN|placeid
;;
;;The output should look like :
;;%%%%%%%%%%BEGIN EXAMPLE OUTPUT%%%%%%%%%%%%%%
;;{
;;    \"entities\": [{\"label\":\"SlackMessage\",\"id\":string,\"text\":string}],
;;    \"relationships\": [\"personid|AT|placeid\"]
;;}
;;%%%%%%%%%%END EXAMPLE OUTPUT%%%%%%%%%%%%%%
;;Please ensure the output is valid json")

;; this is the instruction to gpt which tells it how to create a
;; "tool/function" basically this means gpt is guaranteed (maybe?) to return a
;; well-formed json of this shape
;; see the api docs to understand how it's used
(def extract-entities-tool
  {:type "function"
   :function {:name "extract_entities"
              :description "obtain entities and relationships from text"
              :parameters {:type "object"
                           :properties {:entities {:type "array"
                                                   :description "entities extracted from text"
                                                   :items {:type "object"
                                                           :properties {
                                                                        :label {:type "string"
                                                                                :enum ["Person" "Place"]}
                                                                        :name {:type "string"
                                                                               :description "name of entity as appearing in text" }
                                                                        :description {:type "string"
                                                                                      :description "summary of details about the entity present in the text"}}}}
                                        :relationships {:type "array"
                                                        :description "strings indicating relationships between entities"
                                                        :items {:type "object"
                                                                :properties {
                                                                             :from_entity_name {:type "string"
                                                                                                :description "name of the entity that is the subject of the relationship"}
                                                                             :relationship_type {:type "string"
                                                                                                 :enum ["AT" "KNOWS" "IN" "HAS"]
                                                                                                 :description "the nature of the relationship"}
                                                                             :to_entity_name {:type "string"
                                                                                              :description "name of the entity that is the object of the relationship"}
                                                                             }}}}}}})
                                                        ;;:items {:type "string"
                                                        ;;        :description "string indicating relationship between entities, of the form entity_name|RelationshipType|entity_name"}}}}}})

(defn generate-system-prompt
  "based on the current game state, generate the necessary prompt to send to the llm
   currently this depends on the current game mode and interaction history. For example, it depends on the player having entered some input, however, in the future, it may develop a different prompt if the user has not entered anything. For example, when starting a new session."
  [chat-history dm-prompt]

  (cons
    {:role "system" :content dm-prompt}
    (last-n-elements chat-history default-history-length)))

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
  "accepts a sequence/list of messages each of which is the form:
  {:role \"user|assistant\" :content \"content of the message\"}"
  [messages]
  (
   ;;(println openai-api-key)
   api/create-chat-completion {:model default-model
                               ;;:organization openai-organization
                               :messages messages }
                               {:api-key openai-api-key}))

(defn run-function-completion
  [messages function-type]
  ;;(println "DEBUG: run-function-completion called. messages passed in is ========>")
  ;;(println messages)
  (case function-type
    :extract-entities (api/create-chat-completion {:model default-model
                                                   :messages messages
                                                   :tools [extract-entities-tool]
                                                   :tool-choice { :type "function"
                                                                 :function { :name "extract_entities"}}}
                                                  {:api-key openai-api-key})
    "error, invalid function type"
    )
  )




(defn get-result-message
  "when gpt responds from run-completion the result is a map of a certain
  format. This function extracts the first message. This returns a map of the
  form `{:role \"assistant\" :content \"gpt response.. some content\"}`"
  [gpt-response]
  (-> gpt-response :choices first :message))

(defn get-result-content
  [gpt-response]
  (:content (get-result-message gpt-response)))

(defn get-result-tool-calls
  [gpt-response]
  (first (:tool_calls (get-result-message gpt-response))))

(defn get-result-tool-arguments
  [gpt-response]
  (-> gpt-response get-result-tool-calls :function :arguments))



;; TESTING SECTION
;;(require '[wkok.openai-clojure.api :as api])
;;(require '[dungeon-master.config :refer [openai-api-key]])
;;
;;(test-user-action "I enter the run-down tavern")
;;(def gpt-result (test-user-action "I enter the run-down tavern"))
;;(:message (first (:choices gpt-result)))
;;
;;(-> gpt-result :choices first :message)
;;(defn test-user-action
;;  "general user input to the LLM"
;;  [user-input]
;;  (let [test-messages [{:role "system" :content "some generic prompt"}
;;                         {:role "user" :content user-input} ]]
;;    (run-completion test-messages)))
;;(format openai-api-key)
;;(format openai-organization)
;;
;;(require '[dotenv :refer [env app-env]])
;;(def openai-api-key
;;  (env "OPENAI_API_KEY"))
;;(def openai-organization
;;  (env "OPENAI_ORGANIZATION"))
