(ns cljinvaders.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [cljinvaders.player :as player]
            [cljinvaders.asteroids :as asteroids]
            [cljinvaders.hit :as hit]
            [cljinvaders.dbconnect :as db]))

(def backgroundImg (atom nil))
(def livesImg (atom nil))
(def screen-state (atom :start))

;; Button definitions for start screen
(def new-game-button {:x 0 :y 0 :width 300 :height 80 :text "NEW GAME"})
(def scoreboard-button {:x 0 :y 0 :width 300 :height 80 :text "SCOREBOARD"})
(def player-name (atom ""))
(def input-active (atom false))



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
  ;; Reset player name when starting
  (reset! player-name "")
  (reset! input-active false)
  {:color 0
   :player (player/init-player)
   :screen-width (q/screen-width)
   :screen-height (q/screen-height)})


;; Draw input field
(defn draw-input-field [x y width height label text active?]
  ;; Draw label
  (q/fill 255)
  (q/text-size 24)
  (q/text-align :left :center)
  (q/text label (- x 10) (- y 25))

  ;; Draw input box
  (if active?
    (q/fill 255 0 0) ; Active
    (q/fill 0 0 128))  ; Inactive
  (q/stroke 255)
  (q/stroke-weight 2)
  (q/rect x y width height)

  ;; Draw text
  (q/fill 255)
  (q/text-size 28)
  (q/text-align :left :center)
  (let [display-text (if (empty? text) "Enter your name..." text)]
    (q/text display-text (+ x 10) (+ y (/ height 2))))

  ;; Draw cursor if active
  (when active?
    (let [text-width (q/text-width text)
          cursor-x (+ x 10 text-width)]
      (q/stroke 255)
      (q/stroke-weight 2)
      (q/line cursor-x (+ y 10) cursor-x (+ y height -10)))))


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
        ;; Input field position
        input-field {:x (- center-x 200) :y (- center-y 0)
                     :width 400 :height 50}
        ;; Position buttons centered on screen
        new-game-btn (assoc new-game-button
                            :x (- center-x 150)
                            :y (+ center-y 100))
        scoreboard-btn (assoc scoreboard-button
                              :x (- center-x 150)
                              :y (+ center-y 200))]
    (q/fill 255)
    (q/text-size 120)
    (q/text-align :center :center)
    (q/text "SPACE INVADERS" center-x (- center-y 100))
    ;; Draw input field
    (draw-input-field (:x input-field) (:y input-field)
                      (:width input-field) (:height input-field)
                      "Player Name:" @player-name @input-active)
    ;; Draw buttons (only enable New Game if name is entered)
    (if (empty? @player-name)
      (do
        ;; Disabled New Game button
        (q/fill 100) ; Gray background
        (q/stroke 150)
        (q/stroke-weight 3)
        (q/rect (:x new-game-btn) (:y new-game-btn) (:width new-game-btn) (:height new-game-btn))
        (q/fill 150) ; Gray text
        (q/text-size 32)
        (q/text-align :center :center)
        (q/text (:text new-game-btn)
                (+ (:x new-game-btn) (/ (:width new-game-btn) 2))
                (+ (:y new-game-btn) (/ (:height new-game-btn) 2))))
      ;; Enabled New Game button
      (draw-button new-game-btn))
    ;; Always draw scoreboard button
    (draw-button scoreboard-btn)
    ;; Return button and input field positions for click detection
    {:new-game-button new-game-btn
     :scoreboard-button scoreboard-btn
     :input-field input-field}))

(defn update-state [state]
  (let [updated-state (-> state
                          (asteroids/spawn-asteroids)  ; Spawn asteroids with a 1% chance
                          (asteroids/update-asteroids) ; Move asteroids
                          (hit/handle-hit hit/on-hit)  ; Hit controller
                          (hit/handle-player-hit hit/on-player-hit))] ; Hit player controller
    (if (and (= @screen-state :game) (empty? (:lives (:player updated-state))))  ; Check if player's lives are empty
      (do
        ;; Save score to database when game ends
        (when (and (not (empty? @player-name)) (> (:score (:player updated-state)) 0))
          (try
            (db/save-score @player-name (:score (:player updated-state)))
            (println (str "Score saved: " @player-name " - " (:score (:player updated-state))))
            (catch Exception e
              (println "Error saving score:" (.getMessage e)))))
        (reset! screen-state :end)  ; Change screen state to :end if lives are 0
        updated-state)
      (assoc updated-state
             :color (mod (+ (:color updated-state) 0.7) 255)  ; Update color
             :player (-> updated-state :player player/update-player player/update-projectiles)))))

(defn draw-scoreboard-screen [state]
  (q/cursor) 
  (q/background 0)
  (q/image @backgroundImg 0 0)
  (let [screen-width (:screen-width state)
        screen-height (:screen-height state)
        center-x (/ screen-width 2)
        title-y 100
        scores (get state :scoreboard [])
        back-btn {:x (- center-x 150) :y (- screen-height 100)
                  :width 300 :height 80 :text "BACK"}]
    ;; Draw title
    (q/fill 255)
    (q/text-size 80)
    (q/text-align :center :center)
    (q/text "SCOREBOARD" center-x title-y)

    (q/text-size 32)
    (q/text-align :center :center)
    (if (empty? scores)
      ;; Show message if no scores
      (do
        (q/text-size 24)
        (q/text "No scores yet!" center-x (+ title-y 150)))
      ;; Display scores
      (doseq [[idx score-entry] (map-indexed vector (take 10 scores))] ; Show top 10 scores
        (let [player-name (:player_name score-entry)
              score (:score score-entry)
              created-at (:created_at score-entry)
              y-pos (+ title-y 150 (* idx 50))]
          (q/text-size 28)
          (q/text (str (inc idx) ". " player-name ": " score)
                  center-x y-pos)
          ;; Draw timestamp below 
          (q/text-size 16)
          (q/fill 200) 
          (q/text (str created-at)
                  center-x (+ y-pos 20))
          (q/fill 255)))) 

    ;; Draw back button
    (draw-button back-btn)

    ;; Return back button position for click handling
    {:back-button back-btn}))

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
      (draw-start-screen state)
      (if (= @screen-state :scoreboard)
          (draw-scoreboard-screen state))
        ))
    ))


(defn handle-key-pressed [state event]
  (cond
    ;; Handle game input
    (= @screen-state :game)
    (player/handle-key-pressed state event)

    ;; Handle input field text entry
    (and (= @screen-state :start) @input-active)
    (let [key (:key event)
          raw-key (:raw-key event)
          key-code (:key-code event)]
      (cond
        ;; Handle backspace - try multiple ways
        (or (= key :backspace) (= raw-key 8) (= key-code 8))
        (do
          (swap! player-name #(if (empty? %) % (subs % 0 (dec (count %)))))
          state)

        ;; Handle enter key (deactivate input)
        (or (= key :enter) (= raw-key 10) (= key-code 10))
        (do
          (reset! input-active false)
          state)

        ;; Handle escape (deactivate input)
        (or (= key :esc) (= raw-key 27) (= key-code 27))
        (do
          (reset! input-active false)
          state)

        ;; Handle regular character input - convert keyword to character
        (and (keyword? key)
             (< (count @player-name) 20)) ; Limit name length
        (let [key-str (name key)]
          (if (= (count key-str) 1) ; Single character keys only
            (do
              (swap! player-name str key-str)
              state)
            (do
              (println "Multi-character key ignored:" key-str)
              state)))

        ;; Handle character input from raw-key
        (and (char? raw-key)
             (>= (int raw-key) 32)
             (<= (int raw-key) 126)
             (< (count @player-name) 20))
        (do
          (swap! player-name str raw-key)
          state)

        ;; Handle space key specifically
        (= key :space)
        (do
          (swap! player-name str " ")
          state)

        :else
        (do
          (println "Unhandled key - key:" key "raw-key:" raw-key "key-code:" key-code)
          state)))

    :else
    (do
      (when (= @screen-state :end)
        (reset! screen-state :start)
        (reset! player-name "")
        (reset! input-active false))
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
          ;; Input field position
          input-field {:x (- center-x 200) :y (- center-y 0)
                       :width 400 :height 50}
          ;; Recreate button positions
          new-game-btn (assoc new-game-button
                              :x (- center-x 150)
                              :y (+ center-y 100))
          scoreboard-btn (assoc scoreboard-button
                                :x (- center-x 150)
                                :y (+ center-y 200))
          mouse-x (:x event)
          mouse-y (:y event)]
      (cond
        ;; Check if input field was clicked
        (point-in-button? mouse-x mouse-y input-field)
        (do
          (reset! input-active true)
          state)

        ;; Check if New Game button was clicked (only if name is entered)
        (and (not (empty? @player-name))
             (point-in-button? mouse-x mouse-y new-game-btn))
        (do
          (reset! screen-state :game)
          (reset! input-active false)
          (assoc state
                 :player (player/init-player)
                 :asteroids []))

        ;; Check if Scoreboard button was clicked
        (point-in-button? mouse-x mouse-y scoreboard-btn)
        (do
          (reset! screen-state :scoreboard)
          (reset! input-active false)
          (assoc state :scoreboard (db/get-scoreboard)))

        :else
        (do
          ;; Clicked elsewhere, deactivate input
          (reset! input-active false)
          state)))

    ;; Handle scoreboard screen clicks
    (= @screen-state :scoreboard)
    (let [screen-width (:screen-width state)
          screen-height (:screen-height state)
          center-x (/ screen-width 2)
          back-btn {:x (- center-x 150) :y (- screen-height 100)
                    :width 300 :height 80 :text "BACK"}
          mouse-x (:x event)
          mouse-y (:y event)]
      (if (point-in-button? mouse-x mouse-y back-btn)
        (do
          (reset! screen-state :start)
          state)
        state))

    ;; Handle game over screen clicks - any click returns to start
    (= @screen-state :end)
    (do
      (reset! screen-state :start)
      (reset! player-name "") ; Reset name for new game
      (reset! input-active false)
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

(defn -main [& args]
  (println "Starting CljInvaders!")
  )