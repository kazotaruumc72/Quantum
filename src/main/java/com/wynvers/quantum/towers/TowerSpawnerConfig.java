package com.wynvers.quantum.towers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.*;

public class TowerSpawnerConfig {
    
    private final String id;
    private final EntityType type;
    private final String model;
    private final double baseHealth;
    private final int damageHalfHearts;
    private final int amount;
    private final int intervalSeconds;
    private final int maxAlive;

    private final double healthPerRunPercent;
    private final int amountPerRun;
    private final int maxExtraAmount;

    private final List<Map<String, Object>> skills;
    private final Map<String, String> animations;

    public TowerSpawnerConfig(String id, ConfigurationSection section) {
        this.id = id;
        this.type = EntityType.valueOf(section.getString("type", "ZOMBIE").toUpperCase());
        this.model = section.getString("model", "");
        this.baseHealth = section.getDouble("base_health", 20.0);
        this.damageHalfHearts = section.getInt("damage", 4);
        this.amount = section.getInt("amount", 1);
        this.intervalSeconds = section.getInt("interval", 60);
        this.maxAlive = section.getInt("max_alive", 5);

        ConfigurationSection scaling = section.getConfigurationSection("scaling");
        if (scaling != null) {
            this.healthPerRunPercent = scaling.getDouble("health_per_run_percent", 0.0);
            this.amountPerRun = scaling.getInt("amount_per_run", 0);
            this.maxExtraAmount = scaling.getInt("max_extra_amount", 0);
        } else {
            this.healthPerRunPercent = 0.0;
            this.amountPerRun = 0;
            this.maxExtraAmount = 0;
        }

        this.skills = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("skills")) {
            Map<String, Object> copy = new HashMap<>();
            map.forEach((k, v) -> copy.put(String.valueOf(k), v));
            this.skills.add(copy);
        }

        this.animations = new HashMap<>();
        ConfigurationSection animSec = section.getConfigurationSection("animations");
        if (animSec != null) {
            for (String key : animSec.getKeys(false)) {
                animations.put(key.toLowerCase(), animSec.getString(key));
            }
        }
    }

    public String getId() { return id; }
    public EntityType getType() { return type; }
    public String getModel() { return model; }
    public double getBaseHealth() { return baseHealth; }
    public int getDamageHalfHearts() { return damageHalfHearts; }
    public int getBaseAmount() { return amount; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public int getMaxAlive() { return maxAlive; }

    public int getAmountForRuns(int runs) {
        int extra = Math.min(maxExtraAmount, amountPerRun * Math.max(0, runs));
        return amount + extra;
    }

    public double getHealthForRuns(int runs) {
        double multiplier = 1.0 + (healthPerRunPercent / 100.0) * Math.max(0, runs);
        return baseHealth * multiplier;
    }

    public List<Map<String, Object>> getSkills() { return skills; }
    public Map<String, String> getAnimations() { return animations; }
}
