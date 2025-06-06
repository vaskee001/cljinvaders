(ns cljinvaders.dbconnect
  (:require [clojure.java.jdbc :as jdbc]))

;; Define the DB connection config
(def db-spec
  {:dbtype "mysql"
   :dbname "cljinvaders"
   :host "localhost"
   :user "user"
   :password "cljinvaders"
   :useSSL false
   :serverTimezone "UTC"})

;; Save score to DB
(defn save-score [player-name score]
  (jdbc/insert! db-spec
                :scoreboard
                {:player_name player-name
                 :score score}))

;; Get all scores sorted by highest first
(defn get-scoreboard []
  (jdbc/query db-spec
              ["SELECT player_name, score, created_at FROM scoreboard ORDER BY score DESC"]))
