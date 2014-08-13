(ns skat.responses
  (:require [skat.helpers :as helpers]
            [skat.cards :as cards]))

;;; Responses

(defn allowed-for-null "Filters allowed responses in null games"
  [c cards]
  (let [matching-color (cards/filter-color (:color c) cards)]
    (if (empty? matching-color) cards matching-color)))
(defn allowed-for-grand "Filters allowed responses in grand games"
  [c cards]
  (if (cards/property-matches? :figure :W c)
    (cards/filter-figure :W cards)
    (allowed-for-null c cards)))
(defn allowed-for-color [color c cards]
  (let [trump (helpers/append
                (cards/filter-color color cards)
                (cards/filter-figure :W cards))]
    (if (empty? trump)
      (allowed-for-null c cards)
      trump)))
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
