(ns dungeon-master.repositories.game-state
  (:require [cheshire.core :as json]
            [dungeon-master.repositories.util :refer [with-connection
                                                      run-cypher-read-many-results-with-params
                                                      run-cypher-stmt-with-data-no-return]]))

(defn save-state
  "Dump the current game state into the database for reload later."
  [game-state save-file-name]
  (with-connection [conn]
    (let [json-string (json/generate-string game-state)
          cypher-string
          (str "MERGE (gs:GameState {save_file_name: $filename}) "
               "SET gs.data = $data")]
      (run-cypher-stmt-with-data-no-return
        cypher-string
        {"data" json-string
         "filename" save-file-name}
        conn))))

(defn load-state
  "Load the game state from the database."
  ([]
   (load-state "autosave"))
  ([save-file-name]
   (with-connection [conn]
     (let [cypher-string
           "MATCH (gamestate:GameState {save_file_name: $filename}) RETURN gamestate.data AS data"
           cypher-result
           (run-cypher-read-many-results-with-params cypher-string {"filename" save-file-name} conn)]
       (when-let [game-state-json (some-> cypher-result first (get "data"))]
         (json/parse-string game-state-json true))))))
