package com.wynvers.quantum.dungeonutis;

/**
 * Types of dungeon tools and weapons
 */
public enum DungeonUtilsType {
    PICKAXE("dungeon_pickaxe", "Pioche de Donjon", "miner"),
    AXE("dungeon_axe", "Hache de Donjon", "lumberjack"),
    HOE("dungeon_hoe", "Houe de Donjon", "farmer"),
    SWORD("dungeon_sword", "Épée de Donjon", "hunter"),
    BOW("dungeon_bow", "Arc de Donjon", "hunter"),
    KATANA("dungeon_katana", "Katana de Donjon", "hunter"),
    BROADSWORD("dungeon_broadsword", "Épée Large de Donjon", "hunter"),
    SHIELD("dungeon_shield", "Bouclier de Donjon", "hunter");

    private final String configKey;
    private final String displayName;
    private final String compatibleJob;

    DungeonUtilsType(String configKey, String displayName, String compatibleJob) {
        this.configKey = configKey;
        this.displayName = displayName;
        this.compatibleJob = compatibleJob;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCompatibleJob() {
        return compatibleJob;
    }

    /**
     * Check if this type is a tool
     */
    public boolean isTool() {
        return this == PICKAXE || this == AXE || this == HOE;
    }

    /**
     * Check if this type is a weapon
     */
    public boolean isWeapon() {
        return this == SWORD || this == BOW || this == KATANA || this == BROADSWORD || this == SHIELD;
    }
}
