(ns skat.helpers)

(defn list-from "List of mapped values" [fun coll]
  (reduce conj '() (map fun coll)))

(defn append "Appends one collection to another" [coll coll2]
  (loop [result coll
         appended coll2]
    (if (empty? appended)
      result
      (let [[h & t] appended]
        (recur (conj result h) t)))))
