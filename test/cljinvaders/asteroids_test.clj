(ns cljinvaders.asteroids-test
  (:require [midje.sweet :refer :all]
            [cljinvaders.asteroids :as asteroids]))

(with-redefs [cljinvaders.asteroids/asteroidImgs (atom ["dummy-img"])]
(facts "Test create asteroid function"
       (fact "the :x value should not be bigger than screen width + 50"
             (let [asteroid (asteroids/create-asteroid 1920 1080 800)]
               (and (>= (:x asteroid) -50) (<= (:x asteroid) 1970))) => true)

       
       (fact "the :y value should always be from -51 to 1131"
             (let [asteroid (asteroids/create-asteroid 1920 1080 800)]
               (and (> (:y asteroid) -51) (< (:y asteroid) 1131))) => true)
       
       (fact "the :size value should be between 30 and 130 (values may change later)"
             (let [asteroid (asteroids/create-asteroid 1920 1080 800)]
               (and (>= (:size asteroid) 30) (< (:size asteroid) 130))) => true)
       
       (fact "the :speed value should be between 3 and 6"
             (let [asteroid (asteroids/create-asteroid 1920 1080 0)]
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
                     :asteroids [{:x 100 :y 100 :speed 3 :angle (/ Math/PI 2)}]} ; Pi/2 is same angle as falling down
              new-state (asteroids/update-asteroids state)]
          (let [asteroid (first (:asteroids new-state))]
            (:y asteroid))) => 103.0)
  (fact "off-screen asteroids should be removed"
        (let [state {:screen-width 1920
                     :screen-height 1080
                     ; Speed added by 5 so 1081 is bigger than 1080 and asteroid is removed
                     :asteroids [{:x 100 :y 1076 :speed 5 :angle (/ Math/PI 2)}]}
              new-state (asteroids/update-asteroids state)]
          (count (:asteroids new-state))) => 0 )))