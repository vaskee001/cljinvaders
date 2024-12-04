(ns cljinvaders.player
  (:require [quil.core :as q]))

(defn init-player []
  {:x 500 :y 900})  ; Initial position of the player (at the center of the screen)

(defn update-player [player]
  (assoc player
         :x (q/mouse-x)  ; Update the x position based on mouse's x-coordinate
         :y (q/mouse-y)))  ; Update the y position based on mouse's y-coordinate
