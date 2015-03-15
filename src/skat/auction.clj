(ns skat.auction
  (:require [clojure.set :as sets]
            ;[clojure.tools.trace :refer :all]
            [skat.log :as log]
            [skat.cards :as cards]
            [skat.helpers :as helpers]))

;;; Peaks' values calculation helpers

(defn with-peaks-value-calculator [pattern matched]
  (loop [value 0
         s1    pattern
         s2    matched]
    (if (and (first s1) (first s2) (= (first s1) (first s2)))
      (recur (inc value) (rest s1) (rest s2))
      value)))
(defn without-peaks-value-calculator [pattern matched]
  (loop [value 0
         sub   pattern]
    (if (first sub)
      (let [sub-value (with-peaks-value-calculator sub matched)]
        (if (pos? sub-value)
          value
          (recur (inc value) (rest sub))))
      (count pattern))))
(defn peaks-value-calculator [pattern matched]
  (let [with-value    (with-peaks-value-calculator pattern matched)
        without-value (without-peaks-value-calculator pattern matched)]
    (if (pos? with-value) with-value without-value)))

;;; Peaks' values for normal games

(defn peaks-for-trumphs "Number of peaks" [type trumphs cards]
  (let [sorting          (cards/compare-for-sort
                          (cards/compare-by-color-display type)
                          cards/compare-by-figure-normal)
        invert-sorting   #(- (sorting %1 %2))
        sorted-trumphs   (sort invert-sorting trumphs)
        considered-cards (filter #(helpers/coll-contains? trumphs %) cards)
        sorted-cards     (sort invert-sorting cards)]
    (peaks-value-calculator sorted-trumphs sorted-cards)))
(defn peaks-grand "Number of peaks in grand game" [cards]
  (let [trumphs (filter cards/trumph-grand? cards/deck)]
    (peaks-for-trumphs :grand trumphs cards)))
(defn peaks-color "Number of peaks in color game" [color cards]
  (let [trumphs (filter (partial cards/trumph-color? color) cards/deck)]
    (peaks-for-trumphs color trumphs cards)))
(defn peaks-kreuz "Number of peaks in kreuz game" [cards]
  (peaks-color :kreuz cards))
(defn peaks-grun "Number of peaks in grun game" [cards]
  (peaks-color :grun cards))
(defn peaks-herz "Number of peaks in herz game" [cards]
  (peaks-color :herz cards))
(defn peaks-schell "Number of peaks in schell game" [cards]
  (peaks-color :schell cards))
(def peaks "Peaks value calculation functions"
  { :grand  peaks-grand,
    :kreuz  peaks-kreuz,
    :grun   peaks-grun,
    :herz   peaks-herz,
    :schell peaks-schell })

;;; Normal games values

(def game-type-coefficients
  { :grand 24, :kreuz 12, :grun 11, :herz 10, :schell 9 })

(defn normal-game-value [cards type with-skat? ouvert?]
  (let [game-type-coefficient (game-type-coefficients type)
        peaks-type            (peaks type)
        peaks-value           (peaks-type cards)
        for-skat              (if with-skat? 1 0)
        for-ouvert            (if ouvert? 1 0)]
    (* game-type-coefficient (+ 1 peaks-value for-skat for-ouvert))))
