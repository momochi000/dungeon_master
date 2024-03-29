(ns dungeon-master.game-state
  (:require [dungeon-master.game.data.character-sheet :refer [build-blank-char-sheet]]
            [dungeon-master.repositories.game-state :refer [save-state load-state]]
            ))

;; structure for the game state, encapsulating all the necessary data for the
;; current state of the game. Includes the history of conversation between the
;; player and LLM, current game mode, state of the world, player character
;; sheet(s) etc. interaction history

(defrecord GameState [mode world-state interaction-history player-sheet])

(defn get-last-message
  [game-state]
  (-> game-state :interaction-history last :content))

(defn blank-game-state
  []
  (->GameState :normal {} [] (build-blank-char-sheet "Coran")))


(defn to-plain-map
  [game-state]
  (select-keys game-state [:mode :world-state :interaction-history :player-sheet]))

(defn save-game-state
  "Save the game state to the database so that it can be loaded later
  also clear the previous state"
  [game-state]
  (save-state game-state))

(defn load-game-state
  []
  (let [game-state (load-state)]
    (map->GameState game-state)))

;; TESTING SECTION

;;(defrecord Person [first-name last-name])
;;(def john
;;  (->Person "john" "smith"))
;;
;;(:first-name john)
;;( john)
;;
;;(blank-game-state)
