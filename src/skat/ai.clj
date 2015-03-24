(ns skat.ai
  (:require [skat])
  (:import  [skat Player]))
(set! *warn-on-reflection* true)

(defn ai-player "Creates computer player"
  [id]
  (reify skat.Player
    (id [this] id)
    (play-1st-card [this situation] nil)
    (play-2nd-card [this situation c1] nil)
    (play-3rd-card [this situation c1 c2] nil)
    (place-bid [this cards last-bid] nil)
    (respond-to-bid [this cards bid] nil)
    (declare-suit [this cards final-bid] nil)
    (declare-hand [this cards final-bid] nil)
    (declare-schneider [this cards final-bid] nil)
    (declare-schwarz [this cards final-bid] nil)
    (declare-ouvert [this cards final-bid] nil)))
