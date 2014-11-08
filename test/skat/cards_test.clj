(ns skat.cards_test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [clojure.tools.trace :refer :all]
            [skat.log :as log]
            [skat.cards :refer :all]))

(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :kreuz :K))
(def c3 (skat.cards.Card. :schell :W))
(def c4 (skat.cards.Card. :schell :K))
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
      (is (== 4 (count (filter #(= (:figure %) :Q) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :K) deck-set))))
      (is (== 4 (count (filter #(= (:figure %) :A) deck-set)))))))

(deftest deal-cards-test
  (testing "deal cards correctly"
    (let [deal (deal-cards)]
      (is (== 10 (count (:front deal))))
      (is (== 10 (count (:middle deal))))
      (is (== 10 (count (:rear deal))))
      (is (== 2 (count (:skat deal)))))))
