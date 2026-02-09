package com.wynvers.quantum.storage.upgrades;

public class ExperienceUpgrade extends StorageUpgrade {

    private int experience;
    private static final int LEVEL_UP_EXPERIENCE = 100;

    public ExperienceUpgrade() {
        super("experience_upgrade"); // id interne, tu peux le renommer si tu veux
        this.experience = 0;
        this.level = 0;
    }

    public void addExperience(int amount) {
        this.experience += amount;
        while (this.experience >= LEVEL_UP_EXPERIENCE) {
            levelUp();
        }
    }

    private void levelUp() {
        this.experience -= LEVEL_UP_EXPERIENCE;
        this.level++;
        // Tu pourras envoyer un message au joueur ici si tu veux
    }

    public int getLevel() {
        return level;
    }

    public double calculateDiscount() {
        return level * 0.05; // 5% de réduction par niveau
    }

    @Override
    public String getDisplayName() {
        return "§d§lAmélioration d'expérience";
    }

    @Override
    public String getDescription() {
        int percent = (int) (calculateDiscount() * 100);
        return "§7Réduit le coût des upgrades de §e" + percent + "%";
    }

    @Override
    public double getCost() {
        // coût de base 5 000, qui augmente avec le niveau
        return 5000.0 * (level + 1);
    }
}
