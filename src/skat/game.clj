(ns skat.game
  (:require [skat.helpers :as helpers]))

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

;;; Knowledge

(defrecord PlayerKnowledge [self cards-played cards-owned])

;;; Players

(defrecord Player [id play-1st-card-fun play-2nd-card-fun play-3rd-card-fun])

;;; Turn

(defrecord Turn [players knowledge order])

;;; Knowledge update

(defn update-cards [players-cards fun]
  (let [players (keys players-cards)]
    (zipmap players (map fun players))))

(defn update-cards-played [cards-played played-now]
  (letfn [(add-card [player] (conj (cards-played player) (played-now player)))]
    (update-cards cards-played add-card)))

(defn update-cards-owned [cards-owned played-now]
  (letfn [(used-now? [player card] (= (played-now player) card))
          (remove-card [player] (set
            (remove (partial used-now? player) (cards-owned player))))]
    (update-cards cards-owned remove-card)))

(defn update-knowledge [knowledge played-now]
  (helpers/update-all
    knowledge
    (fn [player-knowledge] (-> player-knowledge
      (update-in [:cards-played] update-cards-played played-now)
      (update-in [:cards-owned] update-cards-owned played-now)))))

;;; Turn update

(defn next-turn-order [order winner]
  (let [{:keys [p1 p2 p3]} order]
    (if (= p1 winner) order
      (if (= p2 winner) { :p1 p2 :p2 p3 :p3 p1 }
        (if (= p3 winner) { :p1 p3 :p2 p1 :p3 p2 })))))
(defn next-turn [turn winner]
  (let [{:keys [order]} turn]
    (assoc turn :order (next-turn-order order winner))))
(defn play-turn [config {:keys [turn knowledge]}]
  (let [{:keys [players order]} turn
        {:keys [p1 p2 p3]} order
        c1 ((:play-1st-card-fun p1) config (knowledge p1))
        c2 ((:play-2nd-card-fun p2) config (knowledge p2) c1)
        c3 ((:play-3rd-card-fun p3) config (knowledge p3) c1 c2)
        played-now { :p1 c1 :p2 c2 :p3 c3 }
        winner ((:who-won? config) c1 c2 c3)]
    { :turn (next-turn turn winner)
      :knowledge (update-knowledge knowledge turn played-now)}))
