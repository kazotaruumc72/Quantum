package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.worldguard.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages tower system - configuration, progress tracking, and integration
 */
public class TowerManager {
    
    private final Quantum plugin;
    private final Map<String, TowerConfig> towers;
    private final Map<UUID, TowerProgress> playerProgress;
    private File progressFile;
    
    public TowerManager(Quantum plugin) {
        this.plugin = plugin;
        this.towers = new HashMap<>();
        this.playerProgress = new HashMap<>();
        
        loadTowers();
        loadProgress();

        this.spawnerManager = new TowerSpawnerManager(plugin, this);
        this.spawnerManager.loadFromConfig(
                YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "towers.yml"))
        );
    }
    public TowerSpawnerManager getSpawnerManager() {
        return spawnerManager;
    }
    /**
     * Load tower configurations from zones.yml
     */
    private void loadTowers() {
        File zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) {
            plugin.getQuantumLogger().warning("zones.yml not found - tower system disabled");
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(zonesFile);
        ConfigurationSection towersSection = config.getConfigurationSection("towers");
        
        if (towersSection == null) {
            plugin.getQuantumLogger().warning("No 'towers' section in zones.yml");
            return;
        }
        
        for (String towerId : towersSection.getKeys(false)) {
            ConfigurationSection towerSection = towersSection.getConfigurationSection(towerId);
            if (towerSection == null) continue;
            
            String name = towerSection.getString("name", towerId);
            String world = towerSection.getString("world", "world");
            int floors = towerSection.getInt("floors", 25);
            List<Integer> bossFloors = towerSection.getIntegerList("boss_floors");
            int finalBossFloor = towerSection.getInt("final_boss_floor", 26);
            
            TowerConfig tower = new TowerConfig(towerId, name, world, floors, bossFloors, finalBossFloor);
            towers.put(towerId, tower);
            
            plugin.getQuantumLogger().success("✓ Loaded tower: " + name + " (" + floors + " floors)");
        }
    }
    
    /**
     * Load player progress from tower_progress.yml
     */
    private void loadProgress() {
        progressFile = new File(plugin.getDataFolder(), "tower_progress.yml");
        
        if (!progressFile.exists()) {
            try {
                progressFile.createNewFile();
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create tower_progress.yml");
                e.printStackTrace();
                return;
            }
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(progressFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        if (playersSection == null) {
            return;
        }
        
        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidStr);
                
                TowerProgress progress = new TowerProgress(uuid);
                
                // Load tower progress
                ConfigurationSection progressSection = playerSection.getConfigurationSection("progress");
                if (progressSection != null) {
                    for (String towerId : progressSection.getKeys(false)) {
                        int floor = progressSection.getInt(towerId);
                        progress.setFloorProgress(towerId, floor);
                    }
                }
                
                playerProgress.put(uuid, progress);
                
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid UUID in tower_progress.yml: " + uuidStr);
            }
        }
        
        plugin.getQuantumLogger().success("✓ Loaded progress for " + playerProgress.size() + " players");
    }
    
    /**
     * Save all player progress
     */
    public void saveProgress() {
        FileConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, TowerProgress> entry : playerProgress.entrySet()) {
            UUID uuid = entry.getKey();
            TowerProgress progress = entry.getValue();
            
            String path = "players." + uuid.toString();
            
            // Save tower progress
            for (Map.Entry<String, Integer> towerEntry : progress.getAllProgress().entrySet()) {
                config.set(path + ".progress." + towerEntry.getKey(), towerEntry.getValue());
            }
        }
        
        try {
            config.save(progressFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save tower_progress.yml");
            e.printStackTrace();
        }
    }
    
    /**
     * Get or create player progress
     * @param uuid Player UUID
     * @return TowerProgress instance
     */
    public TowerProgress getProgress(UUID uuid) {
        return playerProgress.computeIfAbsent(uuid, TowerProgress::new);
    }
    
    /**
     * Get tower configuration
     * @param towerId Tower ID
     * @return TowerConfig or null
     */
    public TowerConfig getTower(String towerId) {
        return towers.get(towerId);
    }
    
    /**
     * Get all towers
     * @return Map of tower ID to config
     */
    public Map<String, TowerConfig> getAllTowers() {
        return new HashMap<>(towers);
    }
    
    /**
     * Get list of all tower IDs
     * @return List of tower IDs
     */
    public List<String> getTowerIds() {
        return new ArrayList<>(towers.keySet());
    }
    
    /**
     * Get tower by zone region name
     * @param regionName WorldGuard region name
     * @return Tower ID or null
     */
    public String getTowerByRegion(String regionName) {
        // Parse region name to extract tower ID
        // Format: towerId_floor_N
        if (regionName.contains("_floor_")) {
            String[] parts = regionName.split("_floor_");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return null;
    }
    
    /**
     * Get floor number from region name
     * @param regionName WorldGuard region name
     * @return Floor number or -1
     */
    public int getFloorByRegion(String regionName) {
        // Format: towerId_floor_N
        if (regionName.contains("_floor_")) {
            String[] parts = regionName.split("_floor_");
            if (parts.length > 1) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }
    
    /**
     * Complete a floor for a player
     * @param player Player
     * @param towerId Tower ID
     * @param floor Floor number
     */
    public void completeFloor(Player player, String towerId, int floor) {
        TowerProgress progress = getProgress(player.getUniqueId());
        progress.setFloorProgress(towerId, floor);
        progress.resetKills();
        
        TowerConfig tower = getTower(towerId);
        if (tower == null) return;
        
        // Si final boss, incrémenter les runs
        if (tower.isFinalBoss(floor)) {
            progress.incrementRuns(towerId);
        }
        
        saveProgress();
        
        // Send completion message
        if (tower.isFinalBoss(floor)) {
            player.sendMessage("§6§l✦ TOUR COMPLÉTÉE ✦");
            player.sendMessage("§7Vous avez terminé: §f" + tower.getName());
            Bukkit.broadcastMessage("§6§l[TOURS] §f" + player.getName() + " §7a complété §f" + tower.getName() + "§7!");
        } else if (tower.isBossFloor(floor)) {
            player.sendMessage("§a§l✓ BOSS VAINCU!");
            player.sendMessage("§7Étage §f" + floor + " §7terminé!");
        } else {
            player.sendMessage("§a§l✓ Étage " + floor + " terminé!");
        }
    }
    
    /**
     * Update player's current location in tower
     * @param player Player
     * @param towerId Tower ID
     * @param floor Floor number
     */
    public void updateCurrentLocation(Player player, String towerId, int floor) {
        TowerProgress progress = getProgress(player.getUniqueId());
        progress.setCurrentTower(towerId);
        progress.setCurrentFloor(floor);
        
        // Démarrer les spawners
        if (spawnerManager != null) {
            spawnerManager.startFloorSpawners(player, towerId, floor);
        }
    }
    
    /**
     * Clear player's current location (left tower)
     * @param player Player
     */
    public void clearCurrentLocation(Player player) {
        TowerProgress progress = getProgress(player.getUniqueId());
        progress.setCurrentTower(null);
        progress.setCurrentFloor(0);
        progress.resetKills();
        
        // Arrêter les spawners
        if (spawnerManager != null) {
            spawnerManager.stopSpawners(player);
        }
    }
    
    /**
     * Add a kill for current floor
     * @param player Player
     * @param mobId Mob ID
     */
    public void addKill(Player player, String mobId) {
        TowerProgress progress = getProgress(player.getUniqueId());
        progress.addKill(mobId);
    }
    
    /**
     * Reset player progress
     * @param uuid Player UUID
     */
    public void resetProgress(UUID uuid) {
        TowerProgress progress = getProgress(uuid);
        progress.reset();
        saveProgress();
    }
    
    /**
     * Reset tower progress for player
     * @param uuid Player UUID
     * @param towerId Tower ID
     */
    public void resetTowerProgress(UUID uuid, String towerId) {
        TowerProgress progress = getProgress(uuid);
        progress.resetTower(towerId);
        saveProgress();
    }
    
    /**
     * Reload tower configurations
     */
    public void reload() {
        towers.clear();
        loadTowers();
    }
    
    /**
     * Get number of loaded towers
     * @return Tower count
     */
    public int getTowerCount() {
        return towers.size();
    }

        /**
     * Get the tower a player is currently in
     * @param player The player
     * @return TowerConfig or null if not in a tower
     */
    public TowerConfig getPlayerTower(Player player) {
        TowerProgress progress = getProgress(player.getUniqueId());
        if (progress != null && progress.getCurrentTower() != null) {
            return getTower(progress.getCurrentTower());
        }
        return null;
    }
}
