(ns skat.game
  (:require [clojure.set :as sets]
            [clojure.pprint :refer [pprint]]
            [skat]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses])
  (:import  [skat Deal
                  Player
                  PlayerKnowledge
                  PlayerSituation
                  Trick
                  GameDriver]))
(set! *warn-on-reflection* true)

;;; Configuration

(def suits "Possible games' suits"
  #{ :grand :kreuz :grun :herz :schell :null })
(def suits-ordinals "suits' ordinals"
  { :grand 6, :kreuz 5, :grun 4, :herz 3, :schell 2, :null 1 })

;;; Deal

(def players-in-deal "Players' positions in each deal"
  #{ :front :middle :rear })
(def player-in-next-deal "Player's position in next deal"
  { :front :rear, :middle :front, :rear :middle })

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
  (let [cards      [c1 c2 c3]
        comparator (cards/compare-for-sort
                    (cards/compare-by-color-display suit)
                    cards/compare-by-figure-normal)]
    (letfn [(trump? [c] (or (cards/property-matches? :figure :W   c)
                            (cards/property-matches? :color  suit c)))
            (highest-card [filtered]
              (first (reverse (sort comparator filtered))))
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
     :as deal }]
  (let [p1-situation (figure-situation config (knowledge p1) order)
        c1           (.play-1st-card ^skat.Player p1 p1-situation)
        p2-situation (figure-situation config (knowledge p2) order c1)
        c2           (.play-2nd-card ^skat.Player p2 p2-situation c1)
        p3-situation (figure-situation config (knowledge p3) order c1)
        c3           (.play-3rd-card ^skat.Player p3 p3-situation c1 c2)
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

(defn requires-hand [hand? modifier?]
  (if modifier? hand? true))

;;; Run game

(defn perform-auction "Performs auction" [driver bidders]
  (loop []
    (let [deal    (cards/deal-cards)
          bidding (.do-auction ^GameDriver driver bidders deal)]
      (if bidding
        { :deal deal, :bidding bidding }
        (recur)))))

(defn declare-game "Declare game suit, hand, schneider, schwarz and ouvert"
  [driver bidding]
  (letfn [(acceptable-game? [config] true)]
    (loop []
      (let [config (.declare-game ^GameDriver driver bidding)]
        (if (acceptable-game? config)
          config
          (recur))))))

(defn swap-skat "Swap skat with owned cards if game without hand declared"
  [hand? config deal winner winner-position]
  (if hand?
    deal
    (letfn [(cards [deal] (-> deal :knowledge winner :cards-owned))
            (replacements [skat owned] { skat owned, owned skat })
            (replacing [replacements] (fn [coll] (replace replacements coll)))
            (swap-cards [deal replacing]
              (update-in deal [:knowledge winner :cards-owned] replacing))]
      (let [skat-1 (-> deal :skat 0)
            card-1 (.skat-swapping ^Player winner config (cards deal) skat-1)
            swap-1 (swap-cards deal (replacing (replacements skat-1 card-1)))
            skat-2 (-> deal :skat 1)
            card-2 (.skat-swapping ^Player winner config (cards swap-1) skat-2)
            swap-2 (swap-cards deal (replacing (replacements skat-2 card-2)))]
        swap-2))))

(defn play-deal "Play whole 10-trick deal and reach conclusion"
  [{ :keys [] :as config }
   { :keys [:front :middle :rear] :as bidders }
   { front-cards :front, middle-cards :middle, rear-cards :rear, skat :skat }]
  (letfn [(out-of-cards? [pk] (some #(-> pk :cards-owned empty?)))
          (game-finished? [knowledge] out-of-cards? (vals knowledge))]
    (let [initial-knowledge { front  (PlayerKnowledge. front  [] front-cards)
                              middle (PlayerKnowledge. middle [] middle-cards)
                              rear   (PlayerKnowledge. rear   [] rear-cards) }
          initial-trick     (Trick. { :p1 front, :p2 middle, :p3 rear })
          initial-deal      (Deal. initial-knowledge initial-trick skat)]
      (loop [deal initial-deal]
        (if (-> deal :knowledge game-finished?)
          deal
          (recur (next-trick config deal)))))))

(defn start-game "Start game using passed driver" [driver]
  (let [initial-bidders (.create-players ^GameDriver driver)
        player-1        (:front  initial-bidders)
        player-2        (:middle initial-bidders)
        player-3        (:rear   initial-bidders)
        initial-points  { player-1 0, player-2 0, player-3 0 }]
    (pprint initial-points)))
