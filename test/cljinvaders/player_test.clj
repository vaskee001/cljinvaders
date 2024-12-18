(ns cljinvaders.player-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.player :as player]))

(facts "Test move-projectile function"
       (fact ":y is updated with speed"
             (let [projectile {:x 100 :y 100 :speed 5}
                   updated-projectile (player/move-projectile projectile)]
               (:y updated-projectile)) => 95))

(facts "Test update-projectiles function"
       (fact "Projectile should be removed when off-screen"
             (let [player {:projectiles [{:x 100 :y 100 :speed 10}
                                         {:x 100 :y -5 :speed 10}]} ;This one is off-screen
                   updated-player (player/update-projectiles player)]
               (count (:projectiles updated-player))) => 1))

