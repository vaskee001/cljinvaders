(defproject cljinvaders "0.1.0-SNAPSHOT"
  :description "Simple clojure video game like chicken invaders"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quil "4.3.1323"]
                 [midje "1.10.10"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [mysql/mysql-connector-java "8.0.33"]]
  :plugins [[lein-midje "3.2"]]
  :test-paths ["test"])

