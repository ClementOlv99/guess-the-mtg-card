import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;

public class Main {

    static int hintsleft = 11;
    static boolean over = false;
    static boolean won = false;
    static JLabel linkLabel;
    static ArrayList<Character> alreadyUsed = new ArrayList<>();
    static int score = 0;



    public static String to8Dash(String text) {
        StringBuilder sb = new StringBuilder();
        String thinSpace = "\u2009"; // Unicode thin space
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append('_');
            } else {
                sb.append(c);
            }
            sb.append(thinSpace); // Add thin space after each character
        }
        return sb.toString();
    }

    public static int score(int currentScore, int hintsleft) {
        return currentScore + 100 + 10*hintsleft;
    }

    public static String withThinSpaces(String text) {
        String thinSpace = " ";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c);
            sb.append(thinSpace);
        }
        return sb.toString();
    }

    public static void openBrowser(String url) {
        if (url == null || url.isEmpty()) {
            System.err.println("Error: URL is null or empty");
            return;
        }
        try {
            // First try the Desktop API (works on Windows and some Linux)
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            // If Desktop API fails, try platform-specific commands
            try {
                String os = System.getProperty("os.name").toLowerCase();
                
                if (os.contains("win")) {
                    // Windows
                    Runtime.getRuntime().exec("cmd /c start " + url);
                } else if (os.contains("mac")) {
                    // macOS
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else {
                    // Linux - try multiple commands in order
                    String[] commands = {
                        "xdg-open " + url,           // Standard freedesktop
                        "gnome-open " + url,         // GNOME
                        "kde-open " + url,           // KDE
                        "firefox " + url,            // Firefox
                        "chromium-browser " + url,   // Chromium
                        "google-chrome " + url,      // Chrome
                        "opera " + url               // Opera
                    };
                    
                    boolean success = false;
                    for (String cmd : commands) {
                        try {
                            Runtime.getRuntime().exec(cmd);
                            success = true;
                            break;
                        } catch (Exception e2) {
                            // Try next command
                        }
                    }
                }
            } catch (Exception e2) {
                ex.printStackTrace();
            }
        }
    }

    public static JLabel createLinkLabel(String text, String url) {
        linkLabel = new JLabel("<html><a href=''>" + text + "</a></html>");
        linkLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openBrowser(url);
            }
        });
        return linkLabel;
    }

    public static void main(String[] args) { 

    

        JFrame frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(1000, 1000); 
        
        Card card = new Card();
        
        // Create a panel with vertical BoxLayout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JTextArea infoArea = new JTextArea();
        if (card.getType().contains("Creature")) {
            infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + " - " + to8Dash(card.getPower()) + "/" + to8Dash(card.getToughness()) + "\n" + to8Dash(card.getRuletext()));
        } else {
            infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + "\n" + to8Dash(card.getRuletext()));
        }
        infoArea.setFont(new Font("Arial", Font.PLAIN, 28));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        // infoArea is kept hidden — it is the text state store used by all reveal logic

        CardPanel cardPanel = new CardPanel(infoArea, card.getType().contains("Creature"));
        infoArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { cardPanel.repaint(); }
            @Override public void removeUpdate(DocumentEvent e)  { cardPanel.repaint(); }
            @Override public void changedUpdate(DocumentEvent e) { cardPanel.repaint(); }
        });
        panel.add(cardPanel);

        linkLabel = createLinkLabel("See on Scryfall", card.getCardurl());

        JLabel hintsleftlabel = new JLabel("Hints left: " + hintsleft);
        hintsleftlabel.setFont(new Font("Arial", Font.PLAIN, 25));

        // Add a text field
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 26));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String placeholderText = "Type letters to reveal the card...";
        textField.setText(placeholderText);
        textField.setForeground(Color.GRAY);
        
        textField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholderText);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        // Add KeyListener
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            char pressedChar = e.getKeyChar();
            if ((Character.isLetter(pressedChar) || Character.isDigit(pressedChar)) && !alreadyUsed.contains(pressedChar) && hintsleft > 0) {
                alreadyUsed.add(pressedChar);
                hintsleft--;
                hintsleftlabel.setText("Hints left:" + hintsleft);

                String originalName = card.getName();
                String originalManacost = card.getManacost();
                String originalType = card.getType();
                String originalRule = card.getRuletext();
                String originalPower = "";
                String originalToughness = "";

                String[] originals;
                if (card.getType().contains("Creature")) {
                    originalPower = card.getPower();
                    originalToughness = card.getToughness();
                    originals = new String[]{originalName, originalManacost, originalType, originalPower, originalToughness, originalRule};
                } else {
                    originals = new String[]{originalName, originalManacost, originalType, originalRule};
                }

                String text = infoArea.getText();
                String[] displayed = new String[originals.length];
                String thinSpace = "\u2009";

                if (card.getType().contains("Creature")) {
                    // For creatures: name + manacost, type + power/toughness, ruletext
                    int firstDoubleSpace = text.indexOf("  "); // two spaces between name and manacost
                    int firstNewline = text.indexOf('\n');
                    int spaceDashSpace = firstNewline == -1 ? -1 : text.indexOf(" - ", firstNewline + 1); // space dash space between type and power/toughness
                    int secondNewline = firstNewline == -1 ? -1 : text.indexOf('\n', firstNewline + 1);

                    if (firstDoubleSpace != -1 && firstNewline != -1 && spaceDashSpace != -1 && secondNewline != -1) {
                        displayed[0] = text.substring(0, firstDoubleSpace); // name
                        displayed[1] = text.substring(firstDoubleSpace + 2, firstNewline).trim(); // manacost
                        displayed[2] = text.substring(firstNewline + 1, spaceDashSpace); // type
                        
                        String powerToughnessSection = text.substring(spaceDashSpace + 3, secondNewline);
                        int slashIndex = powerToughnessSection.indexOf("/");
                        if (slashIndex != -1) {
                            displayed[3] = powerToughnessSection.substring(0, slashIndex); // power
                            displayed[4] = powerToughnessSection.substring(slashIndex + 1); // toughness
                        } else {
                            displayed[3] = "";
                            displayed[4] = "";
                        }
                        displayed[5] = text.substring(secondNewline + 1); // ruletext
                    } else {
                        // fallback
                        String[] lines = text.split("\n", 3);
                        if (lines.length > 0) {
                            String[] nameMana = lines[0].split("  ", 2);
                            displayed[0] = nameMana.length > 0 ? nameMana[0] : "";
                            displayed[1] = nameMana.length > 1 ? nameMana[1] : "";
                        }
                        if (lines.length > 1) {
                            String[] typePowerToughness = lines[1].split(" - ", 2);
                            displayed[2] = typePowerToughness.length > 0 ? typePowerToughness[0] : "";
                            if (typePowerToughness.length > 1) {
                                String[] powerToughness = typePowerToughness[1].split("/", 2);
                                displayed[3] = powerToughness.length > 0 ? powerToughness[0] : "";
                                displayed[4] = powerToughness.length > 1 ? powerToughness[1] : "";
                            }
                        }
                        if (lines.length > 2) displayed[5] = lines[2];
                    }
                } else {
                    // For non-creatures: name + manacost, type, ruletext
                    int firstDoubleSpace = text.indexOf("  "); // two spaces between name and manacost
                    int firstNewline = text.indexOf('\n');
                    int secondNewline = firstNewline == -1 ? -1 : text.indexOf('\n', firstNewline + 1);

                    if (firstDoubleSpace != -1 && firstNewline != -1) {
                        displayed[0] = text.substring(0, firstDoubleSpace); // name
                        displayed[1] = text.substring(firstDoubleSpace + 2, firstNewline).trim(); // manacost
                        if (secondNewline != -1) {
                            displayed[2] = text.substring(firstNewline + 1, secondNewline);
                            displayed[3] = text.substring(secondNewline + 1);
                        } else {
                            displayed[2] = text.substring(firstNewline + 1);
                            displayed[3] = "";
                        }
                    } else {
                        // fallback: try to split by lines
                        String[] lines = text.split("\n", 3);
                        if (lines.length > 0) displayed[0] = lines[0];
                        if (lines.length > 1) displayed[1] = "";
                        if (lines.length > 1) displayed[2] = lines[1];
                        if (lines.length > 2) displayed[3] = lines[2];
                    }
                }

                StringBuilder newInfo = new StringBuilder();

                for (int i = 0; i < originals.length && i < displayed.length; i++) {
                    StringBuilder sb = new StringBuilder();
                    String orig = originals[i];
                    String shown = displayed[i] == null ? "" : displayed[i].replace(thinSpace, "");
                    int shownIdx = 0;

                    for (int j = 0; j < orig.length(); j++) {
                        char origChar = orig.charAt(j);

                        if (Character.isLetterOrDigit(origChar)) {
                            if (shownIdx < shown.length()) {
                                char shownChar = shown.charAt(shownIdx);
                                if (shownChar == '_' && Character.toLowerCase(origChar) == Character.toLowerCase(pressedChar)) {
                                    sb.append(origChar);
                                } else {
                                    sb.append(shownChar);
                                }
                            } else {
                                sb.append('_');
                            }
                            shownIdx++;
                        } else {
                            sb.append(origChar);
                            if (shownIdx < shown.length()) {
                                shownIdx++;
                            }
                        }
                        sb.append(thinSpace);
                    }
                    newInfo.append(sb.toString());
                    
                    if (card.getType().contains("Creature")) {
                        if (i == 0) newInfo.append("  ");
                        if (i == 1) newInfo.append("\n"); 
                        if (i == 2) newInfo.append(" - "); //la il y a un if lol ça sert a quelque chose
                        if (i == 3) newInfo.append("/"); 
                        if (i == 4) newInfo.append("\n"); 
                    } else {
                        if (i == 0) newInfo.append("  "); 
                        if (i == 1 || i == 2) newInfo.append("\n");
                    }
                }
                infoArea.setText(newInfo.toString());
            }
            if (hintsleft < 0) {
                hintsleft = 0;
                if (card.getType().contains("Creature")) {
                    infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + " - " + withThinSpaces(card.getPower()) + "/" + withThinSpaces(card.getToughness()) + "\n" + withThinSpaces(card.getRuletext()));
                } else {
                    infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + "\n" + withThinSpaces(card.getRuletext()));
                }
                textField.setText("");
                panel.add(linkLabel);
                panel.revalidate();
                panel.repaint();
            }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });



        panel.add(hintsleftlabel);

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.X_AXIS));
        scorePanel.setOpaque(false);

        JLabel scoreLabel = new JLabel("Score : " + score );
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        scoreLabel.setAlignmentY(Component.CENTER_ALIGNMENT);



        JButton answerButton = new JButton("Guess Answer");
        answerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        answerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(answerButton);

            answerButton.addActionListener(e -> {
            if (!over) {
            JDialog dialog = new JDialog(frame, "Your Answer", true);
            dialog.setLayout(new BorderLayout());
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
            JTextField answerField = new JTextField();
            answerField.setFont(new Font("Arial", Font.PLAIN, 18));
            JButton validerButton = new JButton("Valider");
            validerButton.setFont(new Font("Arial", Font.PLAIN, 18));
            inputPanel.add(new JLabel("Enter the card name:"));
            inputPanel.add(answerField);
            inputPanel.add(validerButton);
            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);

            validerButton.addActionListener(ev -> {
                String userAnswer = answerField.getText().trim();
                String cardName = card.getName().trim();
                boolean correct = userAnswer.equalsIgnoreCase(cardName);
                JOptionPane pane;
                if (correct) {
                    pane = new JOptionPane("Correct!", JOptionPane.INFORMATION_MESSAGE);
                    
                    if (!over) {
                        score += score(score, hintsleft);
                        scoreLabel.setText("Score : " + score);
                    }
                    over = true;
                    won = true;
                } else {
                    pane = new JOptionPane("Wrong!", JOptionPane.ERROR_MESSAGE);
                    over = true;
                }
                JDialog msgDialog = pane.createDialog(dialog, correct ? "Result" : "Result");
                msgDialog.setModal(false);
                msgDialog.setVisible(true);

                // Close popup after ~1 second and show answer if wrong
                new javax.swing.Timer(2000, evt -> {
                msgDialog.dispose();
                dialog.dispose();
                    if (card.getType().contains("Creature")) {
                        infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + " - " + withThinSpaces(card.getPower()) + "/" + withThinSpaces(card.getToughness()) + "\n" + withThinSpaces(card.getRuletext()));
                    } else {
                        infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + "\n" + withThinSpaces(card.getRuletext()));
                    }
                    textField.setText("");
                    hintsleft = 0;
                    hintsleftlabel.setText("Hints left: 0");
                    panel.add(linkLabel);
                    panel.revalidate();
                    panel.repaint();

                }) {{ setRepeats(false); }}.start();
            });
            
            dialog.setVisible(true);
        }
            });
        

        


        JPanel seedPanel = new JPanel();
        seedPanel.setLayout(new BoxLayout(seedPanel, BoxLayout.X_AXIS));
        seedPanel.setOpaque(false);

        JLabel seedLabel = new JLabel("Seed : " + card.getId());
        seedLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        seedLabel.setAlignmentY(Component.CENTER_ALIGNMENT);








                
        // Add scorePanel to the left of the main panel

        
        scorePanel.add(scoreLabel); 
        panel.add(scorePanel);

        panel.add(seedPanel);
        seedPanel.add(seedLabel);


        JButton newcardButton = new JButton("New Random Card");
        newcardButton.setFont(new Font("Arial", Font.PLAIN, 18));
        newcardButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(newcardButton);
        newcardButton.addActionListener(e -> {
            Card newCard = new Card();
            card.setName(newCard.getName());
            card.setType(newCard.getType());
            card.setRuletext(newCard.getRuletext());
            card.setManacost(newCard.getManacost());
            card.setRarity(newCard.getRarity());
            card.setPower(newCard.getPower());
            card.setToughness(newCard.getToughness());
            card.setCardurl(newCard.getCardurl());
            card.setId(newCard.getId());
            cardPanel.setCreature(card.getType().contains("Creature"));
            if (card.getType().contains("Creature")) {
                infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + " - " + to8Dash(card.getPower()) + "/" + to8Dash(card.getToughness()) + "\n" + to8Dash(card.getRuletext()));
            } else {
                infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + "\n" + to8Dash(card.getRuletext()));
            }
            textField.setText("");
            hintsleft = 11;
            panel.remove(linkLabel);
            linkLabel = createLinkLabel("See on Scryfall", card.getCardurl());
            hintsleftlabel.setText("Hints left: " + hintsleft);
            seedLabel.setText("Seed : " + card.getId());
            panel.revalidate();
            panel.repaint();
            alreadyUsed.clear();

        
            if (!won) {
                score = 0;
            }
            scoreLabel.setText("Score : " + score);
            won = false;
            over = false;
        });


        JButton setCardButton = new JButton("Set Card by seed");
        setCardButton.setFont(new Font("Arial", Font.PLAIN, 18));
        setCardButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(setCardButton);
        setCardButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter Card ID (0 to 28937):", "Set Card by ID", JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                try {
                    int lineNumber = Integer.parseInt(input.trim());
                    Card newCard = new Card(lineNumber);
                    if (newCard.getName() == null || newCard.getName().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Invalid Card ID or card not suitable for the game.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    card.setName(newCard.getName());
                    card.setType(newCard.getType());
                    card.setRuletext(newCard.getRuletext());
                    card.setManacost(newCard.getManacost());
                    card.setRarity(newCard.getRarity());
                    card.setPower(newCard.getPower());
                    card.setToughness(newCard.getToughness());
                    card.setCardurl(newCard.getCardurl());
                    card.setId(newCard.getId());
                    cardPanel.setCreature(card.getType().contains("Creature"));
                    if (card.getType().contains("Creature")) {
                        infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + " - " + to8Dash(card.getPower()) + "/" + to8Dash(card.getToughness()) + "\n" + to8Dash(card.getRuletext()));
                    } else {
                        infoArea.setText(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + "\n" + to8Dash(card.getRuletext()));
                    }
                    textField.setText("");
                    hintsleft = 11;
                    panel.remove(linkLabel);
                    linkLabel = createLinkLabel("See on Scryfall", card.getCardurl());
                    hintsleftlabel.setText("Hints left: " + hintsleft);
                    seedLabel.setText("Seed : " + card.getId());
                    score = 0;
                    scoreLabel.setText("Score : " + score);

                    panel.revalidate();
                    panel.repaint();
                    alreadyUsed.clear();
                    over = false;
                    won = false;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid integer for the Card ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    alreadyUsed.clear();
                }
            }
        });





        JButton showanswerButton = new JButton();
        showanswerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        showanswerButton.setText("Show Answer");
        showanswerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(showanswerButton);
        showanswerButton.addActionListener(e -> {
            over = true;
            hintsleft = 0;
            if (card.getType().contains("Creature")) {
                infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + " - " + withThinSpaces(card.getPower()) + "/" + withThinSpaces(card.getToughness()) + "\n" + withThinSpaces(card.getRuletext()));
            } else {
                infoArea.setText(withThinSpaces(card.getName()) + "  " + withThinSpaces(card.getManacost()) + "\n" + withThinSpaces(card.getType()) + "\n" + withThinSpaces(card.getRuletext()));
            }
            textField.setText("");
            panel.add(linkLabel);
            panel.revalidate();
            panel.repaint();
        });








       
        seedPanel.add(Box.createHorizontalGlue());
        
        // Insert seedPanel just before showanswerButton for vertical alignment
        panel.add(seedPanel, panel.getComponentZOrder(showanswerButton) - 1);
         
        panel.add(textField);

        scorePanel.add(Box.createHorizontalGlue());
        
        // Insert seedPanel just before showanswerButton for vertical alignment
        panel.add(scorePanel, panel.getComponentZOrder(answerButton) + 1);
         
        panel.add(textField);
        



        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        
        frame.setVisible(true); 

        
        
    }

    static class CardPanel extends JPanel {

        // Proportional coords for creature frame (frame.png 570x797)
        private static final double C_NAME_Y    = 0.085;
        private static final double C_TYPE_Y    = 0.600;
        private static final double C_ORA_Y     = 0.640;
        private static final double C_ORA_BOT   = 0.942;
        private static final double C_PT_X      = 0.875;
        private static final double C_PT_Y      = 0.920;

        // Proportional coords for non-creature frame (ncframe.jpg 736x1027)
        private static final double NC_NAME_Y   = 0.085;
        private static final double NC_TYPE_Y   = 0.595;
        private static final double NC_ORA_Y    = 0.638;
        private static final double NC_ORA_BOT  = 0.938;

        // Horizontal margins are the same proportion in both frames
        private static final double TEXT_LEFT   = 0.092;
        private static final double TEXT_RIGHT  = 0.908;

        private final JTextArea source;
        private boolean creature;
        private BufferedImage creatureImg;
        private BufferedImage ncImg;

        CardPanel(JTextArea source, boolean creature) {
            this.source = source;
            this.creature = creature;
            setPreferredSize(new Dimension(500, 620));
            setOpaque(false);
            try {
                InputStream cs = Main.class.getResourceAsStream("/assets/frame.png");
                creatureImg = cs != null ? ImageIO.read(cs) : ImageIO.read(new File("assets/frame.png"));
            } catch (Exception e) { e.printStackTrace(); }
            try {
                InputStream ns = Main.class.getResourceAsStream("/assets/ncframe.jpg");
                ncImg = ns != null ? ImageIO.read(ns) : ImageIO.read(new File("assets/ncframe.jpg"));
            } catch (Exception e) { e.printStackTrace(); }
        }

        void setCreature(boolean c) {
            this.creature = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            BufferedImage img = creature ? creatureImg : ncImg;
            if (img == null) return;

            int pw = getWidth(), ph = getHeight();
            double aspect = (double) img.getWidth() / img.getHeight();
            int iw, ih;
            if ((double) pw / ph > aspect) {
                ih = ph; iw = (int)(ph * aspect);
            } else {
                iw = pw; ih = (int)(pw / aspect);
            }
            int ix = (pw - iw) / 2;
            int iy = (ph - ih) / 2;

            g.drawImage(img, ix, iy, iw, ih, null);

            String[] parts = parseText(source.getText());

            double nameY  = creature ? C_NAME_Y  : NC_NAME_Y;
            double typeY  = creature ? C_TYPE_Y  : NC_TYPE_Y;
            double oraY   = creature ? C_ORA_Y   : NC_ORA_Y;
            double oraBotY = creature ? C_ORA_BOT : NC_ORA_BOT;

            int tl = ix + (int)(iw * TEXT_LEFT);
            int tr = ix + (int)(iw * TEXT_RIGHT);
            int tw = tr - tl;

            int sz     = Math.max(10, iw / 30);
            Font nameF = new Font("Arial", Font.BOLD,  sz);
            Font bodyF = new Font("Arial", Font.PLAIN, Math.max(8, sz - 1));
            Font oraF  = new Font("Arial", Font.PLAIN, Math.max(8, sz - 4));

            g.setColor(Color.BLACK);

            // Name — left-aligned on the name bar
            if (parts.length > 0) {
                g.setFont(nameF);
                g.drawString(parts[0], tl, iy + (int)(ih * nameY));
            }

            // Mana cost — right-aligned on the name bar
            if (parts.length > 1) {
                g.setFont(bodyF);
                FontMetrics fm = g.getFontMetrics();
                String mana = parts[1];
                g.drawString(mana, tr - fm.stringWidth(mana), iy + (int)(ih * nameY));
            }

            // Type line — left-aligned on the type bar
            if (parts.length > 2) {
                g.setFont(bodyF);
                g.drawString(parts[2], tl, iy + (int)(ih * typeY));
            }

            // Oracle text — word-wrapped in the text box
            int oraIdx = creature ? 5 : 3;
            if (parts.length > oraIdx) {
                g.setFont(oraF);
                drawWrapped(g, parts[oraIdx], tl, iy + (int)(ih * oraY), tw, iy + (int)(ih * oraBotY));
            }

            // Power/Toughness — centred on the PT box (creature only)
            if (creature && parts.length > 4) {
                g.setFont(nameF);
                String pt = parts[3] + "/" + parts[4];
                FontMetrics fm = g.getFontMetrics();
                int ptX = ix + (int)(iw * C_PT_X) - fm.stringWidth(pt) / 2;
                int ptY = iy + (int)(ih * C_PT_Y);
                g.drawString(pt, ptX, ptY);
            }
        }

        private String[] parseText(String raw) {
            // Strip thin spaces so separators are findable regardless of how setText was called
            String text = raw.replace(" ", "");
            if (creature) {
                int ds   = text.indexOf("  ");
                int nl1  = text.indexOf('\n');
                int sds  = nl1 == -1 ? -1 : text.indexOf(" - ", nl1 + 1);
                int nl2  = nl1 == -1 ? -1 : text.indexOf('\n', nl1 + 1);
                if (ds != -1 && nl1 != -1 && sds != -1 && nl2 != -1) {
                    String name  = addTS(text.substring(0, ds));
                    String mana  = addTS(text.substring(ds + 2, nl1).trim());
                    String type  = addTS(text.substring(nl1 + 1, sds));
                    String ptSec = text.substring(sds + 3, nl2);
                    int slash    = ptSec.indexOf('/');
                    String pow   = slash != -1 ? addTS(ptSec.substring(0, slash))  : "";
                    String tou   = slash != -1 ? addTS(ptSec.substring(slash + 1)) : "";
                    String ora   = addTS(text.substring(nl2 + 1));
                    return new String[]{name, mana, type, pow, tou, ora};
                }
            } else {
                int ds  = text.indexOf("  ");
                int nl1 = text.indexOf('\n');
                int nl2 = nl1 == -1 ? -1 : text.indexOf('\n', nl1 + 1);
                if (ds != -1 && nl1 != -1) {
                    String name = addTS(text.substring(0, ds));
                    String mana = addTS(text.substring(ds + 2, nl1).trim());
                    String type = addTS(nl2 != -1 ? text.substring(nl1 + 1, nl2) : text.substring(nl1 + 1));
                    String ora  = addTS(nl2 != -1 ? text.substring(nl2 + 1) : "");
                    return new String[]{name, mana, type, ora};
                }
            }
            return new String[]{raw};
        }

        private String addTS(String s) {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) { sb.append(c); sb.append(' '); }
            return sb.toString();
        }

        private void drawWrapped(Graphics2D g, String text, int x, int y, int maxW, int botY) {
            FontMetrics fm = g.getFontMetrics();
            int lh = fm.getHeight();
            int cy = y + fm.getAscent();
            for (String para : text.split("\n", -1)) {
                if (cy > botY) break;
                String[] words = para.split(" ", -1);
                StringBuilder line = new StringBuilder();
                for (String w : words) {
                    String test = line.length() == 0 ? w : line + " " + w;
                    if (fm.stringWidth(test) > maxW && line.length() > 0) {
                        g.drawString(line.toString(), x, cy);
                        cy += lh;
                        if (cy > botY) return;
                        line = new StringBuilder(w);
                    } else {
                        line = new StringBuilder(test);
                    }
                }
                if (line.length() > 0) g.drawString(line.toString(), x, cy);
                cy += lh;
            }
        }
    }

}
