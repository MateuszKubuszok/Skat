(ns skat.cli_test
  (:require [clojure.test :refer :all]
            [skat]
            [skat.cards :as cards]
            [skat.cli :refer :all]
            )
  (:import  [skat Card Player]))

(def c1 (Card. :kreuz  :A))
(def c2 (Card. :grun   :10))
(def c3 (Card. :herz   :K))
(def c4 (Card. :schell :D))
(def cards (list c1 c2 c3 c4))

(deftest card-str-test
  (testing "each card has string of the same length"
    (is (every? #(== 6 %) (map #(count (card-str %)) cards/deck))))
  (testing "cards are mapped to expected strings"
    (is (= "[♣  A]" (card-str c1)))
    (is (= "[♠ 10]" (card-str c2)))
    (is (= "[♥  K]" (card-str c3)))
    (is (= "[♦  D]" (card-str c4)))))

(deftest coll-str-test
  (testing "collection maps strings as expected"
    (is (= "0 [♣  A]  1 [♠ 10]  2 [♥  K]  3 [♦  D]"
           (coll-str cards str card-str "  ")))))

(deftest pid-test
  (testing "obtains Player id correctly"
    (let [p-id   "expected-pid"
          player (reify Player (id [this] p-id))]
      (is (= p-id (pid player))))))
