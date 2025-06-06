(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]
            [cljinvaders.asteroids :as asteroids]
            [cljinvaders.hit :as hit]))

(def backgroundImg (atom nil))
(def livesImg (atom nil))
(def screen-state (atom :start))

;; Button definitions for start screen
(def new-game-button {:x 0 :y 0 :width 300 :height 80 :text "NEW GAME"})
(def scoreboard-button {:x 0 :y 0 :width 300 :height 80 :text "SCOREBOARD"})




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
  (reset! screen-state :start)
  {:color 0
   :player (player/init-player)
   :screen-width (q/screen-width)
   :screen-height (q/screen-height)})

;; Draw a button with text
(defn draw-button [button]
  (q/fill  0 150 255) ; Blue button background
  (q/stroke 255)
  (q/stroke-weight 3)
  (q/rect (:x button) (:y button) (:width button) (:height button))
  (q/fill 255) ; White text
  (q/text-size 32)
  (q/text-align :center :center)
  (q/text (:text button)
          (+ (:x button) (/ (:width button) 2))
          (+ (:y button) (/ (:height button) 2))))

;; Draw start screen with buttons
(defn draw-start-screen [state]
  (q/cursor) ; Show cursor on start screen
  (q/background 240)
  (q/image @backgroundImg 0 0)
  (let [screen-width (:screen-width state)
        screen-height (:screen-height state)
        center-x (/ screen-width 2)
        center-y (/ screen-height 2)
        ;; Position buttons centered on screen
        new-game-btn (assoc new-game-button
                            :x (- center-x 150)
                            :y (+ center-y 50))
        scoreboard-btn (assoc scoreboard-button
                              :x (- center-x 150)
                              :y (+ center-y 150))]
    (q/fill 255)
    (q/text-size 120)
    (q/text-align :center :center)
    (q/text "SPACE INVADERS" center-x (- center-y 100))
    ;; Draw buttons
    (draw-button new-game-btn)
    (draw-button scoreboard-btn)
    ;; Return button positions for click detection
    {:new-game-button new-game-btn
     :scoreboard-button scoreboard-btn}))


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
   (q/no-cursor)  
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
      (draw-start-screen state) ))))

(defn handle-key-pressed [state event]
  (if (= @screen-state :game)
    (player/handle-key-pressed state event)
    (if (= @screen-state :end)
      (do
        (reset! screen-state :start)
        state)
      state)))


;; Check if mouse click is within button bounds
(defn point-in-button? [x y button]
  (and (>= x (:x button))
       (<= x (+ (:x button) (:width button)))
       (>= y (:y button))
       (<= y (+ (:y button) (:height button)))))


;; Handle mouse clicks for start screen buttons
(defn handle-mouse-pressed [state event]
  (cond
    ;; Handle start screen clicks
    (= @screen-state :start)
    (let [screen-width (:screen-width state)
          screen-height (:screen-height state)
          center-x (/ screen-width 2)
          center-y (/ screen-height 2)
          ;; Recreate button positions (same as in draw-start-screen)
          new-game-btn (assoc new-game-button
                              :x (- center-x 150)
                              :y (+ center-y 50))
          scoreboard-btn (assoc scoreboard-button
                                :x (- center-x 150)
                                :y (+ center-y 150))
          mouse-x (:x event)
          mouse-y (:y event)]
      (cond
        ;; Check if New Game button was clicked
        (point-in-button? mouse-x mouse-y new-game-btn)
        (do
          (println "New Game clicked!") ; Debug message
          (reset! screen-state :game)
          (assoc state
                 :player (player/init-player)
                 :asteroids []))
        ;; Check if Scoreboard button was clicked
        (point-in-button? mouse-x mouse-y scoreboard-btn)
        (do
          ;; For now, just print message - implement scoreboard later
          (println "Scoreboard clicked - not implemented yet")
          state)
        :else
        (do
          (println (str "Mouse clicked at: " mouse-x "," mouse-y)) ; Debug message
          state)))

    ;; Handle game over screen clicks - any click returns to start
    (= @screen-state :end)
    (do
      (println "Game over screen clicked - returning to start")
      (reset! screen-state :start)
      state)

    ;; For any other state, do nothing
    :else state))



(q/defsketch cljinvaders
  :title "You shoot my asteroids right round"
  :size :fullscreen
  ; setup function called only once, during sketch initialization.
  :setup setup
  :key-pressed handle-key-pressed
  :mouse-pressed handle-mouse-pressed
  ; update-state is called on each iteration  before draw-state.
  :update update-state
  :draw draw-state
  ;; Function for handling key press (if needed in future).
  :features [:keep-on-top] 
  :middleware [m/fun-mode])


