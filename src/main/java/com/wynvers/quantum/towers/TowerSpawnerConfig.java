package com.wynvers.quantum.towers;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class TowerSpawnerConfig {

    // ... champs existants ...
    private SpawnRegion spawnRegion;

    public void loadFromConfig(ConfigurationSection section) {
        // config existante...

        ConfigurationSection regionSec = section.getConfigurationSection("region");
        if (regionSec != null) {
            String world = regionSec.getString("world");
            double x1 = regionSec.getDouble("x1");
            double y1 = regionSec.getDouble("y1");
            double z1 = regionSec.getDouble("z1");
            double x2 = regionSec.getDouble("x2");
            double y2 = regionSec.getDouble("y2");
            double z2 = regionSec.getDouble("z2");
            if (world != null) {
                this.spawnRegion = new SpawnRegion(world, x1, y1, z1, x2, y2, z2);
            }
        }
    }

    public SpawnRegion getSpawnRegion() {
        return spawnRegion;
    }
}
