(ns dungeon-master.game.state
  (:require [dungeon-master.game.data.character-sheet :refer [build-blank-char-sheet]]
            [dungeon-master.repositories.game-state :refer [save-state load-state]]
            ))

;; structure for the game state, encapsulating all the necessary data for the
;; current state of the game. Includes the history of conversation between the
;; player and LLM, current game mode, state of the world, player character
;; sheet(s) etc. interaction history

(defrecord GameState [mode world-state working-memory interaction-history player-sheet])

(defn get-last-message
  [game-state]
  (-> game-state :interaction-history last :content))

(defn get-messages
  "return the conversation context out of the game state"
  [game-state]
  (:interaction-history game-state))

(defn blank-game-state
  []
  (->GameState :normal {} {} [] (build-blank-char-sheet "Coran")))


;; Deprecated
;; Doesn't look like this is needed. i believe clojure can treat records like plain maps wherever they're used as such
;;(defn to-plain-map
;;  [game-state]
;;  (select-keys game-state [:mode :world-state :interaction-history :player-sheet]))

(defn add-user-input-to-interaction-history
  "Insert the user's input into the interaction history of the game state, returning the new state"
  [game-state user-input]
  (assoc
    game-state
    :interaction-history
    (conj (:interaction-history game-state) {:role "user" :content user-input})))

(defn add-user-input-to-working-memory
  "Insert the user's input into the working-memory of the game state, returning the new state
  This makes it easier to reference what the user's input was on the current (or previous) turn
  when this is called, it replaces whatever was there previously"
  [game-state user-input]
  (assoc
    game-state
    :working-memory
    {:current-input user-input}))

(defn update-knowledge-context-to-working-memory
  "Given some knowledge context queried from the database, store this in working memory so that it may be formulated into any prompts this turn."
  [game-state knowledge-context]
  (assoc
    game-state
    :working-memory
    {:knowledge-context knowledge-context}))

(defn save-game-state
  "Save the game state to the database so that it can be loaded later
  also clear the previous state"
  ([game-state]
   (save-state game-state "autosave"))

  ([game-state save-file-name]
   (save-state game-state save-file-name)))

(defn load-game-state
  []
  (let [game-state (load-state)]
    (if game-state
      (map->GameState game-state)
      nil)))

;; TESTING SECTION
;;(require '[dungeon-master.game.data.character-sheet :refer [build-blank-char-sheet]]
;;          '[dungeon-master.repositories.game-state :refer [save-state load-state]]
;;          :reload )
;;(load-game-state)

;;(defrecord Person [first-name last-name])
;;(def john
;;  (->Person "john" "smith"))
;;
;;(:first-name john)
;;( john)
;;
;;(blank-game-state)
