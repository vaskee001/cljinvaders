(ns cljinvaders.player
  (:require [quil.core :as q]))

(defn init-player []
  {:x 500 :y 900})  ; Initial position of the player (at the center of the screen)

(defn update-player [player]
  (assoc player
         :x (q/mouse-x)  ; Update the x position based on mouse's x-coordinate
         :y (q/mouse-y)))  ; Update the y position based on mouse's y-coordinate

(defn shoot [player]
  (update player :projectiles conj {:x (:x player) :y (:y player) :speed 10}))

(defn handle-key-pressed [state event]
  (let [key (:key event)]
    (= key :s) (update state :player shoot)))

(defn move-projectile [projectile]
  (update projectile :y #(- % (:speed projectile))))

(defn update-projectiles [player]
  (let [updated-projectiles (map move-projectile (:projectiles player))
        visible-projectiles (filter #(> (:y %) 0) updated-projectiles)]
    (assoc player :projectiles visible-projectiles)))


