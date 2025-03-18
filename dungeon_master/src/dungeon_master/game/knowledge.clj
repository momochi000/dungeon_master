(ns dungeon-master.game.knowledge
  (:require [dungeon-master.llm.embedding :refer [get-embedding]]
            [dungeon-master.repositories.embedding :refer [add-embedding find-knn]]))

(defn find-k-similar-descriptions
  "Given a search string for descriptions, convert the query into an embedding
  and use it to find the k most similar nodes along with the scores of their
  relatedness to the given query"
  [query k]

  (let [embedding (get-embedding query)]
    (find-knn (:embedding embedding) k)))

;; TESTING SECTION

;;(require '[dungeon-master.llm.embedding :refer [get-embedding]])
;;(require '[dungeon-master.repositories.embedding :refer [find-knn]])
;;
;;(find-k-similar-descriptions "Cimmeria" 2)
;;(find-k-similar-descriptions "goliath barbarian" 2)

;;(require '[clojure.string])
;;
;;(clojure.string/replace "foo bar baz" #" " "")
;;(replace "foo bar baz" #" " "")
;; looks like there is a `clojure.core.replace already, if import clojure.string, then `replace` becomes ambiguous

