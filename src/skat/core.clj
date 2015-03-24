(ns skat.core
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [skat.cli :as cli]))
(set! *warn-on-reflection* true)

(defn -main [& args]
  (pprint (cli/select-players)))
