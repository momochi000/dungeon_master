(ns dungeon-master.game-state)

(defrecord GameState [mode world-state interaction-history])


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
