(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]))

(def planeImg (atom nil))


(defn setup []
  ; Set frame rate to 60 frames per second.
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; Hide the mouse cursor
  (q/no-cursor)
  ; Setup function returns initial state. It contains
  ; circle color and position.
  (reset! planeImg (q/load-image "plane.png"))
  {:color 0
   :player (player/init-player)
   :screen-width (q/screen-width)
   :screen-height (q/screen-height)})

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  (assoc state
         :color (mod (+ (:color state) 0.7) 255)
         :player (-> state :player player/update-player player/update-projectiles)))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Get player position from the state.
  (let [player (:player state)]
    ; Draw the plane at the specified coordinates based on player position.
    (q/image @planeImg (- (:x player) 76) (- (:y player) 75))
    ; Draw testing circle
    (q/ellipse (:x player) (:y player) 10 10)
    (q/fill 0 0 255)
    (doseq [proj (:projectiles (:player state))]
      (q/ellipse (:x proj) (- (:y proj) 60) 5 10))))

(q/defsketch cljinvaders
  :title "You shoot my plane right round"
  :size :fullscreen
  ; setup function called only once, during sketch initialization.
  :setup setup
  :key-pressed player/handle-key-pressed
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ;; Function for handling key press (if needed in future).
  :features [:keep-on-top]
  :middleware [m/fun-mode])



