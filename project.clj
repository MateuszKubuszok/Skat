(defproject skat "0.1.0-SNAPSHOT"
  :description "Skat game engine"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main skat.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})