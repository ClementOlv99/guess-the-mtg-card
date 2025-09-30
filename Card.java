


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



