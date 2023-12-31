package Project3;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

//import javax.sound.*;

class MainApplication extends JFrame {

    private JPanel contentPane;
    private StartButton startButton;
    private JToggleButton[] tb;
    private JLabel drawpane;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private JButton creditsButton, guideButton, scoresButton;
    private static String selectedDifficulty = "Lunatic";
    private static MySoundEffect title;
    private JSlider volumeSlider;
    private static float volume = 0.5f;
    
    public MainApplication() {
        requestFocus();
        setTitle("Faraway Voyage of 830 000 Kilometers: Main Menu");
        setBounds(200, 200, 620, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        startButton = new StartButton(null);
        contentPane.add(startButton);

        // Combo box for diffculties
        String[] difficulties = { "Baby", "Easy", "Normal", "Hard", "Lunatic" };
        JComboBox<String> difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setBounds(450, 5, 150, 30);
        contentPane.add(difficultyComboBox);
        difficultyComboBox.setSelectedItem("Lunatic");
        difficultyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
                System.out.println(selectedDifficulty);
            }
        });

        // audio
        MainApplication.title = new MySoundEffect();

        // Volume slider
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        volumeSlider.setBounds(15, 135, 200, 30);
        contentPane.add(volumeSlider);
        volumeSlider.setOpaque(false);
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
                public void stateChanged(ChangeEvent e) {
                int sliderValue = volumeSlider.getValue();
                volume = sliderValue / 100.0f;
                title.setVolume(volume * 0.5f);
            }
        });
        

        // Username and Password
        JPanel authPanel = new JPanel(new GridLayout(3, 2));

        authPanel.add(new JLabel("                      Username:")).setForeground(Color.WHITE);
        usernameField = new JTextField();
        authPanel.add(usernameField);

        authPanel.add(new JLabel("                      Password:")).setForeground(Color.WHITE);
        passwordField = new JPasswordField();
        authPanel.add(passwordField);

        authPanel.setBounds(-50, 280, 300, 100);
        contentPane.add(authPanel);
        authPanel.setOpaque(false);
        
        // Credits button
        creditsButton = new JButton("Credits");
        creditsButton.setBounds(470, 325, 130, 30);
        contentPane.add(creditsButton);

        creditsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCredits();
            }
        });
        // Guide button
        guideButton = new JButton("Guide");
        guideButton.setBounds(470, 285, 130, 30);
        contentPane.add(guideButton);

        guideButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGuide();
            }
        });

        // Scoreboard button
        scoresButton = new JButton("Scoreboard");
        scoresButton.setBounds(470, 245, 130, 30);
        contentPane.add(scoresButton);

        scoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showScoreboard();
            }
        });

        // Create mute and unmute toggle buttons
        tb = new JToggleButton[2];
        tb[0] = new JRadioButton("Mute");
        tb[0].setName("Mute");
        tb[0].setForeground(Color.WHITE);
        tb[1] = new JRadioButton("Unmute");
        tb[1].setName("Unmute");
        tb[1].setSelected(true);
        tb[1].setForeground(Color.WHITE);
        // add to group so ensure cant deselect
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(tb[0]);
        buttonGroup.add(tb[1]);

        tb[0].setBounds(30, 100, 100, 30);
        tb[1].setBounds(130, 100, 100, 30);
        contentPane.add(tb[0]);
        contentPane.add(tb[1]);
        tb[0].setOpaque(false);
        tb[1].setOpaque(false);


        tb[0].addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tb[1].setSelected(false); // deeselect the other button
                }
                title.pauseSound();
            }
        });

        tb[1].addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tb[0].setSelected(false); 
                }
                title.resume();
            }
        });

        setPreferredSize(new Dimension(getWidth(), getHeight()));

        AddBackground();

        pack();
        setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                title.SFX(MyConstants.FILE_TITLE, true, 0.5f / 2);
            }
        });
    }

    private void AddBackground() {
        ImageIcon originalGif = new ImageIcon(MyConstants.FILE_STARTBG);
        Image scaledImage = originalGif.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
        ImageIcon scaledGif = new ImageIcon(scaledImage);
        drawpane = new JLabel(scaledGif);
        drawpane.setLayout(null);

        drawpane.setBounds(0, 0, getWidth(), getHeight());
        contentPane.add(drawpane);
        contentPane.setComponentZOrder(drawpane, contentPane.getComponentCount() - 1);
    }

    private void showCredits() {
        JFrame creditsFrame = new JFrame("Credits");
        creditsFrame.setBounds(300, 300, 400, 200);
        creditsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        creditsFrame.setLayout(new GridLayout(7, 1));

        int fontSize = 18;

        JLabel nameLabel1 = new JLabel(" Frank Piyawat Davies 6480255 ");
        Font customFont1 = new Font("Times New Roman", Font.BOLD, fontSize); 
        nameLabel1.setFont(customFont1);

        JLabel nameLabel2 = new JLabel(" Jitsopin Kanthaulis 6480376 ");
        Font customFont2 = new Font("Times New Roman", Font.BOLD, fontSize); 
        nameLabel2.setFont(customFont2);

        JLabel nameLabel3 = new JLabel(" Chanakan Boonchoo 6580128 ");
        Font customFont3 = new Font("Times New Roman", Font.BOLD, fontSize); 
        nameLabel3.setFont(customFont3);

        JLabel nameLabel4 = new JLabel(" Anaphat Sueakhamron 6480228 ");
        Font customFont4 = new Font("Times New Roman", Font.BOLD, fontSize); 
        nameLabel4.setFont(customFont4);

        JLabel nameLabel5 = new JLabel(" ~~ ASSETS ~~ ");
        JLabel nameLabel6 = new JLabel(" Touhou 6, 8, 15 ; HoloCure ");
        JLabel nameLabel7 = new JLabel(" Honkai: Star Rail ; Umineko ");
        nameLabel6.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel5.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel7.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel5.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
        nameLabel6.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
        nameLabel7.setFont(new Font("Times New Roman", Font.BOLD, fontSize));

        creditsFrame.add(nameLabel1);
        creditsFrame.add(nameLabel2);
        creditsFrame.add(nameLabel3);
        creditsFrame.add(nameLabel4);
        creditsFrame.add(nameLabel5);
        creditsFrame.add(nameLabel6);
        creditsFrame.add(nameLabel7);

        creditsFrame.pack();
        creditsFrame.setLocationRelativeTo(null);
        creditsFrame.setVisible(true);
    }

    private void showScoreboard() {
        JFrame scoreFrame = new JFrame("Scoreboard");
        scoreFrame.setBounds(400, 400, 400, 200);
        scoreFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(scorePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scoreFrame.add(scrollPane, BorderLayout.CENTER);

        

        scoreboard sb = new scoreboard();
        ArrayList<playername> players = sb.getAllPlayers();
        for (playername player : players) {
            JLabel playerLabel = new JLabel(player.toString());
            scorePanel.add(playerLabel); 
            System.out.println(player.toString());
            System.out.println("player.toString()");
        }

        JPanel buttonPanel = new JPanel();
        JButton myButton = new JButton("Clear");
        myButton.addActionListener(e -> {
            players.clear();
            scoreboard.clearScores();
            scorePanel.removeAll();
            scorePanel.revalidate();
            scorePanel.repaint();
        });
        buttonPanel.add(myButton);
        scoreFrame.add(buttonPanel, BorderLayout.SOUTH);
        

        scoreFrame.pack(); 
        scoreFrame.setLocationRelativeTo(null);
        scoreFrame.setVisible(true); 
    }

    private void showGuide() {
        JFrame guideFrame = new JFrame("Guide");
        guideFrame.setBounds(300, 300, 400, 200);
        guideFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        addImagePanel(MyConstants.FILE_SLIDE1, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE2, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE3, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE4, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE5, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE6, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE7, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE8, cardPanel);
        addImagePanel(MyConstants.FILE_SLIDE9, cardPanel);

        JButton prevButton = new JButton("<<");
        prevButton.addActionListener(e -> cardLayout.previous(cardPanel));

        JButton nextButton = new JButton(">>");
        nextButton.addActionListener(e -> cardLayout.next(cardPanel));

        prevButton.setPreferredSize(new Dimension(500, 40));
        nextButton.setPreferredSize(new Dimension(500, 40));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        guideFrame.add(buttonPanel);

        guideFrame.setLayout(new BorderLayout());
        guideFrame.add(cardPanel, BorderLayout.CENTER);
        guideFrame.add(buttonPanel, BorderLayout.SOUTH);

        guideFrame.pack();
        guideFrame.setLocationRelativeTo(null);
        guideFrame.setVisible(true);
    }

    public void addImagePanel(String imagePath, JPanel cardPanel) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(new ImageIcon(imagePath));
        panel.add(label);
        cardPanel.add(panel);
    }

    public static String getDifficulty() {
        return selectedDifficulty;
    }
    public static float getVolume(){return volume;}

    public static void stoptitleSound() {
        title.stopSound();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApplication mainapp = new MainApplication();
            mainapp.setResizable(false); 
        });
        System.out.println(System.getProperty("java.version"));
        scoreboard sb = new scoreboard();
        for (playername p : sb.getAllPlayers()) {
            System.out.println(p); // Assuming player class has a meaningful toString() method
        }
    }

    public static String getUsername() {
        return usernameField.getText();
    }
    public static String getPassword() {
        return passwordField.getText();
    }
}

class StartButton extends JButton implements MouseListener {

    private int curX = 50, curY = 0;
    private int width = 100, height = 100;

    private ImageIcon startImage;
    //private game gameInstance;
    private MySoundEffect title;

    public StartButton(game gameInstance) {
        // Load image from the resources
        startImage = new ImageIcon(MyConstants.FILE_STARTBUTTON);

        // Resize the image to fit the button
        Image scaledImage = startImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        startImage = new ImageIcon(scaledImage);

        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);

        setBounds(curX, curY, width, height);
        setIcon(startImage);

        addMouseListener(this);
        //this.gameInstance = gameInstance;

        this.title = new MySoundEffect();

    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        MainApplication.stoptitleSound();
        title.SFX(MyConstants.FILE_OK, false, 0.7f * MainApplication.getVolume());
        System.out.println("Game Started");
        // start game
        startGame();
    }

    public void startGame() {
        game gameInstance = new game(MainApplication.getDifficulty(), MainApplication.getUsername(), MainApplication.getPassword(), MainApplication.getVolume());
        gameInstance.addKeyListener(new KeyInput(gameInstance));
        MouseInput mouseInput = new MouseInput(gameInstance);
        gameInstance.addMouseListener(mouseInput);
        gameInstance.addMouseMotionListener(mouseInput);

        JFrame gameFrame = new JFrame(game.TITLE);
        gameFrame.add(gameInstance);
        gameFrame.setPreferredSize(new Dimension(game.WIDTH, game.HEIGHT));
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setResizable(false);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        gameFrame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                System.out.println("Window gained focus");
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                System.out.println("Window lost focus");
                gameInstance.togglePauseOnWindowLostFocus();
            }
        });

        gameInstance.start();
        // startmenu disappear
        SwingUtilities.getWindowAncestor(this).setVisible(false);
    }
    
}

/////////////////Scoreboard 

class playername implements Comparable<playername> {
    private String name;
    private int score;
    //private String password;

    public playername(String name, int score, String password){
        this.name = name;
        this.score = score;
        //this.password = password;
    }

    @Override
    public int compareTo(playername other) {
        return Integer.compare(other.score, this.score);
    }
    public String toString() {
        return "\nName: " + name + ", Score: " + score;
    }
}

class scoreboard{
    static String inputFile = MyConstants.FILE_SCORES;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> passwords = new ArrayList<>();
    ArrayList<Integer> scores = new ArrayList<>();
    static ArrayList<playername> allplayers = new ArrayList<>();
    String player, password;
    public scoreboard(){
        allplayers.clear();
        try{
            Scanner fscanner = new Scanner(new File(inputFile));
            
            while(fscanner.hasNext()){
                String line = fscanner.nextLine();
                String [] col = line.split(","); 
                
                String name = col[0].trim();
                int score = Integer.parseInt(col[1].trim());
                String password = col[2].trim();

                playername players = new playername(name, score, password);
                allplayers.add(players);
            }
        Collections.sort(allplayers);
        fscanner.close();
        }catch (IOException e) {
            System.err.println("An error occurred while processing the file.");
            e.printStackTrace();
        }
    }
    public ArrayList<playername> getAllPlayers() {
        return allplayers;
    }

    public static void saveScores(String username, int score, String password) {
        try {
            FileWriter writer = new FileWriter(new File(inputFile), true);//a pen
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.println(username + ", " + score + ", " + password);

            printWriter.close();
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    public static void clearScores() {
        try {
            FileWriter writer = new FileWriter(new File(inputFile));
            writer.write("");
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occurred while clearing the file.");
            e.printStackTrace();
        }
    }
}
