package Project3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

class game extends JPanel implements Runnable
{
    //game initialization
    public static final int WIDTH = 1366;
    public static final int HEIGHT = 768;
    public static String TITLE = "Faraway Voyage of 830 001 Kilometers";
    private String difficulty, username, password;
    private float volume;

    private boolean running = false;
    private boolean isPaused = false;
    private Thread thread;

    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private ImageIcon bg;
    private MySoundEffect shoot, hit, bomb, ready, bling, collect, music, kurukuru;
    public JLabel pointsLabel, bombsLabel, pauseLabel, timeLabel, hpLabel;

    private player p;
    public double getPlayerX() {return p.getX();}
    public double getPlayerY() {return p.getY();}

    //lists of all appropriate moving objects currently on screen (except player)
    //enemy gets created in init()
    private List<Bullet> bullets = new ArrayList<>();
    private List<item> items = new ArrayList<>();
    private List<Enemy> enemies;
    private boolean shooting = false;
    private int bulletCounter = 0;
    private int gameTickCounter = 0;

    private final int bulletThreshold = 5; // rate of the player's bullet
    private int totalBulletsShot = 0;

    //stolen variables from other magnet methods
    private double pspawnX; 
    private double pspawnY; 
    private int attractDelay = 0; //for Herta's magnet
    private double pdx = 1.0;
    private double pdy;
    private double pspeed = 2;
    private double plasmaCooldown;
    private int plasmaTimer = 1500; //timer till first plasma appears
    private int cycleLength = 1800; // Total length of one cycle
    private int enemyPhaseLength = 1200;

    private int bombTimer = 0;
    private final int bombTimerThreshold = 10 * 60;
    private boolean bombAvailable = false;
    private int starThreshold = 20000; // initial score required for the first star
    private int starThresholdIncrement = 80000;

    //enemy stuff
    private int enemySpawnCounter = 0;
    private double enemyHpMultiplier =1;
    //this should be the main difficulty parameter
    private int enemySpawnThreshold = 30; 
    private boolean shootingEnemyActive = false;
    private int shootingEnemyTimer = 0;
    private int shootingEnemyCooldown = 5 * 60; 
    private int HertaSpawn;
    private void addEnemy(double x, double y, String enemyType) {
        Enemy newEnemy;
        switch (enemyType) {
            case "shootingEnemy":
                newEnemy = new shootingEnemy(this, x, y, bullets, enemyHpMultiplier);
                switch (this.difficulty) {
                    case "Baby":
                        ((shootingEnemy) newEnemy).setshootCooldown(200);
                        ((shootingEnemy) newEnemy).sethitThreshold(10);
                        ((shootingEnemy) newEnemy).setNumberOfBullets(10);
                        break;
                    case "Easy":
                        ((shootingEnemy) newEnemy).setshootCooldown(300);
                        ((shootingEnemy) newEnemy).sethitThreshold(25);
                        ((shootingEnemy) newEnemy).setNumberOfBullets(15);
                        break;
                    case "Normal":
                        ((shootingEnemy) newEnemy).setshootCooldown(150);
                        ((shootingEnemy) newEnemy).sethitThreshold(40);
                        ((shootingEnemy) newEnemy).setNumberOfBullets(15);
                        break;
                    case "hard":
                        ((shootingEnemy) newEnemy).setshootCooldown(150);
                        ((shootingEnemy) newEnemy).sethitThreshold(55);
                        ((shootingEnemy) newEnemy).setNumberOfBullets(20);
                        break;
                    case "Lunatic":
                        ((shootingEnemy) newEnemy).setshootCooldown(150);
                        ((shootingEnemy) newEnemy).sethitThreshold(70);
                        ((shootingEnemy) newEnemy).setvelX(3);
                        break;
                    default:
                        ((shootingEnemy) newEnemy).setshootCooldown(300);
                        ((shootingEnemy) newEnemy).sethitThreshold(5);
                        break;
                }
                break;

            case "Herta":
                newEnemy = new Herta(this, x, y, enemyHpMultiplier, hpLabel);
                break;
            default:
                newEnemy = new DefaultEnemy(this, x, y, enemyHpMultiplier);
                break;
        }
        enemies.add(newEnemy);
    }

    private void initDifficulty(){
        switch (this.difficulty) {
            case "Baby":
                enemySpawnThreshold = 300;
                pspeed = 1.5;
                plasmaCooldown = 120;
                enemyHpMultiplier = 0.2;
                break;
            case "Easy":
                enemySpawnThreshold = 150;
                shootingEnemyCooldown = 240;
                pspeed = 2;
                plasmaCooldown = 105;
                enemyHpMultiplier = 0.5;

                break;
            case "Normal":
                enemySpawnThreshold = 60;
                shootingEnemyCooldown = 180;
                pspeed = 2;
                plasmaCooldown = 90;
                enemyHpMultiplier = 0.75;
                break;
            case "hard":
                enemySpawnThreshold = 45;
                shootingEnemyCooldown = 120;
                pspeed = 2;
                plasmaCooldown = 60;
                enemyHpMultiplier = 1;

                break;
            case "Lunatic":
                enemySpawnThreshold = 30;
                shootingEnemyCooldown = 60;
                pspeed = 3;
                plasmaCooldown = 45;
                enemyHpMultiplier = 1.2;

                break;
            default:
                enemySpawnThreshold = 50;
                break;
        }
    }
    
    //bomb stuff
    private void activateBomb() {
        bomb.SFX(MyConstants.FILE_BOMB, false, 0.5f * volume);
        //copy item generation technique from other method
        for (Enemy e : enemies) {
            int enemyType = 0;
            if (e instanceof shootingEnemy) {
                enemyType = 1;
                int numItems = 5; // Number of items in the item cluster
                    for (int k = 0; k < numItems; k++) {
                        double offsetX = (Math.random() - 0.5) * 20; // between -10 and 10
                        double offsetY = (Math.random() - 0.5) * 20;
                        items.add(new point(this, e.getX() + offsetX, e.getY() + offsetY, enemyType));
                    } 
            }
            if (e instanceof DefaultEnemy)items.add(new point(this, e.getX(), e.getY(), enemyType));
        }
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (!(bullet instanceof plasma)) {
                bulletIterator.remove();
            }
        }
        Iterator<Enemy> EnemyIterator = enemies.iterator();
        while (EnemyIterator.hasNext()) {
            Enemy enemy = EnemyIterator.next();
            if (!(enemy instanceof Herta)) {
                EnemyIterator.remove();
            }
        }
        enemySpawnCounter = 0;
        shootingEnemyTimer = 0;
        shootingEnemyActive = false;
        
        //magnets to player cuz bomb
        for (item it : items) {
            it.attractToPlayer();
        }
    }
    
    //initialize game
    public void init(){
        requestFocus();
        initDifficulty();

        //audio
        this.shoot = new MySoundEffect();
        this.hit = new MySoundEffect();
        this.bomb = new MySoundEffect();
        this.ready = new MySoundEffect();
        this.bling = new MySoundEffect();
        this.collect = new MySoundEffect();
        this.music = new MySoundEffect();
        this.kurukuru = new MySoundEffect();

        ////Labels
        pointsLabel = new JLabel("");
        pointsLabel.setForeground(Color.WHITE); 
        pointsLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        pointsLabel.setBounds(WIDTH - 340, 45, 350, 30);
        //bomb
        bombsLabel = new JLabel("Bomb:");
        bombsLabel.setForeground(Color.WHITE); 
        bombsLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        bombsLabel.setBounds(WIDTH - 340, 85, 350, 30);
        //pause
        pauseLabel = new JLabel("Time Stopped, ESC to resume") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 100)); // White with alpha
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }    
        };
        pauseLabel.setOpaque(false);
        pauseLabel.setForeground(Color.WHITE);
        pauseLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        pauseLabel.setBounds(350, 50, 666, 666);
        pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pauseLabel.setVisible(false);
        pauseLabel.setLayout(new BorderLayout());
        //time
        timeLabel = new JLabel("Time: ");
        timeLabel.setForeground(Color.WHITE); 
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        timeLabel.setBounds(WIDTH - 340, 125, 350, 30);

        //herta (boss) HP
        hpLabel = new JLabel("");
        hpLabel.setForeground(Color.WHITE); 
        hpLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        hpLabel.setBounds(WIDTH - 340, 165, 700, 30);
        
        this.setLayout(null); // null layout for absolute positioning for other components
        this.add(pointsLabel);
        this.add(bombsLabel);
        this.add(pauseLabel);
        this.add(timeLabel);
        this.add(hpLabel);   

        p = new player(WIDTH / 2, HEIGHT - 32);
        bg = new ImageIcon(MyConstants.FILE_BG);
        enemies = new ArrayList<>();
    }

    synchronized public void start() {
        if (running)
            return;
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private synchronized void stop() {
        if (!running)
            return;
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
    }

    //main method is only for testing here since we are launching the game through main.java
    public static void main(String args[]) {
        game game = new game("Lunatic", "parityfp", "no", 0.5f*2);
        MouseInput mouseInput = new MouseInput(game);
        game.addKeyListener(new KeyInput(game));
        game.addMouseListener(mouseInput);
        game.addMouseMotionListener(mouseInput);


        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame(Project3.game.TITLE);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); //here
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                System.out.println("Window gained focus");
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                System.out.println("Window lost focus");
                game.togglePauseOnWindowLostFocus();
            }
        });
        game.start();
    }
    public game(String difficulty, String username, String password, float volume) {
        this.difficulty = difficulty;
        this.username = username;
        this.password = password;
        this.volume = volume * 2;
    }
    public void togglePauseOnWindowLostFocus() {
        if(!isPaused)isPaused = !isPaused;
        pauseLabel.setVisible(isPaused);
        music.pauseSound();
    }       
    
    @Override
    public void run() {
        System.out.println("Run method started"); 
        init();
        final int TICKS_PER_SECOND = 60;
        final long TIME_PER_TICK = 1000000000 / TICKS_PER_SECOND;
        long lastTime = System.nanoTime();
        long now;
        long delta = 0;
    
        //game loop
        while (running) {
            now = System.nanoTime();
            delta += (now - lastTime);
            lastTime = now;
    
            while (delta >= TIME_PER_TICK) {
                tick(); //tick
                delta -= TIME_PER_TICK;
            }
            
            repaint(); //render
        }
        System.out.println("Game Tick Counter: " + gameTickCounter);
        System.out.println("Bullet Counter: " + bulletCounter);
        System.out.println("Total Bullets Shot: " + totalBulletsShot);
        System.out.println("Points: " + p.getPoints());
        System.out.println("Difficulty: " + this.difficulty);
        System.out.println("powerups: " + p.getUpgrades());
        System.out.println("Cycles: " + (enemyHpMultiplier - 1));
        stop();
    }

    private void tick() {
        if(isPaused)return;
        p.tick();
        if(gameTickCounter == 1)music.SFX(MyConstants.FILE_THEME, true, 0.25f * volume);//the volume level, try from 0-1
        gameTickCounter++;
        System.out.println(bullets.size());
        timeLabel.setText("Time: " + gameTickCounter/60);
        if (shooting  && !enemies.isEmpty()) {
            Enemy targetEnemy;
            //magnet
            if (bulletCounter % bulletThreshold == 0) {
                targetEnemy = enemies.get(0); 
                if (p.getUpgrades() == 0) {
                    bullets.add(new playerBullet(p.getX() + 9, p.getY(), targetEnemy));
                    if(attracting) for (Bullet b : bullets)if (b.getAge() >= 5)b.attractToEnemy();
                } else {
                    // fire multiple bullets with spread
                    double spreadAngle = Math.toRadians(10); // angle between each bullet
                    int totalBullets = 1 + 2 * p.getUpgrades(); // always an odd number to include the center bullet
                    for (int i = 0; i < totalBullets; i++) {
                        double angle = spreadAngle * (i - p.getUpgrades());
                        double dx = Math.sin(angle);
                        double dy = -Math.cos(angle);
                        if (!enemies.isEmpty()) {
                             
                            bullets.add(new playerBullet(this, p.getX() + 9, p.getY() + 9, 10.0, dx, dy, false, targetEnemy));
                        }
                        if(attracting) for (Bullet b : bullets)if (b.getAge() >= 1)b.attractToEnemy();
                    }                    
                }
                totalBulletsShot++;
                shoot.SFX(MyConstants.FILE_SHOOT, false, 0.2f * volume);
            }
            bulletCounter++;
        }
        
    
        //handles on-screen bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).tick();
            if (bullets.get(i).isOffScreen()) {
                bullets.remove(i);
                i--;
            }
        }

        //handles on-screen enemies
        for (Enemy enemy : enemies) {
            enemy.tick();
        }
        enemySpawnCounter++;
        if (!shootingEnemyActive) {
            shootingEnemyTimer++;
        }
        //indicates when enemies should stop spawning, for bosses or special events, number divides by 60 for time in secondsd
        //main game cycle
        int currentCycleTick = gameTickCounter % cycleLength;
        
        if(currentCycleTick < enemyPhaseLength){
            if (enemySpawnCounter >= enemySpawnThreshold) {
                addEnemy(new Random().nextDouble() * (WIDTH - 50), 0, "DefaultEnemy");
                if (!shootingEnemyActive && shootingEnemyTimer >= shootingEnemyCooldown) {
                    addEnemy(new Random().nextDouble() * (WIDTH - 50), 0, "shootingEnemy");
                    shootingEnemyActive = true;
                    shootingEnemyTimer = 0;
                }
                enemySpawnCounter = 0;
            }
        }
        
        if (currentCycleTick == 1) {
            HertaSpawn = new Random().nextInt(100);
            System.out.println("Cycle Started" + HertaSpawn);
        }
        
        //System.out.println("Cycle tick" + currentCycleTick);
        if (currentCycleTick == plasmaTimer + 120 && HertaSpawn <100) kurukuru.SFX(MyConstants.FILE_KURUKURU, false, 1f * volume);
        if (currentCycleTick == cycleLength - 1) {  
            enemyHpMultiplier = enemyHpMultiplier * 1.15;
            System.out.println("Cycle completed" + HertaSpawn);
        if (HertaSpawn <100) { // chance for Herta to spawn every cycle
            double x = new Random().nextDouble() * (WIDTH - 50);
            addEnemy(x, 0, "Herta");
            }
        }


        //every second, adjust plasma spawn rate here. use prime numbers for main delay
        //added break time for player to clear enemies (5 seconds)
        if (currentCycleTick > plasmaTimer && currentCycleTick % plasmaCooldown == 0) {
            //random position for plasma bullets
            if(Math.random() < 0.5){
                pspawnX = 350;
                pdx = -pdx;
            }else{
                pspawnX = WIDTH - 350 -128;
                pdx = -pdx;
            }
            pdy = pdx;
            pspawnY = 50 + (Math.random() * 350);
            plasma plasmaBullet = new plasma(this, pspawnX, pspawnY, pspeed, pdx, pdy, true);
            bullets.add(plasmaBullet);
        } 
        for (Bullet p : bullets) if (p instanceof plasma) p.attractToPlayer();
        
        // update and remove enemies
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.tick();
            if (enemy.isOffScreen()) {
                iterator.remove();
            }
        }

        //star spawn, 0.05% per tick, ~3% per second **add extra 0 first

        if (p.getPoints() >= starThreshold) {
            bling.SFX(MyConstants.FILE_BLING, false, 0.7f * volume);
            items.add(new star(this, new Random().nextInt(game.WIDTH - 350), 100, 3));
            starThreshold += starThresholdIncrement;
            starThresholdIncrement *= 3; 
        }
    

        //item updating
        for (int i = 0; i < items.size(); i++) {
            item it = items.get(i);
            it.tick();
            if(it.isOffScreen()){
                items.remove(i);
                i--;
            }
        }

        if (!bombAvailable) {
            bombTimer++;
            if (bombTimer >= bombTimerThreshold - 1) {
                ready.SFX(MyConstants.FILE_READY, false, 0.5f * volume);
            }
            if (bombTimer >= bombTimerThreshold) {
                bombAvailable = true;
                bombTimer = 0;
            }
        }


        //collision handling
        for (Bullet b : bullets) {
                if (b.isEnemyBullet && p.getBounds().intersects(b.getBounds())) {
                    System.out.println("player hit by enemy bullet, GAME OVER");
                    //SwingUtilities.invokeLater(this::showGameOverScreen);
                    running = false;
                    SwingUtilities.invokeLater(this::showGameOverScreen);
                    return;
                }
            }

        for (Enemy e : enemies) {
            if (p.getBounds().intersects(e.getBounds())) {
                // handle collision between player and enemy
                System.out.println("player hit, GAME OVER");
                running = false; //try to quit game, maybe change this when implementing retry button.
                SwingUtilities.invokeLater(this::showGameOverScreen);
                return;
            }
    
            for (Bullet b : bullets) {
                if (b.getBounds().intersects(e.getBounds()) && !b.isEnemyBullet) {
                    // handle collision between bullet and enemy
                    //remove the enemy and the bullet, or mark them for removal
                    //System.out.println("enemy hit");
                    hit.SFX(MyConstants.FILE_HIT, false, 0.3f * volume);
                }
            }
        } 


        //for enemy hit
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            // reverse loop for enemies
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (b.getBounds().intersects(e.getBounds())) {
                    if(!b.isEnemyBullet){
                        e.hit(); // enemy has been hit
                        bullets.remove(i); //remove the bullet
                    }
                    if (e.isDestroyed()) {
                        int enemyType = 0;
                        if(e instanceof shootingEnemy){
                            shootingEnemyActive = false;
                            enemyType = 1;
                            int numItems = 5; // number of items in the cluster
                            for (int k = 0; k < numItems; k++) {
                                double offsetX = (Math.random() - 0.5) * 20; // between -10 and 10
                                double offsetY = (Math.random() - 0.5) * 20;
                                //spawn a new item with the offset
                                items.add(new point(this, e.getX() + offsetX, e.getY() + offsetY, enemyType));
                            } 
                        }else if (e instanceof Herta) {
                            enemyType = 2;
                            int numItems = 30;
                            for (int k = 0; k < numItems; k++) {
                                double offsetX = (Math.random() - 0.5) * 160; // increase spread
                                double offsetY = (Math.random() - 0.5) * 160;
                                items.add(new point(this, e.getX() + offsetX, e.getY() + offsetY, enemyType));
                            }
                            attractDelay = 60; //after herta killed, how long till attract
                            items.add(new star(this, e.getX(), e.getY(), 3));
                            bling.SFX(MyConstants.FILE_BLING, false, 0.7f * volume);
                        }
                        if(e instanceof DefaultEnemy)items.add(new point(this, e.getX(), e.getY(), enemyType));
                        enemies.remove(j); 
                    }
    
                    //System.out.println("enemy hit");
    
                    break;
                }
            }
        }
        if (attractDelay > 0) {
            attractDelay--;
            if (attractDelay == 0) {
                for (item it : items) it.attractToPlayer();
            }
        }
        
        //item collision with player
        for (int i = 0; i < items.size(); i++) {
            item it = items.get(i);
            it.tick();
            if (p.getBounds().intersects(it.getBounds())) {
                collect.SFX(MyConstants.FILE_ITEM, false, 0.2f * volume);
                // increase player's points
                switch (it.getEnemyType()) {
                    case 0:
                        p.addPoints(1500);
                        pointsLabel.setText("" + p.getPoints());
                        System.out.println(it.getEnemyType());
                        break;
                    case 1:
                        p.addPoints(500);
                        pointsLabel.setText("" + p.getPoints());
                        break;
                    case 2:
                        p.addPoints(5000);
                        pointsLabel.setText("" + p.getPoints());
                        break;
                    default:
                        p.addPoints(100);
                        pointsLabel.setText("" + p.getPoints());
                        break;
                }
                if (it.getEnemyType() == 3) { //enemy type 3 is NOT an enemy, but an indicator that the item is not from an enemy.
                    p.addUpgrades();
                }
                items.remove(i);
                i--;
            }
        }

        if (bombAvailable) {
            bombsLabel.setText("Bomb: READY | press SHIFT");
        }else{
            bombsLabel.setText("Bomb: " + bombTimer / 60 + "/10");
        }
    
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

        int startX = (WIDTH - 666) / 2;
        int startY = (HEIGHT - 666) / 2;

        // Draw the GIF TODO: THIS LINE CAUSES ERROR IN TERMINAL BUT WORKS
        if (bg != null) {
            g.drawImage(bg.getImage(), startX, startY, 666, 666, this);
        }
    
        // render the player
        if (p != null) {
            p.render(g);
        }
    
        // render bullets
        for (Bullet bullet : new ArrayList<>(bullets)) {
            if (bullet != null) {
                bullet.render(g);
            }
        }

        //render items
        for (item it : new ArrayList<>(items)) {
            it.render(g);
        }
    
        // render enemies
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (enemy != null) {
                enemy.render(g);
            }
        }
    
    
        Toolkit.getDefaultToolkit().sync(); // uncomment this if there are any rendering issues
    }
    
    private boolean rightPressed = false;
    private boolean leftPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean isDragging = false;
    private Bullet selectedBullet;
    //store the offset since p.getX,Y() gives the XY of the top left corner. (for dragging projectile)
    private int offsetX, offsetY;
    private boolean attracting;

    public boolean getDragging(){
        return isDragging;
    }

    public void mousePressed(MouseEvent e){
        
        for (Bullet p : bullets) {    
            System.out.println(p.getClass().getSimpleName());
            if (p instanceof plasma && p.getBounds().contains(e.getPoint())) {
                selectedBullet = p;
                isDragging = true;
                offsetX = e.getX() - (int)p.getX();
                offsetY = e.getY() - (int)p.getY();

                System.out.println("clicked on plasma");
                break; //only first plasma that gets clicked
            }
        }
    }
    public void mouseReleased(MouseEvent e){
        isDragging = false;
        selectedBullet = null;
        System.out.println("Released");
    }
    public void mouseDragged(MouseEvent e){
        if (isDragging && selectedBullet != null) {
            System.out.println("dragging" + e.getX() + ", " + e.getY());
            selectedBullet.setX(e.getX() - offsetX);
            selectedBullet.setY(e.getY() - offsetY);
        }
    }
    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_W){
            p.setVelY(-5);
            upPressed = true;
        }else if(key == KeyEvent.VK_A){
            p.setVelX(-5);
            leftPressed = true;
        }else if(key == KeyEvent.VK_S){
            p.setVelY(5);
            downPressed = true;
        }else if(key == KeyEvent.VK_D){
            p.setVelX(5);
            rightPressed = true;
        }
        
        if (key == KeyEvent.VK_SPACE) {
            shooting = true;
        }

        if (key == KeyEvent.VK_ESCAPE) {
            if(isPaused)music.resume();
            else music.pauseSound();
            isPaused = !isPaused;
            pauseLabel.setVisible(isPaused);
            
            
        }

        if (key == KeyEvent.VK_SHIFT) {
            if (bombAvailable) {
                activateBomb();
                bombAvailable = false;
            }
        }

        if (key == KeyEvent.VK_CONTROL) {
            attracting = true;
            //for (Bullet b : bullets)if (b.getAge() >= 5)b.attractToEnemy();
        }
        updateVelocity();

    }
    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();
    
        if(key == KeyEvent.VK_A){
            leftPressed = false;
        }else if(key == KeyEvent.VK_D){
            rightPressed = false;
        }else if(key == KeyEvent.VK_W){
            upPressed = false;
        }else if(key == KeyEvent.VK_S){
            downPressed = false;
        }

        if (key == KeyEvent.VK_SPACE) {
            shooting = false;
        }
        if (key == KeyEvent.VK_CONTROL) {
            attracting = false;
            //for (Bullet b : bullets)if (b.getAge() >= 5)b.Unattract();
        }
        updateVelocity();
    }
    private void updateVelocity() {
        if (leftPressed && !rightPressed) {
            p.setVelX(-5);
        } else if (rightPressed && !leftPressed) {
            p.setVelX(5);
        } else {
            p.setVelX(0);
        }

        if (upPressed && !downPressed) {
            p.setVelY(-5);
        } else if (downPressed && !upPressed) {
            p.setVelY(5);
        } else {
            p.setVelY(0);
        }
    }

    //GAME OVER screen
    public void showGameOverScreen() {
        JFrame gameOverFrame = new JFrame("Game Over");
        gameOverFrame.setSize(500, 350);
        gameOverFrame.setLayout(new BorderLayout());
        gameOverFrame.setLocationRelativeTo(null); 
        String gameOverText = "<html>Game Over<br/>Score: " + p.getPoints() + "</html>";
    
        JLabel gameOverLabel = new JLabel(gameOverText, SwingConstants.CENTER);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));
    
        JButton restartButton = new JButton("Back to Start Menu");
        restartButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            gameOverFrame.dispose(); 
            new MainApplication().setVisible(true); 
        });
        JButton retryButton = new JButton("Retry");
        retryButton.addActionListener(e -> {
            //SwingUtilities.getWindowAncestor(this).dispose();
            SwingUtilities.getWindowAncestor(this).setVisible(true);
            gameOverFrame.dispose(); 
            restartGame();  
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            stop();
            running = false;
            SwingUtilities.getWindowAncestor(this).dispose();
            gameOverFrame.dispose(); 
            System.exit(0); 
        });

        gameOverFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(retryButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        gameOverFrame.add(gameOverLabel, BorderLayout.CENTER);
        gameOverFrame.add(buttonPanel, BorderLayout.SOUTH);

        gameOverFrame.setVisible(true); 
        //SwingUtilities.getWindowAncestor(this).dispose();
        if(!password.isEmpty() && p.getPoints() > 0)scoreboard.saveScores(username, p.getPoints(), password);
        if(password.isEmpty() && username.isEmpty() && p.getPoints() > 0) scoreboard.saveScores("Guest", p.getPoints(), "hackermans");
        music.stopSound();
        SwingUtilities.getWindowAncestor(this).setVisible(false);
    }

    private void restartGame() {
        stop();
        //TODO NOT SURE IF I HAVE EVERYTHING TO RESET HERE. add more if needed plz
        gameTickCounter = 0;
        bulletCounter = 0;
        totalBulletsShot = 0;
        p.setPoints(0);
        p.setUpgrades(0);
        bullets.clear();
        enemies.clear();
        items.clear(); 
        shootingEnemyTimer = 0;
        enemyHpMultiplier = 1;
        shootingEnemyActive = false;
        rightPressed = false;
        leftPressed = false;
        upPressed = false;
        downPressed = false;
        isDragging = false;
        shooting = false;
        bombTimer = 0;
        bombAvailable = false;
        isPaused = false;
        pointsLabel.setText("");
        bombsLabel.setText("Bomb: "); 
        starThreshold = 20000;
        starThresholdIncrement = 80000;
        //removes all jpanel components, very important
        this.removeAll();
        SwingUtilities.invokeLater(() -> {
            init();
            start();
        });
    }
}

class player {

    private double x;
    private double y;

    private double velX = 0;
    private double velY = 0;

    private BufferedImage player;

    private int upgrades;
    public void addUpgrades() {
        upgrades ++;
    }
    public int getUpgrades(){
        return upgrades;
    }
    public void setUpgrades(int upgrades){
        this.upgrades = upgrades;
    }
    private int points = 0;
    public void addPoints(int amount) {
        points += amount;
    }
    public int getPoints(){
        return points;
    }
    public void setPoints(int points){
        this.points = points;
    }


    public player(double x, double y){
        this.x = x;
        this.y = y;

        try {
            File file = new File(MyConstants.FILE_SHIP);
            player = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, player.getWidth() - 10, player.getHeight() - 10);
    }

    public void tick(){
        x += velX;
        y += velY;

        //collision bound with border
        if (x <= 0 + 350) x = 0 + 350;
        if (x >= (1366 - 350) - 32) x = (1366 - 350) - 32;
        if (y <= 0 + 50) y = 0 + 50;
        if (y >= 766 - 50 - 32) y = 766 - 50 - 32;
    }

    public void render(Graphics g){
        g.drawImage(player, (int)x, (int)y, null);
    }

    public void setVelX(double velX){
        this.velX = velX;
    }
    public void setVelY(double velY){
        this.velY = velY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}

///////////////////////////ENEMY CLASSES///////////////////////////

abstract class Enemy {
    protected double x, y;
    protected BufferedImage enemyImage;
    protected Rectangle bounds;
    protected int hitCount = 1;
    protected double thresholdMultiplier = 1;
    protected boolean destroyed = false;
    protected List<Bullet> bulletList;
    game gameinstance;

    public Enemy(game game, double x, double y, double thresholdMultiplier) {
        this.gameinstance = game;
        this.thresholdMultiplier = thresholdMultiplier;
        this.x = x;
        this.y = y;
    }

    public abstract void tick(); 

    public void render(Graphics g) {
        if (enemyImage != null) {
            g.drawImage(enemyImage, (int)x, (int)y, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, enemyImage.getWidth()-10, enemyImage.getHeight()-10);
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void addThreshold(){
        thresholdMultiplier++;
    }
    

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isOffScreen() {
        return y > game.HEIGHT;
    }

    public void hit() {
        hitCount++;
    }
}

class DefaultEnemy extends Enemy {
    private double initialX;
    private double speedY = 1; 
    private double amplitude = 20;
    private double frequency = 0.02;
    private int hitThreshold = 5;
    private ImageIcon enemyImage;
    public DefaultEnemy(game gameInstance, double x, double y, double thresholdMultiplier) {
        super(gameInstance, x, y, thresholdMultiplier);
        initialX = x;
        enemyImage = new ImageIcon(MyConstants.FILE_ALIEN1);
    }

    @Override
    public void tick() {
        //if we want crazy teleporting enemies (for Lunatic difficulty), just add X = 350 + new Random().nextInt(game.WIDTH - 700); to some conditions below
        //enemies do not get removed once off screen, instead they respawn at a random spot at the top or you could say "their friend replaced them"
        y += speedY;
        x = initialX + amplitude * Math.sin(frequency * y);
        if (x <= 0 + 350) x = 0 + 350;
        if (x >= (1366 - 350) - 64) x = (1366 - 350) - 64;
        if (y <= 0 + 50) y = 0 + 50;
        if (y >= 766 - 50 - 64){
            y = 0;
            initialX = 350 + new Random().nextInt(game.WIDTH - 700);
        } 
    }
    @Override
    public void render(Graphics g) {
        enemyImage.paintIcon(null, g, (int)x, (int)y);
    }
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, enemyImage.getIconWidth() - 10, enemyImage.getIconHeight() - 10);
    }

    //detects when to destroy enemy (how many hits)
    @Override
    public void hit() {
        hitCount++;
        if (hitCount >= hitThreshold * thresholdMultiplier) {
            destroyed = true;
        }
    }
}

class shootingEnemy extends Enemy{
    private int shootCooldown = 15;
    private int currentCooldown = 0;
    private int hitThreshold = 35; 
    private double velX = 1.5, velY = 1;

    private int numberOfBullets = 15;
    private double bulletSpeed = 4;
    private double spreadAngle = Math.PI * 2;
    private double startAngle = Math.PI / 2 - spreadAngle / 2;


    public shootingEnemy(game gameInstance, double x, double y, List<Bullet> bulletList, double thresholdMultiplier) {
        super(gameInstance, x, y, thresholdMultiplier);
        this.bulletList = bulletList;
        try {
            File file = new File(MyConstants.FILE_ALIEN2);
            enemyImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setshootCooldown(int shootCooldown) {this.shootCooldown = shootCooldown;}
    public void sethitThreshold(int hitThreshold) {this.hitThreshold = hitThreshold;}
    public void setNumberOfBullets(int numberOfBullets) {this.numberOfBullets = numberOfBullets;}
    public void setBulletSpeed(double bulletSpeed) {this.bulletSpeed = bulletSpeed;}
    public void setvelX(double velX) {this.velX = velX;}
    @Override
    public void tick() {
        // enemy movement logic, probably a linear function
        y+=1;
        x+=velX;
        if (currentCooldown <= 0) {
            shoot();
            currentCooldown = shootCooldown;
        } else {
            currentCooldown--;
        }
        if (x <= 0 + 350) {
            x = 0 + 350;
            velX = -velX;
        }
        if (x >= (1366 - 350) - 64) {
            x = (1366 - 350) - 64;
            velX = -velX;
        }
        if (y <= 0 + 50) {
            y = 0 + 50;
            velY = -velY;
        }
        if (y >= 766 - 550){
            y = 766 - 550;
            velY = -velY;
        }
    }

    private void shoot() {

    System.out.println("enemy Shooting Bullets");
    for (int i = 0; i < numberOfBullets; i++) {
        double angle = startAngle + i * (spreadAngle / (numberOfBullets - 1));
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        Bullet newBullet = new enemyBullet(gameinstance, x, y, bulletSpeed, dx, dy, true);
        bulletList.add(newBullet);

        //System.out.println("Bullet " + (i + 1) + ": Angle = " + angle + ", dx = " + dx + ", dy = " + dy);
    }
}

    //detects when to destroy enemy (how many hits)
    @Override
    public void hit() {
        hitCount++;
        if (hitCount >= hitThreshold * thresholdMultiplier) {
            destroyed = true;
        }
    }
}

class Herta extends Enemy{
    private double velX = 1.5, velY = 1;
    private int hitThreshold = 250; 
    private ImageIcon enemyImage;
    public JLabel hpLabel;
    public Herta(game game, double x, double y, double thresholdMultiplier, JLabel hpLabel) {
        super(game, x, y, thresholdMultiplier);
        this.hpLabel = hpLabel;
        enemyImage = new ImageIcon(MyConstants.FILE_SPECIAL);
    }

    @Override
    public void tick() {
        y+=velY;
        x+=velX;
        if (x <= 0 + 350) {
            x = (1366 - 350) - 200;
            //velX = - velX;
        }
        if (x >= (1366 - 350) - 200) {
            x = 0 + 350;
            //velX = - velX;
        }
        if (y <= 0 + 50) {
            y = 0 + 50;
            velY = - velY;
        }
        if (y >= 766 - 100 - 166){
            y = 766 - 100 - 166;
            velY = - velY;
        }
    }

    @Override
    public void render(Graphics g) {
        enemyImage.paintIcon(null, g, (int)x, (int)y);
    }
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, enemyImage.getIconWidth() - 20, enemyImage.getIconHeight() - 10);
    }
    //detects when to destroy enemy (how many hits)
    @Override
    public void hit() {
        hitCount++;
        //System.out.println((hitThreshold * thresholdMultiplier) - hitCount);
        System.out.println(((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier));
        
        if(((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier) >= 0.9) 
            hpLabel.setText("////////////////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier) > 0.8 &&
                ((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier) < 0.9 )  
            hpLabel.setText("//////////////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier) > 0.7 &&
                ((hitThreshold * thresholdMultiplier) - hitCount) / (hitThreshold * thresholdMultiplier) < 0.8 )  
            hpLabel.setText("////////////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.6 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.7 )  
            hpLabel.setText("//////////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.5 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.6 )  
            hpLabel.setText("////////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.4 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.5 )  
            hpLabel.setText("//////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.3 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.4 )  
            hpLabel.setText("////////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.2 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.3 )  
            hpLabel.setText("//////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0.1 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.2 )  
            hpLabel.setText("////");
        else if(((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) > 0 &&
                ((hitThreshold * thresholdMultiplier) - hitCount)/(hitThreshold * thresholdMultiplier) < 0.1 )  
            hpLabel.setText("//");
        else hpLabel.setText("");
        //hpLabel.setText(String.valueOf((hitThreshold * thresholdMultiplier) - hitCount));
        if (hitCount >= hitThreshold * thresholdMultiplier) {
            destroyed = true;
        }
    }
}

///////////////////////////PROJECTILE CLASSES///////////////////////////

abstract class Bullet {
    protected double x, y, dx; 
    protected double dy = -1; 
    protected double speed = 15.0;
    protected BufferedImage bullet;
    protected boolean isEnemyBullet;
    protected boolean magnet = false, enemymagnet = false;
    protected game gameInstance;
    protected Enemy enemyInstance;
    protected int age = 0;

    public Bullet(game game, double x, double y, double speed, double dx, double dy, boolean isEnemyBullet) {
        this.gameInstance = game;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.dx = dx;
        this.dy = dy;
        this.isEnemyBullet = isEnemyBullet;
    }
    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, bullet.getWidth()-5, bullet.getHeight()-5);
    }

    public void tick() {
        if (magnet) {
            double playerX = gameInstance.getPlayerX();
            double playerY = gameInstance.getPlayerY();
            double speed = this.speed;

            double deltaX = playerX - x;
            double deltaY = playerY - y;
            //pythagorus, calculate hypotenuse 
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > 1) { 
                double directionX = deltaX / distance;
                double directionY = deltaY / distance;

                x += directionX * speed;
                y += directionY * speed;
            }
            //bounds
            if (x <= 0 + 340 && x >= 0 + 250) x = 0 + 342;
            if (x >= ((1366 - 350) - 120) && x <= 1366 - 250) x = (1366 - 350) - 120;
            if (y <= 0 + 42) y = 0 + 42;
            if (y >= 768 - 50 - 120 ) y = 768 - 50 - 120;
        } else{
            //y -= speed; // Moves the bullet upwards
            x += dx * speed;
            y += dy * speed;
        }

        
    }

    public void render(Graphics g) {
        g.drawImage(bullet, (int)x, (int)y, null);
    }

    public boolean isOffScreen() {
        return y > game.HEIGHT - 50 || y < 0 + 50 || x < 0 + 350 || x > game.WIDTH - 350;
    }

    protected void setBulletImage(String imagePath) {
        try {
            File  file = new File(imagePath);
            bullet = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attractToPlayer() {
        this.magnet = true;
    }
    public void attractToEnemy() {
        this.enemymagnet = true;
    }
    public void Unattract() {
        this.enemymagnet = false;
    }

    public void setX(double x){this.x = x;}
    public void setY(double y){this.y = y;}
    public double getX(){return this.x ;}
    public double getY(){return this.y ;}
    public double getAge(){return this.age ;}
}

class playerBullet extends Bullet{

    public playerBullet(double x, double y, Enemy enemyInstance) {
        super(x, y);
        this.enemyInstance = enemyInstance;
        setBulletImage(MyConstants.FILE_BULLET); 
    }
    public playerBullet(game gameInstance, double x, double y, double speed, double dx, double dy, boolean isEnemyBullet, Enemy enemyInstance) {
        super(gameInstance , x, y, speed, dx, dy, isEnemyBullet);
        this.enemyInstance = enemyInstance;
        setBulletImage(MyConstants.FILE_BULLET); 
    }
    @Override
    public void tick(){
        //y-=10;
        this.age++;
        if (enemymagnet){
            // if(enemyInstance != null){
                double enemyX = enemyInstance.getX();
                double enemyY = enemyInstance.getY();
                this.speed = 10;

                double deltaX = enemyX - x;
                double deltaY = enemyY - y;
                //pythagorus, calculate hypotenuse 
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                if (distance > 1) { // Prevent division by zero
                    double directionX = deltaX / distance;
                    //double directionY = deltaY / distance;
                    x += directionX * speed;
                    //y += directionY * speed;
                }
            
            //}
        }
        y += -10;
        x += dx * speed;
    }
}

class enemyBullet extends Bullet{

    public enemyBullet(game gameInstance, double x, double y, double speed, double dx, double dy, boolean isEnemyBullet) {
        super(gameInstance, x, y, speed, dx, dy, isEnemyBullet);
        setBulletImage(MyConstants.FILE_ENEMYBULLET2); 
    }

    @Override
    public boolean isOffScreen() {
        return y > game.HEIGHT - 50 - 32 || y < 0 + 50 || x < 0 + 350 || x > game.WIDTH - 350 - 32;
    }
}

class plasma extends enemyBullet{
    public plasma(game gameInstance, double x, double y, double speed, double dx, double dy, boolean isEnemyBullet) {
        super(gameInstance, x, y, speed, dx, dy, isEnemyBullet);
        setBulletImage(MyConstants.FILE_PLASMA); 
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, bullet.getWidth()-38, bullet.getHeight()-38);
    }
    @Override
    public boolean isOffScreen() {
        return y > game.HEIGHT|| y < 0 || x < 0 + 300 || x > game.WIDTH - 300;
    }

}

///////////////////////////ITEM CLASSES///////////////////////////

abstract class item{
    protected BufferedImage item;
    protected int enemyType;
    protected double x;
    protected double y;
    protected double velX;
    protected double velY;
    protected boolean magnet = false;
    protected game gameInstance;
    double speed;
    public item(game game, double x, double y, int enemyType) {
        this.gameInstance = game;
        this.x = x;
        this.y = y;
        this.enemyType = enemyType;
    }

    public void tick() {
        if (magnet) {
            double playerX = gameInstance.getPlayerX();
            double playerY = gameInstance.getPlayerY();
            if (this.enemyType != 2) {this.speed = 3;}
            else this.speed = 10;

            double deltaX = playerX - x;
            double deltaY = playerY - y;
            //pythagorus, calculate hypotenuse 
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > 1) { 
                double directionX = deltaX / distance;
                double directionY = deltaY / distance;

                x += directionX * speed;
                y += directionY * speed;
            }
        } else {
            // normal falling logic
            x += velX;
            y += velY;

            velX *= 0.95;
        }
        if (x <= 0 + 350) x = 0 + 350;
        if (x >= (1366 - 350) - 32) x = (1366 - 350) - 32;

    }

    public void render(Graphics g) {
        g.drawImage(item, (int)x, (int)y, null);
    }
    protected void setItemImage(String imagePath) {
        try {
            File file = new File(imagePath);
            item = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, item.getWidth(), item.getHeight());
    }
    public int getEnemyType(){
        return enemyType;
    }

    public boolean isOffScreen() {
        return y > game.HEIGHT - 50;
    }
    public void attractToPlayer() {
        this.magnet = true;
    }

}

class point extends item{

    public point(game gameInstance, double x, double y, int enemyType) {
        super(gameInstance, x, y, enemyType);
        setItemImage(MyConstants.FILE_POINT); 
        this.velX = (Math.random() - 0.5) * 2;
        this.velY = (Math.random() + 0.25) * 2; //between 0.5 and 2.5
    }
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, item.getWidth()+10, item.getHeight());
    }
}

class star extends item{
    public star(game gameInstance, double x, double y, int enemyType) {
        super(gameInstance, x, y, enemyType);
        setItemImage(MyConstants.FILE_STAR); 
        this.velX = (Math.random() - 0.5) * 2;
        this.velY = (Math.random() + 0.25) * 2; //between 0.5 and 2.5
    }

}

///////////////////////////INPUT CLASSES///////////////////////////

class KeyInput extends KeyAdapter {
    game game;
    public KeyInput(game game){this.game = game; }

    public void keyPressed(KeyEvent e){
        game.keyPressed(e);
    }
    public void keyReleased(KeyEvent e){
        game.keyReleased(e);
    } 
}

class MouseInput extends MouseAdapter {
    game game;
    public MouseInput(game game){this.game = game;}
    public void mousePressed(MouseEvent e){
        game.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e){
        game.mouseReleased(e);
    }
    public void mouseDragged(MouseEvent e){
        game.mouseDragged(e);
    }
}
