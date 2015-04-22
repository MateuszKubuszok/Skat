(ns skat.gameplay_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.cards :as cards]
            [skat.gameplay :refer :all])
  (:import  [skat Card
                  Bidders
                  Bidding
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
(def deal { :front [c1 c2], :middle [c3 c4], :rear [c5 c6], :skat [c7 c8] })
(defn create-player [pid]
  (reify Player
         (id [_] pid)
         (place-bid [_ _ last-bid] (if (> last-bid 17) 17 18))
         (respond-to-bid [_ _ _] true)
         (skat-swapping [_ _ _ cards skat-card] (first cards))))
(def pl1 (create-player "p1"))
(def pl2 (create-player "p2"))
(def pl3 (create-player "p3"))

(def mock-bidders (Bidders. pl1 pl2 pl3))
(def mock-driver
  (reify GameDriver
    (create-players [this] mock-bidders)
    ))
(def mock-full-deal
  (letfn [(drop-take [seq d t] (take t (drop d seq)))]
    { :front  (drop-take cards/deck 0  10)
      :middle (drop-take cards/deck 10 10)
      :rear   (drop-take cards/deck 20 10)
      :skat   (drop-take cards/deck 30 2) }))

(deftest perform-auction-test
  (testing "auction eventually end with bidding"
    (let [driver  (reify GameDriver
                     (auction-started [_ _])
                     (auction-result [_ _]))
          bidding (perform-auction driver mock-bidders)]
      (is (= pl1 (get-in bidding [:bidding :winner]))))))

(deftest declare-game-test
  (let [config-true  (Configuration. pl1 :grand true true true true 18)
        config-false (Configuration. pl1 :grand false false false false 18)
        driver-true  (reify GameDriver
                       (declare-game [_ _] config-true)
                       (declaration-result [_ _]))
        driver-false (reify GameDriver
                       (declare-game [_ _] config-false)
                       (declaration-result [_ _]))]
    (testing "declaration eventually ends"
      (is (= config-true (declare-game driver-true {})))
      (is (= config-false (declare-game driver-false {}))))))

(deftest swap-skat-test
  (testing "hand leaves deal intact"
    (let [config (Configuration. pl1 :grand true false false false 18)]
      (is (= deal (swap-skat config deal pl1 :front)))))
  (testing "no hand swaps at most 2 cards"
    (let [config  (Configuration. pl1 :grand false false  false false 18)
          swapped (swap-skat config deal pl1 :front)]
      (is (= (set [c2 c8]) (set (swapped :front))))
      (is (= (set [c3 c4]) (set (swapped :middle))))
      (is (= (set [c5 c6]) (set (swapped :rear))))
      (is (= (set [c1 c7]) (set (swapped :skat)))))))

(comment play-deal-test
  (let [config-grand  (Configuration. pl1 :grand true false false false 18)
        config-kreuz  (Configuration. pl1 :kreuz true false false false 18)
        config-null   (Configuration. pl1 :null true false false false 18)
        config-ouvert (Configuration. pl1 :null true true false false 18)
        grand-result (play-deal mock-driver
                                config-grand
                                mock-bidders
                                mock-full-deal)
        kreuz-result (play-deal mock-driver
                                config-kreuz
                                mock-bidders
                                mock-full-deal)
        null-result (play-deal mock-driver
                               config-null
                               mock-bidders
                               mock-full-deal)
        ouvert-result (play-deal mock-driver
                                 config-ouvert
                                 mock-bidders
                                 mock-full-deal)]
    (testing "grand game result has to be as expected"
      (is (== 13
              (-> grand-result
                  (get-in [:knowledge pl1 :cards-taken pl1])
                  count)))
      (is (== 13
              (-> grand-result
                  (get-in [:knowledge pl2 :cards-taken pl2])
                  count)))
      (is (== 13
              (-> grand-result
                  (get-in [:knowledge pl3 :cards-taken pl3])
                  count)))
      )
    (testing "color game has to have solist cards known"
      )
    (testing "null game has to have solist cards known"
      )
    (testing "ouvert game has to have solist cards known"
      )))

(comment deal-end2end-test)

(comment start-game-test)
