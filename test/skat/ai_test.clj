(ns skat.ai_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.ai :refer :all])
  (:import  [skat Player]))

(def player (ai-player "ai-player"))

(deftest ai-player-test
  (testing ".id works as expected"
    (is (= "ai-player" (.id ^Player player)))))
