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

(defn update-all "Updates values of a map" [m fun & args]
  (letfn [(update-one [result [key value]]
            (assoc result key (apply fun value args)))]
    (reduce update-one {} m)))
 
(defn replace-by-key "Replaces all map value basing on its keys" [m fun & args]
  (let [ks (keys m)]
    (zipmap ks (map #(apply fun % args) ks))))
