package com.wynvers.quantum.armor;

/**
 * Types de runes disponibles pour l'armure de donjon
 * Chaque rune a un niveau maximum (1-3) et donne des bonus
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public enum RuneType {
    
    FORCE("Force", 3),           // +10%/20%/30% dégâts
    SPEED("Vitesse", 3),         // +20%/40%/60% vitesse
    RESISTANCE("Résistance", 3), // +10%/20%/30% réduction dégâts
    CRITICAL("Critique", 3),     // +15%/30%/45% chance de crit
    REGENERATION("Régénération", 3), // +0.5/1.0/1.5 HP/s
    VAMPIRISM("Vampirisme", 3),  // Vol de vie 10%/20%/30%
    THORNS("Épines", 3),         // Retour 20%/40%/60% dégâts
    WISDOM("Sagesse", 3),        // +50%/100%/150% XP
    LUCK("Chance", 3);           // +10%/20%/30% loot rare
    
    private final String display;
    private final int maxLevel;
    
    RuneType(String display, int maxLevel) {
        this.display = display;
        this.maxLevel = maxLevel;
    }
    
    /**
     * Nom d'affichage de la rune
     */
    public String getDisplay() {
        return display;
    }
    
    /**
     * Niveau maximum de la rune (généralement 3)
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Récupère une rune depuis son nom
     */
    public static RuneType fromString(String name) {
        for (RuneType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
