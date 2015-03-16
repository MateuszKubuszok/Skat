(ns skat.game_test
  (:require [clojure.test :refer :all]
            ;[clojure.pprint :refer :all]
            ;[clojure.tools.trace :refer :all]
            ;[skat.log :as log]
            [skat.cards :refer :all]
            [skat.game :refer :all]))

(defn create-player [pid]
  (reify skat.game.Player
         (id [_] pid)
         (play-1st-card [_ { :keys [ :cards-allowed ] }]
           (first cards-allowed))
         (play-2nd-card [_ { :keys [ :cards-allowed ] } _]
           (first cards-allowed))
         (play-3rd-card [_ { :keys [ :cards-allowed ] } _ _]
            (first cards-allowed))))
(defn first-won [_ _ _] :p1)
(def pl1 (create-player "p1"))
(def pl2 (create-player "p2"))
(def pl3 (create-player "p3"))
(def order { :p1 pl1, :p2 pl2, :p3 pl3 })
(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :kreuz :K))
(def c3 (skat.cards.Card. :schell :W))
(def c4 (skat.cards.Card. :kreuz :A))
(def c5 (skat.cards.Card. :kreuz :10))
(def c6 (skat.cards.Card. :schell :A))
(def c7 (skat.cards.Card. :schell :10))
(def c8 (skat.cards.Card. :schell :K))
(def played-cards { pl1 [], pl2 [], pl3 [] })
(def players-cards { pl1 #{c1 c2}, pl2 #{c3 c4}, pl3 #{c5 c6} })
(def conf-grand (skat.game.Configuration. pl1 :grand true false false false 48))
(def conf-kreuz (skat.game.Configuration. pl1 :kreuz true false false false 24))
(def conf-null  (skat.game.Configuration. pl1 :null  true false false false 23))

(defn map-equal [m1 m2] (= (into (hash-map) m1) (into (hash-map) m2)))

(deftest update-cards-played-test
  (let [played-now { pl1 c3, pl2 c2, pl3 c1 }]
    (testing "adds cards played at given trick"
      (is (map-equal
           (update-cards-played played-cards played-now)
           { pl1 [c3], pl2 [c2], pl3 [c1] })))))

(deftest update-cards-owned-test
  (let [played-now { pl1 c1, pl2 c3, pl3 c2 }]
    (testing "removed cards played at given trick"
      (is (map-equal
           (update-cards-owned players-cards played-now)
           { pl1 #{c2}, pl2 #{c4}, pl3 #{c5 c6} })))))

(deftest update-knowledge-test
  (let [p-knowledge (skat.game.PlayerKnowledge. pl1 played-cards players-cards)
        knowledge { pl1 p-knowledge }
        played-now { pl1 c1, pl2 c3, pl3 c5 }]
    (testing "updates played and owned cards"
      (is (=
           (update-knowledge knowledge played-now)
           { pl1 (skat.game.PlayerKnowledge.
                  pl1
                  { pl1 [c1], pl2 [c3], pl3 [c5] }
                  { pl3 #{c6}, pl2 #{c4}, pl1 #{c2} }) })))))

(deftest figure-situation-test
  (letfn [(mock-knowledge [pl]
            (skat.game.PlayerKnowledge. pl played-cards players-cards))]
    (let [p1-knowledge (mock-knowledge pl1)
          p2-knowledge (mock-knowledge pl2)
          p3-knowledge (mock-knowledge pl3)]
      (testing "allowed cards are properly calculated"
        (is (= (:cards-allowed
                (figure-situation conf-grand p1-knowledge order c3))
               #{c1}))
        (is (= (:cards-allowed
                (figure-situation conf-grand p2-knowledge order c2))
               #{c4}))
        (is (= (:cards-allowed
                (figure-situation conf-grand p3-knowledge order c1))
              #{c5 c6}))
        (is (= (:cards-allowed
                (figure-situation conf-kreuz p1-knowledge order c3))
               #{c1 c2}))
        (is (= (:cards-allowed
                (figure-situation conf-kreuz p2-knowledge order c2))
               #{c3 c4}))
        (is (= (:cards-allowed
                (figure-situation conf-kreuz p3-knowledge order c1))
               #{c5}))
        (is (= (:cards-allowed
                (figure-situation conf-null p1-knowledge order c3))
               #{c1 c2}))
        (is (= (:cards-allowed
                (figure-situation conf-null p2-knowledge order c2))
               #{c4}))
        (is (= (:cards-allowed
                (figure-situation conf-null p3-knowledge order c1))
               #{c5}))))))

(deftest trick-winning-grand-test
  (testing "jack always wins"
    (is (= :p2 (trick-winning-grand c2 c3 c4)))
    (is (= :p1 (trick-winning-grand c1 c2 c3))))
  (testing "when no jack highest of first card's color wins"
    (is (= :p2 (trick-winning-grand c5 c4 c6)))))

(deftest trick-winning-kreuz-test
  (testing "trumph always wins"
    (is (= :p2 (trick-winning-kreuz c2 c3 c4)))
    (is (= :p1 (trick-winning-kreuz c1 c2 c3)))
    (is (= :p3 (trick-winning-kreuz c6 c5 c4))))
  (testing "when no trumph highest of first card's color wins"
    (is (= :p3 (trick-winning-kreuz c8 c7 c6)))))

(deftest next-trick-order-test
  (let [p1-start { :p1 pl1, :p2 pl2, :p3 pl3 }
        p2-start { :p1 pl2, :p2 pl3, :p3 pl1 }
        p3-start { :p1 pl3, :p2 pl1, :p3 pl2 }]
    (testing "winner rotates correctly"
      (is (= (next-trick-order p1-start pl1) p1-start))
      (is (= (next-trick-order p1-start pl2) p2-start))
      (is (= (next-trick-order p1-start pl3) p3-start))
      (is (= (next-trick-order p2-start pl1) p1-start))
      (is (= (next-trick-order p2-start pl2) p2-start))
      (is (= (next-trick-order p2-start pl3) p3-start))
      (is (= (next-trick-order p3-start pl1) p1-start))
      (is (= (next-trick-order p3-start pl2) p2-start))
      (is (= (next-trick-order p3-start pl3) p3-start)))))

(deftest next-trick-test
  (testing "next trick rotates correctly"
    (is (=
         (next-trick (skat.game.Trick. { :p1 pl1, :p2 pl2, :p3 pl3 }) pl2)
         (skat.game.Trick. { :p1 pl2, :p2 pl3, :p3 pl1 })))))

(deftest play-trick-test
  (letfn [(mock-knowledge [pl]
            (skat.game.PlayerKnowledge. pl played-cards players-cards))]
    (let [p1-knowledge (mock-knowledge pl1)
          p2-knowledge (mock-knowledge pl2)
          p3-knowledge (mock-knowledge pl3)
          knowledge { pl1 p1-knowledge, pl2 p2-knowledge, pl3 p3-knowledge }
          trick (skat.game.Trick. order)
          deal (skat.game.Deal. knowledge trick #{})
          next-deal (skat.game.Deal.
                      (update-knowledge knowledge { pl1 c1, pl2 c3, pl3 c6 })
                      (next-trick trick pl1)
                      #{})]
      (testing "calculates next trick"
        (is (= (play-trick conf-grand deal) next-deal))))))

(deftest enough-points?-test
  (let [c1  (skat.cards.Card. :kreuz  :10)
        c2  (skat.cards.Card. :grun   :10)
        c3  (skat.cards.Card. :herz   :10)
        c4  (skat.cards.Card. :schell :10)
        c5  (skat.cards.Card. :kreuz  :K)
        c6  (skat.cards.Card. :grun   :K)
        c7  (skat.cards.Card. :herz   :K)
        c8  (skat.cards.Card. :schell :K)
        c9  (skat.cards.Card. :kreuz  :Q)
        c10 (skat.cards.Card. :kreuz  :W)
        c11 (skat.cards.Card. :grun   :W)
        enough     [c1 c2 c3 c4 c5 c6 c7 c8 c9 c10]
        not-enough [c1 c2 c3 c4 c5 c6 c7 c8    c10 c11]]
    (testing "61 is enough to win"
      (is (enough-points? enough)))
    (testing "60 is not enough to win"
      (is (not (enough-points? not-enough))))))

(deftest schneider?-test
  (let [c1  (skat.cards.Card. :kreuz  :A)
        c2  (skat.cards.Card. :grun   :A)
        c3  (skat.cards.Card. :herz   :A)
        c4  (skat.cards.Card. :schell :A)
        c5  (skat.cards.Card. :kreuz  :10)
        c6  (skat.cards.Card. :grun   :10)
        c7  (skat.cards.Card. :herz   :10)
        c8  (skat.cards.Card. :schell :10)
        c9  (skat.cards.Card. :kreuz  :K)
        c10 (skat.cards.Card. :kreuz  :Q)
        c11 (skat.cards.Card. :kreuz  :W)
        enough     [c1 c2 c3 c4 c5 c6 c7 c8 c9     c11]
        not-enough [c1 c2 c3 c4 c5 c6 c7 c8    c10 c11]]
    (testing "90 is enough for schneider"
      (is (schneider? enough)))
    (testing "89 is not enough for schneider"
      (is (not (schneider? not-enough))))))

(deftest schwarz?-test
  (let [enough     deck
        not-enough (rest deck)]
    (testing "all cards taken is schawrz"
      (is (schwarz? enough)))
    (testing "at least one trick not taken is not schawrz"
      (is (not (schwarz? not-enough))))))
