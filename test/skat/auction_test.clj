(ns skat.auction_test
  (:require [clojure.test :refer :all]
            ;[clojure.pprint :refer :all]
            ;[clojure.tools.trace :refer :all]
            [skat.cards :refer :all]
            [skat.auction :refer :all]))

(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :grun :W))
(def c3 (skat.cards.Card. :herz :W))
(def c4 (skat.cards.Card. :schell :W))
(def c5 (skat.cards.Card. :kreuz :A))
(def c6 (skat.cards.Card. :grun :A))
(def c7 (skat.cards.Card. :herz :A))
(def c8 (skat.cards.Card. :schell :A))
(def pattern [c1 c2 c3 c4 c5 c6 c7 c8])

(deftest with-peaks-value-calculator-test
  (testing "positive value for cards with peaks"
    (is (== 8 (with-peaks-value-calculator pattern pattern)))
    (is (== 4 (with-peaks-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (== 3 (with-peaks-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (== 2 (with-peaks-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (== 1 (with-peaks-value-calculator pattern [c1 c6 c3 c4 c5]))))
  (testing "0 for cards without peaks"
    (is (zero? (with-peaks-value-calculator pattern (drop 1 pattern))))
    (is (zero? (with-peaks-value-calculator pattern (drop 2 pattern))))
    (is (zero? (with-peaks-value-calculator pattern (drop 3 pattern))))
    (is (zero? (with-peaks-value-calculator pattern (drop 4 pattern))))
    (is (zero? (with-peaks-value-calculator pattern (drop 5 pattern))))))

(deftest without-peaks-value-calculator-test
  (testing "0 for cards with peaks"
    (is (zero? (without-peaks-value-calculator pattern pattern)))
    (is (zero? (without-peaks-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (zero? (without-peaks-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (zero? (without-peaks-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (zero? (without-peaks-value-calculator pattern [c1 c6 c3 c4 c5]))))
  (testing "positive value for cards without peaks"
    (is (== 1 (without-peaks-value-calculator pattern (drop 1 pattern))))
    (is (== 2 (without-peaks-value-calculator pattern (drop 2 pattern))))
    (is (== 3 (without-peaks-value-calculator pattern (drop 3 pattern))))
    (is (== 4 (without-peaks-value-calculator pattern (drop 4 pattern))))
    (is (== 5 (without-peaks-value-calculator pattern (drop 5 pattern))))))

(deftest peaks-value-calculator-test
  (testing "always positive value"
    (is (== 8 (peaks-value-calculator pattern pattern)))
    (is (== 4 (peaks-value-calculator pattern [c1 c2 c3 c4 c6])))
    (is (== 3 (peaks-value-calculator pattern [c1 c2 c3 c6 c5])))
    (is (== 2 (peaks-value-calculator pattern [c1 c2 c6 c4 c5])))
    (is (== 1 (peaks-value-calculator pattern [c1 c6 c3 c4 c5])))
    (is (== 1 (peaks-value-calculator pattern (drop 1 pattern))))
    (is (== 2 (peaks-value-calculator pattern (drop 2 pattern))))
    (is (== 3 (peaks-value-calculator pattern (drop 3 pattern))))
    (is (== 4 (peaks-value-calculator pattern (drop 4 pattern))))
    (is (== 5 (peaks-value-calculator pattern (drop 5 pattern))))))

(deftest peaks-grand-test
  (testing "grand can be with 4 peaks at most"
    (is (== 4 (peaks-grand (shuffle (filter trumph-grand? deck)))))
    (is (== 4 (peaks-grand (shuffle (take 5 pattern)))))
    (is (== 4 (peaks-grand (shuffle (take 4 pattern)))))
    (is (== 3 (peaks-grand (shuffle (take 3 pattern)))))
    (is (== 2 (peaks-grand (shuffle (take 2 pattern)))))
    (is (== 1 (peaks-grand (shuffle (take 1 pattern))))))
  (testing "grand can be without 4 peaks at most"
    (is (== 4 (peaks-grand (list))))
    (is (== 4 (peaks-grand (shuffle (drop 5 pattern)))))
    (is (== 4 (peaks-grand (shuffle (drop 4 pattern)))))
    (is (== 3 (peaks-grand (shuffle (drop 3 pattern)))))
    (is (== 2 (peaks-grand (shuffle (drop 2 pattern)))))
    (is (== 1 (peaks-grand (shuffle (drop 1 pattern)))))))

(deftest peaks-kreuz-test
  (let [cards [c1 c2 c3 c4 c5]]
    (testing "kreuz can be with 11 peaks at most"
      (is (== 11 (peaks-kreuz (shuffle (filter (partial trumph-color? :kreuz)
                                               deck)))))
      (is (== 5 (peaks-kreuz (shuffle cards))))
      (is (== 4 (peaks-kreuz (shuffle (take 4 cards)))))
      (is (== 3 (peaks-kreuz (shuffle (take 3 cards)))))
      (is (== 2 (peaks-kreuz (shuffle (take 2 cards)))))
      (is (== 1 (peaks-kreuz (shuffle (take 1 cards))))))
    (testing "kreuz can be without 11 peaks at most"
      (is (== 11 (peaks-kreuz (list))))
      (is (== 4 (peaks-kreuz (shuffle (drop 4 cards)))))
      (is (== 3 (peaks-kreuz (shuffle (drop 3 cards)))))
      (is (== 2 (peaks-kreuz (shuffle (drop 2 cards)))))
      (is (== 1 (peaks-kreuz (shuffle (drop 1 cards))))))))

(deftest peaks-grun-test
  (let [cards [c1 c2 c3 c4 c6]]
    (testing "grun can be with 11 peaks at most"
      (is (== 11 (peaks-grun (shuffle (filter (partial trumph-color? :grun)
                                               deck)))))
      (is (== 5 (peaks-grun (shuffle cards))))
      (is (== 4 (peaks-grun (shuffle (take 4 cards)))))
      (is (== 3 (peaks-grun (shuffle (take 3 cards)))))
      (is (== 2 (peaks-grun (shuffle (take 2 cards)))))
      (is (== 1 (peaks-grun (shuffle (take 1 cards))))))
    (testing "grun can be without 11 peaks at most"
      (is (== 11 (peaks-grun (list))))
      (is (== 4 (peaks-grun (shuffle (drop 4 cards)))))
      (is (== 3 (peaks-grun (shuffle (drop 3 cards)))))
      (is (== 2 (peaks-grun (shuffle (drop 2 cards)))))
      (is (== 1 (peaks-grun (shuffle (drop 1 cards))))))))

(deftest peaks-herz-test
  (let [cards [c1 c2 c3 c4 c7]]
    (testing "herz can be with 11 peaks at most"
      (is (== 11 (peaks-herz (shuffle (filter (partial trumph-color? :herz)
                                               deck)))))
      (is (== 5 (peaks-herz (shuffle cards))))
      (is (== 4 (peaks-herz (shuffle (take 4 cards)))))
      (is (== 3 (peaks-herz (shuffle (take 3 cards)))))
      (is (== 2 (peaks-herz (shuffle (take 2 cards)))))
      (is (== 1 (peaks-herz (shuffle (take 1 cards))))))
    (testing "herz can be without 11 peaks at most"
      (is (== 11 (peaks-herz (list))))
      (is (== 4 (peaks-herz (shuffle (drop 4 cards)))))
      (is (== 3 (peaks-herz (shuffle (drop 3 cards)))))
      (is (== 2 (peaks-herz (shuffle (drop 2 cards)))))
      (is (== 1 (peaks-herz (shuffle (drop 1 cards))))))))

(deftest peaks-schell-test
  (let [cards [c1 c2 c3 c4 c8]]
    (testing "schell can be with 11 peaks at most"
      (is (== 11 (peaks-schell (shuffle (filter (partial trumph-color? :schell)
                                               deck)))))
      (is (== 5 (peaks-schell (shuffle cards))))
      (is (== 4 (peaks-schell (shuffle (take 4 cards)))))
      (is (== 3 (peaks-schell (shuffle (take 3 cards)))))
      (is (== 2 (peaks-schell (shuffle (take 2 cards)))))
      (is (== 1 (peaks-schell (shuffle (take 1 cards))))))
    (testing "schell can be without 11 peaks at most"
      (is (== 11 (peaks-schell (list))))
      (is (== 4 (peaks-schell (shuffle (drop 4 cards)))))
      (is (== 3 (peaks-schell (shuffle (drop 3 cards)))))
      (is (== 2 (peaks-schell (shuffle (drop 2 cards)))))
      (is (== 1 (peaks-schell (shuffle (drop 1 cards))))))))

(deftest normal-game-value-test
  (let [cards        pattern
        cards-wout-1 (drop 1 cards)]
    (testing "normal games should have positive values always"
      (is (== (* 24 6) (normal-game-value cards        :grand  false false)))
      (is (== (* 24 5) (normal-game-value cards        :grand  true  false)))
      (is (== (* 24 7) (normal-game-value cards        :grand  false true)))
      (is (== (* 24 6) (normal-game-value cards        :grand  true  true)))
      (is (== (* 24 3) (normal-game-value cards-wout-1 :grand  false false)))
      (is (== (* 24 2) (normal-game-value cards-wout-1 :grand  true  false)))
      (is (== (* 24 4) (normal-game-value cards-wout-1 :grand  false true)))
      (is (== (* 24 3) (normal-game-value cards-wout-1 :grand  true  true)))
      (is (== (* 12 7) (normal-game-value cards        :kreuz  false false)))
      (is (== (* 12 6) (normal-game-value cards        :kreuz  true  false)))
      (is (== (* 12 8) (normal-game-value cards        :kreuz  false true)))
      (is (== (* 12 7) (normal-game-value cards        :kreuz  true  true)))
      (is (== (* 12 3) (normal-game-value cards-wout-1 :kreuz  false false)))
      (is (== (* 12 2) (normal-game-value cards-wout-1 :kreuz  true  false)))
      (is (== (* 12 4) (normal-game-value cards-wout-1 :kreuz  false true)))
      (is (== (* 12 3) (normal-game-value cards-wout-1 :kreuz  true  true)))
      (is (== (* 11 7) (normal-game-value cards        :grun   false false)))
      (is (== (* 11 6) (normal-game-value cards        :grun   true  false)))
      (is (== (* 11 8) (normal-game-value cards        :grun   false true)))
      (is (== (* 11 7) (normal-game-value cards        :grun   true  true)))
      (is (== (* 11 3) (normal-game-value cards-wout-1 :grun   false false)))
      (is (== (* 11 2) (normal-game-value cards-wout-1 :grun   true  false)))
      (is (== (* 11 4) (normal-game-value cards-wout-1 :grun   false true)))
      (is (== (* 11 3) (normal-game-value cards-wout-1 :grun   true  true)))
      (is (== (* 10 7) (normal-game-value cards        :herz   false false)))
      (is (== (* 10 6) (normal-game-value cards        :herz   true  false)))
      (is (== (* 10 8) (normal-game-value cards        :herz   false true)))
      (is (== (* 10 7) (normal-game-value cards        :herz   true  true)))
      (is (== (* 10 3) (normal-game-value cards-wout-1 :herz   false false)))
      (is (== (* 10 2) (normal-game-value cards-wout-1 :herz   true  false)))
      (is (== (* 10 4) (normal-game-value cards-wout-1 :herz   false true)))
      (is (== (* 10 3) (normal-game-value cards-wout-1 :herz   true  true)))
      (is (== (*  9 7) (normal-game-value cards        :schell false false)))
      (is (== (*  9 6) (normal-game-value cards        :schell true  false)))
      (is (== (*  9 8) (normal-game-value cards        :schell false true)))
      (is (== (*  9 7) (normal-game-value cards        :schell true  true)))
      (is (== (*  9 3) (normal-game-value cards-wout-1 :schell false false)))
      (is (== (*  9 2) (normal-game-value cards-wout-1 :schell true  false)))
      (is (== (*  9 4) (normal-game-value cards-wout-1 :schell false true)))
      (is (== (*  9 3) (normal-game-value cards-wout-1 :schell true  true))))))

(deftest game-value-test
  (let [cards        pattern
        cards-wout-1 (drop 1 cards)]
    (testing "all games should have positive values always"
      (is (== 35       (game-value cards        :null   false false)))
      (is (== 23       (game-value cards        :null   true  false)))
      (is (== 59       (game-value cards        :null   false true)))
      (is (== 46       (game-value cards        :null   true  true)))
      (is (== 35       (game-value cards-wout-1 :null   false false)))
      (is (== 23       (game-value cards-wout-1 :null   true  false)))
      (is (== 59       (game-value cards-wout-1 :null   false true)))
      (is (== 46       (game-value cards-wout-1 :null   true  true)))
      (is (== (* 24 6) (game-value cards        :grand  false false)))
      (is (== (* 24 5) (game-value cards        :grand  true  false)))
      (is (== (* 24 7) (game-value cards        :grand  false true)))
      (is (== (* 24 6) (game-value cards        :grand  true  true)))
      (is (== (* 24 3) (game-value cards-wout-1 :grand  false false)))
      (is (== (* 24 2) (game-value cards-wout-1 :grand  true  false)))
      (is (== (* 24 4) (game-value cards-wout-1 :grand  false true)))
      (is (== (* 24 3) (game-value cards-wout-1 :grand  true  true)))
      (is (== (* 12 7) (game-value cards        :kreuz  false false)))
      (is (== (* 12 6) (game-value cards        :kreuz  true  false)))
      (is (== (* 12 8) (game-value cards        :kreuz  false true)))
      (is (== (* 12 7) (game-value cards        :kreuz  true  true)))
      (is (== (* 12 3) (game-value cards-wout-1 :kreuz  false false)))
      (is (== (* 12 2) (game-value cards-wout-1 :kreuz  true  false)))
      (is (== (* 12 4) (game-value cards-wout-1 :kreuz  false true)))
      (is (== (* 12 3) (game-value cards-wout-1 :kreuz  true  true)))
      (is (== (* 11 7) (game-value cards        :grun   false false)))
      (is (== (* 11 6) (game-value cards        :grun   true  false)))
      (is (== (* 11 8) (game-value cards        :grun   false true)))
      (is (== (* 11 7) (game-value cards        :grun   true  true)))
      (is (== (* 11 3) (game-value cards-wout-1 :grun   false false)))
      (is (== (* 11 2) (game-value cards-wout-1 :grun   true  false)))
      (is (== (* 11 4) (game-value cards-wout-1 :grun   false true)))
      (is (== (* 11 3) (game-value cards-wout-1 :grun   true  true)))
      (is (== (* 10 7) (game-value cards        :herz   false false)))
      (is (== (* 10 6) (game-value cards        :herz   true  false)))
      (is (== (* 10 8) (game-value cards        :herz   false true)))
      (is (== (* 10 7) (game-value cards        :herz   true  true)))
      (is (== (* 10 3) (game-value cards-wout-1 :herz   false false)))
      (is (== (* 10 2) (game-value cards-wout-1 :herz   true  false)))
      (is (== (* 10 4) (game-value cards-wout-1 :herz   false true)))
      (is (== (* 10 3) (game-value cards-wout-1 :herz   true  true)))
      (is (== (*  9 7) (game-value cards        :schell false false)))
      (is (== (*  9 6) (game-value cards        :schell true  false)))
      (is (== (*  9 8) (game-value cards        :schell false true)))
      (is (== (*  9 7) (game-value cards        :schell true  true)))
      (is (== (*  9 3) (game-value cards-wout-1 :schell false false)))
      (is (== (*  9 2) (game-value cards-wout-1 :schell true  false)))
      (is (== (*  9 4) (game-value cards-wout-1 :schell false true)))
      (is (== (*  9 3) (game-value cards-wout-1 :schell true  true))))))
