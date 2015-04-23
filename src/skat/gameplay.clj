(ns skat.gameplay
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as sets]
            [skat]
            [skat.log :as log]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.game :as game]
            [skat.auction :as auction])
  (:import  [skat Deal
                  Result
                  Player
                  PlayerKnowledge
                  Trick
                  GameDriver]))
(set! *warn-on-reflection* true)

;;; Perform auction

(def ^:dynamic shuffle-cards-and-deal "Mockable cards dealing" cards/deal-cards)

(defn perform-auction "Performs auction" [driver bidders]
  { :pre  [driver bidders]
    :post [(:deal %) (:bidding %)] }
  (do
    (.auction-started ^GameDriver driver bidders)
    (loop []
      (let [deal    (shuffle-cards-and-deal)
            bidding (auction/do-auction bidders deal)]
        (do
          (.auction-result ^GameDriver driver bidding)
          (if (auction/auction-successful? bidding)
            { :deal deal, :bidding bidding }
            (recur)))))))

;;; Declare game

(defn declare-game "Declare game suit, hand, schneider, schwarz and ouvert"
  [driver bidding]
  { :pre  [driver bidding]
    :post [%] }
  (letfn [(acceptable-game? [{ :keys [soloist suit declared-bid] }]
            (and (instance? Player soloist)
                 (game/suits suit)
                 (auction/possible-game-values declared-bid)))]
    (loop []
      (let [config (.declare-game ^GameDriver driver bidding)]
        (if (acceptable-game? config)
          (do
            (.declaration-result ^GameDriver driver config)
            config)
          (recur))))))

;;; Skat swapping

(defn swap-skat "Swap skat with owned cards if game without hand declared"
  [config card-deal soloist soloist-position]
  { :pre  [config card-deal soloist]
    :post [%] }
  (if (:hand? config)
    card-deal
    (letfn [(ask-for-card [old-card-deal swapped]
              (.skat-swapping ^Player soloist
                                      config
                                      (:skat old-card-deal)
                                      (soloist-position old-card-deal)
                                      swapped))]
      (let [skat-1 (-> card-deal :skat first)
            card-1 (ask-for-card card-deal skat-1)
            swap-1 (game/swap-for card-deal card-1 skat-1 soloist-position)
            skat-2 (-> card-deal :skat second)
            card-2 (ask-for-card swap-1 skat-2)
            swap-2 (game/swap-for swap-1 card-2 skat-2 soloist-position)]
        swap-2))))

;;; Play single deal with bidding done

(defn initialize-knowledge "Initializes knowledge for each Player"
  [{ :keys [:soloist :ouvert?] }
   { :keys [:front :middle :rear] }
   { front-cards :front, middle-cards :middle, rear-cards :rear, skat :skat }]
  { :pre  [soloist front middle rear
           (every? cards/card? front-cards)
           (every? cards/card? middle-cards)
           (every? cards/card? rear-cards)
           (every? cards/card? skat)] }
  (letfn [(cards-for [player]
            (match player
              front  front-cards
              middle middle-cards
              rear   rear-cards))
          (owned-knowledge-for [checked current]
            (match checked
              current (cards-for current)
              soloist (if ouvert? (cards-for soloist) [])
              :else   []))
          (initial-knowledge-for [player]
            (PlayerKnowledge. player
                              { front [], middle [], rear [] }
                              { front  (owned-knowledge-for front  player)
                                middle (owned-knowledge-for middle player)
                                rear   (owned-knowledge-for rear   player) }
                              { front #{}, middle #{}, rear #{} }))]
    { front  (initial-knowledge-for front)
      middle (initial-knowledge-for middle)
      rear   (initial-knowledge-for rear) }))

(defn get-cards-owned "Obtains cards for Player from his Knowledge"
  [{ :keys [:self :cards-owned] }]
  (log/pass
    (get cards-owned self)
    :cards-owned-by-player
    "Cards owned by player"))
(defn game-finished? "Check whether game finished (someone run out of cards)"
  [knowledge]
  (log/pass
    (some #(-> %
               second
               (log/pass :trick-knowledge "Player's knowledge")
               get-cards-owned
               empty?)
          knowledge)
    :deal-finished
    "Some Player's knowledge for finished deal"))

(defn player-last-played-card-pair "Creates pair Player -> Card pair"
  [player knowledge]
  { :player player
    :card (-> knowledge (get-in [player :cards-played player]) last) })
(defn last-trick-cards "Obtain cards played in last trick by each player"
  [{ { order :order } :trick }
   { knowledge :knowledge
     { { winner :p1 } :order } :trick }]
  (assoc (helpers/update-all order player-last-played-card-pair knowledge)
         :winner
         winner))

(defn play-deal "Play whole 10-trick deal and reach conclusion"
  [driver
   config
   { :keys [:front :middle :rear] :as bidders }
   { :keys [:skat] :as card-deal }]
  { :pre  [driver config front middle rear (every? cards/card? skat)]
    :post [%] }
    (let [initial-knowledge (initialize-knowledge config bidders card-deal)
          initial-trick     (Trick. { :p1 front, :p2 middle, :p3 rear })
          initial-deal      (Deal. initial-knowledge initial-trick skat)]
      (loop [deal (log/pass initial-deal :deal "Initialize deal")]
        (if (-> deal :knowledge game-finished?)
          deal
          (let [next-deal    (game/play-trick config deal)
                trick-result (last-trick-cards deal next-deal)]
            (do
              (.trick-results ^GameDriver driver trick-result)
              (recur next-deal)))))))

;;; Play whole deal including bidding and calculating Result

(defn deal-end2end "Deal cards, auction and play 10 tricks" [driver bidders]
  { :pre [driver bidders] }
  (let [auction-result   (perform-auction driver bidders)
        bidding          (:bidding auction-result)
        soloist          (:winner bidding)
        soloist-position (-> bidders sets/map-invert (find soloist) (get 1))
        config           (declare-game driver bidding)
        deal-cards       (swap-skat config (:deal auction-result)
                                          soloist
                                          soloist-position)
        results          (play-deal driver config bidders deal-cards)
        skat             (:skat results)
        cards-taken      (-> results
                             (log/pass :deal "deal results")
                             (get-in [:knowledge soloist :cards-taken soloist])
                             (concat skat)
                             (log/pass :deal "all owned cards"))
        game-value       (auction/final-game-value cards-taken config)]
    (Result. soloist
             (auction/contract-fulfilled? config cards-taken)
             (:bid bidding)
             game-value)))

;;; Perform whole game from beginning to end

(def ^:dynamic *rounds-in-tournament* "Numer of rounds in tournament" 10)

(defn start-game "Start game using passed driver" [driver]
  { :pre [driver] }
  (let [rounds-in-tournament *rounds-in-tournament*
        initial-bidders      (.create-players ^GameDriver driver)
        player-1             (:front  initial-bidders)
        player-2             (:middle initial-bidders)
        player-3             (:rear   initial-bidders)
        initial-points       { player-1 0, player-2 0, player-3 0 }]
    (loop [round   1
           bidders initial-bidders
           points  initial-points]
      (if (<= round rounds-in-tournament)
        (let [deal-result (deal-end2end driver bidders)]
          (do
            (.deal-results ^GameDriver driver deal-result)
            (recur (inc round)
                   (game/rotate-bidders bidders)
                   (game/update-points points deal-result))))
        (.game-results ^GameDriver driver points)))))
