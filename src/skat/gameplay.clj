(ns skat.gameplay
  (:require [clojure.set :as sets]
            [clojure.pprint :refer [pprint]]
            [skat]
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
  (loop []
    (let [deal    (cards/deal-cards)
          bidding (.do-auction ^GameDriver driver bidders deal)]
      (if bidding
        { :deal deal, :bidding bidding }
        (recur)))))

(defn declare-game "Declare game suit, hand, schneider, schwarz and ouvert"
  [driver bidding]
  { :pre  [driver bidding]
    :post [%] }
  (letfn [(acceptable-game? [config] true)]
    (loop []
      (let [config (.declare-game ^GameDriver driver bidding)]
        (if (acceptable-game? config)
          config
          (recur))))))

(defn swap-skat "Swap skat with owned cards if game without hand declared"
  [config deal winner winner-position]
  { :pre  [config deal winner]
    :post [%] }
  (if (:hand? config)
    deal
    (letfn [(cards [old-deal] (-> old-deal winner-position))
            (replacements [owned skat] { skat owned, owned skat })
            (replacing [replacements] (fn [coll] (replace replacements coll)))
            (swap-cards [old-deal replacing]
              { :post [(not= old-deal %)] }
              (-> old-deal (update-in [winner-position] replacing)
                           (update-in [:skat] replacing)))
            (swap-for [old-deal owned skat]
              { :pre [old-deal owned skat] }
              (swap-cards old-deal (replacing (replacements owned skat))))]
      (let [skat-1 (-> deal :skat first)
            card-1 (.skat-swapping ^Player winner config (cards deal) skat-1)
            swap-1 (swap-for deal card-1 skat-1)
            skat-2 (-> deal :skat second)
            card-2 (.skat-swapping ^Player winner config (cards swap-1) skat-2)
            swap-2 (swap-for swap-1 card-2 skat-2)]
        swap-2))))

(defn play-deal "Play whole 10-trick deal and reach conclusion"
  [config
   { :keys [:front :middle :rear] :as bidders }
   { f-cards :front, m-cards :middle, r-cards :rear, skat :skat }]
  { :pre  [config front middle rear
           (every? cards/card? f-cards)
           (every? cards/card? m-cards)
           (every? cards/card? r-cards)
           (every? cards/card? skat)]
    :post [%] }
  (letfn [(out-of-cards? [pk] (some #(-> % :cards-owned empty?) pk))
          (game-finished? [knowledge] (out-of-cards? (vals knowledge)))]
    (let [initial-knowledge { front  (PlayerKnowledge. front  [] f-cards #{})
                              middle (PlayerKnowledge. middle [] m-cards #{})
                              rear   (PlayerKnowledge. rear   [] r-cards #{}) }
          initial-trick     (Trick. { :p1 front, :p2 middle, :p3 rear })
          initial-deal      (Deal. initial-knowledge initial-trick skat)]
      (loop [deal initial-deal]
        (if (-> deal :knowledge game-finished?)
          deal
          (recur (game/next-trick config deal)))))))

(defn deal-end2end "Deal cards, auction and play 10 tricks" [driver bidders]
  { :pre [driver bidders] }
  (let [auction-result (perform-auction driver bidders)
        bidding         (:bidding auction-result)
        winner          (:winner bidding)
        winner-position (-> bidders sets/map-invert (find winner) (get 1))
        config          (declare-game driver bidding)
        deal-cards      (swap-skat config (:deal auction-result)
                                          winner
                                          winner-position)
        results         (play-deal config bidders deal-cards)
        skat            (:skat results)
        cards-taken     (-> results :knowledge :cards-taken (concat skat))]
    (Result. winner
             (auction/contract-fulfilled? config cards-taken)
             (:bid bidding))))

(defn start-game "Start game using passed driver" [driver]
  { :pre [driver] }
  (let [initial-bidders (.create-players ^GameDriver driver)
        player-1        (:front  initial-bidders)
        player-2        (:middle initial-bidders)
        player-3        (:rear   initial-bidders)
        initial-points  { player-1 0, player-2 0, player-3 0 }]
    (letfn [(rotate-bidders [b]
              (zipmap (map game/player-in-next-deal (keys b)) (vals b)))
            (update-points [points { :keys [:solist :success? :bid] }]
              (update-in points [solist] #(+ % (if success? bid (- bid)))))]
      (loop [round   1
             bidders initial-bidders
             points  initial-points]
        (if (<= 1 round 10)
          (let [deal-result (deal-end2end driver bidders)]
            (do
              (.deal-results ^GameDriver driver deal-result)
              (recur (inc round)
                     (rotate-bidders bidders)
                     (update-points points deal-result))))
          (.game-results ^GameDriver driver points))))))
