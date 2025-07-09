(ns cljinvaders.player-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.player :as player]
            [quil.core :as q]))

(facts "init-player returns correct state"
       (fact "Player has three lives and zero score at start"
             (let [p (player/init-player)]
               (:lives p) => [:life :life :life]
               (:score p) => 0)))

(facts "Player shooting cooldown logic"
       (fact "Player can't shoot again before cooldown"
             (with-redefs [q/millis (fn [] 1000)]
               (let [player-state {:x 10 :y 10 :last-shot-time 950 :shooting-cooldown 100 :projectiles []}]
                 ;; Only 50ms passed since last shot, and cooldown is 100 so player can't shoot
                 (player/shoot player-state) => player-state))))



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

