package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class TowerSpawnerManager {
    
    private final Quantum plugin;
    private final TowerManager towerManager;
    private final Map<String, TowerSpawnerConfig> spawnerConfigs = new HashMap<>();
    private final Map<UUID, List<ActiveSpawner>> activeByPlayer = new HashMap<>();

    public TowerSpawnerManager(Quantum plugin, TowerManager towerManager) {
        this.plugin = plugin;
        this.towerManager = towerManager;
    }

    public void loadFromConfig(FileConfiguration config) {
        spawnerConfigs.clear();
        
        ConfigurationSection towersSec = config.getConfigurationSection("towers");
        if (towersSec == null) return;

        for (String towerId : towersSec.getKeys(false)) {
            ConfigurationSection towerSec = towersSec.getConfigurationSection(towerId + ".floors");
            if (towerSec == null) continue;

            for (String floorKey : towerSec.getKeys(false)) {
                ConfigurationSection spawnersSec = towerSec.getConfigurationSection(floorKey + ".spawners");
                if (spawnersSec == null) continue;

                for (String spawnerId : spawnersSec.getKeys(false)) {
                    ConfigurationSection spawnerSec = spawnersSec.getConfigurationSection(spawnerId);
                    if (spawnerSec == null) continue;

                    String fullId = towerId + ":" + floorKey + ":" + spawnerId;
                    TowerSpawnerConfig cfg = new TowerSpawnerConfig(fullId, spawnerSec);
                    spawnerConfigs.put(fullId, cfg);
                }
            }
        }
    }

    public TowerSpawnerConfig getSpawnerConfig(String fullId) {
        return spawnerConfigs.get(fullId);
    }

    public void startFloorSpawners(Player player, String towerId, int floor) {
        String prefix = towerId + ":" + floor + ":";
        List<ActiveSpawner> list = new ArrayList<>();

        for (Map.Entry<String, TowerSpawnerConfig> entry : spawnerConfigs.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) continue;

            ActiveSpawner active = new ActiveSpawner(plugin, entry.getValue(), player, towerId, towerManager);
            active.start();
            list.add(active);
        }

        stopSpawners(player);
        activeByPlayer.put(player.getUniqueId(), list);
    }

    public void stopSpawners(Player player) {
        List<ActiveSpawner> list = activeByPlayer.remove(player.getUniqueId());
        if (list == null) return;
        for (ActiveSpawner active : list) {
            active.stop();
        }
    }
    
    /**
     * Arrête tous les spawners d'un étage spécifique
     */
    public void stopFloorSpawners(Player player, String towerId, int floor) {
        List<ActiveSpawner> list = activeByPlayer.get(player.getUniqueId());
        if (list == null) return;
        
        String prefix = towerId + ":" + floor + ":";
        
        // Arrêter les spawners de cet étage
        list.removeIf(spawner -> {
            if (spawner.getConfig().getFullId().startsWith(prefix)) {
                spawner.stop();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Vérifie si tous les spawners d'un étage sont vides (aucun mob vivant)
     */
    public boolean areAllSpawnersEmpty(Player player, String towerId, int floor) {
        List<ActiveSpawner> list = activeByPlayer.get(player.getUniqueId());
        if (list == null || list.isEmpty()) {
            return true; // Aucun spawner actif
        }
        
        String prefix = towerId + ":" + floor + ":";
        
        // Vérifier tous les spawners de cet étage
        for (ActiveSpawner spawner : list) {
            if (spawner.getConfig().getFullId().startsWith(prefix)) {
                // Si le spawner a encore des mobs vivants, l'étage n'est pas complété
                if (spawner.getAliveMobCount() > 0) {
                    return false;
                }
            }
        }
        
        return true; // Tous les spawners de l'étage sont vides
    }
    
    /**
     * Obtient la liste des spawners actifs pour un joueur
     */
    public List<ActiveSpawner> getActiveSpawners(Player player) {
        return activeByPlayer.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }
    
    /**
     * Obtient le nombre total de mobs vivants pour un joueur
     */
    public int getTotalAliveMobs(Player player) {
        List<ActiveSpawner> list = activeByPlayer.get(player.getUniqueId());
        if (list == null) return 0;
        
        return list.stream()
            .mapToInt(ActiveSpawner::getAliveMobCount)
            .sum();
    }
}
