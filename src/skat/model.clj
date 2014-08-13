(ns skat.model
  (:require [skat.helpers :as helpers]))

;;; Colors

(def colors "Possible cards' colors"
  #{ :kreuz :grun :herz :schell })
(def color-ordinal "Colors' ordinals"
  { :kreuz 4 :grun 3 :herz 2 :schell 1 })

;;; Figures

(def figures "Possible cards' figures"
  #{ :7 :8 :9 :10 :W :Q :K :A })
(def figure-ordinal-normal "Figures' ordnals in normal games"
  { :7 0 :8 1 :9 3 :Q 4 :K 5 :10 6 :A 7 :W 8 })
(def figure-ordinal-null "Figures' ordnals in null games"
  { :7 0 :8 1 :9 3 :10 7 :W 4 :Q 5 :K 6 :A 8 })
(def figure-values "Values for each card figure"
  { :7 0 :8 0 :9 0 :W 2 :Q 3 :K 4 :10 10 :A 11 })

;;; Cards

(defrecord Card [color figure])
(defn property-matches? "Whether card's property as given" [n v c]
  (identical? (n c) v))
(defn compare-by-color-normal "Order cards by color in normal game"
  [c1 c2]
  (letfn [(W? [c]
            (property-matches? :figure :W c))
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
(defn compare-by-figure [fun c1 c2]
  (let [o1 ((:figure c1) fun)
        o2 ((:figure c2) fun)]
    (compare o1 o2)))
(def compare-by-figure-normal "Order cards by figure in normal game"
  (partial compare-by-figure figure-ordinal-normal))
(def compare-by-figure-null "Order cards by figure in null game"
  (partial compare-by-figure figure-ordinal-null))
(defn compare-for-sort "Compose comparator of cards for figures and colors"
  [compare-by-color compare-by-figure]
  (fn [c1 c2]
    (if (identical? (:color c1) (:color c2))
      (compare-by-figure c1 c2)
      (compare-by-color c1 c2))))
(defn filter-cards [property-name property-figure cards]
  (filter (partial property-matches? property-name property-figure) cards))
(def filter-color "Filter cards by color"
  (partial filter-cards :color))
(def filter-figure "Filter cards by figure"
  (partial filter-cards :figure))
(def deck "Complete deck of cards (unsorted)"
  (letfn [(cards-of-color [c]
            (let [cards-of-figure #(Card. c %)]
              (helpers/list-from cards-of-figure figures)))
          (cards-grouped-by-color []
            (helpers/list-from cards-of-color colors))]
    (flatten (cards-grouped-by-color))))

;;; Configuration

(def types "Possible games' types"
  (helpers/append colors '( :grand :null )))
(def types-ordinals "Types' ordinals"
  { :grand 6 :kreuz 5 :grun 4 :herz 3 :schell 2 :null 1 })
(defrecord Configuration [type with-skat ouvert])

;;; Game

(defn deal-cards [] "Returns dealt cards"
  (let [shuffled-cards (shuffle deck)
        front (take 10 shuffled-cards)
        middle (take 10 (drop 10 shuffled-cards))
        rear (take 10 (drop 20 shuffled-cards))
        skat (take 2 (drop 30 shuffled-cards))]
    { :front front :middle middle :rear rear :skat skat }))
