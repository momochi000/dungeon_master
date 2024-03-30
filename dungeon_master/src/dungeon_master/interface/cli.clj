(ns dungeon-master.interface.cli
  (:require [dungeon-master.game.turn :refer [run-turn]]
            [dungeon-master.game-state :refer [get-last-message save-game-state load-game-state]]
            [dungeon-master.game.turn :refer [add-user-input-to-interaction-history]]
            ))


(declare parse-user-command)
(declare exec-user-command)
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
        (let [parsed-command (parse-user-command user-command)]
          (if parsed-command
            ;; HERE: execute the command
            (exec-user-command parsed-command curr-state)
            (run-turn curr-state user-command))
          )))))

(defn quit-game []
  (println "Goodbye!")
  (System/exit 0)
  true)

;; TODO: make this actually save the game to different save "files"
(defn save-game
  ([game-state]
   (save-game-state game-state)
   ;;(println "DEBUG: save game called with just the game state -----> " game-state)
   )
  ([save-name game-state]
   (save-game-state game-state save-name)
   ;;(println "DEBUG: save game called with a filename-----> " save-name)
   ;;(println "DEBUG: also called with game state ============> " game-state)
   )
  )

(defn test-command []
  (println "TEST COMMAND RECEIVED")
  true)

(defn ignore-command [] true)

(defn parse-user-command
  "handle non-game command input. Things like exit game or print debug info
  if the input matches some command, then return a symbol representing the command
  if the command has arguments, then return a vector of the symbol and args
  if the input didn't match any command, then return nil"
  [user-command]
  (let [command-list [[#"^/exit" :quit-game]
                      [#"^/quit" :quit-game]
                      [#"^/save (\w+)$" :save-game]
                      [#"^/save" :save-game]
                      [#"^$" :ignore-command]
                      [#"^/debug" :test-command]]

        matched-command (some
                          (fn [[command-regex action-function]]
                            (let [match-result (re-find command-regex user-command)]
                              (when match-result
                                (if (string? match-result)
                                  [action-function]
                                  [action-function (rest match-result)])
                                )))
                          command-list)]
    matched-command
  ))

;; Rethinking this, i'm not sure this is the right structure.
;; because each command may take different arguments or no arguments, i think i should
;; separate identifying what to do and args from the command from actually selecting
;; and executing the command
;;(defn parse-user-command
;;  "handle non-game command input. Things like exit game or print debug info
;;  if the input matches some command, then return the command
;;  if the command has arguments, then return a vector of the command and args
;;  if the input didn't match any command, then return nil"
;;  [user-command]
;;  (let [command-list [[#"^/exit" quit-game]
;;                       [#"^/quit" quit-game]
;;                       [#"^/save (\w+)$" save-game]
;;                       [#"^/save" save-game]
;;                       [#"^$" ignore-command]
;;                       [#"^/debug" test-command]]
;;        matched-command (some
;;                          (fn [[command-regex action-function]]
;;                            (let [match-result (re-find command-regex user-command)]
;;                              (when match-result
;;                                (if (string? match-result)
;;                                  [action-function]
;;                                  [action-function match-result])
;;                                )))
;;                            command-list)]
;;
;;    (if matched-command
;;      (let [[command args] matched-command]
;;        (if args
;;          [command (rest args)]
;;          command))
;;      nil)))

;;(parse-user-command "hobos")
;;(parse-user-command "/save")
;;(parse-user-command "/save foo")
(defn exec-user-command
  "Takes a command and optional arguments along with the game state
  and runs the command with arguments"
  [command game-state]

  (let [[destructured-command args] command]
    (case destructured-command
      :quit-game (
                  ;;(save-game game-state)
                  (quit-game))
      :save-game (if args
                   (save-game args game-state)
                   (save-game game-state))
      :ignore-command (ignore-command)
      :test-command (test-command)
      )
    )
  game-state)
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


;;(re-find #"^/save$" "/save")
;;(re-find #"^/save (\w+)$" "/save foo23")
;;(re-find #"^/save (\w+)$" "/save")
