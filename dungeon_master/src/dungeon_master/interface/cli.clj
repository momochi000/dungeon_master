(ns dungeon-master.interface.cli
  (:require [dungeon-master.game.turn :refer [run-turn]]
            [dungeon-master.game-state :refer [get-last-message]]
            ))

;; I'm not sure how this should work so this is just scratch until i get a
;; better handle on it
(defn game-loop
  "work in progress attempt at a loop to play this game"
  [initial-state]

  ;; LEFT OFF: This doesn't seem to be the right way to set up a repl
  ;; it takes my input once then keeps looping infinitely
  ;; i want to call read line after printing out the prompt

  (loop [curr-state initial-state]
    (println "DEBUG: TURN START: current game state is ")
    (println curr-state)
    (println "Dungeon master ==============================>")
    (println (get-last-message initial-state))
    (println "Your action: ================================>")

    (let [user-command (read-line)]
      (println "DEBUG: spitting back input: " user-command)
      (recur (run-turn curr-state user-command)))

    ))
