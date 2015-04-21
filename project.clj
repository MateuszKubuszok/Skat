(defproject skat "0.1.0-SNAPSHOT"
  :description "Skat game engine"
  :url "https://github.com/MateuszKubuszok/Skat"
  :license { :name "Eclipse Public License"
             :url  "http://www.eclipse.org/legal/epl-v10.html" }
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/tools.trace "0.7.8"]
                 [com.taoensso/tower "3.0.2"]]
  :plugins [[no-man-is-an-island/lein-eclipse "2.0.0"]]
  :javac-options ["-encoding utf8"]
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :profiles { :uberjar { :aot :all } }
  :warn-on-reflection false
  :main skat.core
  :target-path "target/%s"
  :aot [skat.core]
  :test-selectors { :default     (complement :integration)
                    :integration :integration
                    :all         (fn [_] true) } )
