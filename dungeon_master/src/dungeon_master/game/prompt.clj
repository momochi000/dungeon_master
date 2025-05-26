;; This namespace handles generation of the dungeon masters prompts
(ns dungeon-master.game.prompt
  (:require
    [clojure.tools.logging :as logging]))

(def ^:private ^:const dm-base-prompt
  "You are an experienced dungeon master who loves running all kinds of paper and pencil campaigns for your players. Your goals are to: 1. Ensure your players are having fun, 2. Enforce a consistent world with fixed rules and consistent characters.
  Your primary duty is to further the story by describing the actions of non player characters including monsters and animals, etc. You should also describe the player's surroundings and atmosphere, try to create an immersive environment. Only describe what the player character can perceive. For example, don't describe another characters thoughts (unless the player character has a deep insight into that character or is able to read their mind via magic or psionics).
  Never speak for the player or take action for them , only describe how the player character feels in reaction to external stimuli such as the elements, pain, magical effects, etc.")

(def ^:private ^:const working-memory-prompt
  "\n
  Here are some facts about the world that have been pulled out from the database. These are things which you may or may not need to be aware of to further the story, to determine an npc's course of action, to generate dialogue, to describe a person or thing that a player sees, and so forth
  \n")


(defn stats-to-string
  "this should recieve type dungeon-master.game.data.character-sheet/Stats
  and return a string of \"strength: x, dexterity: y...\""
  [stats]
  (->> stats
       ;; map over a map exposing each key and value
       ;; returning the format "<key>: <value>"
       (map (fn [[k v]] (str (name k) ": " v)))
       (clojure.string/join ", ")))

(defn generate-player-prompt
  "Generates the portion of the prompt (for context) describing the player to the DM
  Takes a character sheet"
  [player-character-sheet]
  (format "<PlayerCharacterSheet>The player character's name is: %s. Their stats are: %s. </PlayerCharacterSheet>"
          (:character-name player-character-sheet)
          (-> player-character-sheet :stats stats-to-string)))

;; Example node shape:
;;{:id 1, :name-id  "Coran ", :description  "The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate ", :name  "Coran "}
(defn working-memory-node-prompt
  "Generate a portion of a prompt from a single memory node. This node (currently) has a name and description "
  [node]
  (format
    "name: %s, description: %s\n"
    (:name node)
    (:description node)))

;; Here's  the shape of the working memory that gets passed in:
;; {:knowledge-context  ({:node  {:id 1, :name-id  "Coran ", :description  "The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate ", :name  "Coran "} , :score 0.69367886}  {:node  {:id 2, :name-id  "LordDhelt ", :description  "A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help ", :name  "Lord Dhelt "} , :score 0.6831956}  {:node  {:id 3, :name-id  "BlushingMermaidTavern ", :description  "The tavern in Baldur's Gate known for a warm atmosphere and busy clientele ", :name  "Blushing Mermaid Tavern "} , :score 0.66116095}  {:node  {:id 4, :name-id  "BaldursGate ", :description  "The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran ", :name  "Baldur's Gate " } , :score 0.6402278}})
(defn generate-working-memory-prompt
  "Given the working memory, return a prompt that gives the LLM context around information from the world knowledgebase that is relevant to recent events/location/setting/characters/etc"
  [working-memory]
  ;(logging/debug (str "working memory ---> " working-memory))
  (println (str "working memory ---> " working-memory))

  ; Gather the data from the working memory (the nodes), and format them for a prompt
  (let [memory-data-as-text (map
                             (fn [node]
                               (working-memory-node-prompt (:node node)))
                             (:knowledge-context working-memory))
        memory-data-prompt (clojure.string/join "\n" memory-data-as-text)]

    ; combine that data prompt with an explanation/instruction prompt so the llm knows what this is about
    (str
      working-memory-prompt
      (lcojure.string/join "" ["<WorldContext>" memory-data-prompt "</WorldContext>"]))))

;; LEFT OFF ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Here i want to also include a part of the prompt that gives context in working memory.
;;   Also disable clojure_lsp and see if vim sexp slurping is broken
(defn generate-dm-prompts
  "given the game state, generate the necessary prompts to instruct the LLM to operate the given turn.
  returns a list of strings that are each parts of the prompt"
  [game-state]
    (list
       dm-base-prompt
       (generate-player-prompt (:player-sheet game-state))
       (generate-working-memory-prompt (:working-memory game-state))
       ))



;; Testing
;;(require '[dungeon-master.game.data.character-sheet :as char-sheet])
;;(stats-to-string (:stats (char-sheet/build-blank-char-sheet "foo")))
;;(generate-player-prompt (char-sheet/build-blank-char-sheet "foo"))
