(ns skat.auction
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as sets]
            ;[clojure.tools.trace :refer :all]
            ;[skat.log :as log]
            [skat.cards :as cards]
            [skat.game :as game]
            [skat.helpers :as helpers]))

;;; Possible game values

(def passed-game-value "Value used when player gives up bidding" 17)

(def suit-base-value "Base values for each suit (except Null with not use them)"
  { :grand 24, :kreuz 12, :grun 11, :herz 10, :schell 9 })

(def null-game-values "Predefined null game values"
  { [true  false] 23,
    [false false] 35,
    [true  true ] 46,
    [false true ] 59 })

(def possible-game-values "All possible game values"
  (let [min-normal-game-level 2
                                         ; game mat. hand ouvert schn. schw.
        max-normal-game-level { :grand  (+ 1    11   1    1      2     2),
                                :kreuz  (+ 1    7    1    1      2     2),
                                :grun   (+ 1    7    1    1      2     2),
                                :herz   (+ 1    7    1    1      2     2),
                                :schell (+ 1    7    1    1      2     2) }]
    (letfn [(vals-for [suit]
              (map #(* (suit-base-value suit) %)
                   (range min-normal-game-level
                          (inc (max-normal-game-level suit)))))]
      (set (concat (list passed-game-value)
                   (vals null-game-values)
                   (vals-for :grand)
                   (vals-for :kreuz)
                   (vals-for :grun)
                   (vals-for :herz)
                   (vals-for :schell))))))

;;; Matadors' values calculation helpers

(defn with-matadors-value-calculator [pattern matched]
  { :pre  [(every? cards/card? pattern) (every? cards/card? matched)]
    :post [(not (neg? %))] }
  (loop [value 0
         s1    pattern
         s2    matched]
    (if (and (first s1) (first s2) (= (first s1) (first s2)))
      (recur (inc value) (rest s1) (rest s2))
      value)))
(defn without-matadors-value-calculator [pattern matched]
  { :pre  [(every? cards/card? pattern) (every? cards/card? matched)]
    :post [(not (neg? %))] }
  (loop [value 0
         sub   pattern]
    (if (first sub)
      (let [sub-value (with-matadors-value-calculator sub matched)]
        (if (pos? sub-value)
          value
          (recur (inc value) (rest sub))))
      (count pattern))))
(defn matadors-value-calculator [pattern matched]
  (let [with-value    (with-matadors-value-calculator pattern matched)
        without-value (without-matadors-value-calculator pattern matched)]
    (if (pos? with-value) with-value without-value)))

;;; Matadors' values for normal games

(defn matadors-for-trumps "Count matadors by trumps" [suit trumps cards]
  { :pre  [(game/suits suit)
           (every? cards/card? trumps)
           (every? cards/card? cards)]
    :post [(pos? %)] }
  (let [sorting          (cards/compare-for-sort
                          (cards/compare-by-color-display suit)
                          cards/compare-by-figure-normal)
        invert-sorting   #(- (sorting %1 %2))
        sorted-trumps    (sort invert-sorting trumps)
        considered-cards (filter #(helpers/coll-contains? trumps %) cards)
        sorted-cards     (sort invert-sorting cards)]
    (matadors-value-calculator sorted-trumps sorted-cards)))
(defn matadors-grand "Count matadors in grand game" [cards]
  (let [trumps (filter cards/trump-grand? cards/deck)]
    (matadors-for-trumps :grand trumps cards)))
(defn matadors-color "Count matadors in color game" [color cards]
  (let [trumps (filter (partial cards/trump-color? color) cards/deck)]
    (matadors-for-trumps color trumps cards)))
(defn matadors-kreuz "Count matadors in kreuz game" [cards]
  (matadors-color :kreuz cards))
(defn matadors-grun "Count matadors in grun game" [cards]
  (matadors-color :grun cards))
(defn matadors-herz "Count matadors in herz game" [cards]
  (matadors-color :herz cards))
(defn matadors-schell "Count matadors in schell game" [cards]
  (matadors-color :schell cards))
(def matadors "Matadors' values calculation functions"
  { :grand  matadors-grand,
    :kreuz  matadors-kreuz,
    :grun   matadors-grun,
    :herz   matadors-herz,
    :schell matadors-schell })

;;; Normal games values

(defn normal-game-value "Calculate normal game value"
  ([cards
    suit
    hand?
    ouvert?
    announced-schneider?
    announced-schwarz?]
   (normal-game-value cards
                      suit
                      hand?
                      ouvert?
                      false
                      announced-schneider?
                      false
                      announced-schwarz?))
  ([cards
    suit
    hand?
    ouvert?
    schneider?
    announced-schneider?
    schwarz?
    announced-schwarz?]
   (let [base-value   (suit-base-value suit)
         for-game                1
         for-matadors            ((matadors suit) cards)
         for-hand                (if hand? 1 0)
         for-ouvert              (if ouvert? 1 0)
         for-schneider           (if schneider? 1 0)
         for-announced-schneider (if announced-schneider? 1 0)
         for-schwarz             (if schwarz? 1 0)
         for-announced-schwarz   (if announced-schwarz? 1 0)]
     (* base-value (+ for-game
                      for-matadors
                      for-hand
                      for-ouvert
                      for-schneider
                      for-announced-schneider
                      for-schwarz
                      for-announced-schwarz)))))

;;; Null game values

(defn null-game-value "Calculate null game value" [hand? ouvert?]
  { :post [(identity %)] }
  (null-game-values [hand? ouvert?]))


;;; Game values

(defn game-value "Calculate overall game value"
  ([cards
    suit
    hand?
    ouvert?
    announced-schneider?
    announced-schwarz?]
   (game-value cards
               suit
               hand?
               ouvert?
               false
               announced-schneider?
               false
               announced-schwarz?))
  ([cards
    suit
    hand?
    ouvert?
    schneider?
    announced-schneider?
    schwarz?
    announced-schwarz?]
   { :pre  [(game/suits suit)]
     :post [(possible-game-values %)] }
   (match [suit]
     [:null] (null-game-value hand? ouvert?)
     [_]     (normal-game-value cards
                                suit
                                hand?
                                ouvert?
                                schneider?
                                announced-schneider?
                                schwarz?
                                announced-schwarz?))))

;;; Bidding

(defrecord Bidders [front middle rear])

(defrecord Bidding [winner cards bid])

(defn bids? "Nil and 17 means pass, accept otherwise" [bid]
  (and bid (not (= bid passed-game-value))))

(defn bidding-101 "Determines who of two players wins the bid"
  [bidder bidder-cards responder responder-cards current-bid]
  { :pre  [(or (nil? current-bid) (possible-game-values current-bid))]
    :post [(possible-game-values %)] }
  (loop [last-bid current-bid]
    (let [bid (.place-bid bidder bidder-cards last-bid)]
      (if (bids? bid)
        (let [response (.respond-to-bid responder responder-cards bid)]
          (if (bids? response)
            (recur bid)
            (Bidding. bidder bidder-cards bid)))
        (Bidding. responder responder-cards bid)))))

(defn do-auction [{:keys [front middle rear] :as bidders}
                  { front-cards  :front,
                    middle-cards :middle,
                    rear-cards   :rear }]
  (let [bid-1st (bidding-101 middle middle-cards front front-cards 17)
        bid-2nd (bidding-101 rear
                             rear-cards
                             (:winner bid-1st)
                             (:cards bid-1st)
                             (:bid bid-1st))]
    (.declare-suit (:winner bid-2nd) (:cards bid-2nd) (:bid bid-2nd))))
