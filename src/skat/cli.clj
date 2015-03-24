(ns skat.cli
  (:require [clojure.string :refer [join]]
            [skat.helpers :as helpers]
            [skat.cards :as cards]
            [skat.responses :as responses]
            [skat.game :as game]
            [skat.auction :as auction]))

;; Printing

(def color-str "Maps card colors to character"
  { :kreuz \♣, :grun \♠, :herz \♥, :schell \♦ })

(def figure-str "Maps card figure to string"
  { :7 " 7", :8 " 8", :9 " 9", :10 "10", :W " W", :Q " Q", :K " K", :A " A" })

(defn card-str "Maps card to string" [card]
  { :pre [(cards/card? card)] }
  (let [color  (color-str (:color card))
        figure (figure-str (:figure card))]
    (str \[ color \space figure \])))

;; Reading

(defn select-nth "User select nth element of collection (size in [1, 10])"
  [coll to-s separator]
  { :pre [(<= 1 (count coll) 10)] }
  (letfn [(rotate [in pos] (if in (mod (+ in pos) 10)))
          (parse-int [in] (if (re-find #"^\d$" in) (Integer/parseInt in)))
          (pos-to-idx [in] (rotate (parse-int in) -1))
          (idx-to-pos [in] (rotate in 1))]
    (let [size    (count coll)
          preview (join
                   separator
                   (map-indexed #(str (idx-to-pos %1) \space (to-s %2)) coll))]
      (loop [idx nil]
        (if (and idx (< idx size))
          (coll idx)
          (do
            (println preview)
            (recur (pos-to-idx (read-line)))))))))

(defn select-nth-card "User select nth card" [coll]
  { :pre [(every? cards/card? coll)] }
  (select-nth coll color-str "  "))

;; TODO:
;; select 3 players [human/computer]
;; initiate board (0 p each player), assign positions [front, middle, rear]
;; 10 times do:
;;   do
;;     deal cards
;;     perform
;;   while auction is not successful
;;   play 10 tricks
;;   determine whether solist win
;;   rotate positions
;; show results
