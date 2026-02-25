package com.wynvers.quantum.healthbar;

import com.wynvers.quantum.Quantum;
import kr.toxicity.healthbar.api.BetterHealthBarAPI;
import kr.toxicity.healthbar.api.trigger.HealthBarTrigger;
import kr.toxicity.healthbar.api.healthbar.HealthBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager for BetterHealthBar integration
 * Uses models instead of glyphs for health bars
 */
public class HealthBarManager {

    private final Quantum plugin;
    private final HealthBarConfig config;
    private BetterHealthBarAPI betterHealthBarAPI;
    private boolean apiAvailable;
    private final Map<UUID, String> activeHealthBars;

    public HealthBarManager(Quantum plugin) {
        this.plugin = plugin;
        this.config = new HealthBarConfig(plugin);
        this.activeHealthBars = new HashMap<>();
        this.apiAvailable = false;

        if (!config.isEnabled()) {
            plugin.getQuantumLogger().info("HealthBar system is disabled in config");
            return;
        }

        initializeBetterHealthBar();
    }

    private void initializeBetterHealthBar() {
        try {
            if (Bukkit.getPluginManager().getPlugin("BetterHealthBar") == null) {
                plugin.getQuantumLogger().warning("⚠ BetterHealthBar plugin not found - health bar system disabled");
                return;
            }

            betterHealthBarAPI = BetterHealthBarAPI.inst();
            if (betterHealthBarAPI == null) {
                plugin.getQuantumLogger().warning("⚠ BetterHealthBar API not available");
                return;
            }

            apiAvailable = true;
            plugin.getQuantumLogger().success("✓ BetterHealthBar integration initialized!");
            plugin.getQuantumLogger().info("  - Using model-based health bars");
            plugin.getQuantumLogger().info("  - Model ID: " + config.getModelId());
            plugin.getQuantumLogger().info("  - View distance: " + config.getViewDistance() + " blocks");

        } catch (NoClassDefFoundError | Exception e) {
            plugin.getQuantumLogger().warning("⚠ Failed to initialize BetterHealthBar: " + e.getMessage());
            apiAvailable = false;
        }
    }

    /**
     * Apply health bar to an entity
     */
    public void applyHealthBar(LivingEntity entity) {
        if (!apiAvailable || !config.isEnabled()) {
            return;
        }

        try {
            // Skip if already has health bar
            if (activeHealthBars.containsKey(entity.getUniqueId())) {
                return;
            }

            // Check entity type filters
            if (entity instanceof Player) {
                if (!config.isPlayersEnabled() || !config.isShowOnPlayers()) {
                    return;
                }
            } else {
                if (!config.isMobsEnabled() || !config.isShowOnMobs()) {
                    return;
                }
            }

            // Determine which model to use
            String modelId = determineModelId(entity);
            double scale = determineScale(entity);
            double yOffset = determineYOffset(entity);

            // Get health bar from BetterHealthBar
            HealthBar healthBar = betterHealthBarAPI.getHealthBar(modelId);
            if (healthBar == null) {
                plugin.getQuantumLogger().warning("Health bar model not found: " + modelId);
                return;
            }

            // Apply health bar with custom settings
            HealthBarTrigger trigger = HealthBarTrigger.builder()
                    .entity(entity)
                    .healthBar(healthBar)
                    .scale(scale)
                    .yOffset(yOffset)
                    .build();

            betterHealthBarAPI.addHealthBar(trigger);
            activeHealthBars.put(entity.getUniqueId(), modelId);

        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Failed to apply health bar to entity: " + e.getMessage());
        }
    }

    /**
     * Remove health bar from an entity
     */
    public void removeHealthBar(LivingEntity entity) {
        if (!apiAvailable) {
            return;
        }

        try {
            UUID entityId = entity.getUniqueId();
            if (activeHealthBars.containsKey(entityId)) {
                betterHealthBarAPI.removeHealthBar(entity);
                activeHealthBars.remove(entityId);
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Failed to remove health bar: " + e.getMessage());
        }
    }

    /**
     * Update health bar for an entity
     */
    public void updateHealthBar(LivingEntity entity) {
        if (!apiAvailable || !config.isEnabled()) {
            return;
        }

        try {
            if (activeHealthBars.containsKey(entity.getUniqueId())) {
                // Remove and reapply to update
                removeHealthBar(entity);
                applyHealthBar(entity);
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Failed to update health bar: " + e.getMessage());
        }
    }

    /**
     * Determine which model to use based on entity type
     */
    private String determineModelId(LivingEntity entity) {
        // Boss entities get special model
        if (isBoss(entity) && config.isBossesEnabled() && config.isBossUseCustomModel()) {
            return config.getBossModelId();
        }

        // Tower mobs (check if entity is in tower zone)
        if (config.isTowerMobsEnabled() && isInTowerZone(entity)) {
            return config.getModelId();
        }

        // Default model
        return config.getModelId();
    }

    /**
     * Determine scale based on entity type
     */
    private double determineScale(LivingEntity entity) {
        if (isBoss(entity) && config.isBossesEnabled()) {
            return config.getBossScale();
        }
        return config.getModelScale();
    }

    /**
     * Determine Y offset based on entity type
     */
    private double determineYOffset(LivingEntity entity) {
        if (isBoss(entity) && config.isBossesEnabled()) {
            return config.getBossYOffset();
        }
        return config.getYOffset();
    }

    /**
     * Check if entity is a boss
     */
    private boolean isBoss(LivingEntity entity) {
        // Check for vanilla bosses
        return switch (entity.getType()) {
            case ENDER_DRAGON, WITHER -> true;
            default -> false;
        };
    }

    /**
     * Check if entity is in a tower zone
     */
    private boolean isInTowerZone(Entity entity) {
        if (plugin.getZoneManager() == null) {
            return false;
        }
        try {
            return plugin.getZoneManager().isInAnyZone(entity.getLocation());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clean up health bars when entities are removed
     */
    public void cleanup() {
        activeHealthBars.clear();
    }

    /**
     * Reload configuration
     */
    public void reload() {
        config.reload();
        cleanup();
        plugin.getQuantumLogger().success("✓ HealthBar system reloaded!");
    }

    /**
     * Check if API is available
     */
    public boolean isAvailable() {
        return apiAvailable;
    }

    /**
     * Get configuration
     */
    public HealthBarConfig getConfig() {
        return config;
    }

    /**
     * Get active health bar count
     */
    public int getActiveHealthBarCount() {
        return activeHealthBars.size();
    }
}
