(ns cljinvaders.hit-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.hit :as hit]))

(facts "Test distance function"
       (fact "Classic pythagoras theorem"
             (hit/distance 0 0 3 4) => 5.0))

(facts "Test hit function"
       (fact "Projectile should hit asteroid"
             (hit/hit? {:x 0 :y 0} {:x 0 :y 0 :size 10}) => true)
       (fact "Projectile shouldn't hit"
             (hit/hit? {:x 0 :y 0} {:x 20 :y 20 :size 10}) => false))

(facts "Test handle-hit function"
       (fact "it removes the projectile and asteroid when they touch"
             (let [state {:player {:projectiles [{:x 0 :y 0}]}
                          :asteroids [{:x 0 :y 0 :size 10}]
                          :events []}
                   new-state (hit/handle-hit state hit/on-hit)]
                ;; There should be any asteroids or projectiles since it was only one and it is hit
               (and (empty? (get-in new-state [:player :projectiles]))
                    (empty? (:asteroids new-state)))) => true)

       (fact "it removes the projectile and asteroid when they touch"
             (let [state {:player {:projectiles [{:x 0 :y 0} {:x 30 :y 40}]}
                          :asteroids [{:x 0 :y 0 :size 10}]
                          :events []}
                   new-state (hit/handle-hit state hit/on-hit)]
                ;; There were two projectiles so one is left
               (empty? (get-in new-state [:player :projectiles]))) => false))


(facts "Test handle-player-hit function"
       (fact "it removes one life when the player hits an asteroid"
             (let [state {:player {:x 500 :y 900 :lives [:life :life :life]}
                          :asteroids [{:x 500 :y 900 :size 10}]
                          :events []}
                   new-state (hit/handle-player-hit state hit/on-player-hit)]
               (count (get-in new-state [:player :lives])) => 2))) 

