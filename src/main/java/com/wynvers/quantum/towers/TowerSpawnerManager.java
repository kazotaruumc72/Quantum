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
}
