package com.wynvers.quantum.armor;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ArmorRarity {
    // ✅ ON UTILISE LES IDS CONFIRMÉS PAR LE DEBUG ET TA CONFIG
    COMMON("minecraft:common"),
    UNCOMMON("minecraft:uncommon"),
    RARE("minecraft:rare"),
    EPIC("minecraft:epic"),
    LEGENDARY("minecraft:legendary");

    private final String tooltipId;
    private static final Map<ArmorRarity, RarityData> DATA_MAP = new HashMap<>();

    ArmorRarity(String tooltipId) {
        this.tooltipId = tooltipId;
    }

    // --- Méthode pour récupérer la clé correcte ---
    public NamespacedKey getTooltipKey() {
        // On découpe "minecraft:common" pour avoir namespace="minecraft" et key="common"
        String[] parts = tooltipId.split(":");
        if (parts.length == 2) {
            return new NamespacedKey(parts[0], parts[1]);
        }
        // Sécurité
        return NamespacedKey.minecraft(tooltipId);
    }

    // --- LE RESTE DU FICHIER (NE CHANGE PAS) ---
    public String getDisplayName() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        if (data == null) return ChatColor.WHITE + this.name();
        return data.color + "§l" + data.displayName.toUpperCase();
    }
    public String getColoredName() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        if (data == null) return ChatColor.WHITE + this.name();
        return data.color + data.displayName;
    }
    public ChatColor getColor() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data != null ? data.color : ChatColor.WHITE;
    }
    public int getMaxRuneSlots() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data != null ? data.maxRuneSlots : 0;
    }
    public String getNexoId(String armorType) {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        return data != null ? data.nexoIds.get(armorType) : null;
    }
    public Map<Enchantment, Integer> getEnchantments() {
        RarityData data = DATA_MAP.getOrDefault(this, DATA_MAP.get(COMMON));
        Map<Enchantment, Integer> result = new HashMap<>();
        if (data != null) {
            for (EnchantmentConfig config : data.enchantments) {
                result.put(config.enchantment, config.getRandomLevel());
            }
        }
        return result;
    }
    public static void init(JavaPlugin plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon_armor.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon_armor.yml", false);
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");
            if (raritiesSection == null) {
                loadDefaults();
                return;
            }
            for (ArmorRarity rarity : values()) {
                ConfigurationSection raritySection = raritiesSection.getConfigurationSection(rarity.name());
                if (raritySection == null) continue;
                String displayName = raritySection.getString("display_name", rarity.name());
                ChatColor color = parseChatColor(raritySection.getString("color", "WHITE"));
                int maxRuneSlots = raritySection.getInt("max_rune_slots", 1);
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
                    int minLevel = getInt(enchantMap.get("min_level"), 1);
                    int maxLevel = getInt(enchantMap.get("max_level"), 1);
                    Enchantment enchant = parseEnchantment(enchantName);
                    if (enchant != null) {
                        enchantments.add(new EnchantmentConfig(enchant, minLevel, maxLevel));
                    }
                }
                DATA_MAP.put(rarity, new RarityData(displayName, color, maxRuneSlots, nexoIds, enchantments));
            }
            if (DATA_MAP.isEmpty()) loadDefaults();
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaults();
        }
    }
    private static int getInt(Object obj, int def) {
        return (obj instanceof Number) ? ((Number) obj).intValue() : def;
    }
    private static void loadDefaults() {
        Map<String, String> dummyIds = new HashMap<>();
        RarityData defaultData = new RarityData("Défaut", ChatColor.WHITE, 1, dummyIds, new ArrayList<>());
        for (ArmorRarity r : values()) {
            DATA_MAP.putIfAbsent(r, defaultData);
        }
    }
    private static ChatColor parseChatColor(String colorStr) {
        try { return ChatColor.valueOf(colorStr.toUpperCase()); } 
        catch (Exception e) { return ChatColor.WHITE; }
    }
    private static Enchantment parseEnchantment(String name) {
        if (name == null) return null;
        try { return Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase())); } 
        catch (Exception e) { return Enchantment.getByName(name.toUpperCase()); }
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
            if (minLevel >= maxLevel) return minLevel;
            return minLevel + (int) (Math.random() * (maxLevel - minLevel + 1));
        }
    }
}
