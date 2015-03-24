(ns skat.core
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [skat.cli :as cli]))

(defn -main [& args]
  (pprint (cli/select-players)))
