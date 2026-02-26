package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.events.TowerCompleteEvent;
import com.wynvers.quantum.towers.events.TowerFloorCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages tower system - configuration, progress tracking, and integration
 */
public class TowerManager {
    
    private final Quantum plugin;
    private final Map<String, TowerConfig> towers;
    private final Map<UUID, TowerProgress> playerProgress;
    private File progressFile;
    private TowerRewardManager rewardManager;
    
    public TowerManager(Quantum plugin) {
        this.plugin = plugin;
        this.towers = new HashMap<>();
        this.playerProgress = new HashMap<>();
        this.rewardManager = new TowerRewardManager(plugin);

        loadTowers();
        loadProgress();
    }

    /**
     * Load tower configurations from towers.yml
     */
    private void loadTowers() {
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) {
            plugin.getQuantumLogger().warning("towers.yml not found - tower system disabled");
            return;
        }
    
        FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        ConfigurationSection towersSection = config.getConfigurationSection("towers");
    
        if (towersSection == null) {
            plugin.getQuantumLogger().warning("No 'towers' section in towers.yml");
            return;
        }
    
        for (String towerId : towersSection.getKeys(false)) {
            ConfigurationSection towerSection = towersSection.getConfigurationSection(towerId);
            if (towerSection == null) continue;
    
            String name = towerSection.getString("name", towerId);
            String world = towerSection.getString("world", "world");
            
            int minLevel = towerSection.getInt("min_level", 1);
            int maxLevel = towerSection.getInt("max_level", 1000);
            
            // Load floors section
            ConfigurationSection floorsSection = towerSection.getConfigurationSection("floors");
            if (floorsSection == null) {
                plugin.getQuantumLogger().warning("No 'floors' section for tower: " + towerId);
                continue;
            }
            
            int totalFloors = floorsSection.getKeys(false).size();
            List<Integer> bossFloors = towerSection.getIntegerList("boss_floors");
            int finalBossFloor = towerSection.getInt("final_boss_floor", totalFloors);
    
            TowerConfig tower = new TowerConfig(
                    towerId, name, world, totalFloors, bossFloors, finalBossFloor,
                    minLevel, maxLevel
            );
            
            // Load display-condition (for TAB plugin integration)
            String displayCondition = towerSection.getString("display-condition");
            if (displayCondition != null && !displayCondition.isEmpty()) {
                tower.setDisplayCondition(displayCondition);
            }
            
            // Load floor regions
            for (String floorKey : floorsSection.getKeys(false)) {
                try {
                    int floorNum = Integer.parseInt(floorKey);
                    ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
                    if (floorSection != null) {
                        String regionName = floorSection.getString("worldguard_region");
                        if (regionName != null && !regionName.isEmpty()) {
                            tower.setFloorRegion(floorNum, regionName);
                            plugin.getQuantumLogger().info("  Floor " + floorNum + " -> region: " + regionName);
                        }
                        // mob_kills_required: list format (new) or plain int (legacy)
                        List<?> mobKillsList = floorSection.getList("mob_kills_required");
                        if (mobKillsList != null && !mobKillsList.isEmpty()) {
                            // New list format: ['mm:MobId:5', 'zombie:3', ...]
                            List<FloorMobRequirement> reqs = new ArrayList<>();
                            for (Object entry : mobKillsList) {
                                FloorMobRequirement req = FloorMobRequirement.parse(String.valueOf(entry));
                                if (req != null) {
                                    reqs.add(req);
                                } else {
                                    plugin.getQuantumLogger().warning(
                                            "  Invalid mob_kills_required entry '" + entry + "' in tower " + towerId + " floor " + floorNum);
                                }
                            }
                            if (!reqs.isEmpty()) {
                                tower.setFloorMobRequirements(floorNum, reqs);
                                plugin.getQuantumLogger().info("  Floor " + floorNum + " -> " + reqs.size() + " mob kill requirement(s)");
                            }
                        } else {
                            // Legacy plain-int format (kept for backward compatibility)
                            int legacy = floorSection.getInt("mob_kills_required", 0);
                            if (legacy > 0) {
                                plugin.getQuantumLogger().warning(
                                        "  Floor " + floorNum + " uses legacy mob_kills_required integer (" + legacy + "). "
                                        + "Please migrate to the list format: [\"mm:MobId:" + legacy + "\"] or [\"zombie:" + legacy + "\"]");
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    plugin.getQuantumLogger().warning("Invalid floor number: " + floorKey + " in tower " + towerId);
                }
            }
            
            towers.put(towerId, tower);
    
            plugin.getQuantumLogger().success("✓ Loaded tower: " + name + " (" + totalFloors + " floors)");
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
     * Get tower by region name
     * @param regionName Region name
     * @return Tower ID or null
     */
    public String getTowerByRegion(String regionName) {
        for (Map.Entry<String, TowerConfig> entry : towers.entrySet()) {
            TowerConfig tower = entry.getValue();
            
            // Vérifier d'abord si c'est la région principale de la tour
            if (regionName.equals(tower.getWorldGuardRegion())) {
                return entry.getKey();
            }
            
            // Sinon chercher dans les floors
            if (tower.getFloorByRegion(regionName) != -1) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Get floor number from region name
     * @param regionName Region name
     * @return Floor number or -1
     */
    public int getFloorByRegion(String regionName) {
        for (TowerConfig tower : towers.values()) {
            // Si c'est la région principale, retourner l'étage 0 (entrée)
            if (regionName.equals(tower.getWorldGuardRegion())) {
                return 0;  // ou 1 selon ta logique
            }
            
            int floor = tower.getFloorByRegion(regionName);
            if (floor != -1) {
                return floor;
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
        TowerConfig tower = getTower(towerId);
        if (tower == null) return;

        // Fire cancellable event – external plugins may veto the completion
        TowerFloorCompleteEvent floorEvent = new TowerFloorCompleteEvent(player, tower, floor);
        Bukkit.getPluginManager().callEvent(floorEvent);
        if (floorEvent.isCancelled()) return;

        TowerProgress progress = getProgress(player.getUniqueId());
        progress.setFloorProgress(towerId, floor);

        // Distribute rewards for this floor
        rewardManager.giveFloorRewards(player, towerId, floor);

        // Si final boss, incrementer les runs et broadcast
        if (tower.isFinalBoss(floor)) {
            progress.incrementRuns(towerId);
            saveProgress();

            int runs = progress.getRuns(towerId);
            TowerCompleteEvent completeEvent = new TowerCompleteEvent(player, tower, runs);
            Bukkit.getPluginManager().callEvent(completeEvent);

            player.sendMessage("§6§l✦ TOUR COMPLETEE ✦");
            player.sendMessage("§7Vous avez termine: §f" + tower.getName());
            Bukkit.broadcastMessage("§6§l[TOURS] §f" + player.getName() + " §7a complete §f" + tower.getName() + "§7!");
        } else {
            saveProgress();
            if (tower.isBossFloor(floor)) {
                player.sendMessage("§a§l✓ BOSS VAINCU!");
                player.sendMessage("§7Etage §f" + floor + " §7termine!");
            } else {
                player.sendMessage("§a§l✓ Etage " + floor + " termine!");
            }
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
        
        plugin.getQuantumLogger().info("Player " + player.getName() + " entered " + towerId + " floor " + floor);
    }
    
    /**
     * Clear player's current location (left tower)
     * @param player Player
     */
    public void clearCurrentLocation(Player player) {
        TowerProgress progress = getProgress(player.getUniqueId());
        progress.setCurrentTower(null);
        progress.setCurrentFloor(0);
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
     * Reload tower configurations and spawners
     */
    public void reload() {
        // Clear old data
        towers.clear();
        
        // Reload tower configs
        loadTowers();
        
        plugin.getQuantumLogger().success("Towers reloaded from towers.yml");
    }
    
    /**
     * Get number of loaded towers
     * @return Tower count
     */
    public int getTowerCount() {
        return towers.size();
    }

    /**
     * Get the reward manager
     * @return TowerRewardManager instance
     */
    public TowerRewardManager getRewardManager() {
        return rewardManager;
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

    /**
     * Get the current floor a player is on
     * @param uuid Player UUID
     * @return Floor number (0 if not in a tower)
     */
    public int getPlayerFloor(UUID uuid) {
        TowerProgress progress = getProgress(uuid);
        if (progress != null) {
            return progress.getCurrentFloor();
        }
        return 0;
    }
}
