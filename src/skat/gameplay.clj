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

;;; Run game

(defn perform-auction "Performs auction" [driver bidders]
  { :pre  [driver bidders]
    :post [(:deal %) (:bidding %)] }
  (do
    (.auction-started ^GameDriver driver bidders)
    (loop []
      (let [deal    (cards/deal-cards)
            bidding (auction/do-auction bidders deal)]
        (do
          (.auction-result ^GameDriver driver bidding)
          (if (-> bidding :bid auction/bids?)
            { :deal deal, :bidding bidding }
            (recur)))))))

(defn declare-game "Declare game suit, hand, schneider, schwarz and ouvert"
  [driver bidding]
  { :pre  [driver bidding]
    :post [%] }
  (letfn [(acceptable-game? [config] true)] ; TODO
    (loop []
      (let [config (.declare-game ^GameDriver driver bidding)]
        (if (acceptable-game? config)
          (do
            (.declaration-result ^GameDriver driver config)
            config)
          (recur))))))

(defn swap-skat "Swap skat with owned cards if game without hand declared"
  [config c-deal solist solist-position]
  { :pre  [config c-deal solist]
    :post [%] }
  (if (:hand? config)
    c-deal
    (letfn [(cards [old-c-deal] (-> old-c-deal solist-position))
            (replacements [owned skat] { skat owned, owned skat })
            (replacing [replacements] (fn [coll] (replace replacements coll)))
            (swap-cards [old-c-deal replacing]
              { :post [(not= old-c-deal %)] }
              (-> old-c-deal (update-in [solist-position] replacing)
                             (update-in [:skat] replacing)))
            (swap-for [old-c-deal owned skat]
              { :pre [old-c-deal owned skat] }
              (swap-cards old-c-deal (replacing (replacements owned skat))))
            (ask-for-card [deal swapped]
              (.skat-swapping ^Player solist
                                      config
                                      (:skat deal)
                                      (cards deal)
                                      swapped))]
      (let [skat-1 (-> c-deal :skat first)
            card-1 (ask-for-card c-deal skat-1)
            swap-1 (swap-for c-deal card-1 skat-1)
            skat-2 (-> c-deal :skat second)
            card-2 (ask-for-card swap-1 skat-2)
            swap-2 (swap-for swap-1 card-2 skat-2)]
        swap-2))))

(defn play-deal "Play whole 10-trick deal and reach conclusion"
  [driver
   { :keys [:solist :ouvert?] :as config }
   { :keys [:front :middle :rear] :as bidders }
   { front-cards :front, middle-cards :middle, rear-cards :rear, skat :skat }]
  { :pre  [driver config front middle rear
           (every? cards/card? front-cards)
           (every? cards/card? middle-cards)
           (every? cards/card? rear-cards)
           (every? cards/card? skat)]
    :post [%] }
  (letfn [(cards-for [player]
            (match player
              front  front-cards
              middle middle-cards
              rear   rear-cards))
          (owned-knowledge-for [checked current]
            (match checked
              current (cards-for current)
              solist  (if ouvert? (cards-for solist) [])
              :else   []))
          (initial-knowledge-for [player]
            (PlayerKnowledge. player
                              { front [], middle [], rear [] }
                              { front  (owned-knowledge-for front  player)
                                middle (owned-knowledge-for middle player)
                                rear   (owned-knowledge-for rear   player) }
                              { front #{}, middle #{}, rear #{} }))
          (player-card-pair [player knowledge]
            {
              :player player
              :card (-> knowledge (get-in [player :cards-played player]) last)
            })
          (last-trick-cards [{ { order :order } :trick }
                             { knowledge :knowledge
                               { { winner :p1 } :order } :trick }]
            (assoc (helpers/update-all order player-card-pair knowledge)
                   :winner
                   winner))
          (get-cards-owned [{ :keys [:self :cards-owned] }]
            (log/pass
              (get cards-owned self)
              :cards-owned-by-player
              "Cards owned by player"))
          (game-finished? [knowledge]
            (log/pass
              (some #(-> %
                         second
                         (log/pass :trick-knowledge "Player's knowledge")
                         get-cards-owned
                         empty?)
                    knowledge)
              :deal-finished
              "Some Player's knowledge for finished deal"))]
    (let [initial-knowledge { front  (initial-knowledge-for front)
                              middle (initial-knowledge-for middle)
                              rear   (initial-knowledge-for rear) }
          initial-trick     (Trick. { :p1 front, :p2 middle, :p3 rear })
          initial-deal      (Deal. initial-knowledge initial-trick skat)]
      (loop [deal (log/pass initial-deal :deal "Initialize deal")]
        (if (-> deal :knowledge game-finished?)
          deal
          (let [next-deal    (game/play-trick config deal)
                trick-result (last-trick-cards deal next-deal)]
            (do
              (.trick-results ^skat.GameDriver driver trick-result)
              (recur next-deal))))))))

(defn final-game-value "Final game value for solist"
  [cards-taken
   { :keys [:suit :hand? :ouvert? :announced-schneider? :announced-schwarz?] }]
  (let [schneider? (game/schneider? cards-taken)
        schwarz?   (game/schwarz? cards-taken)]
    (auction/game-value cards-taken
                        suit
                        hand?
                        ouvert?
                        schneider?
                        announced-schneider?
                        schwarz?
                        announced-schwarz?)))

(defn deal-end2end "Deal cards, auction and play 10 tricks" [driver bidders]
  { :pre [driver bidders] }
  (let [auction-result  (perform-auction driver bidders)
        bidding         (:bidding auction-result)
        solist          (:winner bidding)
        solist-position (-> bidders sets/map-invert (find solist) (get 1))
        config          (declare-game driver bidding)
        deal-cards      (swap-skat config (:deal auction-result)
                                          solist
                                          solist-position)
        results         (play-deal driver config bidders deal-cards)
        skat            (:skat results)
        cards-taken     (-> results
                            (log/pass :deal "deal results")
                            (get-in [:knowledge solist :cards-taken solist])
                            (concat skat)
                            (log/pass :deal "all owned cards"))
        game-value      (final-game-value cards-taken config)]
    (Result. solist
             (auction/contract-fulfilled? config cards-taken)
             (:bid bidding)
             game-value)))

(defn start-game "Start game using passed driver" [driver]
  { :pre [driver] }
  (let [rounds-in-tournament 10
        initial-bidders      (.create-players ^GameDriver driver)
        player-1             (:front  initial-bidders)
        player-2             (:middle initial-bidders)
        player-3             (:rear   initial-bidders)
        initial-points       { player-1 0, player-2 0, player-3 0 }]
    (letfn [(rotate-bidders [b]
              (zipmap (map game/player-in-next-deal (keys b)) (vals b)))
            (update-points [points { :keys [:solist :success? :game-value] }]
              (update-in points
                         [solist]
                         #(+ % (if success? game-value (- game-value)))))]
      (loop [round   1
             bidders initial-bidders
             points  initial-points]
        (if (<= round rounds-in-tournament)
          (let [deal-result (deal-end2end driver bidders)]
            (do
              (.deal-results ^GameDriver driver deal-result)
              (recur (inc round)
                     (rotate-bidders bidders)
                     (update-points points deal-result))))
          (.game-results ^GameDriver driver points))))))
