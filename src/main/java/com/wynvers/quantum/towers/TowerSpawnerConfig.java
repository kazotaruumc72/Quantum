package com.wynvers.quantum.towers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration d'un spawner de mob dans une tour.
 */
public class TowerSpawnerConfig {

    private String fullId;  // ex: "tower_water:1:slime_pack_1"
    private String mobId;   // ex: "slime_basic"
    private EntityType type;
    private double baseHealth;
    private int damage;
    private int baseAmount;
    private int intervalSeconds;
    private int maxAlive;
    
    // Scaling
    private int healthPerRunPercent;
    private int amountPerRun;
    private int maxExtraAmount;
    
    // Skills
    private List<MobSkillConfig> skills = new ArrayList<>();
    
    // Animations
    private Map<String, String> animations = new HashMap<>();
    
    // Région de spawn (optionnelle)
    private SpawnRegion spawnRegion;

    public TowerSpawnerConfig(String fullId, ConfigurationSection section) {
        this.fullId = fullId;
        loadFromConfig(section);
    }

    private void loadFromConfig(ConfigurationSection section) {
        this.type = parseEntityType(section.getString("type", "ZOMBIE"));
        this.mobId = section.getString("model", "default");
        this.baseHealth = section.getDouble("base_health", 20.0);
        this.damage = section.getInt("damage", 1);
        this.baseAmount = section.getInt("amount", 1);
        this.intervalSeconds = section.getInt("interval", 60);
        this.maxAlive = section.getInt("max_alive", 10);
        
        // Scaling
        ConfigurationSection scalingSec = section.getConfigurationSection("scaling");
        if (scalingSec != null) {
            this.healthPerRunPercent = scalingSec.getInt("health_per_run_percent", 5);
            this.amountPerRun = scalingSec.getInt("amount_per_run", 1);
            this.maxExtraAmount = scalingSec.getInt("max_extra_amount", 5);
        }
        
        // Skills
        if (section.contains("skills")) {
            List<?> rawSkills = section.getList("skills");
            if (rawSkills != null) {
                for (Object obj : rawSkills) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        skills.add(new MobSkillConfig(map));
                    }
                }
            }
        }
        
        // Animations
        ConfigurationSection animSec = section.getConfigurationSection("animations");
        if (animSec != null) {
            for (String key : animSec.getKeys(false)) {
                animations.put(key, animSec.getString(key));
            }
        }
        
        // Région de spawn
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

    private EntityType parseEntityType(String str) {
        try {
            return EntityType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EntityType.ZOMBIE;
        }
    }

    // ============ GETTERS ============

    public String getFullId() {
        return fullId;
    }
    
    public String getId() {
        return fullId;
    }

    public String getMobId() {
        return mobId;
    }

    public EntityType getType() {
        return type;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public int getDamageHalfHearts() {
        return damage;
    }

    public int getBaseAmount() {
        return baseAmount;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getMaxAlive() {
        return maxAlive;
    }

    public List<MobSkillConfig> getSkills() {
        return skills;
    }

    public Map<String, String> getAnimations() {
        return animations;
    }

    public SpawnRegion getSpawnRegion() {
        return spawnRegion;
    }

    /**
     * Calcule la santé du mob en fonction du nombre de runs.
     */
    public double getHealthForRuns(int runs) {
        if (runs <= 0) return baseHealth;
        double multiplier = 1.0 + (runs * healthPerRunPercent / 100.0);
        return baseHealth * multiplier;
    }

    /**
     * Calcule le nombre de mobs à spawner en fonction du nombre de runs.
     */
    public int getAmountForRuns(int runs) {
        if (runs <= 0) return baseAmount;
        int extra = Math.min(runs * amountPerRun, maxExtraAmount);
        return baseAmount + extra;
    }
}
