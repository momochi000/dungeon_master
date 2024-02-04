(ns dungeon-master.config
  (:require [environ.core :refer [env]]))


;(defn debug-env []
;  (println "Current ENV:" env))  ; 'env' is directly printed here; no 'deref' necessary

(def database-url
  (env :database-url))
