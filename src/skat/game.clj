(ns skat.game)

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

(defrecord Knowledge [cards-played cards-owned])
(defn add-knowledge [knowledge turn c1 c2 c3] knowledge); TODO: calc knowledge

;;; Players

(defrecord Player [id play-1st-card-fun play-2nd-card-fun play-3rd-card-fun])

;;; Turn

(defrecord Turn [players knowledge order])
(defn next-players [order winner]
  (let [{:keys [p1 p2 p3]} order]
    (if (= p1 winner) order
      (if (= p2 winner) { :p1 p2 :p2 p3 :p3 p1 }
        (if (= p3 winner) { :p1 p3 :p2 p1 :p3 p2 })))))
(defn next-turn [turn winner]
  (let [{:keys [order]} turn]
    (assoc turn :order (next-players order winner))))
(defn play-turn [config {:keys [turn knowledge]}]
  (let [{:keys [players order]} turn
        {:keys [p1 p2 p3]} order
        c1 ((:play-1st-card-fun p1) config (knowledge p1))
        c2 ((:play-2nd-card-fun p2) config (knowledge p2) c1)
        c3 ((:play-3rd-card-fun p3) config (knowledge p3) c1 c2)
        winner ((:who-won? config) c1 c2 c3)]
    { :turn (next-turn turn winner)
      :knowledge (add-knowledge knowledge turn c1 c2 c3)}))
