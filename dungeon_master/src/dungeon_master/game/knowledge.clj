(ns dungeon-master.game.knowledge
  (:require [dungeon-master.llm.embedding :refer [get-embedding]]
             [ dungeon-master.repositories.embedding :refer [add-embedding find-knn]]))

(defn find-k-similar-descriptions
  "Given a search string for descriptions, convert the query into an embedding
  and use it to find the k most similar nodes along with the scores of their
  relatedness to the given query"
  [query k]

  (let [embedding (get-embedding query)]
    (find-knn (:embedding embedding) k)))
