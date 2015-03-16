(ns skat.responses
  (:require ;[clojure.tools.trace :refer :all]
            [skat.helpers :as helpers]
            [skat.cards :as cards]))

;;; General patterns

(defn filter-if-not-empty "Filters collection if it won't make it empty"
  [fun coll]
  (let [filtered (filter fun coll)]
    (if (empty? filtered) coll filtered)))

(defn allow-trump-then-color-then-everything "Filters only allowed cards"
  [trump? c coll]
  {:pre [(cards/card? c)]}
  (letfn [(matching-non-trump? [c2]
            {:pre [(cards/card? c2)]}
            (and (not (trump? c2))
                 (cards/property-matches? :color (:color c) c2)))]
    (filter-if-not-empty (if (trump? c) trump? matching-non-trump?) coll)))

;;; Responses

(defn allowed-for-null "Filters allowed responses in null games" [c cards]
  (allow-trump-then-color-then-everything cards/trump-null? c cards))
(defn allowed-for-grand "Filters allowed responses in grand games" [c cards]
  (allow-trump-then-color-then-everything cards/trump-grand? c cards))
(defn allowed-for-color [color c cards]
  (let [trump-color? (partial cards/trump-color? color)]
    (allow-trump-then-color-then-everything trump-color? c cards)))
(def allowed-for-kreuz "Filters allowed responses in kreuz games"
  (partial allowed-for-color :kreuz))
(def allowed-for-grun "Filters allowed responses in grun games"
  (partial allowed-for-color :grun))
(def allowed-for-herz "Filters allowed responses in herz games"
  (partial allowed-for-color :herz))
(def allowed-for-schell "Filters allowed responses in schell games"
  (partial allowed-for-color :schell))
(def allowed-for "Allowed responses for each game"
  { :grand  allowed-for-grand,
    :kreuz  allowed-for-kreuz,
    :grun   allowed-for-grun,
    :herz   allowed-for-herz,
    :schell allowed-for-schell,
    :null   allowed-for-null })
