(defproject skat "0.1.0-SNAPSHOT"
  :description "Skat game engine"
  :url "https://bitbucket.org/MateuszKubuszok/skat"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[no-man-is-an-island/lein-eclipse "2.0.0"]]
  :profiles {:uberjar {:aot :all}}
  :main skat.core
  :target-path "target/%s"
  :aot [skat.core]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (fn [_] true)})
