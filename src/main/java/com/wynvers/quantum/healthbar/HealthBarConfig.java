package com.wynvers.quantum.healthbar;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration manager for the BetterHealthBar addon
 */
public class HealthBarConfig {

    private final Quantum plugin;
    private YamlConfiguration config;

    // Display settings
    private boolean enabled;
    private int viewDistance;
    private int updateInterval;
    private boolean showOnPlayers;
    private boolean showOnMobs;

    // Model settings
    private boolean modelEnabled;
    private String modelId;
    private double modelScale;
    private double yOffset;
    private double animationSpeed;

    // Appearance settings
    private boolean showName;
    private String nameFormat;
    private boolean showHealthText;
    private String healthFormat;
    private boolean colorGradientEnabled;
    private int[] fullHealthColor;
    private int[] mediumHealthColor;
    private int[] lowHealthColor;

    // Entity-specific settings
    private boolean playersEnabled;
    private boolean showSelf;
    private boolean showOthers;
    private String playerNameFormat;

    private boolean mobsEnabled;
    private boolean vanillaMobsEnabled;
    private boolean mythicMobsEnabled;
    private String mythicMobNameFormat;

    // Boss settings
    private boolean bossesEnabled;
    private boolean bossUseCustomModel;
    private String bossModelId;
    private double bossScale;
    private double bossYOffset;
    private boolean bossShowToAll;

    // Integration settings
    private boolean towerMobsEnabled;
    private String towerMobNameFormat;
    private boolean showFloorLevel;
    private boolean showArmorStats;
    private boolean placeholderAPIEnabled;
    private List<String> customPlaceholders;

    // Performance settings
    private int maxRendersPerTick;
    private boolean cacheModels;
    private boolean updateOnlyVisible;

    public HealthBarConfig(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "healthbar.yml");
        if (!configFile.exists()) {
            plugin.saveResource("healthbar.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load display settings
        enabled = config.getBoolean("enabled", true);
        ConfigurationSection displaySection = config.getConfigurationSection("display");
        if (displaySection != null) {
            viewDistance = displaySection.getInt("view_distance", 32);
            updateInterval = displaySection.getInt("update_interval", 5);
            showOnPlayers = displaySection.getBoolean("show_on_players", true);
            showOnMobs = displaySection.getBoolean("show_on_mobs", true);
        }

        // Load model settings
        ConfigurationSection modelSection = config.getConfigurationSection("model");
        if (modelSection != null) {
            modelEnabled = modelSection.getBoolean("enabled", true);
            modelId = modelSection.getString("model_id", "healthbar_model");
            modelScale = modelSection.getDouble("scale", 1.0);
            yOffset = modelSection.getDouble("y_offset", 0.5);
            animationSpeed = modelSection.getDouble("animation_speed", 1.0);
        }

        // Load appearance settings
        ConfigurationSection appearanceSection = config.getConfigurationSection("appearance");
        if (appearanceSection != null) {
            showName = appearanceSection.getBoolean("show_name", true);
            nameFormat = appearanceSection.getString("name_format", "&f%entity_name%");
            showHealthText = appearanceSection.getBoolean("show_health_text", true);
            healthFormat = appearanceSection.getString("health_format", "&c{current}&7/&a{max}");

            ConfigurationSection gradientSection = appearanceSection.getConfigurationSection("color_gradient");
            if (gradientSection != null) {
                colorGradientEnabled = gradientSection.getBoolean("enabled", true);
                fullHealthColor = listToIntArray(gradientSection.getIntegerList("full_health"));
                mediumHealthColor = listToIntArray(gradientSection.getIntegerList("medium_health"));
                lowHealthColor = listToIntArray(gradientSection.getIntegerList("low_health"));
            }
        }

        // Load entity-specific settings
        ConfigurationSection entitiesSection = config.getConfigurationSection("entities");
        if (entitiesSection != null) {
            ConfigurationSection playersSection = entitiesSection.getConfigurationSection("players");
            if (playersSection != null) {
                playersEnabled = playersSection.getBoolean("enabled", true);
                showSelf = playersSection.getBoolean("show_self", false);
                showOthers = playersSection.getBoolean("show_others", true);
                playerNameFormat = playersSection.getString("name_format", "&b%player_name%");
            }

            ConfigurationSection mobsSection = entitiesSection.getConfigurationSection("mobs");
            if (mobsSection != null) {
                mobsEnabled = mobsSection.getBoolean("enabled", true);
                ConfigurationSection vanillaSection = mobsSection.getConfigurationSection("vanilla");
                if (vanillaSection != null) {
                    vanillaMobsEnabled = vanillaSection.getBoolean("enabled", true);
                }
                ConfigurationSection mythicSection = mobsSection.getConfigurationSection("mythic_mobs");
                if (mythicSection != null) {
                    mythicMobsEnabled = mythicSection.getBoolean("enabled", true);
                    mythicMobNameFormat = mythicSection.getString("name_format", "&6%mob_name%");
                }
            }
        }

        // Load boss settings
        ConfigurationSection bossesSection = config.getConfigurationSection("bosses");
        if (bossesSection != null) {
            bossesEnabled = bossesSection.getBoolean("enabled", true);
            bossUseCustomModel = bossesSection.getBoolean("use_custom_model", true);
            bossModelId = bossesSection.getString("model_id", "boss_healthbar_model");
            bossScale = bossesSection.getDouble("scale", 1.5);
            bossYOffset = bossesSection.getDouble("y_offset", 1.0);
            bossShowToAll = bossesSection.getBoolean("show_to_all", true);
        }

        // Load integration settings
        ConfigurationSection integrationSection = config.getConfigurationSection("integration");
        if (integrationSection != null) {
            ConfigurationSection towerSection = integrationSection.getConfigurationSection("tower_mobs");
            if (towerSection != null) {
                towerMobsEnabled = towerSection.getBoolean("enabled", true);
                towerMobNameFormat = towerSection.getString("name_format", "&c[Tower] &6%mob_name%");
                showFloorLevel = towerSection.getBoolean("show_floor_level", true);
            }
            showArmorStats = integrationSection.getBoolean("show_armor_stats", false);

            ConfigurationSection papiSection = integrationSection.getConfigurationSection("placeholderapi");
            if (papiSection != null) {
                placeholderAPIEnabled = papiSection.getBoolean("enabled", true);
                customPlaceholders = papiSection.getStringList("placeholders");
            }
        }

        // Load performance settings
        ConfigurationSection performanceSection = config.getConfigurationSection("performance");
        if (performanceSection != null) {
            maxRendersPerTick = performanceSection.getInt("max_renders_per_tick", 50);
            cacheModels = performanceSection.getBoolean("cache_models", true);
            updateOnlyVisible = performanceSection.getBoolean("update_only_visible", true);
        }

        plugin.getQuantumLogger().success("✓ HealthBar configuration loaded!");
    }

    public void reload() {
        loadConfig();
    }

    private int[] listToIntArray(List<Integer> list) {
        if (list == null || list.size() != 3) {
            return new int[]{255, 255, 255};
        }
        return new int[]{list.get(0), list.get(1), list.get(2)};
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public int getViewDistance() { return viewDistance; }
    public int getUpdateInterval() { return updateInterval; }
    public boolean isShowOnPlayers() { return showOnPlayers; }
    public boolean isShowOnMobs() { return showOnMobs; }

    public boolean isModelEnabled() { return modelEnabled; }
    public String getModelId() { return modelId; }
    public double getModelScale() { return modelScale; }
    public double getYOffset() { return yOffset; }
    public double getAnimationSpeed() { return animationSpeed; }

    public boolean isShowName() { return showName; }
    public String getNameFormat() { return nameFormat; }
    public boolean isShowHealthText() { return showHealthText; }
    public String getHealthFormat() { return healthFormat; }
    public boolean isColorGradientEnabled() { return colorGradientEnabled; }
    public int[] getFullHealthColor() { return fullHealthColor; }
    public int[] getMediumHealthColor() { return mediumHealthColor; }
    public int[] getLowHealthColor() { return lowHealthColor; }

    public boolean isPlayersEnabled() { return playersEnabled; }
    public boolean isShowSelf() { return showSelf; }
    public boolean isShowOthers() { return showOthers; }
    public String getPlayerNameFormat() { return playerNameFormat; }

    public boolean isMobsEnabled() { return mobsEnabled; }
    public boolean isVanillaMobsEnabled() { return vanillaMobsEnabled; }
    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }
    public String getMythicMobNameFormat() { return mythicMobNameFormat; }

    public boolean isBossesEnabled() { return bossesEnabled; }
    public boolean isBossUseCustomModel() { return bossUseCustomModel; }
    public String getBossModelId() { return bossModelId; }
    public double getBossScale() { return bossScale; }
    public double getBossYOffset() { return bossYOffset; }
    public boolean isBossShowToAll() { return bossShowToAll; }

    public boolean isTowerMobsEnabled() { return towerMobsEnabled; }
    public String getTowerMobNameFormat() { return towerMobNameFormat; }
    public boolean isShowFloorLevel() { return showFloorLevel; }
    public boolean isShowArmorStats() { return showArmorStats; }
    public boolean isPlaceholderAPIEnabled() { return placeholderAPIEnabled; }
    public List<String> getCustomPlaceholders() { return customPlaceholders; }

    public int getMaxRendersPerTick() { return maxRendersPerTick; }
    public boolean isCacheModels() { return cacheModels; }
    public boolean isUpdateOnlyVisible() { return updateOnlyVisible; }
}
