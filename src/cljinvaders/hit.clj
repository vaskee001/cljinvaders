(ns cljinvaders.hit
  (:require [cljinvaders.player :as player]))

(defn distance [x1 y1 x2 y2]
  (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))))

(defn hit? [projectile asteroid]
  (< (distance (:x projectile) (:y projectile) (:x asteroid) (:y asteroid))
     (/ (:size asteroid) 2)))

;; When projectile touch asteroid, both should be removed 
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
  {:type :add-points
   :projectile projectile
   :asteroid asteroid})
