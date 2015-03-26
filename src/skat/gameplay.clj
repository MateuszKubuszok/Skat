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
  (loop []
    (let [deal    (cards/deal-cards)
          bidding (.do-auction ^GameDriver driver bidders deal)]
      (if bidding
        { :deal deal, :bidding bidding }
        (recur)))))

(defn declare-game "Declare game suit, hand, schneider, schwarz and ouvert"
  [driver bidding]
  (letfn [(acceptable-game? [config] true)]
    (loop []
      (let [config (.declare-game ^GameDriver driver bidding)]
        (if (acceptable-game? config)
          config
          (recur))))))

(defn swap-skat "Swap skat with owned cards if game without hand declared"
  [conf deal winner]
  (if (:hand? conf)
    deal
    (let [winner-position (-> deal sets/map-invert (find winner))]
      (letfn [(cards [deal] (-> deal winner-position))
              (replacements [skat owned] { skat owned, owned skat })
              (replacing [replacements] (fn [coll] (replace replacements coll)))
              (swap-cards [deal replacing]
                (-> deal (update-in [winner-position] replacing)
                         (update-in [:skat] replacing)))]
        (let [skat-1 (-> deal :skat 0)
              card-1 (.skat-swapping ^Player winner conf (cards deal) skat-1)
              swap-1 (swap-cards deal (replacing (replacements skat-1 card-1)))
              skat-2 (-> deal :skat 1)
              card-2 (.skat-swapping ^Player winner conf (cards swap-1) skat-2)
              swap-2 (swap-cards deal (replacing (replacements skat-2 card-2)))]
          swap-2)))))

(defn play-deal "Play whole 10-trick deal and reach conclusion"
  [config
   { :keys [:front :middle :rear] :as bidders }
   { f-cards :front, m-cards :middle, r-cards :rear, skat :skat }]
  (letfn [(out-of-cards? [pk] (some #(-> pk :cards-owned empty?)))
          (game-finished? [knowledge] out-of-cards? (vals knowledge))]
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
  (let [auction-result (perform-auction driver bidders)
        bidding        (:bidding auction-result)
        winner         (:winner bidding)
        config         (declare-game driver bidding)
        deal-cards     (swap-skat config (:deal auction-result) winner)
        results        (play-deal config bidders deal-cards)
        skat           (:skat results)
        cards-taken    (-> results :knowledge :cards-taken (concat skat))]
    (Result. winner
             (auction/contract-fulfilled? config cards-taken)
             (bidding :bid))))

(defn start-game "Start game using passed driver" [driver]
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
