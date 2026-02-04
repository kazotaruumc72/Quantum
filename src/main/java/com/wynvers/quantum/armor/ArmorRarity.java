package com.wynvers.quantum.armor;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * Système de rareté des armures de donjon avec enchantements
 */
public enum ArmorRarity {
    
    COMMON("Commun", ChatColor.GRAY, 1, new EnchantConfig()
        .add(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 2)
        .add(Enchantment.DURABILITY, 1, 1)
    ),
    
    UNCOMMON("Peu Commun", ChatColor.GREEN, 2, new EnchantConfig()
        .add(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 3)
        .add(Enchantment.DURABILITY, 2, 2)
        .add(Enchantment.THORNS, 1, 1)
    ),
    
    RARE("Rare", ChatColor.BLUE, 3, new EnchantConfig()
        .add(Enchantment.PROTECTION_ENVIRONMENTAL, 3, 4)
        .add(Enchantment.DURABILITY, 3, 3)
        .add(Enchantment.THORNS, 1, 2)
        .add(Enchantment.PROTECTION_FIRE, 1, 2)
    ),
    
    EPIC("Épique", ChatColor.DARK_PURPLE, 4, new EnchantConfig()
        .add(Enchantment.PROTECTION_ENVIRONMENTAL, 4, 5)
        .add(Enchantment.DURABILITY, 3, 3)
        .add(Enchantment.THORNS, 2, 3)
        .add(Enchantment.PROTECTION_FIRE, 2, 3)
        .add(Enchantment.PROTECTION_EXPLOSIONS, 2, 3)
    ),
    
    LEGENDARY("Légendaire", ChatColor.GOLD, 5, new EnchantConfig()
        .add(Enchantment.PROTECTION_ENVIRONMENTAL, 5, 6)
        .add(Enchantment.DURABILITY, 3, 3)
        .add(Enchantment.THORNS, 3, 3)
        .add(Enchantment.PROTECTION_FIRE, 3, 4)
        .add(Enchantment.PROTECTION_EXPLOSIONS, 3, 4)
        .add(Enchantment.PROTECTION_PROJECTILE, 2, 3)
    );
    
    private final String displayName;
    private final ChatColor color;
    private final int maxRuneSlots;
    private final EnchantConfig enchants;
    
    ArmorRarity(String displayName, ChatColor color, int maxRuneSlots, EnchantConfig enchants) {
        this.displayName = displayName;
        this.color = color;
        this.maxRuneSlots = maxRuneSlots;
        this.enchants = enchants;
    }
    
    public String getDisplayName() {
        return color + "§l" + displayName.toUpperCase();
    }
    
    public String getColoredName() {
        return color + displayName;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public int getMaxRuneSlots() {
        return maxRuneSlots;
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return enchants.getRandomEnchants();
    }
    
    /**
     * Classe helper pour configurer les enchantements avec niveaux min/max
     */
    private static class EnchantConfig {
        private final Map<Enchantment, EnchantRange> enchants = new HashMap<>();
        
        public EnchantConfig add(Enchantment enchant, int minLevel, int maxLevel) {
            enchants.put(enchant, new EnchantRange(minLevel, maxLevel));
            return this;
        }
        
        public Map<Enchantment, Integer> getRandomEnchants() {
            Map<Enchantment, Integer> result = new HashMap<>();
            for (Map.Entry<Enchantment, EnchantRange> entry : enchants.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getRandomLevel());
            }
            return result;
        }
    }
    
    /**
     * Range de niveau d'enchantement (min-max)
     */
    private static class EnchantRange {
        private final int min;
        private final int max;
        
        public EnchantRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        public int getRandomLevel() {
            if (min == max) return min;
            return min + (int) (Math.random() * (max - min + 1));
        }
    }
}
