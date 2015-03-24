(ns skat.cards_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.cards :refer :all])
  (:import  [skat Card]))

(def c1 (Card. :kreuz :W))
(def c2 (Card. :kreuz :K))
(def c3 (Card. :schell :W))
(def c4 (Card. :schell :K))
(def cards (list c1 c2 c3 c4))

(defn same-elements? [coll1 coll2] (= (set coll1) (set coll2)))

(deftest property-matches?-test
  (testing "checks Cards's property"
    (is (property-matches? :color :kreuz c1))
    (is (property-matches? :figure :W c1))))

(deftest compare-by-color-normal-test
  (testing "puts :W before other cards"
    (is (pos? (compare-by-color-normal c1 c2)))
    (is (pos? (compare-by-color-normal c3 c2)))
    (is (pos? (compare-by-color-normal c2 c4)))))

(deftest compare-by-color-null-test
  (testing "compare only by colors"
    (is (zero? (compare-by-color-null c1 c2)))
    (is (neg? (compare-by-color-null c3 c2)))
    (is (pos? (compare-by-color-null c2 c4)))))

(deftest compare-by-figure-normal-test
  (testing "puts :W before other cards"
    (is (pos? (compare-by-figure-normal c1 c2)))
    (is (pos? (compare-by-figure-normal c3 c2)))
    (is (zero? (compare-by-figure-normal c2 c4)))))

(deftest compare-by-figure-null-test
  (testing "puts :W before other cards"
    (is (neg? (compare-by-figure-null c1 c2)))
    (is (neg? (compare-by-figure-null c3 c2)))
    (is (zero? (compare-by-figure-null c2 c4)))))

(deftest compare-for-sort-test
  (testing "composes color and figure comparison"
    (let [compare-for-sort-normal
            (compare-for-sort compare-by-color-normal compare-by-figure-normal)
          compare-for-sort-null
            (compare-for-sort compare-by-color-null compare-by-figure-null)]
      (is (pos? (compare-for-sort-normal c1 c2)))
      (is (pos? (compare-for-sort-normal c3 c2)))
      (is (neg? (compare-for-sort-normal c2 c3)))
      (is (neg? (compare-for-sort-null c1 c2)))
      (is (neg? (compare-for-sort-null c3 c2)))
      (is (pos? (compare-for-sort-null c2 c3))))))

(deftest filter-cards-test
  (testing "filters by property"
    (is (same-elements?
         (filter-cards :color :kreuz cards)
         (list c1 c2)))
    (is (same-elements?
         (filter-cards :figure :W cards)
         (list c1 c3)))))

(deftest filter-color-test
  (testing "filters by color"
    (is (same-elements?
         (filter-color :kreuz cards)
         (list c1 c2)))))

(deftest filter-figure-test
  (testing "filters by figure"
    (is (same-elements?
         (filter-figure :W cards)
         (list c1 c3)))))

(deftest deck-test
  (testing "deck contains one piece of each card"
    (let [deck-set (set deck)]
      (is (== 32 (count deck-set)))
      (is (== 8 (count (filter #(= (:color %) :kreuz) deck-set))))
      (is (== 8 (count (filter #(= (:color %) :grun) deck-set))))
      (is (== 8 (count (filter #(= (:color %) :herz) deck-set))))
      (is (== 8 (count (filter #(= (:color %) :schell) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :7) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :8) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :9) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :10) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :W) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :D) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :K) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :A) deck-set)))))))

(deftest deal-cards-test
  (testing "deal cards correctly"
    (let [deal (deal-cards)]
      (is (== 10 (count (:front deal))))
      (is (== 10 (count (:middle deal))))
      (is (== 10 (count (:rear deal))))
      (is (== 2 (count (:skat deal)))))))

(deftest trump-null?-test
  (testing "no card is trump"
    (not (some trump-null? deck))))

(deftest trump-grand?-test
  (testing "4 card are trump"
    (let [trumps-grand (filter trump-grand? deck)]
      (is (== 4 (count trumps-grand)))
      (is (every? #(= :W (:figure %)) trumps-grand)))))

(deftest trump-color?-test
  (testing "4 card are trump"
    (let [trump-kreuz  (filter (partial trump-color? :kreuz) deck)
          trump-grun   (filter (partial trump-color? :grun) deck)
          trump-herz   (filter (partial trump-color? :herz) deck)
          trump-schell (filter (partial trump-color? :schell) deck)]
      (is (every? #(or (= :W (:figure %)) (= :kreuz (:color %))) trump-kreuz))
      (is (== 11 (count trump-kreuz)))
      (is (every? #(or (= :W (:figure %)) (= :grun (:color %))) trump-grun))
      (is (== 11 (count trump-grun)))
      (is (every? #(or (= :W (:figure %)) (= :herz (:color %))) trump-herz))
      (is (== 11 (count trump-grun)))
      (is (every? #(or (= :W (:figure %)) (= :schell (:color %))) trump-schell))
      (is (== 11 (count trump-grun))))))

(deftest calculate-points-test
  (let [worth-11 (Card. :kreuz :A)
        worth-10 (Card. :kreuz :10)
        worth-4  (Card. :kreuz :K)
        worth-3  (Card. :kreuz :D)
        worth-2  (Card. :kreuz :W)
        worth-30 [worth-11 worth-10 worth-4 worth-3 worth-2]]
    (testing "sum is calculated correctly"
      (is (== 30 (calculate-points worth-30))))))
