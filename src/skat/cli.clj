(ns skat.cli
  (:require [clojure.string :refer [join]]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses]
            [skat.game :as game]
            [skat.auction :as auction]))

;;; Players

(defn create-cpu-player "Creates computer player"
  [id]
  (reify skat.game.Player
    (id [this] id)
    (play-1st-card [this situation] nil)
    (play-2nd-card [this situation c1] nil)
    (play-3rd-card [this situation c1 c2] nil)
    (place-bid [this cards last-bid] nil)
    (respond-to-bid [this cards bid] nil)
    (declare-suit [this cards final-bid] nil)))

(defn create-human-player "Creates human player who express decisions using CLI"
  [id]
  (reify skat.game.Player
    (id [this] id)
    (play-1st-card [this situation] nil)
    (play-2nd-card [this situation c1] nil)
    (play-3rd-card [this situation c1 c2] nil)
    (place-bid [this cards last-bid] nil)
    (respond-to-bid [this cards bid] nil)
    (declare-suit [this cards final-bid] nil)))

(def player-types "Players typed to choose"
  #{ create-cpu-player, create-human-player })

;;; Displaying data

(def select-nth-question "Select which one you want:")
(def select-player-name-question "Select player's name:")

(def color-str "Maps card colors to character"
  { :kreuz \♣, :grun \♠, :herz \♥, :schell \♦ })

(def figure-str "Maps card figure to string"
  { :7 " 7", :8 " 8", :9 " 9", :10 "10", :W " W", :Q " Q", :K " K", :A " A" })

(defn card-str "Maps card to string" [card]
  { :pre [(cards/card? card)] }
  (let [color  (color-str (:color card))
        figure (figure-str (:figure card))]
    (str \[ color \space figure \])))

(def player-types-str "Maps player types to string"
  { create-cpu-player "CPU player", create-human-player "Human player" })

(defn coll-str "Preview collection" [coll k2str v2str separator]
  (join separator (map-indexed #(str (k2str %1) \space (v2str %2)) coll)))

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
            (println select-nth-question)
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
        (println select-player-name-question)
        (recur (read-line))))))

(defn select-player-type "Select player type" []
  (select-nth (vec player-types) player-types-str " | "))

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

;; TODO:
;; select 3 players [human/computer]
;; initiate board (0 p each player), assign positions [front, middle, rear]
;; 10 times do:
;;   do
;;     deal cards
;;     perform auction
;;   while auction is not successful
;;   play 10 tricks
;;   determine whether solist win
;;   rotate positions
;; show results

;;; Gameplay
