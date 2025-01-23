(ns cljinvaders.player
  (:require [quil.core :as q]))

(def planeImg (atom nil))

(defn setup-player-images []
  (reset! planeImg
          [(q/load-image "src/cljinvaders/img/player/plane1.png")
           (q/load-image "src/cljinvaders/img/player/plane2.png")]))

(defn random-plane-img []
  (rand-nth @planeImg))

(defn init-player []
  {:x 500 
   :y 900
   :image (random-plane-img) 
   :last-shot-time 0
   :shooting-cooldown 500
   :lives [:life :life :life]
   :score 0}) 


(defn update-player [player]
  (assoc player
         :x (q/mouse-x)  ; Update the x position based on mouse's x-coordinate
         :y (q/mouse-y)
         ))  ; Update the y position based on mouse's y-coordinate

(defn shoot [player]
  (let [current-time (q/millis)
        time-diff (- current-time (:last-shot-time player))
        shooting-cooldown (:shooting-cooldown player)]  
    (if (>= time-diff shooting-cooldown)  ; Check if cooldown has passed
      (let [new-player (assoc player
                              :projectiles (conj (:projectiles player) {:x (:x player) :y (- (:y player) 50) :speed 10})
                              :last-shot-time current-time)]  
        new-player)
      player)))

(defn handle-key-pressed [state event]
  (let [key (:key event)]
    (if (= key :s) (update state :player shoot) state)))

(defn move-projectile [projectile]
  (update projectile :y #(- % (:speed projectile))))

(defn update-projectiles [player]
  (let [updated-projectiles (map move-projectile (:projectiles player))
        visible-projectiles (filter #(> (:y %) 0) updated-projectiles)]
    (assoc player :projectiles visible-projectiles)))
