(ns skat.core
  (:gen-class)
  (:require ;[clojure.tools.trace :refer :all]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses]
            [skat.game :as game]))

(defn -main [& args]
  (println "Compile test! These are your args:" args))
