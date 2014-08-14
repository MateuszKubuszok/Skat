(ns skat.cards-test
  (:require [clojure.test :refer :all]
            [skat.cards :refer :all]))

(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :kreuz :K))
(def c3 (skat.cards.Card. :schell :W))
(def c4 (skat.cards.Card. :schell :K))

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

(compare-by-color-normal-test)
