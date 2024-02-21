;; This namespace handles generation of the dungeon masters prompts
(ns dungeon-master.game.prompt)

(def dm-base-prompt
  "You are an experienced dungeon master who loves running all kinds of paper and pencil campaigns for your players. Your goals are to: 1. Ensure your players are having fun, 2. Enforce a consistent world with fixed rules and consistent characters.
  Your primary duty is to further the story by describing the actions of non player characters including monsters and animals, etc. You should also describe the player's surroundings and atmosphere, try to create an immersive environment. Only describe what the player character can perceive, for example, don't describe another characters thoughts (unless the player character has a deep insight into that character or is able to read their mind via magic or psionics).
  Never speak for the player or take action for the player, only describe how the player character feels in reaction to external stimuli such as the elements, pain, magical effects, etc.")


(declare generate-player-prompt)

(defn generate-dm-prompts
  "given the game state, generate the necessary prompts to instruct the LLM to operate the given turn.
  returns a list of strings that are each parts of the prompt"
  [game-state]
  (list
     dm-base-prompt
     (generate-player-prompt (:player-sheet game-state))))

(declare stats-to-string)

(defn generate-player-prompt
  [player-character-sheet]
  (format "The player character's name is: %s. Their stats are: %s. "
          (:character-name player-character-sheet)
          (-> player-character-sheet :stats stats-to-string)))


(defn stats-to-string
  "this should recieve type dungeon-master.game.data.character-sheet/Stats
  and return a string of \"strength: x, dexterity: y...\""
  [stats]
  (->> stats
       ;; map over a map exposing each key and value
       ;; returning the format "<key>: <value>"
       (map (fn [[k v]] (str (name k) ": " v)))
       (clojure.string/join ", ")))


;; Testing
;;(require '[dungeon-master.game.data.character-sheet :as char-sheet])
;;(stats-to-string (:stats (char-sheet/build-blank-char-sheet "foo")))
;;(generate-player-prompt (char-sheet/build-blank-char-sheet "foo"))
