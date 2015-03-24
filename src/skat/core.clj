(ns skat.core
  (:gen-class)
  (:require [skat.cli :refer [start-cli-game]]))
(set! *warn-on-reflection* true)

(defn -main [& args]
  (start-cli-game))
