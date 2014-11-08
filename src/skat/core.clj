(ns skat.core
  (:gen-class)
  (:require [skat.helpers :as helpers]
  	        [skat.cards :as cards]
  	        [skat.responses :as responses]
  	        [skat.game :as game]))
;(require '[clojure.pprint :refer :all])
;(require '[clojure.tools.trace :refer :all])

(defn -main [& args]
  (println "Compile test! These are your args:" args))
