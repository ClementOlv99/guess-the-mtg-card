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

public class Main {

    static int hintsleft = 11;
    static boolean over = false;

    public static Card randomCard() {

        Random r = new Random();

        try {
            // Read all lines from the file
            java.util.List<String> lines = Files.readAllLines(Paths.get("cardlist.json"));
            // Pick a random line
            String randomLine = lines.get(r.nextInt(lines.size()));
            // Parse the line as a JSONObject
            JSONObject currentCard = new JSONObject(randomLine);

            String name = currentCard.getString("name");

            JSONObject legalities = currentCard.getJSONObject("legalities");

            String legal = legalities.getString("vintage");

            String type = currentCard.getString("type_line");

            while (name.contains("//") || !legal.equals("legal") || type.contains("creature") || type.contains("battle") || type.contains("planeswalker")) {

                // Pick a random line
                randomLine = lines.get(r.nextInt(lines.size()));
                // Parse the line as a JSONObject
                currentCard = new JSONObject(randomLine);

                name = currentCard.getString("name");
                legalities = currentCard.getJSONObject("legalities");
                legal = legalities.getString("vintage");

            }


            
            Card returncard = new Card();

            returncard.setName(name);
            returncard.setManacost(currentCard.getString("mana_cost"));
            returncard.setRarity(currentCard.getString("rarity"));
            returncard.setRuletext(currentCard.getString("oracle_text"));
            returncard.setType(currentCard.getString("type_line"));
            returncard.setCardurl(currentCard.getString("scryfall_uri").toString());

            if (returncard.getType().contains("Creature")) {
                returncard.setPower(currentCard.getString("power"));
                returncard.setToughness(currentCard.getString("toughness"));
            }

            return returncard;
        
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    public static void main(String[] args) { 

        

        JFrame frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(1000, 1000); 
        
        Card card = randomCard();
        
        // Create a panel with vertical BoxLayout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JTextArea infoArea = new JTextArea(to8Dash(card.getName()) + "  " + to8Dash(card.getManacost()) + " " + "\n" + to8Dash(card.getType()) + "\n" + to8Dash(card.getRuletext()));
        infoArea.setFont(new Font("Arial", Font.PLAIN, 28));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        panel.add(infoArea);

        JLabel linkLabel = new JLabel("<html><a href=''>View Card on Scryfall</a></html>");
        linkLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(card.getCardurl()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Could not open link: " + ex.getMessage(), 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


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
            if (Character.isLetter(pressedChar) || Character.isDigit(pressedChar)) {
                hintsleft--;
                hintsleftlabel.setText("Hints left:" + hintsleft);

                String originalName = card.getName();
                String originalManacost = card.getManacost();
                String originalType = card.getType();
                String originalRule = card.getRuletext();

                String[] originals = {originalName, originalManacost, originalType, originalRule};
                String text = infoArea.getText();
                String[] displayed = new String[4];
                String thinSpace = "\u2009";

                // Split the infoArea text into 3 lines: name+manacost, type, ruletext
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
                if (i == 0) newInfo.append("  "); // two spaces between name and manacost
                if (i == 1 || i == 2) newInfo.append("\n");
                }
                infoArea.setText(newInfo.toString());
            }
            if (hintsleft < 0) {
                String answerString;
                if (card.getType().contains("Creature")) {
                hintsleft = 0;
                answerString = card.getName() + "  " + card.getManacost() + " " + "\n" + card.getType() + "\n" + card.getRuletext();
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

        JButton newcardButton = new JButton("New Card");
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
            infoArea.setText(to8Dash(card.getName()) + " - " + to8Dash(card.getManacost()) +  "\n" + to8Dash(card.getType()) + "\n" + to8Dash(card.getRuletext()));
            textField.setText("");
            hintsleft = 10;
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
                answerString = card.getName() + " - " + card.getManacost() + "\n" + card.getType() + " - " + card.getPower() + "/" + card.getToughness() + "\n" + card.getRuletext() ;
            } else {
                hintsleft = 0;
                answerString = card.getName() + " - " + card.getManacost() + "\n" + card.getType() + "\n" + card.getRuletext();
            }
            infoArea.setText(answerString.replace("", "\u2009").trim());
            textField.setText("");
            panel.add(linkLabel);
        });

        panel.add(textField);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        
        frame.setVisible(true); 

        
        
    }
    
}
