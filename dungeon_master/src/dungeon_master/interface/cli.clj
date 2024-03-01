(ns dungeon-master.interface.cli
  (:require [dungeon-master.game.turn :refer [run-turn]]
            [dungeon-master.game-state :refer [get-last-message]]
            [dungeon-master.game.turn :refer [add-user-input-to-interaction-history]]
            ))



(declare parse-user-command)
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
      ;;(println "DEBUG: last 3 interactions: " (take-last 3 (:interaction-history curr-state)))
      (recur
        (if-not (parse-user-command user-command) (run-turn curr-state user-command))))))

(defn quit-game []
  ;; save the game or something
  (println "Goodbye!")
  (System/exit 0)
  true)

(defn test-command []
  (println "TEST COMMAND RECEIVED")
  true)

(defn ignore-command [] true)

(defn parse-user-command
  "handle non-game command input. Things like exit game or print debug info"
  [user-command]
  (let [command-list [[#"^/exit" quit-game]
                       [#"^/quit" quit-game]
                       [#"^$" ignore-command]
                       [#"^/debug" test-command]]
        matched-command (some
                          (fn [[command-regex action-function]]
                             (when (re-find command-regex user-command) action-function))
                          command-list)]

    (if matched-command (matched-command) user-command)))

;;(parse-user-command "hobos")

;;(defn parse-command
;;  [input]
;;  (let [command-list '([#"^/exit" quit-game]
;;                       [#"^/quit" quit-game]
;;                       [#"^/debug" test-command])
;;        matched-command (some
;;                          (fn [[command-regex action-function]]
;;                             (when (re-find command-regex user-command) action-function))
;;                          command-list)]
;;
;;    (println (str "DEBUG: by here, what is matched-command?" matched-command))
;;    (if matched-command (matched-command) nil)))


;;(def command-list '([#"/exit" "exit command match"]
;;                   [#"/quit" "quit command match"]
;;                   [#"/debug" "debug command match"]))
;;
;;(defn test-command-matcher
;;  [input]
;;  (some
;;    (fn [[regex output]] (when (re-find regex input) output))
;;    command-list)
;;  )
;;(test-command-matcher "/quit")

