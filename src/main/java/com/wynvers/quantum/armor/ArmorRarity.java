package com.wynvers.quantum.armor;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;

public enum ArmorRarity {
    // ⚠️ IMPORTANT : Remplace "nexo:rarity_common" par le VRAI nom défini dans ta config Nexo
    // Si tu ne connais pas le nom, fais /data get entity @s SelectedItem sur un item Nexo en jeu
    COMMON("nexo:common"),
    UNCOMMON("nexo:uncommon"),
    RARE("nexo:rare"),
    EPIC("nexo:epic"),
    LEGENDARY("nexo:legendary");

    private final String tooltipId;

    // Constructeur de l'enum
    ArmorRarity(String tooltipId) {
        this.tooltipId = tooltipId;
    }

    /**
     * Récupère la NamespacedKey nécessaire pour meta.setTooltipStyle()
     */
    public NamespacedKey getTooltipKey() {
        // Gestion du namespace (ex: "nexo:common" -> namespace="nexo", key="common")
        if (tooltipId.contains(":")) {
            String[] parts = tooltipId.split(":");
            return new NamespacedKey(parts[0], parts[1]);
        }
        // Si aucun namespace n'est précisé, on suppose que c'est "nexo" ou "minecraft"
        // Tu peux changer "nexo" par "minecraft" si tes styles sont enregistrés sous minecraft
        return new NamespacedKey("nexo", tooltipId);
    }
    
    public String getRawId() {
        return tooltipId;
    }
}
    
    private static final Map<ArmorRarity, RarityData> DATA_MAP = new HashMap<>();
    
    /**
     * Initialise les raretés depuis la config
     */
    public static void init(JavaPlugin plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon_armor.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon_armor.yml", false);
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");
            
            if (raritiesSection == null) {
                plugin.getLogger().warning("⚠️ Section 'rarities' absente dans dungeon_armor.yml, utilisation des valeurs par défaut");
                loadDefaults();
                return;
            }
            
            for (ArmorRarity rarity : values()) {
                ConfigurationSection raritySection = raritiesSection.getConfigurationSection(rarity.name());
                if (raritySection == null) {
                    plugin.getLogger().warning("⚠️ Rareté " + rarity.name() + " absente dans la config, utilisation valeur par défaut");
                    continue;
                }
                
                String displayName = raritySection.getString("display_name", rarity.name());
                String colorStr = raritySection.getString("color", "WHITE");
                ChatColor color = parseChatColor(colorStr);
                int maxRuneSlots = raritySection.getInt("max_rune_slots", 1);
                
                // Charger les Nexo IDs
                Map<String, String> nexoIds = new HashMap<>();
                ConfigurationSection nexoIdsSection = raritySection.getConfigurationSection("nexo_ids");
                if (nexoIdsSection != null) {
                    for (String key : nexoIdsSection.getKeys(false)) {
                        nexoIds.put(key, nexoIdsSection.getString(key));
                    }
                }
                
                List<EnchantmentConfig> enchantments = new ArrayList<>();
                List<Map<?, ?>> enchantList = raritySection.getMapList("enchantments");
                for (Map<?, ?> enchantMap : enchantList) {
                    String enchantName = (String) enchantMap.get("enchant");
                    
                    // Utiliser get() et vérifier si null
                    Object minLevelObj = enchantMap.get("min_level");
                    Object maxLevelObj = enchantMap.get("max_level");
                    
                    int minLevel = 1;
                    int maxLevel = 1;
                    
                    if (minLevelObj instanceof Number) {
                        minLevel = ((Number) minLevelObj).intValue();
                    }
                    
                    if (maxLevelObj instanceof Number) {
                        maxLevel = ((Number) maxLevelObj).intValue();
                    }
                    
                    Enchantment enchant = parseEnchantment(enchantName);
                    if (enchant != null) {
                        enchantments.add(new EnchantmentConfig(enchant, minLevel, maxLevel));
                    } else {
                        plugin.getLogger().warning("⚠️ Enchantement invalide: " + enchantName);
                    }
                }
                
                DATA_MAP.put(rarity, new RarityData(displayName, color, maxRuneSlots, nexoIds, enchantments));
            }
            
            plugin.getLogger().info("✔ Raretés d'armure chargées depuis dungeon_armor.yml");
            
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Erreur lors du chargement des raretés: " + e.getMessage());
            e.printStackTrace();
            loadDefaults();
        }
    }
    
    private static void loadDefaults() {
        Map<String, String> commonIds = Map.of(
            "helmet", "dungeon_helmet_common",
            "chestplate", "dungeon_chestplate_common",
            "leggings", "dungeon_leggings_common",
            "boots", "dungeon_boots_common"
        );
        
        DATA_MAP.put(COMMON, new RarityData("Commun", ChatColor.GRAY, 1, commonIds,
            List.of(
                new EnchantmentConfig(Enchantment.PROTECTION, 1, 2),
                new EnchantmentConfig(Enchantment.UNBREAKING, 1, 1)
            )));
        
        Map<String, String> uncommonIds = Map.of(
            "helmet", "dungeon_helmet_uncommon",
            "chestplate", "dungeon_chestplate_uncommon",
            "leggings", "dungeon_leggings_uncommon",
            "boots", "dungeon_boots_uncommon"
        );
        
        DATA_MAP.put(UNCOMMON, new RarityData("Peu Commun", ChatColor.GREEN, 2, uncommonIds,
            List.of(
                new EnchantmentConfig(Enchantment.PROTECTION, 2, 3),
                new EnchantmentConfig(Enchantment.UNBREAKING, 2, 2),
                new EnchantmentConfig(Enchantment.THORNS, 1, 1)
            )));
        
        Map<String, String> rareIds = Map.of(
            "helmet", "dungeon_helmet_rare",
            "chestplate", "dungeon_chestplate_rare",
            "leggings", "dungeon_leggings_rare",
            "boots", "dungeon_boots_rare"
        );
        
        DATA_MAP.put(RARE, new RarityData("Rare", ChatColor.BLUE, 3, rareIds,
            List.of(
                new EnchantmentConfig(Enchantment.PROTECTION, 3, 4),
                new EnchantmentConfig(Enchantment.UNBREAKING, 3, 3),
                new EnchantmentConfig(Enchantment.THORNS, 1, 2),
                new EnchantmentConfig(Enchantment.FIRE_PROTECTION, 1, 2)
            )));
        
        Map<String, String> epicIds = Map.of(
            "helmet", "dungeon_helmet_epic",
            "chestplate", "dungeon_chestplate_epic",
            "leggings", "dungeon_leggings_epic",
            "boots", "dungeon_boots_epic"
        );
        
        DATA_MAP.put(EPIC, new RarityData("Épique", ChatColor.DARK_PURPLE, 4, epicIds,
            List.of(
                new EnchantmentConfig(Enchantment.PROTECTION, 4, 5),
                new EnchantmentConfig(Enchantment.UNBREAKING, 3, 3),
                new EnchantmentConfig(Enchantment.THORNS, 2, 3),
                new EnchantmentConfig(Enchantment.FIRE_PROTECTION, 2, 3),
                new EnchantmentConfig(Enchantment.BLAST_PROTECTION, 2, 3)
            )));
        
        Map<String, String> legendaryIds = Map.of(
            "helmet", "dungeon_helmet_legendary",
            "chestplate", "dungeon_chestplate_legendary",
            "leggings", "dungeon_leggings_legendary",
            "boots", "dungeon_boots_legendary"
        );
        
        DATA_MAP.put(LEGENDARY, new RarityData("Légendaire", ChatColor.GOLD, 5, legendaryIds,
            List.of(
                new EnchantmentConfig(Enchantment.PROTECTION, 5, 6),
                new EnchantmentConfig(Enchantment.UNBREAKING, 3, 3),
                new EnchantmentConfig(Enchantment.THORNS, 3, 3),
                new EnchantmentConfig(Enchantment.FIRE_PROTECTION, 3, 4),
                new EnchantmentConfig(Enchantment.BLAST_PROTECTION, 3, 4),
                new EnchantmentConfig(Enchantment.PROJECTILE_PROTECTION, 2, 3)
            )));
    }
    
    private static ChatColor parseChatColor(String colorStr) {
        try {
            return ChatColor.valueOf(colorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }
    
    private static Enchantment parseEnchantment(String name) {
        try {
            return Enchantment.getByName(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getDisplayName() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data.color + "§l" + data.displayName.toUpperCase();
    }
    
    public String getColoredName() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data.color + data.displayName;
    }
    
    public ChatColor getColor() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data.color;
    }
    
    public int getMaxRuneSlots() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data.maxRuneSlots;
    }
    
    public String getNexoId(String armorType) {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data.nexoIds.get(armorType);
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        Map<Enchantment, Integer> result = new HashMap<>();
        for (EnchantmentConfig config : data.enchantments) {
            result.put(config.enchantment, config.getRandomLevel());
        }
        return result;
    }
    
    private static class RarityData {
        final String displayName;
        final ChatColor color;
        final int maxRuneSlots;
        final Map<String, String> nexoIds;
        final List<EnchantmentConfig> enchantments;
        
        RarityData(String displayName, ChatColor color, int maxRuneSlots, Map<String, String> nexoIds, List<EnchantmentConfig> enchantments) {
            this.displayName = displayName;
            this.color = color;
            this.maxRuneSlots = maxRuneSlots;
            this.nexoIds = nexoIds;
            this.enchantments = enchantments;
        }
    }
    
    private static class EnchantmentConfig {
        final Enchantment enchantment;
        final int minLevel;
        final int maxLevel;
        
        EnchantmentConfig(Enchantment enchantment, int minLevel, int maxLevel) {
            this.enchantment = enchantment;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }
        
        int getRandomLevel() {
            if (minLevel == maxLevel) return minLevel;
            return minLevel + (int) (Math.random() * (maxLevel - minLevel + 1));
        }
    }
}
