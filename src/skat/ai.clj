(ns skat.ai
  (:require [skat])
  (:import  [skat Player]))
(set! *warn-on-reflection* true)

; Implementation (when done) ideally should make use of knowlledge passed as
; PlayerSituation - PlayerKnowledge contains information on currently owned
; cards, cards that we used before and cards that each player took during the
; deal. With this information it should be often possible to deduce what cards
; other players have or have not. Together with information about played game
; type it should be possible to point some prefered allowed card to play in the
; trick.

(defn ai-player "Creates computer player" [id]
  (reify Player
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
         (declare-ouvert [this cards final-bid] nil)
         (skat-swapping [this config cards-owned skat-card] nil)))
