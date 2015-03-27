(ns skat.cli
  (:require [clojure.string :refer [join]]
            [taoensso.tower :refer [with-tscope]]
            [skat]
            [skat.i18n :as i18n :refer [*lang*]]
            [skat.cards :as cards]
            [skat.auction :as auction]
            [skat.gameplay :as gameplay]
            [skat.ai :as ai])
  (:import  [skat Bidders Configuration Player GameDriver]))
(set! *warn-on-reflection* true)

;;; Players forward declarations

(declare create-cpu-player create-human-player player-types player-types-str)

;;; Generating displayed content

(def bool-separator "; ")
(def card-separator "  ")
(def player-separator " | ")
(def suit-separator " | ")

(def bool-str "Maps booleans to string"
  { true  (with-tscope :skat/cli (i18n/t *lang* :answer/yes))
    false (with-tscope :skat/cli (i18n/t *lang* :answer/no)) })

(def color-str "Maps card colors to strings"
  { :kreuz "♣", :grun "♠", :herz   "♥", :schell "♦" })

(def figure-str "Maps card figure to string"
  { :7  (with-tscope :skat/cli (i18n/t *lang* :figure/r7))
    :8  (with-tscope :skat/cli (i18n/t *lang* :figure/r8))
    :9  (with-tscope :skat/cli (i18n/t *lang* :figure/r9))
    :10 (with-tscope :skat/cli (i18n/t *lang* :figure/r10))
    :W  (with-tscope :skat/cli (i18n/t *lang* :figure/W))
    :D  (with-tscope :skat/cli (i18n/t *lang* :figure/D))
    :K  (with-tscope :skat/cli (i18n/t *lang* :figure/K))
    :A  (with-tscope :skat/cli (i18n/t *lang* :figure/A)) })

(def suit-str "Maps suit to string"
  { :grand  (with-tscope :skat/cli (i18n/t *lang* :suit/grand)),
    :kreuz  (with-tscope :skat/cli (i18n/t *lang* :suit/kreuz)),
    :grun   (with-tscope :skat/cli (i18n/t *lang* :suit/grun)),
    :herz   (with-tscope :skat/cli (i18n/t *lang* :suit/herz)),
    :schell (with-tscope :skat/cli (i18n/t *lang* :suit/schell)),
    :null   (with-tscope :skat/cli (i18n/t *lang* :suit/null)) })

(defn card-str "Maps card to string" [card]
  { :pre [(cards/card? card)] }
  (let [color  (color-str (:color card))
        figure (figure-str (:figure card))]
    (str \[ color \space figure \])))

(defn coll-str "Preview collection" [coll k2str v2str separator]
  (join separator (map-indexed #(str (k2str %1) \space (v2str %2)) coll)))

(def card-sorters "Sorting functions for different suits"
  { :grand  (cards/compare-for-sort (cards/compare-by-color-display :grand)
                                    cards/compare-by-figure-normal)
    :kreuz  (cards/compare-for-sort (cards/compare-by-color-display :kreuz)
                                    cards/compare-by-figure-normal)
    :grun   (cards/compare-for-sort (cards/compare-by-color-display :grun)
                                    cards/compare-by-figure-normal)
    :herz   (cards/compare-for-sort (cards/compare-by-color-display :herz)
                                    cards/compare-by-figure-normal)
    :schell (cards/compare-for-sort (cards/compare-by-color-display :schell)
                                    cards/compare-by-figure-normal)
    :null   (cards/compare-for-sort cards/compare-by-color-null
                                    cards/compare-by-figure-null) })

(defn sort-cards-for-suit "Sorts cards for current game type"
  [{ :keys [:suit] } cards]
  { :pre [(every? cards/card? cards)] }
  (letfn [(sorter [coll] (sort (card-sorters suit) coll))]
    (-> cards vec sorter vec)))

(def mock-cards-sort "Default config for sorting cards" { :suit :null })

(defn cards-str "Show cards" [idx-str config cards]
  (coll-str (sort-cards-for-suit config cards)
            idx-str
            card-str
            card-separator))
(def cards-no-idx-str "Show cards without index"
  (partial cards-str (fn [_] " ")))
(def cards-num-idx-str "Show cards without index"
  (partial cards-str str))

;;; Displaying data

(defn pid "Show player ID" [player]
  (.id ^Player player))

(defmacro show-t [& args]
  (list `with-tscope
        :skat/cli
        (list `println (concat (list `i18n/t `*lang*) args))))
(defn show-owned-cards "Prints owned cards" [config cards]
  (show-t :cards/owned (cards-no-idx-str config cards)))
(defn show-allowed-cards "Prints owned and allowed cards"
  [{ :keys [:self :config :cards-allowed] { :keys [:cards-owned] } :knowledge }]
  (do
    (show-owned-cards config (get cards-owned self))
    (show-t :cards/allowed (cards-no-idx-str config cards-allowed))))
(defn show-player-make-bid "Shows new bid question" [last-bid cards]
  (show-t :player/make-bid
          last-bid
          (cards-no-idx-str mock-cards-sort cards)))
(defn show-player-answer-bid "Shows bid response question" [bid cards]
  (show-t :player/answer-bid bid (cards-no-idx-str mock-cards-sort cards)))
(defn show-player-name "Shows player's name" [pid]
  (show-t :player/name pid))
(defn show-player-choose-suit "Shows suit choice question" [cards]
  (do
    (show-t :player/choose-suit)
    (show-owned-cards mock-cards-sort cards)))
(defn show-player-choose-hand "Shows hand choice question" [cards]
  (do
    (show-t :player/choose-hand)
    (show-owned-cards mock-cards-sort cards)))
(defn show-player-choose-schneider "Shows schneider choice question" [cards]
  (do
    (show-t :player/choose-schneider)
    (show-owned-cards mock-cards-sort cards)))
(defn show-player-choose-schwarz "Shows schwarz choice question" [cards]
  (do
    (show-t :player/choose-schwarz)
    (show-owned-cards mock-cards-sort cards)))
(defn show-player-choose-ouvert "Shows ouvert choice question" [cards]
  (do
    (show-t :player/choose-ouvert)
    (show-owned-cards mock-cards-sort cards)))
(defn show-player-swap-skat-card "Shows skat card swap choice question"
  [cards skat-card]
  (do
    (show-t :player/swap-skat-card (card-str skat-card))
    (show-owned-cards mock-cards-sort cards)))
(defn show-player1-card "Prints cards played by player 1" [situation c1]
  (show-t :player/played (-> situation :order :p1 pid) (card-str c1)))
(defn show-player2-card "Prints cards played by player 2" [situation c2]
  (show-t :player/played (-> situation :order :p2 pid) (card-str c2)))
(defn show-player-won-bid "Print bid result" [pid bid]
  (show-t :player/won-bid pid bid))
(defn show-player-bid-draw "Prints bid draw result" []
  (show-t :player/bid-draw))
(defn show-result-deal "Shows deal results" [pid bid success?]
  (show-t :results/deal pid bid success?))
(defn show-result-game "Shows game results" [points]
  (doseq [player (keys points)]
    (show-t :results/game (pid player) (points player))))
(defn show-select-nth-item "Shows nth item question" []
  (show-t :select/nth-item))
(defn show-select-player-name "Shows player name question" []
  (show-t :select/player-name))

;;; Obtaining data

(defn select-nth "User selects nth element of collection (size in [1, 10])"
  [coll value-str separator]
  { :pre [coll (<= 1 (count coll) 10)] }
  (letfn [(rotate [in pos] (if in (mod (+ in pos) 10)))
          (parse-int [in] (if (re-find #"^\d$" in) (Integer/parseInt in)))
          (pos-to-idx [in] (rotate (parse-int in) -1))
          (idx-to-pos [in] (rotate in 1))]
    (let [size    (count coll)
          preview (coll-str coll idx-to-pos value-str separator)]
      (loop [idx nil]
        (if (and idx (< idx size))
          (-> coll vec (get idx))
          (do
            (show-select-nth-item)
            (println preview)
            (recur (pos-to-idx (read-line)))))))))

(defn select-new-bid "User selects new bid" [last-bid cards]
  (letfn [(parse-int [in] (if (re-find #"\d+" in) (Integer/parseInt in)))
          (correct-bid? [bid] (and (auction/possible-game-values bid)
                                   (or (== 17 bid) (< last-bid bid))))]
    (loop [bid nil]
      (if (correct-bid? bid)
        bid
        (do
          (show-player-make-bid last-bid cards)
          (recur (parse-int (read-line))))))))

(defn select-yes-no-answer "User answers to yes-no question" []
  (select-nth [true false] bool-str bool-separator))

(defn select-card "User selects nth card" [config cards]
  { :pre [(every? cards/card? cards)], :post [%] }
  (select-nth (sort-cards-for-suit config cards) card-str card-separator))

(defn select-player-name "User selects player name" [used-names]
  (loop [id nil]
    (if (and id (not (used-names id)))
      id
      (do
        (show-select-player-name)
        (recur (read-line))))))

(defn select-player-type "User selects player type" []
  (select-nth player-types player-types-str player-separator))

(defn select-players "User selects all players" []
  (let [pl-front-name  (select-player-name #{})
        pl-front-type  (select-player-type)
        pl-middle-name (select-player-name #{ pl-front-name })
        pl-middle-type (select-player-type)
        pl-rear-name   (select-player-name #{ pl-front-name pl-middle-name })
        pl-rear-type   (select-player-type)]
    (Bidders. (pl-front-type  pl-front-name)
              (pl-middle-type pl-middle-name)
              (pl-rear-type   pl-rear-name))))

(defn select-suit "User selects suit" []
  (select-nth [:grand :kreuz :grun :herz :schell :null]
              suit-str
              suit-separator))

(defn select-config "User selects used config" [{ :keys [:winner :cards :bid] }]
  (let [solist       winner
        suit         (.declare-suit ^Player solist cards bid)
        hand         (.declare-hand ^Player solist cards bid)
        ouvert       (if hand (.declare-ouvert ^Player solist cards bid))
        schneider    (.declare-schneider ^Player solist cards bid)
        schwarz      (if hand (.declare-schwarz ^Player solist cards bid))
        declared-bid bid]
    (Configuration. solist suit hand ouvert schneider schwarz declared-bid)))

;;; Players

(def create-cpu-player "Creates computer player" ai/ai-player)

(defn create-human-player "Creates human player using CLI" [id]
  (reify skat.Player
    (id [this] id)
    (play-1st-card [this { :keys [:config :cards-allowed] :as situation }]
      (do
        (show-player-name id)
        (show-allowed-cards situation)
        (select-card config cards-allowed)))
    (play-2nd-card [this { :keys [:config :cards-allowed] :as situation } c1]
      (do
        (show-player-name id)
        (show-player1-card situation c1)
        (show-allowed-cards situation)
        (select-card config cards-allowed)))
    (play-3rd-card [this { :keys [:config :cards-allowed] :as situation } c1 c2]
      (do
        (show-player-name id)
        (show-player1-card situation c1)
        (show-player2-card situation c2)
        (show-allowed-cards situation)
        (select-card config cards-allowed)))
    (place-bid [this cards last-bid]
      (do
        (show-player-name id)
        (show-owned-cards mock-cards-sort cards)
        (select-new-bid last-bid cards)))
    (respond-to-bid [this cards bid]
      (do
        (show-player-name id)
        (show-owned-cards mock-cards-sort cards)
        (show-player-answer-bid bid cards)
        (select-yes-no-answer)))
    (declare-suit [this cards final-bid]
      (do
        (show-player-name id)
        (show-player-choose-suit cards)
        (select-suit)))
    (declare-hand [this cards final-bid]
      (do
        (show-player-name id)
        (show-player-choose-hand cards)
        (select-yes-no-answer)))
    (declare-schneider [this cards final-bid]
      (do
        (show-player-name id)
        (show-player-choose-schneider cards)
        (select-yes-no-answer)))
    (declare-schwarz [this cards final-bid]
      (do
        (show-player-name id)
        (show-player-choose-schwarz cards)
        (select-yes-no-answer)))
    (declare-ouvert [this cards final-bid]
      (do
        (show-player-name id)
        (show-player-choose-ouvert cards)
        (select-yes-no-answer)))
    (skat-swapping [this config cards-owned skat-card]
      (do
        (show-player-name id)
        (show-player-swap-skat-card cards-owned skat-card)
        (select-card mock-cards-sort cards-owned)))))

(def player-types "Players types to choose"
  [ create-human-player ])
  ; [ create-cpu-player, create-human-player ]) ; Disabled till AI isn't done

(def player-types-str "Maps player types to string"
  { create-cpu-player   (i18n/t *lang* :skat/cli/player/cpu-type)
    create-human-player (i18n/t *lang* :skat/cli/player/human-type ) })

;;; Gameplay

(defn start-cli-game "Starts CLI game" []
  (let [driver (reify GameDriver
                      (create-players [this] (select-players))
                      (do-auction [this bidders deal]
                        (let [result (auction/do-auction bidders deal)]
                          (do
                            (if result
                              (show-player-won-bid (-> result :winner pid)
                                                   (-> result :bid))
                              (show-player-bid-draw))
                            (identity result))))
                      (declare-game [this bidding] (select-config bidding))
                      (deal-results [this { :keys [:solist :success? :bid] }]
                        (show-result-deal (pid solist) bid (bool-str success?)))
                      (game-results [this points] (show-result-game points)))]
    (gameplay/start-game driver)))
