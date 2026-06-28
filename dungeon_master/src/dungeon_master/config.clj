(ns dungeon-master.config
  (:require [dotenv :refer [env]]))

(def database-path
  (or (env "DATABASE_PATH")
      (env "DATABASE_URL")
      "./ladybug/data/dungeon-master.lbug"))

(def openai-api-key
  (env "OPENAI_API_KEY"))
