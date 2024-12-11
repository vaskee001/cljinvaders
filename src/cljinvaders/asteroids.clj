(ns cljinvaders.asteroids
  (:require [quil.core :as q]))

(def asteroidImgs (atom []))

(defn random-asteroid-img []
  (rand-nth @asteroidImgs))

(defn setup-asteroid-images []
  (reset! asteroidImgs
          [(q/load-image "src/cljinvaders/img/asteroids/asteroid1.png")
           (q/load-image "src/cljinvaders/img/asteroids/asteroid2.png")
           (q/load-image "src/cljinvaders/img/asteroids/asteroid3.png")]))

(defn create-asteroid [screen-width]
  ; Init asteroid on random place with random speed (between 2 and 3 for now)
  {:x (rand screen-width)
   :y -50
   :size (+ 30 (rand 100))
   :speed (+ 3 (rand 3))
   :image (random-asteroid-img)})

(defn spawn-asteroids [state]
  ; There is 1% chance asteroid is spawned (every 100th frame)
  (if (< (rand) 0.01)
    (update state :asteroids conj (create-asteroid (:screen-width state)))
    state))

(defn update-asteroids [state]
  (let [moved-asteroids (map (fn [asteroid]
                               (update asteroid :y #(+ % (:speed asteroid))))
                             (:asteroids state))
        visible-asteroids (filter #(<= (:y %) (:screen-height state)) moved-asteroids)]
    (assoc state :asteroids visible-asteroids)))
