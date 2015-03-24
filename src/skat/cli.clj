(ns skat.cli
  (:require [clojure.string :refer [join]]
            [taoensso.tower :refer [with-tscope]]
            [skat.i18n :as i18n :refer [*lang*]]
            [skat.cards :as cards]
            [skat.responses :as responses]
            [skat.game :as game]
            [skat.auction :as auction]
            [skat.ai :as ai]))
(set! *warn-on-reflection* true)

;;; Players forward declarations

(declare create-cpu-player create-human-player player-types player-types-str)

;;; Generating displayed content

(def card-separator "  ")
(def player-separator " | ")

(def color-str "Maps card colors to character"
  { :kreuz \♣, :grun \♠, :herz \♥, :schell \♦ })

(def figure-str "Maps card figure to string"
  { :7 " 7", :8 " 8", :9 " 9", :10 "10", :W " W", :Q " Q", :K " K", :A " A" })

(defn card-str "Maps card to string" [card]
  { :pre [(cards/card? card)] }
  (let [color  (color-str (:color card))
        figure (figure-str (:figure card))]
    (str \[ color \space figure \])))

(defn coll-str "Preview collection" [coll k2str v2str separator]
  (join separator (map-indexed #(str (k2str %1) \space (v2str %2)) coll)))

;;; Displaying data

(defmacro show-t [& args]
  (with-tscope [:skat :cli]
    (println (i18n/t *lang* args))))

(defn show-nth-item "Shows nth item question" []
  (show-t :select-nth-item))

(defn show-player-name-question "Shows player name question" []
  (show-t :select-player-name))

(defn show-allowed-cards "Prints owned and allowed cards"
  [{ :keys [:cards-allowed] { :keys [:cards-owned] } :knowledge }]
  (do
    (show-t :select-card-owned
            (coll-str cards-owned (fn [_] " ") card-str card-separator))
    (show-t :select-card-allowed
            (coll-str cards-allowed str card-str card-separator))))

(defn show-player1-card "Prints cards played by player 1" [situation c1]
  (show-t :player-played (-> situation :order :p1 .id) c1))

(defn show-player2-card "Prints cards played by player 2" [situation c2]
  (show-t :player-played (-> situation :order :p2 .id) c2))

;;; Obtaining data

(defn select-nth "User select nth element of collection (size in [1, 10])"
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
            (show-nth-item)
            (println preview)
            (recur (pos-to-idx (read-line)))))))))

(defn select-card "User select nth card" [cards]
  { :pre [(every? cards/card? cards)] }
  (select-nth cards color-str "  "))

(defn select-player-name "Select player name" [used-names]
  (loop [id nil]
    (if (and id (not (used-names id)))
      id
      (do
        (show-player-name-question)
        (recur (read-line))))))

(defn select-player-type "Select player type" []
  (select-nth (vec player-types) player-types-str player-separator))

(defn select-players "Select all players" []
  (let [pl-front-name  (select-player-name #{})
        pl-front-type  (select-player-type)
        pl-middle-name (select-player-name #{ pl-front-name })
        pl-middle-type (select-player-type)
        pl-rear-name   (select-player-name #{ pl-front-name pl-middle-name })
        pl-rear-type   (select-player-type)]
    (skat.auction.Bidders. (pl-front-type  pl-front-name)
                           (pl-middle-type pl-middle-name)
                           (pl-rear-type   pl-rear-name))))

;;; Players

(def create-cpu-player "Creates computer player" ai/ai-player)

(defn create-human-player "Creates human player using CLI" [id]
  (reify skat.game.Player
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
    (place-bid [this cards last-bid] nil)
    (respond-to-bid [this cards bid] nil)
    (declare-suit [this cards final-bid] nil)))

(def player-types "Players types to choose"
  #{ create-cpu-player, create-human-player })

(def player-types-str "Maps player types to string"
  { create-cpu-player "CPU player", create-human-player "Human player" })

;;; Gameplay

;; TODO:
;; ☑ select 3 players [human/computer]
;; ☐ initiate board (0 p each player), assign positions [front, middle, rear]
;; ☐ 10 times do:
;; ☐   do
;; ☐     deal cards
;; ☐     perform auction
;; ☐   while auction is not successful
;; ☐   play 10 tricks
;; ☐   determine whether solist win
;; ☐   rotate positions
;; ☐ show results

