(ns skat.ai
  (:require [skat.cards :as cards]
            [skat.responses :as responses]
            [skat.game :as game]
            [skat.auction :as auction]))
(set! *warn-on-reflection* true)

(defn ai-player "Creates computer player"
  [id]
  (reify skat.game.Player
    (id [this] id)
    (play-1st-card [this situation] nil)
    (play-2nd-card [this situation c1] nil)
    (play-3rd-card [this situation c1 c2] nil)
    (place-bid [this cards last-bid] nil)
    (respond-to-bid [this cards bid] nil)
    (declare-suit [this cards final-bid] nil)))
