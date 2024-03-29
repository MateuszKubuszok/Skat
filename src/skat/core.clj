(ns skat.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [skat.i18n :as i18n]
            [skat.log :as log]
            [skat.gameplay :as gameplay]
            [skat.cli :refer [start-cli-game]]))
(set! *warn-on-reflection* true)

(def available-langs "Available languages"
  (-> i18n/skat-i18n-config :dictionary keys set))

(def options-config "Argument parser options configuration"
  [["-l" "--lang LANG" "Interface language"
    :default  :en
    :parse-fn #(-> % clojure.string/lower-case keyword)
    :validate [#(available-langs %) "Language must be within available ones"]]
   ["-r" "--rounds ROUNDS" "Number of rounds played"
    :default  10
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Number of rounds must be positive integer"]]
   ["-d" "--debug" "Debug mode on"
    :default false]
   ["-h" "--help" "Display help"
    :default false]])

(defn -main "CLI running main" [& args]
  (let [options (parse-opts args options-config)]
    (if (get-in options [:options :help])
      (println (options :summary))
      (if (options :errors)
        (doseq [error (options :errors)] (println error))
        (binding [i18n/*lang*       (get-in options [:options :lang])
                  gameplay/*rounds* (get-in options [:options :rounds])
                  log/*print-debug* (fn [_] (get-in options [:options :debug]))]
          (start-cli-game))))))
