(ns dungeon-master.config
  (:require [environ.core :refer [env]]))

(def database-url
  (env :database-url))
