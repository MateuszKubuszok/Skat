(ns skat.auction_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.cards :refer :all]
            [skat.auction :refer :all])
  (:import  [skat Card Bidders Bidding Configuration Player]))

(def c1 (Card. :kreuz :W))
(def c2 (Card. :grun :W))
(def c3 (Card. :herz :W))
(def c4 (Card. :schell :W))
(def c5 (Card. :kreuz :A))
(def c6 (Card. :grun :A))
(def c7 (Card. :herz :A))
(def c8 (Card. :schell :A))
(def pattern [c1 c2 c3 c4 c5 c6 c7 c8])

(deftest with-matadors-value-calculator-test
  (testing "positive value for cards with matadors"
    (is (== 8 (with-matadors-value-calculator pattern pattern)))
    (is (== 4 (with-matadors-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (== 3 (with-matadors-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (== 2 (with-matadors-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (== 1 (with-matadors-value-calculator pattern [c1 c6 c3 c4 c5]))))
  (testing "0 for cards without matadors"
    (is (zero? (with-matadors-value-calculator pattern (drop 1 pattern))))
    (is (zero? (with-matadors-value-calculator pattern (drop 2 pattern))))
    (is (zero? (with-matadors-value-calculator pattern (drop 3 pattern))))
    (is (zero? (with-matadors-value-calculator pattern (drop 4 pattern))))
    (is (zero? (with-matadors-value-calculator pattern (drop 5 pattern))))))

(deftest without-matadors-value-calculator-test
  (testing "0 for cards with matadors"
    (is (zero? (without-matadors-value-calculator pattern pattern)))
    (is (zero? (without-matadors-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (zero? (without-matadors-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (zero? (without-matadors-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (zero? (without-matadors-value-calculator pattern [c1 c6 c3 c4 c5]))))
  (testing "positive value for cards without matadors"
    (is (== 1 (without-matadors-value-calculator pattern (drop 1 pattern))))
    (is (== 2 (without-matadors-value-calculator pattern (drop 2 pattern))))
    (is (== 3 (without-matadors-value-calculator pattern (drop 3 pattern))))
    (is (== 4 (without-matadors-value-calculator pattern (drop 4 pattern))))
    (is (== 5 (without-matadors-value-calculator pattern (drop 5 pattern))))))

(deftest matadors-value-calculator-test
  (testing "always positive value"
    (is (== 8 (matadors-value-calculator pattern pattern)))
    (is (== 4 (matadors-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (== 3 (matadors-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (== 2 (matadors-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (== 1 (matadors-value-calculator pattern [c1 c6 c3 c4 c5])))
    (is (== 1 (matadors-value-calculator pattern (drop 1 pattern))))
    (is (== 2 (matadors-value-calculator pattern (drop 2 pattern))))
    (is (== 3 (matadors-value-calculator pattern (drop 3 pattern))))
    (is (== 4 (matadors-value-calculator pattern (drop 4 pattern))))
    (is (== 5 (matadors-value-calculator pattern (drop 5 pattern))))))

(deftest matadors-grand-test
  (testing "grand can be with 4 matadors at most"
    (is (== 4 (matadors-grand (shuffle (filter trump-grand? deck)))))
    (is (== 4 (matadors-grand (shuffle (take 5 pattern)))))
    (is (== 4 (matadors-grand (shuffle (take 4 pattern)))))
    (is (== 3 (matadors-grand (shuffle (take 3 pattern)))))
    (is (== 2 (matadors-grand (shuffle (take 2 pattern)))))
    (is (== 1 (matadors-grand (shuffle (take 1 pattern))))))
  (testing "grand can be without 4 matadors at most"
    (is (== 4 (matadors-grand (list))))
    (is (== 4 (matadors-grand (shuffle (drop 5 pattern)))))
    (is (== 4 (matadors-grand (shuffle (drop 4 pattern)))))
    (is (== 3 (matadors-grand (shuffle (drop 3 pattern)))))
    (is (== 2 (matadors-grand (shuffle (drop 2 pattern)))))
    (is (== 1 (matadors-grand (shuffle (drop 1 pattern)))))))

(deftest matadors-kreuz-test
  (let [cards [c1 c2 c3 c4 c5]]
    (testing "kreuz can be with 11 matadors at most"
      (is (== 11 (matadors-kreuz (shuffle (filter (partial trump-color? :kreuz)
                                               deck)))))
      (is (== 5 (matadors-kreuz (shuffle cards))))
      (is (== 4 (matadors-kreuz (shuffle (take 4 cards)))))
      (is (== 3 (matadors-kreuz (shuffle (take 3 cards)))))
      (is (== 2 (matadors-kreuz (shuffle (take 2 cards)))))
      (is (== 1 (matadors-kreuz (shuffle (take 1 cards))))))
    (testing "kreuz can be without 11 matadors at most"
      (is (== 11 (matadors-kreuz (list))))
      (is (== 4 (matadors-kreuz (shuffle (drop 4 cards)))))
      (is (== 3 (matadors-kreuz (shuffle (drop 3 cards)))))
      (is (== 2 (matadors-kreuz (shuffle (drop 2 cards)))))
      (is (== 1 (matadors-kreuz (shuffle (drop 1 cards))))))))

(deftest matadors-grun-test
  (let [cards [c1 c2 c3 c4 c6]]
    (testing "grun can be with 11 matadors at most"
      (is (== 11 (matadors-grun (shuffle (filter (partial trump-color? :grun)
                                               deck)))))
      (is (== 5 (matadors-grun (shuffle cards))))
      (is (== 4 (matadors-grun (shuffle (take 4 cards)))))
      (is (== 3 (matadors-grun (shuffle (take 3 cards)))))
      (is (== 2 (matadors-grun (shuffle (take 2 cards)))))
      (is (== 1 (matadors-grun (shuffle (take 1 cards))))))
    (testing "grun can be without 11 matadors at most"
      (is (== 11 (matadors-grun (list))))
      (is (== 4 (matadors-grun (shuffle (drop 4 cards)))))
      (is (== 3 (matadors-grun (shuffle (drop 3 cards)))))
      (is (== 2 (matadors-grun (shuffle (drop 2 cards)))))
      (is (== 1 (matadors-grun (shuffle (drop 1 cards))))))))

(deftest matadors-herz-test
  (let [cards [c1 c2 c3 c4 c7]]
    (testing "herz can be with 11 matadors at most"
      (is (== 11 (matadors-herz (shuffle (filter (partial trump-color? :herz)
                                               deck)))))
      (is (== 5 (matadors-herz (shuffle cards))))
      (is (== 4 (matadors-herz (shuffle (take 4 cards)))))
      (is (== 3 (matadors-herz (shuffle (take 3 cards)))))
      (is (== 2 (matadors-herz (shuffle (take 2 cards)))))
      (is (== 1 (matadors-herz (shuffle (take 1 cards))))))
    (testing "herz can be without 11 matadors at most"
      (is (== 11 (matadors-herz (list))))
      (is (== 4 (matadors-herz (shuffle (drop 4 cards)))))
      (is (== 3 (matadors-herz (shuffle (drop 3 cards)))))
      (is (== 2 (matadors-herz (shuffle (drop 2 cards)))))
      (is (== 1 (matadors-herz (shuffle (drop 1 cards))))))))

(deftest matadors-schell-test
  (let [cards [c1 c2 c3 c4 c8]]
    (testing "schell can be with 11 matadors at most"
      (is (== 11 (matadors-schell
                  (shuffle (filter (partial trump-color? :schell) deck)))))
      (is (== 5 (matadors-schell (shuffle cards))))
      (is (== 4 (matadors-schell (shuffle (take 4 cards)))))
      (is (== 3 (matadors-schell (shuffle (take 3 cards)))))
      (is (== 2 (matadors-schell (shuffle (take 2 cards)))))
      (is (== 1 (matadors-schell (shuffle (take 1 cards))))))
    (testing "schell can be without 11 matadors at most"
      (is (== 11 (matadors-schell (list))))
      (is (== 4 (matadors-schell (shuffle (drop 4 cards)))))
      (is (== 3 (matadors-schell (shuffle (drop 3 cards)))))
      (is (== 2 (matadors-schell (shuffle (drop 2 cards)))))
      (is (== 1 (matadors-schell (shuffle (drop 1 cards))))))))

(deftest normal-game-value-test
  (let [t            true
        f            false
        cards        pattern
        cards-wout-1 (drop 1 cards)]
    (testing "normal games should have positive values always"
      (is (== (* 24 5) (normal-game-value cards        :grand  f f f f)))
      (is (== (* 24 6) (normal-game-value cards        :grand  t f f f)))
      (is (== (* 24 7) (normal-game-value cards        :grand  t t f f)))
      (is (== (* 24 2) (normal-game-value cards-wout-1 :grand  f f f f)))
      (is (== (* 24 3) (normal-game-value cards-wout-1 :grand  t f f f)))
      (is (== (* 24 4) (normal-game-value cards-wout-1 :grand  t t f f)))
      (is (== (* 12 6) (normal-game-value cards        :kreuz  f f f f)))
      (is (== (* 12 7) (normal-game-value cards        :kreuz  t f f f)))
      (is (== (* 12 8) (normal-game-value cards        :kreuz  t t f f)))
      (is (== (* 12 2) (normal-game-value cards-wout-1 :kreuz  f f f f)))
      (is (== (* 12 3) (normal-game-value cards-wout-1 :kreuz  t f f f)))
      (is (== (* 12 4) (normal-game-value cards-wout-1 :kreuz  t t f f)))
      (is (== (* 11 6) (normal-game-value cards        :grun   f f f f)))
      (is (== (* 11 7) (normal-game-value cards        :grun   t f f f)))
      (is (== (* 11 8) (normal-game-value cards        :grun   t t f f)))
      (is (== (* 11 2) (normal-game-value cards-wout-1 :grun   f f f f)))
      (is (== (* 11 3) (normal-game-value cards-wout-1 :grun   t f f f)))
      (is (== (* 11 4) (normal-game-value cards-wout-1 :grun   t t f f)))
      (is (== (* 10 6) (normal-game-value cards        :herz   f f f f)))
      (is (== (* 10 7) (normal-game-value cards        :herz   t f f f)))
      (is (== (* 10 8) (normal-game-value cards        :herz   t t f f)))
      (is (== (* 10 2) (normal-game-value cards-wout-1 :herz   f f f f)))
      (is (== (* 10 3) (normal-game-value cards-wout-1 :herz   t f f f)))
      (is (== (* 10 4) (normal-game-value cards-wout-1 :herz   t t f f)))
      (is (== (*  9 6) (normal-game-value cards        :schell f f f f)))
      (is (== (*  9 7) (normal-game-value cards        :schell t f f f)))
      (is (== (*  9 8) (normal-game-value cards        :schell t t f f)))
      (is (== (*  9 2) (normal-game-value cards-wout-1 :schell f f f f)))
      (is (== (*  9 3) (normal-game-value cards-wout-1 :schell t f f f)))
      (is (== (*  9 4) (normal-game-value cards-wout-1 :schell t t f f))))))

(deftest game-value-test
  (let [t            true
        f            false
        cards        pattern
        cards-wout-1 (drop 1 cards)]
    (testing "all games should have positive values always"
      (is (== 23       (game-value cards        :null   f f f f)))
      (is (== 35       (game-value cards        :null   t f f f)))
      (is (== 46       (game-value cards        :null   f t f f)))
      (is (== 59       (game-value cards        :null   t t f f)))
      (is (== 23       (game-value cards-wout-1 :null   f f f f)))
      (is (== 35       (game-value cards-wout-1 :null   t f f f)))
      (is (== 46       (game-value cards-wout-1 :null   f t f f)))
      (is (== 59       (game-value cards-wout-1 :null   t t f f)))
      (is (== (* 24 5) (game-value cards        :grand  f f f f)))
      (is (== (* 24 6) (game-value cards        :grand  t f f f)))
      (is (== (* 24 7) (game-value cards        :grand  t t f f)))
      (is (== (* 24 2) (game-value cards-wout-1 :grand  f f f f)))
      (is (== (* 24 3) (game-value cards-wout-1 :grand  t f f f)))
      (is (== (* 24 4) (game-value cards-wout-1 :grand  t t f f)))
      (is (== (* 12 6) (game-value cards        :kreuz  f f f f)))
      (is (== (* 12 7) (game-value cards        :kreuz  t f f f)))
      (is (== (* 12 8) (game-value cards        :kreuz  t t f f)))
      (is (== (* 12 2) (game-value cards-wout-1 :kreuz  f f f f)))
      (is (== (* 12 3) (game-value cards-wout-1 :kreuz  t f f f)))
      (is (== (* 12 4) (game-value cards-wout-1 :kreuz  t t f f)))
      (is (== (* 11 6) (game-value cards        :grun   f f f f)))
      (is (== (* 11 7) (game-value cards        :grun   t f f f)))
      (is (== (* 11 8) (game-value cards        :grun   t t f f)))
      (is (== (* 11 2) (game-value cards-wout-1 :grun   f f f f)))
      (is (== (* 11 3) (game-value cards-wout-1 :grun   t f f f)))
      (is (== (* 11 4) (game-value cards-wout-1 :grun   t t f f)))
      (is (== (* 10 6) (game-value cards        :herz   f f f f)))
      (is (== (* 10 7) (game-value cards        :herz   t f f f)))
      (is (== (* 10 8) (game-value cards        :herz   t t f f)))
      (is (== (* 10 2) (game-value cards-wout-1 :herz   f f f f)))
      (is (== (* 10 3) (game-value cards-wout-1 :herz   t f f f)))
      (is (== (* 10 4) (game-value cards-wout-1 :herz   t t f f)))
      (is (== (*  9 6) (game-value cards        :schell f f f f)))
      (is (== (*  9 7) (game-value cards        :schell t f f f)))
      (is (== (*  9 8) (game-value cards        :schell t t f f)))
      (is (== (*  9 2) (game-value cards-wout-1 :schell f f f f)))
      (is (== (*  9 3) (game-value cards-wout-1 :schell t f f f)))
      (is (== (*  9 4) (game-value cards-wout-1 :schell t t f f))))))

(deftest final-game-value-test
  (let [t            true
        f            false
        cards        pattern
        cards-wout-1 (drop 1 cards)]
    (testing "all games should have positive values always"
      (is (== 23       (final-game-value
                        cards
                        (Configuration. nil :null   f f f f 18))))
      (is (== 35       (final-game-value
                        cards
                        (Configuration. nil :null   t f f f 18))))
      (is (== 46       (final-game-value
                        cards
                        (Configuration. nil :null   f t f f 18))))
      (is (== 59       (final-game-value
                        cards
                        (Configuration. nil :null   t t f f 18))))
      (is (== 23       (final-game-value
                        cards-wout-1
                        (Configuration. nil :null   f f f f 18))))
      (is (== 35       (final-game-value
                        cards-wout-1
                        (Configuration. nil :null   t f f f 18))))
      (is (== 46       (final-game-value
                        cards-wout-1
                        (Configuration. nil :null   f t f f 18))))
      (is (== 59       (final-game-value
                        cards-wout-1
                        (Configuration. nil :null   t t f f 18))))
      (is (== (* 24 5) (final-game-value
                        cards
                        (Configuration. nil :grand  f f f f 18))))
      (is (== (* 24 6) (final-game-value
                        cards
                        (Configuration. nil :grand  t f f f 18))))
      (is (== (* 24 7) (final-game-value
                        cards
                        (Configuration. nil :grand  t t f f 18))))
      (is (== (* 24 2) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grand  f f f f 18))))
      (is (== (* 24 3) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grand  t f f f 18))))
      (is (== (* 24 4) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grand  t t f f 18))))
      (is (== (* 12 6) (final-game-value
                        cards
                        (Configuration. nil :kreuz  f f f f 18))))
      (is (== (* 12 7) (final-game-value
                        cards
                        (Configuration. nil :kreuz  t f f f 18))))
      (is (== (* 12 8) (final-game-value
                        cards
                        (Configuration. nil :kreuz  t t f f 18))))
      (is (== (* 12 2) (final-game-value
                        cards-wout-1
                        (Configuration. nil :kreuz  f f f f 18))))
      (is (== (* 12 3) (final-game-value
                        cards-wout-1
                        (Configuration. nil :kreuz  t f f f 18))))
      (is (== (* 12 4) (final-game-value
                        cards-wout-1
                        (Configuration. nil :kreuz  t t f f 18))))
      (is (== (* 11 6) (final-game-value
                        cards
                        (Configuration. nil :grun   f f f f 18))))
      (is (== (* 11 7) (final-game-value
                        cards
                        (Configuration. nil :grun   t f f f 18))))
      (is (== (* 11 8) (final-game-value
                        cards
                        (Configuration. nil :grun   t t f f 18))))
      (is (== (* 11 2) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grun   f f f f 18))))
      (is (== (* 11 3) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grun   t f f f 18))))
      (is (== (* 11 4) (final-game-value
                        cards-wout-1
                        (Configuration. nil :grun   t t f f 18))))
      (is (== (* 10 6) (final-game-value
                        cards
                        (Configuration. nil :herz   f f f f 18))))
      (is (== (* 10 7) (final-game-value
                        cards
                        (Configuration. nil :herz   t f f f 18))))
      (is (== (* 10 8) (final-game-value
                        cards
                        (Configuration. nil :herz   t t f f 18))))
      (is (== (* 10 2) (final-game-value
                        cards-wout-1
                        (Configuration. nil :herz   f f f f 18))))
      (is (== (* 10 3) (final-game-value
                        cards-wout-1
                        (Configuration. nil :herz   t f f f 18))))
      (is (== (* 10 4) (final-game-value
                        cards-wout-1
                        (Configuration. nil :herz   t t f f 18))))
      (is (== (*  9 6) (final-game-value
                        cards
                        (Configuration. nil :schell f f f f 18))))
      (is (== (*  9 7) (final-game-value
                        cards
                        (Configuration. nil :schell t f f f 18))))
      (is (== (*  9 8) (final-game-value
                        cards
                        (Configuration. nil :schell t t f f 18))))
      (is (== (*  9 2) (final-game-value
                        cards-wout-1
                        (Configuration. nil :schell f f f f 18))))
      (is (== (*  9 3) (final-game-value
                        cards-wout-1
                        (Configuration. nil :schell t f f f 18))))
      (is (== (*  9 4) (final-game-value
                        cards-wout-1
                        (Configuration. nil :schell t t f f 18)))))))

(deftest game-value?-test
  (testing "nil is valid game value"
    (is (game-value? nil)))
   (testing "all possible-game-values are game values"
    (is (every? game-value? possible-game-values)))
   (testing "other values are invalid"
    (is (not (game-value? 0)))
    (is (not (game-value? 16)))
    (is (not (game-value? -48)))))

(deftest bids?-test
  (testing "values nil or 17 means pass"
    (is (not (bids? nil)))
    (is (not (bids? 17))))
  (testing "other game values means bid"
    (is (every? bids? [18 27 20 30 22 33 24 36 46]))))

(deftest bidding-101-test
  (letfn [(numeric-bid [bid] (if (bids? bid) bid 17))
          (mock-player [pid max-bid suit]
            (reify Player
              (id [this] pid)
              (place-bid [this _ last-bid]
                (if (< (numeric-bid last-bid) max-bid) max-bid))
              (respond-to-bid [this _ bid]
                (<= (numeric-bid bid) max-bid))
              (declare-suit [this _ _] suit)))]
    (let [bid-17   (mock-player "a" 17 :null)
          bid-24   (mock-player "b" 24 :kreuz)
          bid-48   (mock-player "c" 48 :grand)
          bid-48-2 (mock-player "d" 48 :kreuz)]
      (testing "two passes don't make a bid winner"
        (is (not (bids? (:bid (bidding-101 bid-17 [] bid-17 [] nil))))))
      (testing "bid wins agains pass"
        (let [pass-bid (bidding-101 bid-17 [] bid-24 [] nil)
              bid-pass (bidding-101 bid-24 [] bid-17 [] nil)]
          (is (not (bids? (:bid pass-bid))))
          (is (= bid-24 (:winner pass-bid)))
          (is (== 24 (:bid bid-pass)))
          (is (= bid-24 (:winner bid-pass)))))
      (testing "higher bid wins"
        (let [hi-low (bidding-101 bid-48 [] bid-24 [] nil)
              low-hi (bidding-101 bid-24 [] bid-48 [] nil)]
          (is (== 48 (:bid hi-low)))
          (is (= bid-48 (:winner hi-low)))
          (is (== 24 (:bid low-hi)))
          (is (= bid-48 (:winner low-hi)))))
      (testing "bidder backs first"
        (let [same-1 (bidding-101 bid-48   [] bid-48-2 [] nil)
              same-2 (bidding-101 bid-48-2 [] bid-48   [] nil)]
          (is (== 48 (:bid same-1)))
          (is (= bid-48-2 (:winner same-1)))
          (is (== 48 (:bid same-2)))
          (is (= bid-48 (:winner same-2))))))))

(deftest do-auction-test
  (letfn [(numeric-bid [bid] (if (bids? bid) bid 17))
          (mock-player [pid max-bid suit]
            (reify Player
              (id [this] pid)
              (place-bid [this _ last-bid]
                (if (< (numeric-bid last-bid) max-bid) max-bid))
              (respond-to-bid [this _ bid]
                (<= (numeric-bid bid) max-bid))
              (declare-suit [this _ _] suit)))]
    (let [bid-17   (mock-player "a" 17 :null)
          bid-24   (mock-player "b" 24 :kreuz)
          bid-48   (mock-player "c" 48 :grand)
          bid-48-2 (mock-player "d" 48 :kreuz)]
      (testing "for all players passing aucion is undecided"
        (is (nil? (do-auction (Bidders. bid-17 bid-17 bid-17)
                              { :front [], :middle [], :rear [] }))))
      (testing "highest bidder wins"
        (let [only-bid (do-auction (Bidders. bid-17 bid-17 bid-24)
                                   { :front [], :middle [], :rear [] })
              no-min   (do-auction (Bidders. bid-17 bid-24 bid-48)
                                   { :front [], :middle [], :rear [] })
              min-bid  (do-auction (Bidders. bid-24 bid-48 bid-48-2)
                                   { :front [], :middle [], :rear [] })]
          (is (== 24 (:bid only-bid)))
          (is (= bid-24 (:winner only-bid)))
          (is (== 48 (:bid min-bid)))
          (is (= bid-48 (:winner min-bid)))
          (is (== 48 (:bid min-bid)))
          (is (= bid-48 (:winner min-bid))))))))

(deftest auction-successful?-test
  (testing "if at least one of Players bid auction is successful"
    (is (auction-successful? (Bidding. nil nil 18))))
  (testing "if all Players passed auction is not successful"
    (is (not (auction-successful?
              (Bidding. nil nil passed-game-value))))
    (is (not (auction-successful? (Bidding. nil nil nil))))))

(deftest contract-fulfilled?-test
  (let [t true
        f false
        no-cards         (list)
        cards-not-enough (filter #(-> % :figure #{:A}) deck)
        cards-enough     (filter #(-> % :figure #{:A :10}) deck)
        cards-schneider  (filter #(-> % :figure #{:A :10 :K :D :W}) deck)
        cards-schwarz    deck]
    (testing "validation pass fulfilled contracts win minimal bid"
      (is (contract-fulfilled? (Configuration. nil :grand f f f f 18)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :kreuz f f f f 18)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :grun f f f f 18)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :herz f f f f 18)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :schell f f f f 18)
                               cards-enough)))
    (testing "validation pass fulfilled contracts win maximal bid"
      (is (contract-fulfilled? (Configuration. nil :grand f f f f 120)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :kreuz f f f f 60)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :grun f f f f 55)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :herz f f f f 50)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :schell f f f f 45)
                               cards-enough)))
    (testing "validation pass fulfilled contracts with modifiers"
      (is (contract-fulfilled? (Configuration. nil :grand t f f f 72)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :kreuz t t f f 72)
                               cards-enough))
      (is (contract-fulfilled? (Configuration. nil :grun f f t f 66)
                               cards-schneider))
      (is (contract-fulfilled? (Configuration. nil :herz t f f t 66)
                               cards-schwarz))
      (is (contract-fulfilled? (Configuration. nil :schell t t f f 54)
                               cards-enough)))
    (testing "validation fails unfulfilled contracts"
      (is (not (contract-fulfilled? (Configuration. nil :grand f f f f 18)
                                    cards-not-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :kreuz f f f f 18)
                                    cards-not-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :grun f f f f 18)
                                    cards-not-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :herz f f f f 18)
                                    cards-not-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :schell f f f f 18)
                                    cards-not-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :grand f f f f 144)
                                    cards-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :kreuz f f f f 72)
                                    cards-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :grun f f f f 66)
                                    cards-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :herz f f f f 60)
                                    cards-enough)))
      (is (not (contract-fulfilled? (Configuration. nil :schell f f f f 54)
                                    cards-enough))))
    (testing "validation works for null games"
      (is (contract-fulfilled? (Configuration. nil :null f f f f 23) no-cards))
      (is (contract-fulfilled? (Configuration. nil :null t f f f 35) no-cards))
      (is (contract-fulfilled? (Configuration. nil :null f f f f 46) no-cards))
      (is (contract-fulfilled? (Configuration. nil :null t t f f 59) no-cards))
      (is (not (contract-fulfilled? (Configuration. nil :null f f f f 18)
                                    cards-enough))))))
