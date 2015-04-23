(ns skat.gameplay_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.cards :as cards]
            [skat.auction :as auction]
            [skat.gameplay :refer :all])
  (:import  [skat Card
                  Bidders
                  PlayerKnowledge
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
         (play-1st-card [_ { :keys [cards-allowed] }] (first cards-allowed))
         (play-2nd-card [_ { :keys [cards-allowed] } _] (first cards-allowed))
         (play-3rd-card [_ { :keys [cards-allowed] } _ _] (first cards-allowed))
         (place-bid [_ _ last-bid] (if (> last-bid 17) 17 18))
         (respond-to-bid [_ _ _] true)
         (declare-suit [_ _ _] :grand)
         (declare-hand [_ _ _] false)
         (declare-schneider [_ _ _] false)
         (declare-schwarz [_ _ _] false)
         (declare-ouvert [_ _ _] false)
         (skat-swapping [_ _ _ cards _] (first cards))))
(def pl1 (create-player "p1"))
(def pl2 (create-player "p2"))
(def pl3 (create-player "p3"))

(def mock-bidders  (Bidders. pl1 pl2 pl3))
(def mock-bidders2 (Bidders. pl2 pl3 pl1))
(def mock-bidders3 (Bidders. pl3 pl1 pl2))
(def mock-driver
  (reify GameDriver
    (create-players [_] mock-bidders)
    (auction-started [_ _])
    (auction-result [_ _])
    (declare-game [_ { :keys [winner bid] }]
      (Configuration. winner :grand false false false false bid))
    (declaration-result [_ _])
    (trick-results [_ _])
    (deal-results [this results])
    (game-results [this points])))
(def mock-full-deal
  (letfn [(drop-take [seq d t] (take t (drop d seq)))]
    (let [sorted (sort (cards/compare-for-sort cards/compare-by-color-normal
                                               cards/compare-by-figure-normal)
                       cards/deck)]
      { :front  (drop-take sorted 0  10)
        :middle (drop-take sorted 10 10)
        :rear   (drop-take sorted 20 10)
        :skat   (drop-take sorted 30 2) })))

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

(deftest get-cards-owned-test
  (testing "obtain cards ofned for each player"
    (let [cards-owned { 1 2, 3 4, 5 6 }
          knowledge-1 (PlayerKnowledge. 1 nil cards-owned nil)
          knowledge-2 (PlayerKnowledge. 3 nil cards-owned nil)
          knowledge-3 (PlayerKnowledge. 5 nil cards-owned nil)]
      (is (== 2 (get-cards-owned knowledge-1)))
      (is (== 4 (get-cards-owned knowledge-2)))
      (is (== 6 (get-cards-owned knowledge-3))))))

(deftest game-finished?-test
  (testing "game is finished if some Player run out of cards"
    (let [knowledge { 1 (PlayerKnowledge. 1 nil { 1 [] } nil) }]
      (is (game-finished? knowledge))))
  (testing "game is not finished if all Player have any cards"
    (let [knowledge { 1 (PlayerKnowledge. 1 nil { 1 [1] } nil) }]
      (is (not (game-finished? knowledge))))))

(deftest play-deal-test
  (let [config-grand  (Configuration. pl1 :grand true false false false 18)
        config-kreuz  (Configuration. pl2 :kreuz true false false false 18)
        config-null   (Configuration. pl3 :null true false false false 18)
        config-ouvert (Configuration. pl2 :null true true false false 18)
        grand-result  (play-deal mock-driver
                                 config-grand
                                 mock-bidders3
                                 mock-full-deal)
        kreuz-result  (play-deal mock-driver
                                 config-kreuz
                                 mock-bidders2
                                 mock-full-deal)
        null-result   (play-deal mock-driver
                                 config-null
                                 mock-bidders
                                 mock-full-deal)
        ouvert-result (play-deal mock-driver
                                 config-ouvert
                                 mock-bidders2
                                 mock-full-deal)]
    (testing "grand game result has to be as expected"
      (is (== 3  (-> grand-result
                     (get-in [:knowledge pl1 :cards-taken pl1])
                     count)))
      (is (== 27 (-> grand-result
                     (get-in [:knowledge pl2 :cards-taken pl2])
                     count)))
      (is (== 0  (-> grand-result
                     (get-in [:knowledge pl3 :cards-taken pl3])
                     count))))
    (testing "color game has to be as expected"
      (is (== 30 (-> kreuz-result
                     (get-in [:knowledge pl1 :cards-taken pl1])
                     count)))
      (is (== 0  (-> kreuz-result
                     (get-in [:knowledge pl2 :cards-taken pl2])
                     count)))
      (is (== 0  (-> kreuz-result
                     (get-in [:knowledge pl3 :cards-taken pl3])
                     count))))
    (testing "null game has to be as expected"
      (is (== 3  (-> null-result
                     (get-in [:knowledge pl1 :cards-taken pl1])
                     count)))
      (is (== 3  (-> null-result
                     (get-in [:knowledge pl2 :cards-taken pl2])
                     count)))
      (is (== 24 (-> null-result
                     (get-in [:knowledge pl3 :cards-taken pl3])
                     count))))
    (testing "ouvert game has to be as expected"
      (is (== 24 (-> ouvert-result
                     (get-in [:knowledge pl1 :cards-taken pl1])
                     count)))
      (is (== 3  (-> ouvert-result
                     (get-in [:knowledge pl2 :cards-taken pl2])
                     count)))
      (is (== 3  (-> ouvert-result
                     (get-in [:knowledge pl3 :cards-taken pl3])
                     count))))))

(deftest deal-end2end-test
  (letfn [(predictable-deal [] mock-full-deal)]
    (binding [shuffle-cards-and-deal predictable-deal]
      (let [result1 (deal-end2end mock-driver mock-bidders)
            result2 (deal-end2end mock-driver mock-bidders2)
            result3 (deal-end2end mock-driver mock-bidders3)]
        (testing "deal end2end ends as expected"
          (is (= pl1 (:soloist     result1)))
          (is (not   (:success?   result1)))
          (is (= 18  (:bid        result1)))
          (is (= 96  (:game-value result1)))
          (is (= pl2 (:soloist     result2)))
          (is (not   (:success?   result2)))
          (is (= 18  (:bid        result2)))
          (is (= 96  (:game-value result2)))
          (is (= pl3 (:soloist     result3)))
          (is (not   (:success?   result3)))
          (is (= 18  (:bid        result3)))
          (is (= 96  (:game-value result3))))))))

(deftest start-game-test
  (testing "Whole game finish at some point"
    (start-game mock-driver)))
