(ns dungeon-master.game.prompt
  (:require [dungeon-master.llm.gpt]
            [dungeon-master.util]))

(def default-history-length 8)

(defn generate-dm-prompt
  "based on the current game state, generate the necessary prompt to send to the llm
   currently this depends on the current game mode and interaction history. For example, it depends on the player having entered some input, however, in the future, it may develop a different prompt if the user has not entered anything. For example, when starting a new session."
  [game-state]

  (cons
    {:role "system" :content dm-system-prompt}
    (last-n-elements (:interaction-history game-state) default-history-length)))


(def extract-entities-prompt
  "From the given text extract the following Entities & relationships described in the mentioned format
0. ALWAYS FINISH THE OUTPUT. Never send partial responses
1. First, look for these Entity types in the text and generate as comma-separated format similar to entity type.
   `id` property of each entity must be alphanumeric and must be unique among the entities. You will be referring this property to define the relationship between entities. Do not create new entity types that aren't mentioned below. You will have to generate as many entities as needed as per the types below:
    Entity Types:
    label:'Person',id:string,name:string,description:string // A person who appears in the text. `id` property is the name of the person, in camel-case; for example, \"michaelClark\", or \"emmaMartinez\"; 'name' is the person's name, as spelled in the text if available. description is a description of the person from the context of the text.
    label:'Place',id:string,name:string,description:string // A location or place. It could be a building, a business, a city, a neighborhood in a city etc.; 'id' property should be the name of the place, camel-case. if no name is available, make up a placeholder name based on how it is described or referred. Name is the name of the place, if unnamed, then omit this property. Description is a description of the place or location.
3. Next generate each relationships as triples of head, relationship and tail. To refer the head and tail entity, use their respective `id` property. Relationship property should be mentioned within brackets as comma-separated. They should follow these relationship types below. You will have to generate as many relationships as needed as defined below:
    Relationship types:
    personid|AT|placeid
    personid|KNOWS|personid
    personid|KNOWS|placeid
    placeid|IN|placeid

The output should look like :
%%%%%%%%%%BEGIN EXAMPLE OUTPUT%%%%%%%%%%%%%%
{
    \"entities\": [{\"label\":\"SlackMessage\",\"id\":string,\"text\":string}],
    \"relationships\": [\"personid|AT|placeid\"]
}
%%%%%%%%%%END EXAMPLE OUTPUT%%%%%%%%%%%%%%
Please ensure the output is valid json")

;; TESTING SECTION

;;(use 'dungeon-master.util)
;;(require '[dungeon-master.game-state :refer :all])
;;(require '[dungeon-master.llm.gpt :as gpt])
;;(require '[dungeon-master.llm.gpt :refer :all])
;;(require '[dungeon-master.llm.gpt :refer [dm-system-prompt]])
;;
;;(generate-dm-prompt
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
