(ns skat.core
  (:gen-class)
  (:require [skat.cli :as cli]))

(defn -main [& args]
  (let [coll     [\q \w \e \r \t \y \u \i \o \p]
        selected (cli/select-nth coll str " | ")]
    (println "Selected: " (str selected))))
