(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]
            [cljinvaders.asteroids :as asteroids]
            [cljinvaders.hit :as hit]))

(def backgroundImg (atom nil))


(defn setup []
  ; Set frame rate to 60 frames per second.
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; Hide the mouse cursor
  (q/no-cursor)
  ; Setup function returns initial state. It contains
  ; circle color and position.
  (player/setup-player-images)
  (let [screen-width (q/screen-width)
        screen-height (q/screen-height)
        background-img (cond
                         (< screen-width 1921) "src/cljinvaders/img/background/background.png"
                         (< screen-width 2561 ) "src/cljinvaders/img/background/background2k.png"
                         :else "src/cljinvaders/img/background/background4k.png")]
    (reset! backgroundImg (q/load-image background-img)))
  (asteroids/setup-asteroid-images)
  {:color 0
   :player (player/init-player)
   :screen-width (q/screen-width)
   :screen-height (q/screen-height)})


(defn update-state [state]
  ; Update the game state.
  (let [updated-state (-> state
                          (asteroids/spawn-asteroids)  ; Spawn asteroids with a 1% chance
                          (asteroids/update-asteroids) ; Move asteroids
                          (hit/handle-hit hit/on-hit)  ; Hit controller
                          (hit/handle-player-hit hit/on-player-hit))] ; Hit player controller
    (assoc updated-state
           :color (mod (+ (:color updated-state) 0.7) 255)  ; Update color
           :player (-> updated-state :player player/update-player player/update-projectiles))))  ; Update player and projectiles


(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  (q/image @backgroundImg 0 0)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Get player position from the state.
  (let [player (:player state)]
    ; Draw the plane at the specified coordinates based on player position.
    (q/image (:image player) (- (:x player) 76) (- (:y player) 75))
    ; Draw testing circle
    (q/ellipse (:x player) (:y player) 10 10)
    (q/fill 0 0 255)
    (doseq [proj (:projectiles (:player state))]
      (q/ellipse (:x proj) (- (:y proj) 60) 5 10))
    ; Draw asteriuds
    (q/fill 255 0 0)  ; Color
    (doseq [asteroid (:asteroids state)]  
      (q/image (:image asteroid) (- (:x asteroid) (/ (:size asteroid) 2)) 
         (- (:y asteroid) (/ (:size asteroid) 2)) 
         (:size asteroid) 
         (:size asteroid)))))

(q/defsketch cljinvaders
  :title "You shoot my asteroids right round"
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

