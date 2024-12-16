(ns cljinvaders.asteroids-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.asteroids :as asteroids]))

(facts "Test create asteroid function"
       (fact "the :x value should not be bigger than screen width"
             (let [asteroid (asteroids/create-asteroid 1920)]
               (and (< (:x asteroid) 0) (< (:x asteroid) 1920))) => false)
       
       (fact "the :y value should always be -50"
             (let [asteroid (asteroids/create-asteroid 1920)]
               (:y asteroid)) => -50)
       
       (fact "the :size value should be between 30 and 130 (values may change later)"
             (let [asteroid (asteroids/create-asteroid 1920)]
               (and (>= (:size asteroid) 30) (< (:size asteroid) 130))) => true)
       
       (fact "the :speed value should be between 3 and 6"
             (let [asteroid (asteroids/create-asteroid 1920)]
               (and (>= (:speed asteroid) 3) (< (:speed asteroid) 6))) => true))

(facts "Test spawn asteroid function"
       (fact "spawn first asteroid"
             (let [state {:screen-width 1920 :screen-height 1080 :asteroids []}
                   new-state (asteroids/spawn-asteroids state)]
               (<= (count (:asteroids new-state)) (count (:asteroids state)) 1)) => true))


(facts "test update asteroids function"
  (fact ":y should be incremented by speed (3)"
        (let [state {:screen-width 1920
                     :screen-height 1080
                     :asteroids [{:x 100 :y 100 :speed 5}]}
              new-state (asteroids/update-asteroids state)]
          (let [asteroid (first (:asteroids new-state))]
            (:y asteroid))) => 105))






