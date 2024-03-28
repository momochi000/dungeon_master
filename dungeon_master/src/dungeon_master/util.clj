(ns dungeon-master.util)

(defn last-n-elements
  "return the last n elements of a vector"
  [v n]
  (subvec v (max 0 (- (count v) n))))
