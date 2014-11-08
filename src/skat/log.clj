(ns skat.log
  (:require [clojure.pprint :refer :all]))

(defn pass [v & [m]] "Pass value while logging it"
  (do
    (if m (pprint m))
    (pprint v)
    (identity v)))
