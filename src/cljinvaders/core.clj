(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  ; Set frame rate to 60 frames per second.
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :playerX 500
   :playerY 900})

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  (assoc state :color (mod (+ (:color state) 0.7) 255)))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Calculate x and y coordinates of the circle.
  (let [x (:playerX state)
         y (:playerY state)]
      ; Draw the ellipse at the specified coordinates.
     (q/ellipse x y 100 100)))

(defn handle-key-pressed [state event]
  (let [key (:key event)]  ; Extract the key from the event
    (cond
      (= key :left) (update state :playerX #(max 0 (- % 10)))   ; Left arrow
      (= key :right) (update state :playerX #(min (q/width) (+ % 10))) ; Right arrow
      (= key :up) (update state :playerY #(max 0 (- % 10)))     ; Up arrow
      (= key :down) (update state :playerY #(min (q/height) (+ % 10))) ; Down arrow
      :else state)))



(q/defsketch cljinvaders
  :title "You move my circle right round"
  :size [1000 1000]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ;; Function for moving
  :key-pressed handle-key-pressed
  :features [:keep-on-top]
  :middleware [m/fun-mode])
