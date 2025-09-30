import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.*; 
import java.awt.*; 
import java.io.File; 
import javax.imageio.ImageIO; 
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;

public class Main {

    static int hintsleft = 11;
    static boolean over = false;
    static JLabel linkLabel;
    static ArrayList<Character> alreadyUsed = new ArrayList<>();

// ...existing code...
public static Card randomCard() {
    Random r = new Random();
    int lineNumber = 0;
    try {
        // Read from resource inside JAR instead of file system
        java.io.InputStream inputStream = Main.class.getResourceAsStream("/cardlist.json");
        if (inputStream == null) {
            // Fallback to file system if not found in JAR (for development)
            java.util.List<String> lines = Files.readAllLines(Paths.get("cardlist.json"));
            lineNumber = r.nextInt(lines.size());
            String randomLine = lines.get(lineNumber);
            JSONObject currentCard = new JSONObject(randomLine);
            System.out.println("Resource not found in JAR, please ensure cardlist.json is included.");
            return null;            
        } else {
            // Read from JAR resource
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            java.util.List<String> lines = reader.lines().collect(java.util.stream.Collectors.toList());
            reader.close();
            lineNumber = r.nextInt(lines.size());
            String randomLine = lines.get(lineNumber);
            JSONObject currentCard = new JSONObject(randomLine);
            
            String name = currentCard.getString("name");
            JSONObject legalities = currentCard.getJSONObject("legalities");
            String legal = legalities.getString("vintage");
            String type = currentCard.getString("type_line");

            while (name.contains("//") || !legal.equals("legal") || type.contains("battle") || type.contains("planeswalker")) {
                randomLine = lines.get(r.nextInt(lines.size()));
                currentCard = new JSONObject(randomLine);
                name = currentCard.getString("name");
                legalities = currentCard.getJSONObject("legalities");
                legal = legalities.getString("vintage");
                type = currentCard.getString("type_line");
            }

            Card returncard = new Card();
            returncard.setName(name);
            returncard.setManacost(currentCard.getString("mana_cost"));
            returncard.setRarity(currentCard.getString("rarity"));
            returncard.setRuletext(currentCard.getString("oracle_text"));
            returncard.setType(currentCard.getString("type_line"));
            returncard.setCardurl(currentCard.getString("scryfall_uri").toString());
            returncard.setId(lineNumber);

            

            if (returncard.getType().contains("Creature")) {
                returncard.setPower(currentCard.getString("power"));
                returncard.setToughness(currentCard.getString("toughness"));
            }

            return returncard;
        }
        
    } catch (IOException e) {
        e.printStackTrace();
        return null;
        }

    }

    public static Card giveSetCard(int lineNumber) {
        try {
            java.io.InputStream inputStream = Main.class.getResourceAsStream("/cardlist.json");
            java.util.List<String> lines;
            if (inputStream == null) {
                lines = Files.readAllLines(Paths.get("cardlist.json"));
            } else {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                lines = reader.lines().collect(java.util.stream.Collectors.toList());
                reader.close();
            }
            if (lineNumber < 0 || lineNumber >= lines.size()) {
                return new Card(); // Return empty card if out of bounds
            }
            String line = lines.get(lineNumber);
            JSONObject currentCard = new JSONObject(line);
            String name = currentCard.getString("name");
            JSONObject legalities = currentCard.getJSONObject("legalities");
            String legal = legalities.getString("vintage");
            String type = currentCard.getString("type_line");
        
            if (name.contains("//") || !legal.equals("legal") || type.contains("battle") || type.contains("planeswalker")) {
                return new Card(); // Return empty card if invalid
            }
        
            Card returncard = new Card();
            returncard.setName(name);
            returncard.setManacost(currentCard.getString("mana_cost"));
            returncard.setRarity(currentCard.getString("rarity"));
            returncard.setRuletext(currentCard.getString("oracle_text"));
            returncard.setType(currentCard.getString("type_line"));
            returncard.setCardurl(currentCard.getString("scryfall_uri").toString());
            returncard.setId(lineNumber);
        
            if (returncard.getType().contains("Creature")) {
                returncard.setPower(currentCard.getString("power"));
                returncard.setToughness(currentCard.getString("toughness"));
            }
        
            return returncard;
        } catch (Exception e) {
            e.printStackTrace();
            return new Card(); // Return empty card on error
        }
    }



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

    public static JLabel createLinkLabel(String text, String url) {
        linkLabel = new JLabel("<html><a href=''>" + text + "</a></html>");
        linkLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return linkLabel;
    }

    public static void main(String[] args) { 

    

        JFrame frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(1000, 1000); 
        
        Card card = randomCard();
        
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
        panel.add(infoArea);

        linkLabel = createLinkLabel("See on Scryfall", card.getCardurl());

        JLabel hintsleftlabel = new JLabel("Hints left: " + hintsleft);
        hintsleftlabel.setFont(new Font("Arial", Font.PLAIN, 18));

        // Add a text field
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 26));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Add KeyListener
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            char pressedChar = e.getKeyChar();
            if ((Character.isLetter(pressedChar) || Character.isDigit(pressedChar)) && !alreadyUsed.contains(pressedChar)) {
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
                        if (i == 0) newInfo.append("  "); // two spaces between name and manacost
                        if (i == 1) newInfo.append("\n"); // newline after manacost
                        if (i == 2) newInfo.append(" - "); // space dash space between type and power
                        if (i == 3) newInfo.append("/"); // slash between power and toughness
                        if (i == 4) newInfo.append("\n"); // newline after toughness
                    } else {
                        if (i == 0) newInfo.append("  "); // two spaces between name and manacost
                        if (i == 1 || i == 2) newInfo.append("\n");
                    }
                }
                infoArea.setText(newInfo.toString());
            }
            if (hintsleft < 0) {
                String answerString;
                if (card.getType().contains("Creature")) {
                hintsleft = 0;
                answerString = card.getName() + "  " + card.getManacost() + " " + "\n" + card.getType() + " - " + card.getPower() + "/" + card.getToughness() + "\n" + card.getRuletext();
                } else {
                hintsleft = 0;
                answerString = card.getName() + "  " + card.getManacost() + " " + "\n" + card.getType() + "\n" + card.getRuletext();
                }
                infoArea.setText(answerString.replace("", "\u2009").trim());
                textField.setText("");
                panel.add(linkLabel);
            }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });



        panel.add(hintsleftlabel);

        JButton answerButton = new JButton("Give Answer");
        answerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        answerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(answerButton);
        if (!over) {
            answerButton.addActionListener(e -> {
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
                } else {
                pane = new JOptionPane("Wrong!", JOptionPane.ERROR_MESSAGE);
                }
                JDialog msgDialog = pane.createDialog(dialog, correct ? "Result" : "Result");
                msgDialog.setModal(false);
                msgDialog.setVisible(true);

                // Close popup after ~1 second and show answer if wrong
                new javax.swing.Timer(3000, evt -> {
                msgDialog.dispose();
                dialog.dispose();
                    String answerString;
                    if (card.getType().contains("Creature")) {
                    answerString = card.getName() + " - " + card.getManacost() + "\n" + card.getType() + " - " + card.getPower() + "/" + card.getToughness() + "\n" + card.getRuletext();
                    } else {
                    answerString = card.getName() + " - " + card.getManacost() + "\n" + card.getType() + "\n" + card.getRuletext();
                    }
                    infoArea.setText(answerString.replace("", "\u2009").trim());
                    textField.setText("");
                    hintsleft = 0;
                    hintsleftlabel.setText("Hints left: 0");
                    panel.add(linkLabel);
                
                }) {{ setRepeats(false); }}.start();
            });
            
            dialog.setVisible(true);
            });
        }



        JPanel seedPanel = new JPanel();
        seedPanel.setLayout(new BoxLayout(seedPanel, BoxLayout.X_AXIS));
        seedPanel.setOpaque(false);

        JLabel seedLabel = new JLabel("seed : " + card.getId());
        seedLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        seedLabel.setAlignmentY(Component.CENTER_ALIGNMENT);



        JButton newcardButton = new JButton("New Random Card");
        newcardButton.setFont(new Font("Arial", Font.PLAIN, 18));
        newcardButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(newcardButton);
        newcardButton.addActionListener(e -> {
            Card newCard = randomCard();
            card.setName(newCard.getName());
            card.setType(newCard.getType());
            card.setRuletext(newCard.getRuletext());
            card.setManacost(newCard.getManacost());
            card.setRarity(newCard.getRarity());
            card.setPower(newCard.getPower());
            card.setToughness(newCard.getToughness());
            card.setCardurl(newCard.getCardurl());
            card.setId(newCard.getId());
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
            seedLabel.setText("seed : " + card.getId());
            panel.revalidate();
            panel.repaint();
        });


        JButton setCardButton = new JButton("Set Card by seed");
        setCardButton.setFont(new Font("Arial", Font.PLAIN, 18));
        setCardButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(setCardButton);
        setCardButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter Card ID (line number):", "Set Card by ID", JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                try {
                    int lineNumber = Integer.parseInt(input.trim());
                    Card newCard = giveSetCard(lineNumber);
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
                    seedLabel.setText("seed : " + card.getId());

                    panel.revalidate();
                    panel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid integer for the Card ID.", "Error", JOptionPane.ERROR_MESSAGE);
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
            String answerString;
            if (card.getType().contains("Creature")) {
                hintsleft = 0;
                answerString = card.getName() + "  " + card.getManacost() + "\n" + card.getType() + " - " + card.getPower() + "/" + card.getToughness() + "\n" + card.getRuletext() ;
            } else {
                hintsleft = 0;
                answerString = card.getName() + "  " + card.getManacost() + "\n" + card.getType() + "\n" + card.getRuletext();
            }
            infoArea.setText(answerString.replace("", "\u2009").trim());
            textField.setText("");
            panel.add(linkLabel);
        });



        seedPanel.add(seedLabel);
        seedPanel.add(Box.createHorizontalGlue());

        // Insert seedPanel just before showanswerButton for vertical alignment
        panel.add(seedPanel, panel.getComponentZOrder(showanswerButton));
        panel.add(textField);



        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        
        frame.setVisible(true); 

        
        
    }
    
}
