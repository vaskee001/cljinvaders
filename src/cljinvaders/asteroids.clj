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

(defn create-asteroid [screen-width screen-height score]
  (let [edge (rand-nth [:top :bottom :left :right])
        angle (rand (* 2 Math/PI))] ; Random angle in whitch asteroid will move
    (case edge
      :top {:x (rand screen-width)
            :y -50
            :size (+ 30 (rand 100))
            :speed (+ 3 (rand 3) (/ score 500))
            :angle angle 
            :image (random-asteroid-img)}
      :bottom {:x (rand screen-width)
               :y (+ screen-height 50)
               :size (+ 30 (rand 100))
               :speed (+ 3 (rand 3) (/ score 500))
               :angle angle
               :image (random-asteroid-img)}
      :left {:x -50
             :y (rand screen-height)
             :size (+ 30 (rand 100))
             :speed (+ 3 (rand 3) (/ score 500))
             :angle angle
             :image (random-asteroid-img)}
      :right {:x (+ screen-width 50)
              :y (rand screen-height)
              :size (+ 30 (rand 100))
              :speed (+ 3 (rand 3) (/ score 500))
              :angle angle
              :image (random-asteroid-img)})))


(defn spawn-asteroids [state]
  ; There is 1% chance asteroid is spawned (every 100th frame)
  (if (< (rand) 0.1)
    (update state :asteroids conj (create-asteroid (:screen-width state) (:screen-height state) (:score (:player state))))
    state))

(defn update-asteroids [state]
  (let [moved-asteroids (map (fn [asteroid]
                               (let [angle (:angle asteroid) ; Use the stored angle
                                     speed (:speed asteroid)
                                     dx (* speed (Math/cos angle)) ; Horizontal movement
                                     dy (* speed (Math/sin angle))] ; Vertical movement
                                 (-> asteroid
                                     (update :x #(+ % dx))  ; Update x position
                                     (update :y #(+ % dy))))) ; Update y position
                             (:asteroids state))
        visible-asteroids (filter #(<= (:y %) (:screen-height state)) moved-asteroids)]
    (assoc state :asteroids visible-asteroids)))

