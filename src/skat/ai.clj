(ns skat.ai
  (:require [skat]
            [skat.log :as log]
            [skat.auction :as auction])
  (:import  [skat Player]))
(set! *warn-on-reflection* true)

; Implementation (when done) ideally should make use of knowlledge passed as
; PlayerSituation - PlayerKnowledge contains information on currently owned
; cards, cards that we used before and cards that each player took during the
; deal. With this information it should be often possible to deduce what cards
; other players have or have not. Together with information about played game
; type it should be possible to point some prefered allowed card to play in the
; trick.

(def max-bid-value "Max bid value for " 48)

(defn next-bid-value "Next bid value" [bid]
  (letfn [(greater-than-bid [coll] (filter #(> % bid) coll))]
    (-> auction/possible-game-values
        greater-than-bid
        vec
        sort
        first
        (log/pass :ai "next bid"))))

(defn ai-player "Creates computer player" [id]
  (reify Player
         (id [this] id)
         (play-1st-card [this { :keys [cards-allowed] :as situation }]
           (first cards-allowed))
         (play-2nd-card [this { :keys [cards-allowed] :as situation } c1]
           (first cards-allowed))
         (play-3rd-card [this { :keys [cards-allowed] :as situation } c1 c2]
            (first cards-allowed))
         (place-bid [this cards last-bid]
           (if (> max-bid-value last-bid)
             (next-bid-value last-bid)
             auction/passed-game-value))
         (respond-to-bid [this cards bid] (> max-bid-value bid))
         (declare-suit [this cards final-bid] :grand)
         (declare-hand [this cards final-bid] false)
         (declare-schneider [this cards final-bid] false)
         (declare-schwarz [this cards final-bid] false)
         (declare-ouvert [this cards final-bid] false)
         (skat-swapping [this config skat-owned cards-owned skat-card]
           (first cards-owned))))
