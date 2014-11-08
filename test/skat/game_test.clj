(ns skat.game_test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [clojure.tools.trace :refer :all]
            [skat.helpers :as helpers]
  	        [skat.cards :refer :all]
            [skat.game :refer :all]))

(defn play-first [{:keys [:cards-allowed]} & _] (first cards-allowed))
(defn first-won [_ _ _] :p1)
(def pl1 (skat.game.Player. "p1" play-first play-first play-first))
(def pl2 (skat.game.Player. "p2" play-first play-first play-first))
(def pl3 (skat.game.Player. "p3" play-first play-first play-first))
(def order { :p1 pl1, :p2 pl2, :p3 pl3 })
(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :kreuz :K))
(def c3 (skat.cards.Card. :schell :W))
(def played-cards { pl1 [], pl2 [], pl3 [] })
(def players-cards { pl1 #{c1 c2}, pl2 #{c1 c3}, pl3 #{c2 c3} })
(def conf-grand (skat.game.Configuration. :grand true false first-won))
(def conf-kreuz (skat.game.Configuration. :kreuz true false first-won))
(def conf-null  (skat.game.Configuration. :null  true false first-won))

(defn map-equal [m1 m2] (= (into (hash-map) m1) (into (hash-map) m2)))

(deftest update-cards-played-test
  (let [played-now { pl1 c3, pl2 c2, pl3 c1 }]
    (testing "adds cards played at given turn"
      (is (map-equal
        (update-cards-played played-cards played-now)
        { pl1 [c3], pl2 [c2], pl3 [c1] })))))

(deftest update-cards-owned-test
  (let [played-now { pl1 c1, pl2 c3, pl3 c2 }]
    (testing "removed cards played at given turn"
      (is (map-equal
        (update-cards-owned players-cards played-now)
        { pl1 #{c2}, pl2 #{c1}, pl3 #{c3} })))))

(deftest update-knowledge-test
  (let [p-knowledge (skat.game.PlayerKnowledge. pl1 played-cards players-cards)
        knowledge { pl1 p-knowledge }
        played-now { pl1 c1, pl2 c3, pl3 c2 }]
    (testing "updates played and owned cards"
      (is (=
        (update-knowledge knowledge played-now)
        { pl1 (skat.game.PlayerKnowledge. pl1
                                          { pl1 [c1], pl2 [c3], pl3 [c2] }
                                          { pl1 #{c2}, pl2 #{c1}, pl3 #{c3} }) })))))

(deftest figure-situation-test
  (letfn [(mock-knowledge [pl]
            (skat.game.PlayerKnowledge. pl played-cards players-cards))]
    (let [p1-knowledge (mock-knowledge pl1)
          p2-knowledge (mock-knowledge pl2)
          p3-knowledge (mock-knowledge pl3)]  
      (testing "allowed cards are properly calculated"
        (is (=
          (:cards-allowed (figure-situation conf-grand p1-knowledge order c3))
          #{c1 c2}))
        (is (=
          (:cards-allowed (figure-situation conf-grand p2-knowledge order c2))
          #{c1 c3}))
        (is (=
          (:cards-allowed (figure-situation conf-grand p3-knowledge order c1))
          #{c2 c3}))
        (is (=
          (:cards-allowed (figure-situation conf-kreuz p1-knowledge order c3))
          #{c1 c2}))
        (is (=
          (:cards-allowed (figure-situation conf-kreuz p2-knowledge order c2))
          #{c1 c3}))
        (is (=
          (:cards-allowed (figure-situation conf-kreuz p3-knowledge order c1))
          #{c2 c3}))
        (is (=
          (:cards-allowed (figure-situation conf-null p1-knowledge order c3))
          #{c1 c2}))
        (is (=
          (:cards-allowed (figure-situation conf-null p2-knowledge order c2))
          #{c1 c3}))
        (is (=
          (:cards-allowed (figure-situation conf-null p3-knowledge order c1))
          #{c2 c3}))))))

(deftest next-turn-order-test
  (let [p1-start { :p1 pl1, :p2 pl2, :p3 pl3 }
        p2-start { :p1 pl2, :p2 pl3, :p3 pl1 }
        p3-start { :p1 pl3, :p2 pl1, :p3 pl2 }]
    (testing "winner rotates correctly"
      (is (= (next-turn-order p1-start pl1) p1-start))
      (is (= (next-turn-order p1-start pl2) p2-start))
      (is (= (next-turn-order p1-start pl3) p3-start))
      (is (= (next-turn-order p2-start pl1) p1-start))
      (is (= (next-turn-order p2-start pl2) p2-start))
      (is (= (next-turn-order p2-start pl3) p3-start))
      (is (= (next-turn-order p3-start pl1) p1-start))
      (is (= (next-turn-order p3-start pl2) p2-start))
      (is (= (next-turn-order p3-start pl3) p3-start)))))
 
(deftest next-turn-test
  (testing "next turn rotates correctly"
    (is (=
      (next-turn (skat.game.Turn. { :p1 pl1, :p2 pl2, :p3 pl3 }) pl2)
      (skat.game.Turn. { :p1 pl2, :p2 pl3, :p3 pl1 })))))

(deftest play-turn-test
  (letfn [(mock-knowledge [pl]
            (skat.game.PlayerKnowledge. pl played-cards players-cards))]
    (let [p1-knowledge (mock-knowledge pl1)
          p2-knowledge (mock-knowledge pl2)
          p3-knowledge (mock-knowledge pl3)
          knowledge { pl1 p1-knowledge, pl2 p2-knowledge, pl3 p3-knowledge }
          turn (skat.game.Turn. order)
          deal (skat.game.Deal. knowledge turn #{})
          next-deal (skat.game.Deal.
                      (update-knowledge knowledge { pl1 c1, pl2 c3, pl3 c3 })
                      (next-turn turn pl1)
                      #{})]
      (testing "calculates next turn"
        (is (= (play-turn conf-grand deal) next-deal))))))
