(ns dungeon-master.core
  (:require [dungeon-master.game.data :refer [initialize-strawman-state]]
            [dungeon-master.interface.cli :refer [game-loop]]
            [dungeon-master.game-state :refer [load-game-state get-last-message]]
            )
  (:gen-class))

;(defn -main
;  "I don't do a whole lot ... yet."
;  [& args]
;  (println "Hello, World!"))

;(defn -main
;  "create a node without properties"
;  [& args]
;  (with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
;    (with-open [session (.session driver)]
;      (let [result (.run session "CREATE (n) RETURN n")]
;        (println (.single result))))))

(defn -main
  "The main entry point to the program."
  [& args]
  (println "Dungeon master (working title) v0.1.0")

  (let [
        previous-saved-state (load-game-state)
        initial-game-state (if previous-saved-state
                             previous-saved-state
                             (initialize-strawman-state)) ]

    (println "The story left off.....")

    ;; print out the last system prompt
    (println (get-last-message initial-game-state))

    ;; start the game loop
    (game-loop initial-game-state)
    ))
