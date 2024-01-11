(ns dungeon-master.game-state)

;; structure for the game state, encapsulating all the necessary data for the
;; current state of the game. Includes the history of conversation between the
;; player and LLM, current game mode, state of the world, player character
;; sheet(s) etc. interaction history

(defrecord GameState [mode world-state interaction-history])

(defn get-last-message
  [game-state]
  (-> game-state :interaction-history last :content))

(defn blank-game-state
  []
  (->GameState :normal {} []))




;; TESTING SECTION

;;(defrecord Person [first-name last-name])
;;(def john
;;  (->Person "john" "smith"))
;;
;;(:first-name john)
;;( john)
;;
;;(blank-game-state)
