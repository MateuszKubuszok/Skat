(ns skat.responses_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.responses :refer :all])
  (:import  [skat Card]))

(def c1 (Card. :kreuz :W))
(def c2 (Card. :kreuz :K))
(def c3 (Card. :grun :W))
(def c4 (Card. :grun :K))
(def c5 (Card. :herz :W))
(def c6 (Card. :herz :K))
(def c7 (Card. :schell :W))
(def c8 (Card. :schell :K))
(def cards (list c1 c2 c3 c4 c5 c6 c7 c8))

(defn same-elements? [coll1 coll2] (= (set coll1) (set coll2)))

(deftest allowed-for-null-test
  (testing "Filters allowed responses in null game"
    (is (same-elements?
         (allowed-for-null c1 cards)
         (list c1 c2)))
    (is (same-elements?
         (allowed-for-null c2 cards)
         (list c1 c2)))
    (is (same-elements?
         (allowed-for-null c3 cards)
         (list c3 c4)))
    (is (same-elements?
         (allowed-for-null c4 cards)
         (list c3 c4)))))

(deftest allowed-for-grand-test
  (testing "Filters allowed responses in grand game"
    (is (same-elements?
         (allowed-for-grand c1 cards)
         (list c1 c3 c5 c7)))
    (is (same-elements?
         (allowed-for-grand c2 cards)
         (list c2)))
    (is (same-elements?
         (allowed-for-grand c3 cards)
         (list c1 c3 c5 c7)))
    (is (same-elements?
         (allowed-for-grand c4 cards)
         (list c4)))))

(deftest allowed-for-kreuz-test
  (testing "Filters allowed responses in kreuz game"
    (is (same-elements?
         (allowed-for-kreuz c1 cards)
         (list c1 c2 c3 c5 c7)))
    (is (same-elements?
         (allowed-for-kreuz c2 cards)
         (list c1 c2 c3 c5 c7)))
    (is (same-elements?
         (allowed-for-kreuz c3 cards)
         (list c1 c2 c3 c5 c7)))
    (is (same-elements?
         (allowed-for-kreuz c4 cards)
         (list c4)))))

(deftest allowed-for-grun-test
  (testing "Filters allowed responses in grun game"
    (is (same-elements?
         (allowed-for-grun c1 cards)
         (list c1 c3 c4 c5 c7)))
    (is (same-elements?
         (allowed-for-grun c2 cards)
         (list c2)))
    (is (same-elements?
         (allowed-for-grun c3 cards)
         (list c1 c3 c4 c5 c7)))
    (is (same-elements?
         (allowed-for-grun c4 cards)
         (list c1 c3 c4 c5 c7)))))

(deftest allowed-for-herz-test
  (testing "Filters allowed responses in herz game"
    (is (same-elements?
         (allowed-for-herz c3 cards)
         (list c1 c3 c5 c6 c7)))
    (is (same-elements?
         (allowed-for-herz c4 cards)
         (list c4)))
    (is (same-elements?
         (allowed-for-herz c5 cards)
         (list c1 c3 c5 c6 c7)))
    (is (same-elements?
         (allowed-for-herz c6 cards)
         (list c1 c3 c5 c6 c7)))))
