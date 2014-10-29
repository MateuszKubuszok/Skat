(ns skat.game_test
  (:require [clojure.test :refer :all]
  	        skat.cards
            [skat.game :refer :all]))

(def c1 (skat.cards.Card. :kreuz :W))
(def c2 (skat.cards.Card. :kreuz :K))
(def c3 (skat.cards.Card. :schell :W))
(def players-cards { :p1 #{c1 c2}, :p2 #{c1 c3}, :p3 #{c2 c3} })

(defn map-equal [m1 m2] (.equals (into (sorted-map) m1) (into (sorted-map) m2)))

(deftest update-cards-played-test
  (let [all #{c1 c2 c3}
        played-now { :p1 c3, :p2 c2, :p3 c1 }]
    (testing "adds cards played at given turn"
      (is (=
        (update-cards-played players-cards played-now)
        { :p1 all :p2 all :p3 all })))))

(deftest update-cards-owned-test
  (let [played-now { :p1 c1, :p2 c3, :p3 c2 }]
    (testing "removed cards played at given turn"
      (is (=
        (update-cards-owned players-cards played-now)
        { :p1 #{c2} :p2 #{c1} :p3 #{c3} })))))

 (deftest update-knowledge-test
   (let [p-knowledge (skat.game.PlayerKnowledge. :p1
                                                 { :p1 #{}, :p2 #{} }
                                                 { :p1 #{ c1 }, :p2 #{} })
         knowledge { :p1 p-knowledge }
         played-now { :p1 c1, :p2 c2 }]
     (testing "updates played and owned cards"
       (is (=
         (update-knowledge knowledge played-now)
         { :p1 (skat.game.PlayerKnowledge. :p1
                                           { :p1 #{ c1 }, :p2 #{ c2 } }
                                           { :p1 #{}, :p2 #{} }) })))))

 (deftest next-turn-order-test
   (let [p1-start { :p1 :p1, :p2 :p2, :p3 :p3 }
         p2-start { :p1 :p2, :p2 :p3, :p3 :p1 }
         p3-start { :p1 :p3, :p2 :p1, :p3 :p2 }]
     (testing "winner rotates correctly"
       (is (= (next-turn-order p1-start :p1) p1-start))
       (is (= (next-turn-order p1-start :p2) p2-start))
       (is (= (next-turn-order p1-start :p3) p3-start))
       (is (= (next-turn-order p2-start :p1) p1-start))
       (is (= (next-turn-order p2-start :p2) p2-start))
       (is (= (next-turn-order p2-start :p3) p3-start))
       (is (= (next-turn-order p3-start :p1) p1-start))
       (is (= (next-turn-order p3-start :p2) p2-start))
       (is (= (next-turn-order p3-start :p3) p3-start)))))
 
 (deftest next-turn-test
   (testing "next turn rotates correctly"
     (is (=
       (next-turn (skat.game.Turn. {} {} { :p1 :p1, :p2 :p2, :p3 :p3 }) :p2)
       (skat.game.Turn. {} {} { :p1 :p2, :p2 :p3, :p3 :p1 })))))
 
 
 