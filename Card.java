import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import org.json.JSONObject;

public class Card {

    private String name;

    private String manacost;

    private String type;

    private String rarity;

    private String ruletext;

    private String power;

    private String toughness;

    private String cardurl;

    private int id;

    public Card() {
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
            } else {
                // Read from JAR resource
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                java.util.List<String> lines = reader.lines().collect(java.util.stream.Collectors.toList());
                reader.close();
                lineNumber = r.nextInt(lines.size());
                String randomLine = lines.get(lineNumber);
                JSONObject currentCard = new JSONObject(randomLine);

                this.name = currentCard.getString("name");
                this.manacost = currentCard.getString("mana_cost");
                this.rarity = currentCard.getString("rarity");
                this.ruletext = currentCard.getString("oracle_text");
                this.type = currentCard.getString("type_line");
                //this.cardurl = currentCard.getString("image_uris").toString();
                this.id = lineNumber;

                if (this.type.contains("Creature")) {
                    this.power = currentCard.getString("power");
                    this.toughness = currentCard.getString("toughness");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Card(int lineNumber) {
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
            if (lineNumber > 0 && lineNumber <= lines.size()) {

                String line = lines.get(lineNumber);
                JSONObject currentCard = new JSONObject(line);

                this.name = currentCard.getString("name");
                this.manacost = currentCard.getString("mana_cost");
                this.rarity = currentCard.getString("rarity");
                this.ruletext = currentCard.getString("oracle_text");
                this.type = currentCard.getString("type_line");
                //this.cardurl = currentCard.getString("image_uris").toString();
                this.id = lineNumber;

                
            
                if (this.type.contains("Creature")) {
                    this.power = currentCard.getString("power");
                    this.toughness = currentCard.getString("toughness");
                }
        }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardurl() {
        return this.cardurl;
    }

    public void setCardurl(String cardurl) {
        this.cardurl = cardurl;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManacost() {
        return this.manacost;
    }

    public void setManacost(String manacost) {
        this.manacost = manacost;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRarity() {
        return this.rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getRuletext() {
        return this.ruletext;
    }

    public void setRuletext(String ruletext) {
        this.ruletext = ruletext;
    }

    public String getPower() {
        return this.power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getToughness() {
        return this.toughness;
    }

    public void setToughness(String toughness) {
        this.toughness = toughness;
    }

    @Override
    public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Name: ").append(name).append("\n");
    sb.append("Mana Cost: ").append(manacost).append("\n");
    sb.append("Type: ").append(type).append("\n");
    sb.append("Rarity: ").append(rarity).append("\n");
    sb.append("Rules: ").append(ruletext).append("\n");
    if (type != null && type.contains("Creature")) {
        sb.append("Power/Toughness: ").append(power).append("/").append(toughness).append("\n");
    }
    return sb.toString();
    }




    
}



