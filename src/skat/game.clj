(ns skat.game
  (:require [clojure.set :as sets]
            ;[clojure.tools.trace :refer :all]
            ;[skat.log :as log]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses]))

;;; Configuration

(def suits "Possible games' suits"
  #{ :grand :kreuz :grun :herz :schell :null })
(def suits-ordinals "suits' ordinals"
  { :grand 6, :kreuz 5, :grun 4, :herz 3, :schell 2, :null 1 })
(defrecord Configuration [declarer
                          suit
                          hand?
                          ouvert?
                          announced-schneider?
                          announced-schwarz?
                          declared-bid])

;;; Deal

(def players-in-deal "Players' positions in each deal"
  #{ :front :middle :rear })
(def player-in-next-deal "Player's position in next deal"
  { :front :rear, :middle :front, :rear :middle })

(defrecord Deal [knowledge trick skat])

;;; Knowledge

(defrecord PlayerKnowledge [self cards-played cards-owned])

;;; Players

(defprotocol Player
  (id [this])
  (play-1st-card [this situation])
  (play-2nd-card [this situation c1])
  (play-3rd-card [this situation c1 c2])
  (place-bid [this cards last-bid])
  (respond-to-bid [this cards bid])
  (declare-suit [this cards final-bid]))

;;; Situation

(defrecord PlayerSituation [config knowledge order cards-allowed])

;;; Trick

(defrecord Trick [order])

;;; Knowledge update

(defn update-cards "Updates current players' cards" [players-cards fun]
  (helpers/replace-by-key players-cards fun))

(defn update-cards-played "Updates played cards coll" [cards-played played-now]
  (letfn [(add-card [player] (conj (cards-played player) (played-now player)))]
    (update-cards cards-played add-card)))

(defn update-cards-owned "Updates owned cards coll" [cards-owned played-now]
  (letfn [(used-now? [player card] (= (played-now player) card))
          (remove-card [player] (set
            (remove (partial used-now? player) (cards-owned player))))]
    (update-cards cards-owned remove-card)))

(defn update-knowledge "Updates all players' knowledge" [knowledge played-now]
  {:pre [(sets/subset? (set (keys knowledge)) (set (keys played-now)))]}
  (helpers/update-all
    knowledge
    (fn [player-knowledge]
      (-> player-knowledge
        (update-in [:cards-played] update-cards-played played-now)
        (update-in [:cards-owned]  update-cards-owned played-now)))))

;;; Situation update

(defn figure-situation [{ :keys [:suit] :as config }
                        { :keys [:self :cards-owned] :as knowledge }
                        order
                        & [c1]]
  {:pre [(if c1 (cards/card? c1) true)]}
  (let [players-cards (cards-owned self)
        cards-allowed (set (if c1
                             ((responses/allowed-for suit) c1 players-cards)
                             players-cards))]
    (PlayerSituation. config knowledge order cards-allowed)))

;;; Trick winner

(defn highest-trump-wins "Determines winner for normal game" [suit c1 c2 c3]
  { :pre  [(suits suit) (cards/card? c1) (cards/card? c2) (cards/card? c3)] }
  (let [cards [c1 c2 c3]]
    (letfn [(trump? [c] (or (cards/property-matches? :figure :W   c)
                            (cards/property-matches? :color  suit c)))
            (highest-card [filtered]
              (first
                (reverse
                  (sort (cards/compare-by-color-display suit) filtered))))
            (of-color? [c] cards/property-matches? :color (:color c1) c)
            (card-to-player [c] ({ c1 :p1, c2 :p2, c3 :p3 } c))]
      (card-to-player (highest-card (filter (if (some trump? cards)
                                              trump?
                                              of-color?)
                                            cards))))))
(def trick-winning-grand "Determines winner for grand game"
  (partial highest-trump-wins :grand))
(def trick-winning-kreuz "Determines winner for kreuz game"
  (partial highest-trump-wins :kreuz))
(def trick-winning-grun "Determines winner for grun game"
  (partial highest-trump-wins :grun))
(def trick-winning-herz "Determines winner for herz game"
  (partial highest-trump-wins :herz))
(def trick-winning-schell "Determines winner for schell game"
  (partial highest-trump-wins :schell))
(defn trick-winning-null "Determines winner for null game" [c1 c2 c3]
  { :pre  [(cards/card? c1) (cards/card? c2) (cards/card? c3)] }
  (let [cards [c1 c2 c3]]
    (letfn [(highest-card [filtered]
              (first (reverse (sort cards/compare-by-color-null filtered))))
            (of-color? [c] cards/property-matches? :color (:color c1) c)
            (card-to-player [c] ({ c1 :p1, c2 :p2, c3 :p3 } c))]
      (card-to-player (highest-card (filter of-color? cards))))))

(def trick-winning
  { :grand  trick-winning-grand,
    :kreuz  trick-winning-kreuz,
    :grun   trick-winning-grun,
    :herz   trick-winning-herz,
    :schell trick-winning-schell,
    :null   trick-winning-null })

;;; Trick update

(defn next-trick-order [{:keys [p1 p2 p3] :as order} winner]
  {:pre [(contains? #{p1 p2 p3} winner)]}
  (cond
    (= winner p1) order
    (= winner p2) { :p1 p2, :p2 p3, :p3 p1 }
    (= winner p3) { :p1 p3, :p2 p1, :p3 p2 }))

(defn next-trick "Updates trick" [{:keys [order] :as trick} winner]
  (assoc trick :order (next-trick-order order winner)))

(defn play-trick "Plays trick"
  [{ :keys [suit] :as config }
   { { { :keys [p1 p2 p3] :as order } :order :as trick } :trick
     knowledge :knowledge
     :as deal}]
  (let [p1-situation (figure-situation config (knowledge p1) order)
        c1           (.play-1st-card p1 p1-situation)
        p2-situation (figure-situation config (knowledge p2) order c1)
        c2           (.play-2nd-card p2 p2-situation c1)
        p3-situation (figure-situation config (knowledge p3) order c1)
        c3           (.play-3rd-card p3 p3-situation c1 c2)
        played-now   { p1 c1, p2 c2, p3 c3 }
        winner       (order ((trick-winning suit) c1 c2 c3))]
    (-> deal
      (update-in [:knowledge] update-knowledge played-now)
      (update-in [:trick]     next-trick winner))))

;;; Win conditions

(defn enough-points? "Is minimal required number of points reached" [cards]
  (> (cards/calculate-points cards) 60))

(defn schneider? "Is enough points for Schneider reached" [cards]
  (>= (cards/calculate-points cards) 90))

(defn schwarz? "Are all cards taken" [cards]
  (== (count cards) (count cards/deck)))
