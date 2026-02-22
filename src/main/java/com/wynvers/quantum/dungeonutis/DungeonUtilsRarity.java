package com.wynvers.quantum.dungeonutis;

import org.bukkit.ChatColor;

/**
 * Rarity levels for dungeon tools and weapons
 */
public enum DungeonUtilsRarity {
    COMMON("Commun", ChatColor.GRAY, 1.0, 1.0, 1.0),
    UNCOMMON("Peu Commun", ChatColor.GREEN, 1.25, 1.25, 1.5),
    RARE("Rare", ChatColor.BLUE, 1.5, 1.5, 2.0),
    EPIC("Épique", ChatColor.DARK_PURPLE, 2.0, 2.0, 3.0),
    LEGENDARY("Légendaire", ChatColor.GOLD, 3.0, 3.0, 5.0);

    private final String displayName;
    private final ChatColor color;
    private final double jobExpBonus;
    private final double jobMoneyBonus;
    private final double durabilityBonus;

    DungeonUtilsRarity(String displayName, ChatColor color, double jobExpBonus, double jobMoneyBonus, double durabilityBonus) {
        this.displayName = displayName;
        this.color = color;
        this.jobExpBonus = jobExpBonus;
        this.jobMoneyBonus = jobMoneyBonus;
        this.durabilityBonus = durabilityBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public double getJobExpBonus() {
        return jobExpBonus;
    }

    public double getJobMoneyBonus() {
        return jobMoneyBonus;
    }

    public double getDurabilityBonus() {
        return durabilityBonus;
    }

    /**
     * Get formatted display name with color
     */
    public String getFormattedName() {
        return color + displayName;
    }

    /**
     * Get exp bonus as percentage (e.g., 1.5 -> 50%)
     */
    public int getExpBonusPercent() {
        return (int) ((jobExpBonus - 1.0) * 100);
    }

    /**
     * Get money bonus as percentage (e.g., 2.0 -> 100%)
     */
    public int getMoneyBonusPercent() {
        return (int) ((jobMoneyBonus - 1.0) * 100);
    }

    /**
     * Get durability bonus as percentage (e.g., 3.0 -> 200%)
     */
    public int getDurabilityBonusPercent() {
        return (int) ((durabilityBonus - 1.0) * 100);
    }
}
