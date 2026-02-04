package com.wynvers.quantum.armor;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

/**
 * Types de runes disponibles pour l'armure de donjon
 * Chaque rune a un niveau maximum et donne des bonus chargés depuis dungeon.yml
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public enum RuneType {
    
    FORCE("Force", 3),
    SPEED("Vitesse", 3),
    RESISTANCE("Résistance", 3),
    CRITICAL("Critique", 3),
    REGENERATION("Régénération", 3),
    VAMPIRISM("Vampirisme", 3),
    THORNS("Épines", 3),
    WISDOM("Sagesse", 3),
    LUCK("Chance", 3);
    
    private final String display;
    private final int maxLevel;
    private static JavaPlugin plugin;
    private static YamlConfiguration dungeonConfig;
    private static Map<RuneType, ConfigurationSection> runeConfigs = new HashMap<>();
    
    RuneType(String display, int maxLevel) {
        this.display = display;
        this.maxLevel = maxLevel;
    }
    
    /**
     * Initialise les configs depuis dungeon.yml
     */
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        loadConfig();
    }
    
    /**
     * Charge la config dungeon.yml
     */
    private static void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon.yml", false);
            }
            dungeonConfig = YamlConfiguration.loadConfiguration(configFile);
            
            // Charger les configs de chaque rune
            ConfigurationSection runesSection = dungeonConfig.getConfigurationSection("runes");
            if (runesSection != null) {
                for (RuneType rune : values()) {
                    ConfigurationSection runeConfig = runesSection.getConfigurationSection(rune.name());
                    if (runeConfig != null) {
                        runeConfigs.put(rune, runeConfig);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du chargement de dungeon.yml: " + e.getMessage());
        }
    }
    
    /**
     * Nom d'affichage de la rune
     */
    public String getDisplay() {
        ConfigurationSection config = runeConfigs.get(this);
        if (config != null && config.contains("display")) {
            return config.getString("display");
        }
        return display;
    }
    
    /**
     * Description de la rune
     */
    public String getDescription() {
        ConfigurationSection config = runeConfigs.get(this);
        if (config != null && config.contains("description")) {
            return config.getString("description");
        }
        return "";
    }
    
    /**
     * Niveau maximum de la rune
     */
    public int getMaxLevel() {
        ConfigurationSection config = runeConfigs.get(this);
        if (config != null && config.contains("max_level")) {
            return config.getInt("max_level");
        }
        return maxLevel;
    }
    
    /**
     * Lore dynamique selon le niveau
     */
    public List<String> getLore(int level) {
        ConfigurationSection config = runeConfigs.get(this);
        if (config == null) return new ArrayList<>();
        
        ConfigurationSection loreSection = config.getConfigurationSection("lore");
        if (loreSection == null) return new ArrayList<>();
        
        ConfigurationSection levelSection = loreSection.getConfigurationSection(String.valueOf(level));
        if (levelSection == null) {
            return loreSection.getStringList(String.valueOf(level));
        }
        
        return loreSection.getStringList(String.valueOf(level));
    }
    
    /**
     * ID Nexo de la rune pour un niveau donné
     */
    public String getNexoId(int level) {
        ConfigurationSection config = runeConfigs.get(this);
        if (config == null) return null;
        
        ConfigurationSection nexoIds = config.getConfigurationSection("nexo_ids");
        if (nexoIds == null) return null;
        
        return nexoIds.getString(String.valueOf(level));
    }
    
    /**
     * Récupère les bonus pour un niveau spécifique
     */
    public Map<String, Object> getBonuses(int level) {
        ConfigurationSection config = runeConfigs.get(this);
        if (config == null) return new HashMap<>();
        
        ConfigurationSection bonusesSection = config.getConfigurationSection("bonuses");
        if (bonusesSection == null) return new HashMap<>();
        
        ConfigurationSection levelBonuses = bonusesSection.getConfigurationSection(String.valueOf(level));
        if (levelBonuses == null) return new HashMap<>();
        
        return levelBonuses.getValues(false);
    }
    
    /**
     * Bonus de dégâts (FORCE rune)
     */
    public double getDamageBonus(int level) {
        if (this != FORCE) return 1.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("damage_multiplier")) {
            return ((Number) bonuses.get("damage_multiplier")).doubleValue();
        }
        return 1.0;
    }
    
    /**
     * Bonus de vitesse (SPEED rune)
     */
    public double getSpeedBonus(int level) {
        if (this != SPEED) return 1.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("speed_multiplier")) {
            return ((Number) bonuses.get("speed_multiplier")).doubleValue();
        }
        return 1.0;
    }
    
    /**
     * Réduction de dégâts (RESISTANCE rune)
     */
    public double getDamageReduction(int level) {
        if (this != RESISTANCE) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("damage_reduction")) {
            return ((Number) bonuses.get("damage_reduction")).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Chance de critique (CRITICAL rune)
     */
    public double getCriticalChance(int level) {
        if (this != CRITICAL) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("critical_chance")) {
            return ((Number) bonuses.get("critical_chance")).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Régénération par seconde (REGENERATION rune)
     */
    public double getRegeneration(int level) {
        if (this != REGENERATION) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("regeneration_per_second")) {
            return ((Number) bonuses.get("regeneration_per_second")).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Pourcentage de vampirisme (VAMPIRISM rune)
     */
    public double getVampirismPercent(int level) {
        if (this != VAMPIRISM) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("vampirism_percent")) {
            return ((Number) bonuses.get("vampirism_percent")).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Pourcentage de retour de dégâts (THORNS rune)
     */
    public double getThornsPercent(int level) {
        if (this != THORNS) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("thorns_percent")) {
            return ((Number) bonuses.get("thorns_percent")).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Multiplicateur d'XP (WISDOM rune)
     */
    public double getXpMultiplier(int level) {
        if (this != WISDOM) return 1.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("xp_multiplier")) {
            return ((Number) bonuses.get("xp_multiplier")).doubleValue();
        }
        return 1.0;
    }
    
    /**
     * Chance de loot rare (LUCK rune)
     */
    public double getRareLootChance(int level) {
        if (this != LUCK) return 0.0;
        
        Map<String, Object> bonuses = getBonuses(level);
        if (bonuses.containsKey("rare_loot_chance")) {
            return ((Number) bonuses.get("rare_loot_chance")).doubleValue();
        }
        return 0.0;
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
