package com.wynvers.quantum.storage.upgrades;

public class ExperienceUpgrade extends StorageUpgrade {
    private int experience;
    private int level;
    private static final int LEVEL_UP_EXPERIENCE = 100;

    public ExperienceUpgrade() {
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
        // Notify user of level up, if necessary
    }

    public int getLevel() {
        return level;
    }

    public double calculateDiscount() {
        return level * 0.05; // Example: 5% discount per level
    }
}