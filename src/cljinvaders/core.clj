(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]
            [cljinvaders.asteroids :as asteroids]
            [cljinvaders.hit :as hit]))

(def backgroundImg (atom nil))
(def livesImg (atom nil))
(def screen-state (atom :start))

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
    (reset! backgroundImg (q/load-image background-img))
    (reset! livesImg (q/load-image "src/cljinvaders/img/player/life.png")))
  (asteroids/setup-asteroid-images)
  ;; For now it starts as game
  (reset! screen-state :game)
  {:color 0
   :player (player/init-player)
   :screen-width (q/screen-width)
   :screen-height (q/screen-height)})


(defn update-state [state]
  (let [updated-state (-> state
                          (asteroids/spawn-asteroids)  ; Spawn asteroids with a 1% chance
                          (asteroids/update-asteroids) ; Move asteroids
                          (hit/handle-hit hit/on-hit)  ; Hit controller
                          (hit/handle-player-hit hit/on-player-hit))] ; Hit player controller
    (if (and (= @screen-state :game) (empty? (:lives (:player updated-state))))  ; Check if player's lives are empty
      (do
        (reset! screen-state :end)  ; Change screen state to :end if lives are 0
        updated-state) 
      (assoc updated-state
             :color (mod (+ (:color updated-state) 0.7) 255)  ; Update color
             :player (-> updated-state :player player/update-player player/update-projectiles)))))  ; Update player and projectiles




(defn draw-state [state]
;; State when playing game
  (if (= @screen-state :game)
   (do
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  (q/image @backgroundImg 0 0)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Get player position from the state.
  (let [player (:player state)]
    ; Draw the plane at the specified coordinates based on player position.
    (q/image (:image player) (- (:x player) 76) (- (:y player) 75))
    (q/fill 0 0 255)
    (doseq [proj (:projectiles (:player state))]
      (q/ellipse (:x proj) (- (:y proj) 0) 5 10))
    ; Draw asteriuds
    (q/fill 255 0 0)  ; Color
    (doseq [asteroid (:asteroids state)]  
      (q/image (:image asteroid) (- (:x asteroid) (/ (:size asteroid) 2)) 
         (- (:y asteroid) (/ (:size asteroid) 2)) 
         (:size asteroid) 
         (:size asteroid)))
    ; Draw score
    (q/fill 255) ; White color
    (q/text-size 32)
    (q/text-align :left :top)
    (q/text (str "Score: " (:score player)) 10 10)
    ; Draw lives
     (let [lives (:lives player)
           x-pos (- (:screen-width state) 20 (* 50 (count lives)))
           y-pos (- (:screen-height state) 20 50)]
       (doseq [i (range (count lives))]
         (q/image @livesImg (+ x-pos (* 50 i)) y-pos 50 50)))))
    
    ;; State when game over
    (if (= @screen-state :end) 
      (do
        (q/background 240)
        (q/image @backgroundImg 0 0)
        (let [screen-width (q/screen-width)
              screen-height (q/screen-height)
              player-score (:score (:player state))]
          (q/fill 255) ; White color
          (q/text-size 180)
          (q/text-align :center :center)
          (q/text "GAME OVER" (/ screen-width 2) (/ screen-height 2))
          (q/text-size 60)
          (q/text (str "Score: " player-score) (/ screen-width 2) (+ (/ screen-height 2) 100))))
      ;; State at start
      (if (= @screen-state :start) 
      (do
        (q/background 240)
        (q/image @backgroundImg 0 0)
        (let [screen-width (q/screen-width)
              screen-height (q/screen-height)]
          (q/fill 255) ; White color
          (q/text-size 180)
          (q/text-align :center :center)
          (q/text "Click to start" (/ screen-width 2) (/ screen-height 2))))))))

(defn handle-key-pressed [state event]
  (if (= @screen-state :game) 
    (player/handle-key-pressed state event)
      (if (= @screen-state :end)
        (do 
          (reset! screen-state :start) 
          state) 
        (if (= @screen-state :start)
          (do
            (reset! screen-state :game)
            (assoc state 
                   :player (player/init-player)
                   :asteroids []))
          state))))

(q/defsketch cljinvaders
  :title "You shoot my asteroids right round"
  :size :fullscreen
  ; setup function called only once, during sketch initialization.
  :setup setup
  :key-pressed handle-key-pressed
  ; update-state is called on each iteration  before draw-state.
  :update update-state
  :draw draw-state
  ;; Function for handling key press (if needed in future).
  :features [:keep-on-top]
  :middleware [m/fun-mode])


