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
