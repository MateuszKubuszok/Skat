(ns skat.helpers_test
  (:require [clojure.test :refer :all]
            [skat.helpers :refer :all]))

(deftest coll-contains?-test
  (testing "should return true if collection contains element"
    (is (coll-contains? [1 2 3] 2)))
  (testing "should return false if collection doesn't contains element"
    (is (not (coll-contains? [1 2 3] 4)))))

(deftest list-from-test
  (testing "creates list of mapped values"
    (is (= (list-from identity '(1 2 3))
           '(3 2 1)))))

(deftest append-test
  (testing "joins two collections."
    (is (= (append '(1 2) '(3 4))
           '(4 3 1 2)))
    (is (= (append [1 2] [3 4])
            [1 2 3 4]))))

(deftest update-all-test
  (testing "updates all map values"
    (is (= (update-all { :a 1, :b 2, :c 3 } inc)
            { :a 2, :b 3, :c 4 }))))

