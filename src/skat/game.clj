(ns skat.game)

;;; Configuration

(def types "Possible games' types"
  #{ :grand :kreuz :grun :herz :schell :null })
(def types-ordinals "Types' ordinals"
  { :grand 6 :kreuz 5 :grun 4 :herz 3 :schell 2 :null 1 })
(defrecord Configuration [type with-skat ouvert])

;; Deal

(def players-in-deal "Players' positions in each deal"
  #{ :front :middle :rear })
(def player-in-next-deal "Player's position in next deal"
  { :front :rear, :middle :front, :rear :middle })
