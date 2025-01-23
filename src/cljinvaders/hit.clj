(ns cljinvaders.hit
  (:require [cljinvaders.player :as player]))

(defn distance [x1 y1 x2 y2]
  (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))))

(defn hit? [projectile asteroid]
  (< (distance (:x projectile) (:y projectile) (:x asteroid) (:y asteroid))
     (/ (:size asteroid) 2)))

(defn hit2? [player asteroid]
  (< (distance (:x player) (:y player) (:x asteroid) (:y asteroid))
     (+ (/ (:size asteroid) 2) 50)))

;; When projectile touch asteroid, both should be removed 
(defn handle-hit [state on-hit]
  ;; First get their state before hit
  (let [projectiles (:projectiles (:player state))
        ;; Check if any is hit and return not hit. 
        ;; Events added to allow points
        asteroids (:asteroids state)
        results (reduce (fn [[remaining-projectiles remaining-asteroids events score] proj]
                          (if-let [hit (some #(when (hit? proj %) %) remaining-asteroids)]
                            [(remove #{proj} remaining-projectiles)
                             (remove #{hit} remaining-asteroids)
                             (conj events (on-hit proj hit))
                             (+ score (- 150 (Math/round (:size hit))))] ;Making score round number 
                            [remaining-projectiles remaining-asteroids events score]))
                        [projectiles asteroids [] 0]
                        projectiles)
        updated-score (+ (:score (:player state)) (nth results 3))]
    (-> state
        (assoc-in [:player :projectiles] (first results))
        (assoc :asteroids (second results))
        (assoc :events (nth results 2))
        (assoc-in [:player :score] updated-score) )))


(defn on-hit [projectile asteroid]
  {:type :add-points
   :projectile projectile
   :asteroid asteroid})


;; I SHOULD MERGE THIS FUNCTIONS WITH LOGIC OF ASTEROID-PROJECTILE HIT

(defn handle-player-hit [state on-player-hit]
  (let [player (:player state)
        asteroids (:asteroids state)
        results (reduce (fn [[remaining-asteroids remaining-lives events] asteroid]
                          (if (hit2? player asteroid)
                            ;; Problem with last one was that it removed life for every asteroid iteration
                            (if (empty? remaining-lives)
                              [remaining-asteroids remaining-lives events]
                              [(remove #{asteroid} remaining-asteroids)
                               (rest remaining-lives)
                               (conj events (on-player-hit player asteroid))])
                            [remaining-asteroids remaining-lives events]))
                        [asteroids (:lives player) []]
                        asteroids)]
    (-> state
        (assoc :player (assoc player :lives (second results)))
        (assoc :asteroids (first results))
        (update :events concat (nth results 2)))))



(defn on-player-hit [player asteroid]
  {:type :player-hit
   :player player
   :asteroid asteroid})