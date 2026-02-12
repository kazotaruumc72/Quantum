package com.wynvers.quantum.worldguard.gui;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manager for WorldGuard zone configurations
 * Handles loading, saving, and managing zone settings
 */
public class ZoneGUIManager {
    
    private final Quantum plugin;
    private final Map<String, ZoneConfig> zoneConfigs;
    private File configFile;
    private YamlConfiguration config;
    
    public ZoneGUIManager(Quantum plugin) {
        this.plugin = plugin;
        this.zoneConfigs = new HashMap<>();
        loadConfig();
    }
    
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "zone_configs.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("zone_configs.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        loadZoneConfigs();
    }
    
    private void loadZoneConfigs() {
        zoneConfigs.clear();
        
        ConfigurationSection zonesSection = config.getConfigurationSection("zones");
        if (zonesSection == null) {
            return;
        }
        
        for (String regionName : zonesSection.getKeys(false)) {
            ConfigurationSection zoneSection = zonesSection.getConfigurationSection(regionName);
            if (zoneSection == null) continue;
            
            ZoneConfig zoneConfig = new ZoneConfig(regionName);
            zoneConfig.setPvpEnabled(zoneSection.getBoolean("pvp", false));
            zoneConfig.setMobSpawning(zoneSection.getBoolean("mob_spawning", true));
            
            // Load allowed mobs
            List<String> allowedMobs = zoneSection.getStringList("allowed_mobs");
            for (String mobName : allowedMobs) {
                try {
                    EntityType type = EntityType.valueOf(mobName.toUpperCase());
                    zoneConfig.addAllowedMob(type);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid mob type: " + mobName);
                }
            }
            
            // Load denied mobs
            List<String> deniedMobs = zoneSection.getStringList("denied_mobs");
            for (String mobName : deniedMobs) {
                try {
                    EntityType type = EntityType.valueOf(mobName.toUpperCase());
                    zoneConfig.addDeniedMob(type);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid mob type: " + mobName);
                }
            }
            
            // Load custom flags
            ConfigurationSection flagsSection = zoneSection.getConfigurationSection("flags");
            if (flagsSection != null) {
                for (String flagName : flagsSection.getKeys(false)) {
                    zoneConfig.setFlag(flagName, flagsSection.getBoolean(flagName));
                }
            }
            
            zoneConfigs.put(regionName, zoneConfig);
        }
        
        plugin.getLogger().info("✓ Loaded " + zoneConfigs.size() + " zone configurations");
    }
    
    /**
     * Get zone configuration by region name
     */
    public ZoneConfig getZoneConfig(String regionName) {
        return zoneConfigs.computeIfAbsent(regionName, ZoneConfig::new);
    }
    
    /**
     * Save zone configuration
     */
    public void saveZoneConfig(ZoneConfig zoneConfig) {
        zoneConfigs.put(zoneConfig.getRegionName(), zoneConfig);
        
        String path = "zones." + zoneConfig.getRegionName();
        config.set(path + ".pvp", zoneConfig.isPvpEnabled());
        config.set(path + ".mob_spawning", zoneConfig.isMobSpawning());
        
        // Save allowed mobs
        List<String> allowedMobs = new ArrayList<>();
        for (EntityType type : zoneConfig.getAllowedMobs()) {
            allowedMobs.add(type.name());
        }
        config.set(path + ".allowed_mobs", allowedMobs);
        
        // Save denied mobs
        List<String> deniedMobs = new ArrayList<>();
        for (EntityType type : zoneConfig.getDeniedMobs()) {
            deniedMobs.add(type.name());
        }
        config.set(path + ".denied_mobs", deniedMobs);
        
        // Save flags
        for (Map.Entry<String, Boolean> entry : zoneConfig.getFlags().entrySet()) {
            config.set(path + ".flags." + entry.getKey(), entry.getValue());
        }
        
        saveConfigFile();
    }
    
    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save zone configs: " + e.getMessage());
        }
    }
    
    /**
     * Get all zone configurations
     */
    public Collection<ZoneConfig> getAllZoneConfigs() {
        return new ArrayList<>(zoneConfigs.values());
    }
    
    /**
     * Reload configurations from file
     */
    public void reload() {
        loadConfig();
    }
}
