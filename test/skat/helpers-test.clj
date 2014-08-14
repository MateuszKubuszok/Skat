(ns skat.helpers-test
  (:require [clojure.test :refer :all]
            [skat.helpers :refer :all]))

(deftest list-from-test
  (testing "creates list of mapped values"
    (is (=
          (list-from identity '(1 2 3))
          '(3 2 1)))))

(deftest append-test
  (testing "joins two collections."
    (is (=
          (append '(1 2) '(3 4))
          '(4 3 1 2)))
    (is (=
          (append [1 2] [3 4])
          [1 2 3 4]))))
