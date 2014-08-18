(ns skat.responses
  (:require [skat.helpers :as helpers]
            [skat.cards :as cards]))

;;; General patterns

(defn filter-if-not-empty [fun coll]
  (let [filtered (filter fun coll)]
    (if (empty? filtered) coll filtered)))
(defn allow-trumph-then-color-then-everything [trumph? c coll]
  (letfn [(not-trumph? [c2]
            (and (not (trumph? c2))
                 (cards/property-matches? :color (:color c) c2)))]
    (filter-if-not-empty (if (trumph? c) trumph? not-trumph?) coll)))

(defn W? [c]
  (cards/property-matches? :figure :W c))
(defn color? [color c]
  (cards/property-matches? :color color c))

;;; Responses

(defn allowed-for-null "Filters allowed responses in null games" [c cards]
  (letfn [(trumph? [c] false)]
    (allow-trumph-then-color-then-everything trumph? c cards)))
(defn allowed-for-grand "Filters allowed responses in grand games" [c cards]
  (letfn [(trumph? [c] (W? c))]
    (allow-trumph-then-color-then-everything trumph? c cards)))
(defn allowed-for-color [color c cards]
  (letfn [(trumph? [c] (or (W? c) (color? color c)))]
     (allow-trumph-then-color-then-everything trumph? c cards)))
(def allowed-for-kreuz "Filters allowed responses in kreuz games"
  (partial allowed-for-color :kreuz))
(def allowed-for-grun "Filters allowed responses in grun games"
  (partial allowed-for-color :grun))
(def allowed-for-herz "Filters allowed responses in herz games"
  (partial allowed-for-color :herz))
(def allowed-for-schell "Filters allowed responses in schell games"
  (partial allowed-for-color :schell))
(def allowed-for "Allowed responses for each game"
  { :grand allowed-for-grand
    :kreuz allowed-for-kreuz
    :grun allowed-for-grun
    :herz allowed-for-herz
    :schell allowed-for-schell
    :null allowed-for-null })
