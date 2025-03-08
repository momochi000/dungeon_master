(ns dungeon-master.config
  ;;(:require [environ.core :refer [env]])
  (:require '[dotenv :refer [env app-env]]))

(def database-url
  (env "DATABASE_URL"))

(def openai-api-key
  (env "OPENAI_API_KEY"))

;; Only need this if your openai account has multiple organizations
;;(def openai-organization
;;  (env "OPENAI_ORGANIZATION"))
;; TESTING SECTION
;;(require '[dotenv :refer [env app-env]])
;;(format database-url)
;;(format app-env)
;;(env "DATABASE_URL")
