(ns skat.log
  (:require [clojure.pprint :refer :all]))
(set! *warn-on-reflection* true)

(defn pass [v & [m]] "Pass value while logging it"
  (do
    (if m (pprint m))
    (pprint v)
    (identity v)))
