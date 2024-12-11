;; (ns cljinvaders.projectiles)

;; (defn init [][])

;; ;; Spawn new projectile
;; (defn shoot [projectiles player]
;;   (conj projectiles {:x (:x player) :y (:y player) :speed 10}))

;; (defn move-projectile [projectile]
;;   (update projectile :y #(- % (:speed projectile))))

;; (defn update-projectiles [projectiles]
;;   (let [updated-projectiles (map move-projectile projectiles)
;;         visible-projectiles (filter #(> (:y %) 0) updated-projectiles)]
;;     visible-projectiles))