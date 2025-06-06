# cljinvaders

### STARTING APPLICATION ###
On linux app needs to be started with sudo privileges: sudo lein run  

### DATABASE CONNECTION ###
1. Import user to mariadb: mysql -u root -p < create_user.sql    
2. Import database: mysql -u user -p cljinvaders < cljinvaders_dump.sql  

### FUNCTIONS EXPLAINED ###
1. cljinvaders.dbconnect  
    save-score – Saves a player's score and name to the database  
    get-scoreboard – Retrieves all scores from the database, sorted by highest  

2. cljinvaders.hit  
    distance – Calculates the distance between two points  
    hit? – Checks if a projectile has collided with an asteroid  
    hit2? – Checks if the player has collided with an asteroid
    handle-hit – Removes hit asteroids/projectiles and updates score/events  
    on-hit – Returns event data for scoring on hit  
    handle-player-hit – Handles player-asteroid collisions and lives/events  
    on-player-hit – Returns event data for a player being hit  

3. cljinvaders.player  
    setup-player-images – Loads plane images  
    random-plane-img – Picks a random plane image  
    init-player – Initializes player state  
    update-player – Updates player position based on the mouse  
    shoot – Handles projectile creation with cooldown  
    handle-key-pressed – Handles key presses for player actions  
    move-projectile – Moves a projectile upwards  
    update-projectiles – Moves and filters active projectiles  

4. cljinvaders.asteroids  
    setup-asteroid-images – Loads asteroid images  
    random-asteroid-img – Picks a random asteroid image  
    create-asteroid – Creates a new asteroid at a random edge  
    spawn-asteroids – Occasionally spawns new asteroids  
    update-asteroids – Updates positions of all asteroids  

5. cljinvaders.core  
    setup – Initializes the game state and assets  
    draw-input-field – Renders the player name input field  
    draw-button – Renders a button with text  
    draw-start-screen – Draws the main menu with input/buttons  
    update-state – Main game logic loop: updates all entities and checks game state  
    draw-scoreboard-screen – Draws the scoreboard screen  
    draw-state – Top-level draw function for all game screens  
    handle-key-pressed – Handles all keyboard input based on screen state  
    point-in-button? – Checks if a point is inside a button  
    handle-mouse-pressed – Handles mouse clicks for all screens  
    main – Entry point to start the game  


### CONTROLS ###
Mouse: Move your plane  
S Key: Shoot  
Start Menu: Enter your name, click New Game or Scoreboard  
Game Over: Click any button to return to the start  
