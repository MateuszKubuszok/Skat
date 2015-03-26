(ns skat.game_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.gameplay :refer :all])
  (:import  [skat Bidding
                  Player
                  Configuration
                  GameDriver]))

(def c1 (Card. :kreuz :W))
(def c2 (Card. :kreuz :K))
(def c3 (Card. :schell :W))
(def c4 (Card. :kreuz :A))
(def c5 (Card. :kreuz :10))
(def c6 (Card. :schell :A))
(def c7 (Card. :schell :10))
(def c8 (Card. :schell :K))
(def deal { :front '(c1 c2), :middle '(c3 c4), :rear '(c5 c6), :skat '(c7 c8) })
(defn create-player [pid]
  (reify Player
         (id [_] pid)
         (skat-swapping [_ _ cards skat-card] (if (= skat-card c7)
                                                (first cards)
                                                (first (rest cards))))))
(def pl1 (create-player "p1"))
(def pl2 (create-player "p2"))
(def pl3 (create-player "p3"))

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

(deftest swap-skat-test
  (testing "hand leaves deal intact"
    (let [config (Configuration. pl1 :grand true false false false false 18)]
      (is (= deal (swap-skat config deal pl1)))))
  (testing "no hand swaps at most 2 cards"
    (let [config (Configuration. pl1 :grand false false false false false 18)]
      (is (= { :front '(c7 c8)
               :middle '(c3 c4)
               :rear '(c5 c6)
               :skat '(c1 c2) }
             deal (swap-skat config deal pl1))))))

(deftest play-deal-test)

(deftest deal-end2end-test)

(deftest start-game)
