package com.wynvers.quantum.armor;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public enum ArmorRarity {
    
    COMMON("Commun", ChatColor.GRAY, 1, new EnchantConfig()
        .add(Enchantment.PROTECTION, 1, 2)
        .add(Enchantment.UNBREAKING, 1, 1)
    ),
    
    UNCOMMON("Peu Commun", ChatColor.GREEN, 2, new EnchantConfig()
        .add(Enchantment.PROTECTION, 2, 3)
        .add(Enchantment.UNBREAKING, 2, 2)
        .add(Enchantment.THORNS, 1, 1)
    ),
    
    RARE("Rare", ChatColor.BLUE, 3, new EnchantConfig()
        .add(Enchantment.PROTECTION, 3, 4)
        .add(Enchantment.UNBREAKING, 3, 3)
        .add(Enchantment.THORNS, 1, 2)
        .add(Enchantment.FIRE_PROTECTION, 1, 2)
    ),
    
    EPIC("Épique", ChatColor.DARK_PURPLE, 4, new EnchantConfig()
        .add(Enchantment.PROTECTION, 4, 5)
        .add(Enchantment.UNBREAKING, 3, 3)
        .add(Enchantment.THORNS, 2, 3)
        .add(Enchantment.FIRE_PROTECTION, 2, 3)
        .add(Enchantment.BLAST_PROTECTION, 2, 3)
    ),
    
    LEGENDARY("Légendaire", ChatColor.GOLD, 5, new EnchantConfig()
        .add(Enchantment.PROTECTION, 5, 6)
        .add(Enchantment.UNBREAKING, 3, 3)
        .add(Enchantment.THORNS, 3, 3)
        .add(Enchantment.FIRE_PROTECTION, 3, 4)
        .add(Enchantment.BLAST_PROTECTION, 3, 4)
        .add(Enchantment.PROJECTILE_PROTECTION, 2, 3)
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
