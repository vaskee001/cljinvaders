(ns cljinvaders.hit-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.hit :as hit]))

(facts "Test distance function"
       (fact "Classic pythagoras theorem"
             (hit/distance 0 0 3 4) => 5.0))

(facts "aTest hit function"
       (fact "Projectile should hit asteroid"
             (hit/hit? {:x 0 :y 0} {:x 0 :y 0 :size 10}) => true))
