(ns skat.cli
  (:require [clojure.string :refer [join]]
            [taoensso.tower :refer [with-tscope]]
            [skat]
            [skat.i18n :as i18n :refer [*lang*]]
            [skat.cards :as cards]
            [skat.game :as game]
            [skat.auction :as auction]
            [skat.ai :as ai])
  (:import  [skat Bidders Player GameDriver]))
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

(def color-str "Maps card colors to character"
  { :kreuz \♣, :grun \♠, :herz \♥, :schell \♦ })

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

;;; Displaying data

(defn pid "Show player ID" [player]
  (.id ^Player player))

(defmacro show-t [& args]
  (list `with-tscope
        :skat/cli
        (list `println (concat (list `i18n/t `*lang*) args))))
(defn show-owned-cards "Prints owned cards" [cards]
  (show-t :cards/owned
          (coll-str cards (fn [_] " ") card-str card-separator)))
(defn show-allowed-cards "Prints owned and allowed cards"
  [{ :keys [:cards-allowed] { :keys [:cards-owned] } :knowledge }]
  (do
    (show-owned-cards cards-owned)
    (show-t :cards/allowed
            (coll-str cards-allowed str card-str card-separator))))
(defn show-player-make-bid "Shows new bid question" [last-bid cards]
  (show-t :player/make-bid
          last-bid
          (coll-str cards (fn [_] " ") card-str card-separator)))
(defn show-player-answer-bid "Shows bid response question" [bid cards]
  (show-t :player/answer-bid
          bid
          (coll-str cards (fn [_] " ") card-str card-separator)))
(defn show-player-choose-suit "Shows suit choice question" [cards]
  (do
    (show-owned-cards cards)
    (show-t :player/choose-suit)))
(defn show-player-choose-hand "Shows hand choice question" [cards]
  (do
    (show-owned-cards cards)
    (show-t :player/choose-hand)))
(defn show-player-choose-schneider "Shows schneider choice question" [cards]
  (do
    (show-owned-cards cards)
    (show-t :player/choose-schneider)))
(defn show-player-choose-schwarz "Shows schwarz choice question" [cards]
  (do
    (show-owned-cards cards)
    (show-t :player/choose-schwarz)))
(defn show-player-choose-ouvert "Shows ouvert choice question" [cards]
  (do
    (show-owned-cards cards)
    (show-t :player/choose-ouvert)))
(defn show-player1-card "Prints cards played by player 1" [situation c1]
  (show-t :player/played (-> situation :order :p1 pid) c1))
(defn show-player2-card "Prints cards played by player 2" [situation c2]
  (show-t :player/played (-> situation :order :p2 pid) c2))
(defn show-player-won-bid "Print bid result" [pid bid]
  (show-t :player/won-bid pid bid))
(defn show-player-bid-draw "Print bid draw result" []
  (show-t :player/bid-draw))
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
          (coll idx)
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

(defn select-card "User selects nth card" [cards]
  { :pre [(every? cards/card? cards)] }
  (select-nth cards color-str card-separator))

(defn select-player-name "User selects player name" [used-names]
  (loop [id nil]
    (if (and id (not (used-names id)))
      id
      (do
        (show-select-player-name)
        (recur (read-line))))))

(defn select-player-type "User selects player type" []
  (select-nth (vec player-types) player-types-str player-separator))

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
  (select-nth (vec game/suits) suit-str suit-separator))

;;; Players

(def create-cpu-player "Creates computer player" ai/ai-player)

(defn create-human-player "Creates human player using CLI" [id]
  (reify skat.Player
    (id [this] id)
    (play-1st-card [this { :keys [:cards-allowed] :as situation }]
      (do
        (show-allowed-cards situation)
        (select-card cards-allowed)))
    (play-2nd-card [this { :keys [:cards-allowed] :as situation } c1]
      (do
        (show-player1-card situation c1)
        (show-allowed-cards situation)
        (select-card cards-allowed)))
    (play-3rd-card [this { :keys [:cards-allowed] :as situation } c1 c2]
      (do
        (show-player1-card situation c1)
        (show-player2-card situation c2)
        (show-allowed-cards situation)
        (select-card cards-allowed)))
    (place-bid [this cards last-bid]
      (do
        (show-owned-cards cards)
        (select-new-bid last-bid cards)))
    (respond-to-bid [this cards bid]
      (do
        (show-owned-cards cards)
        (show-player-answer-bid bid cards)
        (select-yes-no-answer bid cards)))
    (declare-suit [this cards final-bid]
      (do
        (show-player-choose-suit cards)
        (select-suit)))
    (declare-hand [this cards final-bid]
      (do
        (show-player-choose-hand cards)
        (select-yes-no-answer)))
    (declare-schneider [this cards final-bid]
      (do
        (show-player-choose-schneider cards)
        (select-yes-no-answer)))
    (declare-schwarz [this cards final-bid]
      (do
        (show-player-choose-schwarz cards)
        (select-yes-no-answer)))
    (declare-ouvert [this cards final-bid]
      (do
        (show-player-choose-ouvert cards)
        (select-yes-no-answer)))))

(def player-types "Players types to choose"
  #{ create-cpu-player, create-human-player })

(def player-types-str "Maps player types to string"
  { create-cpu-player "CPU player", create-human-player "Human player" })

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
                      (declare-game [this bidding] nil))]
    (game/start-game driver)))

;; TODO:
;; ☑ select 3 players [human/computer]
;; ☑ initiate board (0 p each player), assign positions [front, middle, rear]
;; ☐ 10 times do:
;; ☑   do
;; ☑     deal cards
;; ☑     perform auction
;; ☑   while auction is not successful
;; ☐   play 10 tricks
;; ☐   determine whether solist win
;; ☐   rotate positions
;; ☐ show results

