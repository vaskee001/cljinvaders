(ns cljinvaders.asteroids)

(defn create-asteroid [screen-width]
  ; Init asteroid on random place with random speed (between 2 and 3 for now)
  {:x (rand screen-width)  
   :y -50                  
   :speed (+ 2 (rand 3))}) 

(defn spawn-asteroids [state]
  ; There is 2% chance asteroid is spawned (every 50th frame)
  (if (< (rand) 0.02) 
    (update state :asteroids conj (create-asteroid (:screen-width state)))
    state))