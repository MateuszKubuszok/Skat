(ns skat.game
  (:require [clojure.set :as sets]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses]))
;(require '[clojure.pprint :refer :all])
(require '[clojure.tools.trace :refer :all])

;;; Configuration

(def types "Possible games' types"
  #{ :grand :kreuz :grun :herz :schell :null })
(def types-ordinals "Types' ordinals"
  { :grand 6 :kreuz 5 :grun 4 :herz 3 :schell 2 :null 1 })
(defrecord Configuration [type with-skat ouvert who-won?])

;;; Deal

(def players-in-deal "Players' positions in each deal"
  #{ :front :middle :rear })
(def player-in-next-deal "Player's position in next deal"
  { :front :rear, :middle :front, :rear :middle })
(defrecord Deal [knowledge turn skat])

;;; Knowledge

(defrecord PlayerKnowledge [self cards-played cards-owned])

;;; Players

(defrecord Player [id play-1st-card-fun play-2nd-card-fun play-3rd-card-fun])

;;; Situation

(defrecord PlayerSituation [config knowledge order cards-allowed])

;;; Turn

(defrecord Turn [order])

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
        (update-in [:cards-owned] update-cards-owned played-now)))))

;;; Situation update

(defn figure-situation [{:keys [:type] :as config}
                        {:keys [:self :cards-played :cards-owned] :as knowledge}
                        order
                        & [c1]]
  {:pre [(if c1 (cards/card? c1) true)]}
  (let [players-cards (cards-owned self)
        cards-allowed (set (if c1
                             ((responses/allowed-for type) c1 players-cards)
                             players-cards))]
    (PlayerSituation. config knowledge order cards-allowed)))

;;; Turn update

(defn next-turn-order [{:keys [p1 p2 p3] :as order} winner]
  {:pre [(contains? #{p1 p2 p3} winner)]}
  (cond
    (= winner p1) order
    (= winner p2) { :p1 p2, :p2 p3, :p3 p1 }
    (= winner p3) { :p1 p3, :p2 p1, :p3 p2 }))
(defn next-turn "Updates turn" [{:keys [order] :as turn} winner]
  (assoc turn :order (next-turn-order order winner)))
(defn play-turn "Plays turn"
  [config
   { { { :keys [p1 p2 p3] :as order } :order :as turn } :turn
     knowledge :knowledge
     :as deal}]
  (let [p1-situation (figure-situation config (knowledge p1) order)
        c1 ((:play-1st-card-fun p1) p1-situation)
        p2-situation (figure-situation config (knowledge p2) order c1)
        c2 ((:play-2nd-card-fun p2) p2-situation c1)
        p3-situation (figure-situation config (knowledge p3) order c1)
        c3 ((:play-3rd-card-fun p3) p3-situation c1 c2)
        played-now { p1 c1, p2 c2, p3 c3 } 
        winner (order ((:who-won? config) c1 c2 c3))]
    (-> deal
      (update-in [:knowledge] update-knowledge played-now)
      (update-in [:turn]      next-turn winner))))
