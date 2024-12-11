(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]
            [cljinvaders.asteroids :as asteroids]))

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

(defn distance [x1 y1 x2 y2]
  (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))))

(defn hit? [projectile asteroid]
  (< (distance (:x projectile) (:y projectile) (:x asteroid) (:y asteroid))
     (/ (:size asteroid) 2)))

;; When projectile touch asteroid, both should be removed (Try 3)
(defn handle-hit [state on-hit]
  ;; First get their state before hit
  (let [projectiles (:projectiles (:player state))
        ;; Check if any is hit and return not hit. 
        ;; Events added to allow points
        asteroids (:asteroids state)
        results (reduce (fn [[remaining-projectiles remaining-asteroids events] proj]
                          (if-let [hit (some #(when (hit? proj %) %) remaining-asteroids)]
                            [(remove #{proj} remaining-projectiles)
                             (remove #{hit} remaining-asteroids)
                             (conj events (on-hit proj hit))]
                            [remaining-projectiles remaining-asteroids events]))
                        [projectiles asteroids []]
                        projectiles)]
    (-> state
        (assoc-in [:player :projectiles] (first results))
        (assoc :asteroids (second results))
        (assoc :events (nth results 2)))))

(defn on-hit [projectile asteroid]
  {
   :type :add-points
   :projectile projectile
   :asteroid asteroid})

(defn update-state [state]
  ; Update the game state.
  (let [updated-state (-> state
                          (asteroids/spawn-asteroids)  ; Spawn asteroids with a 1% chance
                          (asteroids/update-asteroids) ; Move asteroids
                          (handle-hit on-hit))] ; Hit controller
    (assoc updated-state
           :color (mod (+ (:color updated-state) 0.7) 255)  ; Update color
           :player (-> updated-state :player player/update-player player/update-projectiles))))  ; Update player and projectiles


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
      (q/ellipse (:x proj) (- (:y proj) 60) 5 10))
    ; Draw asteriuds
    (q/fill 255 0 0)  ; Color
    (doseq [asteroid (:asteroids state)]  
      (q/ellipse (:x asteroid) (:y asteroid) (:size asteroid) (:size asteroid)))))  ;))

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




