(ns skat.model
  (:require [skat.helpers :as helpers]))

;;; Colors

(def colors "Possible cards' colors"
  #{ :kreuz :grun :herz :schell })
(def color-ordinal "Colors' ordinals"
  { :kreuz 4 :grun 3 :herz 2 :schell 1 })

;;; Values

(def values "Possible cards' values"
  #{ :7 :8 :9 :10 :W :Q :K :A })
(def value-ordinal-normal "Values' ordnals in normal games"
  { :7 0 :8 1 :9 3 :Q 4 :K 5 :10 6 :A 7 :W 8 })
(def value-ordinal-null "Values' ordnals in null games"
  { :7 0 :8 1 :9 3 :10 7 :W 4 :Q 5 :K 6 :A 8 })
(def value-points "Points for each card value"
  { :7 0 :8 0 :9 0 :W 2 :Q 3 :K 4 :10 10 :A 11 })

;;; Cards

(defrecord Card [color value])
(defn property-matches? "Whether card's property as given" [n v c]
  (identical? (n c) v))
(defn compare-by-color-normal "Order cards by color in normal game"
  [c1 c2]
  (letfn [(W? [c]
            (property-matches? :value :W c))
          (W-exceptonal-ordinal [c]
            (if (W? c)
              (inc (:kreuz color-ordinal))
              ((:color c) color-ordinal)))]
    (let [o1 (W-exceptonal-ordinal c1)
          o2 (W-exceptonal-ordinal c2)]
      (compare o1 o2))))
(defn compare-by-color-null "Order cards by color in null game"
  [c1 c2]
  (letfn [(ordinal [c]
            ((:color c) color-ordinal))]
    (let [o1 (ordinal c1)
          o2 (ordinal c2)]
      (compare o1 o2))))
(defn compare-by-value [fun c1 c2]
  (let [o1 ((:value c1) fun)
        o2 ((:value c2) fun)]
    (compare o1 o2)))
(def compare-by-value-normal "Order cards by value in normal game"
  (partial compare-by-value value-ordinal-normal))
(def compare-by-value-null "Order cards by value in null game"
  (partial compare-by-value value-ordinal-null))
(defn compare-for-sort "Compose comparator of cards for values and colors"
  [compare-by-value compare-by-color]
  (fn [c1 c2]
    (if (identical? (:color c1) (:color c2))
      (compare-by-value c1 c2)
      (compare-by-color c1 c2))))
(defn filter-cards [property-name property-value cards]
  (filter (partial property-matches? property-name property-value) cards))
(def filter-color "Filter cards by color"
  (partial filter-cards :color))
(def filter-value "Filter cards by value"
  (partial filter-cards :value))
(def deck "Complete deck of cards (unsorted)"
  (letfn [(cards-of-color [c]
            (let [cards-of-value #(Card. c %)]
              (helpers/list-from cards-of-value values)))
          (cards-grouped-by-color []
            (helpers/list-from cards-of-color colors))]
    (flatten (cards-grouped-by-color))))

;;; Configuration

(def types "Possible games' types"
  (helpers/append colors '( :grand :null )))
(def types-ordinals "Types' ordinals"
  { :grand 6 :kreuz 5 :grun 4 :herz 3 :schell 2 :null 1 })
(defrecord Configuration [type with-skat ouvert])
