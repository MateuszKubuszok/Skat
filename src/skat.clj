(ns skat)
(set! *warn-on-reflection* true)

;;; Cards

(defrecord Card [color figure])

;;; Bidding

(defrecord Bidders [front middle rear])

(defrecord Bidding [winner cards bid])

;;; Configuration

(defrecord Configuration [declarer
                          suit
                          hand?
                          ouvert?
                          announced-schneider?
                          announced-schwarz?
                          declared-bid])

;;; Deal

(defrecord Deal [knowledge trick skat])

;;; Result

(defrecord Result [solist success? bid])

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
  (declare-suit [this cards final-bid])
  (declare-hand [this cards final-bid])
  (declare-schneider [this cards final-bid])
  (declare-schwarz [this cards final-bid])
  (declare-ouvert [this cards final-bid])
  (skat-swapping [this config cards-owned skat-card]))

;;; Situation

(defrecord PlayerSituation [config knowledge order cards-allowed])

;;; Trick

(defrecord Trick [order])

;;; Driver

(defprotocol GameDriver
  (create-players [this])
  (do-auction [this bidders deal])
  (declare-game [this bidding]))
