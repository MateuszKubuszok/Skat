(ns skat.auction
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as sets]
            ;[clojure.tools.trace :refer :all]
            [skat.log :as log]
            [skat.cards :as cards]
            [skat.helpers :as helpers]))

;;; Peaks' values calculation helpers

(defn with-matadors-value-calculator [pattern matched]
  (loop [value 0
         s1    pattern
         s2    matched]
    (if (and (first s1) (first s2) (= (first s1) (first s2)))
      (recur (inc value) (rest s1) (rest s2))
      value)))
(defn without-matadors-value-calculator [pattern matched]
  (loop [value 0
         sub   pattern]
    (if (first sub)
      (let [sub-value (with-matadors-value-calculator sub matched)]
        (if (pos? sub-value)
          value
          (recur (inc value) (rest sub))))
      (count pattern))))
(defn matadors-value-calculator [pattern matched]
  (let [with-value    (with-matadors-value-calculator pattern matched)
        without-value (without-matadors-value-calculator pattern matched)]
    (if (pos? with-value) with-value without-value)))

;;; Peaks' values for normal games

(defn matadors-for-trumps "Count matadors by trumps" [suit trumps cards]
  (let [sorting          (cards/compare-for-sort
                          (cards/compare-by-color-display suit)
                          cards/compare-by-figure-normal)
        invert-sorting   #(- (sorting %1 %2))
        sorted-trumps    (sort invert-sorting trumps)
        considered-cards (filter #(helpers/coll-contains? trumps %) cards)
        sorted-cards     (sort invert-sorting cards)]
    (matadors-value-calculator sorted-trumps sorted-cards)))
(defn matadors-grand "Count matadors in grand game" [cards]
  (let [trumps (filter cards/trump-grand? cards/deck)]
    (matadors-for-trumps :grand trumps cards)))
(defn matadors-color "Count matadors in color game" [color cards]
  (let [trumps (filter (partial cards/trump-color? color) cards/deck)]
    (matadors-for-trumps color trumps cards)))
(defn matadors-kreuz "Count matadors in kreuz game" [cards]
  (matadors-color :kreuz cards))
(defn matadors-grun "Count matadors in grun game" [cards]
  (matadors-color :grun cards))
(defn matadors-herz "Count matadors in herz game" [cards]
  (matadors-color :herz cards))
(defn matadors-schell "Count matadors in schell game" [cards]
  (matadors-color :schell cards))
(def matadors "Peaks' values calculation functions"
  { :grand  matadors-grand,
    :kreuz  matadors-kreuz,
    :grun   matadors-grun,
    :herz   matadors-herz,
    :schell matadors-schell })

;;; Normal games values

(def suit-base-value
  { :grand 24, :kreuz 12, :grun 11, :herz 10, :schell 9 })

(defn normal-game-value "Calculate normal game value" [cards suit hand? ouvert?]
  (let [base-value   (suit-base-value suit)
        for-game     1
        for-matadors ((matadors suit) cards)
        for-hand     (if hand? 1 0)
        for-ouvert   (if ouvert? 1 0)]
    (* base-value (+ for-game for-matadors for-hand for-ouvert))))

;;; Null game values

(def null-game-values
  { [true  false] 23,
    [false false] 35,
    [true  true ] 46,
    [false true ] 59 })

(defn null-game-value "Calculate null game value" [hand? ouvert?]
  (null-game-values [hand? ouvert?]))

;;; Game values

(defn game-value "Calculate overall game value" [cards suit hand? ouvert?]
  (match [suit]
    [:null] (null-game-value hand? ouvert?)
    [_]     (normal-game-value cards suit hand? ouvert?)))

;;; Possible game values

(def min-normal-game-level 2)
(def max-normal-game-level
  ;            for playing | matadors | hand | ouvert
  { :grand  (+ 1             11         1      1),
    :kreuz  (+ 1             7          1      1),
    :grun   (+ 1             7          1      1),
    :herz   (+ 1             7          1      1),
    :schell (+ 1             7          1      1) })

(def possible-game-values
  (letfn [(vals-for [suit]
            (map #(* (suit-base-value suit) %)
                 (range min-normal-game-level
                        (inc (max-normal-game-level suit)))))]
    (set (concat (vals null-game-values)
                 (vals-for :grand)
                 (vals-for :kreuz)
                 (vals-for :grun)
                 (vals-for :herz)
                 (vals-for :schell)))))
