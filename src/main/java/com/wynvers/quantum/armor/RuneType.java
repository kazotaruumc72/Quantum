package com.wynvers.quantum.armor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * Enum des types de runes avec chargement depuis dungeon.yml
 */
public enum RuneType {
    FORCE,
    SPEED,
    RESISTANCE,
    CRITICAL,
    VAMPIRISM,
    REGENERATION,
    STRENGTH,
    DEFENSE,
    AGILITY;
    
    private static final Map<RuneType, RuneConfig> configs = new EnumMap<>(RuneType.class);
    
    public static void init(JavaPlugin plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon.yml", false);
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection runesSection = config.getConfigurationSection("runes");
            
            if (runesSection == null) {
                plugin.getLogger().warning("Section 'runes' non trouvée dans dungeon.yml");
                return;
            }
            
            for (String key : runesSection.getKeys(false)) {
                try {
                    RuneType type = RuneType.valueOf(key.toUpperCase());
                    ConfigurationSection runeSection = runesSection.getConfigurationSection(key);
                    
                    if (runeSection != null) {
                        RuneConfig runeConfig = new RuneConfig(runeSection);
                        configs.put(type, runeConfig);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Type de rune inconnu: " + key);
                }
            }
            
            plugin.getLogger().info("✓ " + configs.size() + " types de runes chargés depuis dungeon.yml");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du chargement de dungeon.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String getDisplay() {
        RuneConfig config = configs.get(this);
        return config != null ? config.displayName : "§c" + name();
    }
    
    public String getDescription(int level) {
        RuneConfig config = configs.get(this);
        if (config == null) return "§7???";
        return config.descriptions.getOrDefault(level, "§7Niveau " + level);
    }
    
    public int getMaxLevel() {
        RuneConfig config = configs.get(this);
        return config != null ? config.maxLevel : 3;
    }
    
    public String getNexoId(int level) {
        RuneConfig config = configs.get(this);
        return config != null ? config.nexoIds.get(level) : null;
    }
    
    public double getDamageBonus(int level) {
        if (this != FORCE) return 1.0;
        return getValueOrDefault(level, 1.0);
    }
    
    public double getSpeedBonus(int level) {
        if (this != SPEED) return 1.0;
        return getValueOrDefault(level, 1.0);
    }
    
    public double getDamageReduction(int level) {
        if (this != RESISTANCE) return 0.0;
        return getValueOrDefault(level, 0.0);
    }
    
    public double getCriticalChance(int level) {
        if (this != CRITICAL) return 0.0;
        return getValueOrDefault(level, 0.0);
    }
    
    public double getVampirismPercent(int level) {
        if (this != VAMPIRISM) return 0.0;
        return getValueOrDefault(level, 0.0);
    }
    
    public double getRegeneration(int level) {
        if (this != REGENERATION) return 0.0;
        return getValueOrDefault(level, 0.0);
    }
    
    private double getValueOrDefault(int level, double defaultValue) {
        RuneConfig config = configs.get(this);
        if (config == null) return defaultValue;
        return config.values.getOrDefault(level, defaultValue);
    }
    
    private static class RuneConfig {
        String displayName;
        int maxLevel;
        Map<Integer, String> descriptions;
        Map<Integer, String> nexoIds;
        Map<Integer, Double> values;
        
        RuneConfig(ConfigurationSection section) {
            this.displayName = section.getString("display_name", "§cUnknown");
            this.maxLevel = section.getInt("max_level", 3);
            this.descriptions = new HashMap<>();
            this.nexoIds = new HashMap<>();
            this.values = new HashMap<>();
            
            ConfigurationSection levelsSection = section.getConfigurationSection("levels");
            if (levelsSection != null) {
                for (String levelKey : levelsSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelKey);
                        ConfigurationSection levelSection = levelsSection.getConfigurationSection(levelKey);
                        
                        if (levelSection != null) {
                            descriptions.put(level, levelSection.getString("description", ""));
                            nexoIds.put(level, levelSection.getString("nexo_id"));
                            
                            // Charger toutes les valeurs possibles
                            if (levelSection.contains("damage_bonus")) {
                                values.put(level, levelSection.getDouble("damage_bonus"));
                            }
                            if (levelSection.contains("speed_bonus")) {
                                values.put(level, levelSection.getDouble("speed_bonus"));
                            }
                            if (levelSection.contains("damage_reduction")) {
                                values.put(level, levelSection.getDouble("damage_reduction"));
                            }
                            if (levelSection.contains("critical_chance")) {
                                values.put(level, levelSection.getDouble("critical_chance"));
                            }
                            if (levelSection.contains("vampirism_percent")) {
                                values.put(level, levelSection.getDouble("vampirism_percent"));
                            }
                            if (levelSection.contains("regeneration")) {
                                values.put(level, levelSection.getDouble("regeneration"));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
            }
        }
    }
}
