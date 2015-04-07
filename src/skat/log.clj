(ns skat.log
  (:require [clojure.pprint :refer [pprint]])
  (:require [clojure.tools.trace :refer :all]))
(set! *warn-on-reflection* true)

(def ^:dynamic *print-debug* "Check if some module is debugged" #{})

(defn pref-enabled [pref] "Is pref enabled"
  (if (keyword? pref)
    (*print-debug* pref)
    (some pref-enabled pref)))

(defn show [value pref] "Show value"
  (if (pref-enabled pref) (pprint value)))

(defn optional-description [pref description] "Optional description"
  (if (and (pref-enabled pref) description) (pprint description)))

(defn pass [value pref & [description]] "Pass value while logging it"
  (do
    (optional-description pref description)
    (show value pref)
    value))

(defn for-fun [name] "Trace names"
  (trace-vars name))
