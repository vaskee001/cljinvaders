CREATE USER 'user'@'localhost' IDENTIFIED BY 'cljinvaders';
GRANT ALL PRIVILEGES ON cljinvaders.* TO 'user'@'localhost';
FLUSH PRIVILEGES;

