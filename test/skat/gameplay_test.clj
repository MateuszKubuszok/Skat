(ns skat.game_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.gameplay :refer :all])
  (:import  [skat Bidding
                  Configuration
                  GameDriver]))

(deftest perform-auction-test
  (testing "auction eventually end with bidding"
    (let [bidding (Bidding. nil [] 18)
          driver  (reify GameDriver (do-auction [_ _ _] bidding))]
      (is (= bidding (:bidding (perform-auction driver [])))))))

(deftest declare-game-test
  (let [config-true  (Configuration. nil :grand true true true true 18)
        config-false (Configuration. nil :grand false false false false 18)
        driver-true  (reify GameDriver (declare-game [_ _] config-true))
        driver-false (reify GameDriver (declare-game [_ _] config-false))]
    (testing "declaration eventually ends"
      (is (= config-true (declare-game driver-true {})))
      (is (= config-false (declare-game driver-false {}))))))
