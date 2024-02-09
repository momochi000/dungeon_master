(ns dungeon-master.interface.cli
  (:require [dungeon-master.game.turn :refer [run-turn]]
            [dungeon-master.game-state :refer [get-last-message]]
            [dungeon-master.game.turn :refer [add-user-input-to-interaction-history]]
            ))


(declare mock-run-turn)
;; I'm not sure how this should work so this is just scratch until i get a
;; better handle on it
(defn game-loop
  "work in progress attempt at a loop to play this game"
  [initial-state]

  
  (loop [curr-state initial-state]
    ;;(println "DEBUG: TURN START: current game state is ")
    ;;(println curr-state)
    (println "Dungeon master ==============================>")
    (println (get-last-message curr-state))
    (println "Your action: ================================>")

    (let [user-command (read-line)]
      ;;(println "DEBUG: spitting back input: " user-command)
      (println "DEBUG: last 3 interactions: " (take-last 3 (:interaction-history curr-state)))
      (recur (run-turn curr-state user-command)))
    ))
