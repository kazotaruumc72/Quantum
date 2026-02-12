package com.wynvers.quantum.tab;

import com.wynvers.quantum.Quantum;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * TAB Plugin Integration Manager
 * Provides MiniMessage support and custom placeholders for TAB
 * Reference: https://github.com/NEZNAMY/TAB/
 * Version: 5.5.0 (Compatible with Minecraft 1.21.11)
 */
public class TABManager {

    private final Quantum plugin;
    private TabAPI tabAPI;
    private boolean enabled = false;

    public TABManager(Quantum plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Check if TAB is loaded
        if (Bukkit.getPluginManager().getPlugin("TAB") == null) {
            plugin.getLogger().warning("TAB plugin not found - TAB integration disabled");
            return;
        }

        try {
            // Get TAB API instance
            this.tabAPI = TabAPI.getInstance();
            
            // Register custom Quantum placeholders
            registerPlaceholders();
            
            this.enabled = true;
            plugin.getLogger().info("✓ TAB integration enabled! (v5.5.0, MiniMessage support available)");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize TAB integration: " + e.getMessage());
            this.enabled = false;
        }
    }

    /**
     * Register custom placeholders for TAB
     * These can be used in TAB's configuration files
     */
    private void registerPlaceholders() {
        PlaceholderManager placeholderManager = tabAPI.getPlaceholderManager();

        // Quantum Level Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_level%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getLevel(bukkitPlayer.getUniqueId()));
        });

        // Quantum Job Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_job%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "None";
            
            if (plugin.getJobManager() == null) return "None";
            
            var jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
            if (jobData == null) return "None";
            
            var job = plugin.getJobManager().getJob(jobData.getJobId());
            return job != null ? job.getDisplayName() : "Unknown";
        });

        // Quantum Job Level Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_job_level%", 1000, player -> {
            if (plugin.getJobManager() == null) return "0";
            
            var jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getLevel()) : "0";
        });

        // Quantum Tower Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_tower%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "None";
            
            if (plugin.getTowerManager() == null) return "None";
            
            var tower = plugin.getTowerManager().getPlayerTower(bukkitPlayer);
            return tower != null ? tower.getId() : "None";
        });

        // Quantum Tower Floor Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_tower_floor%", 1000, player -> {
            if (plugin.getTowerManager() == null) return "0";
            
            var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
            return progress != null ? String.valueOf(progress.getCurrentFloor()) : "0";
        });

        plugin.getLogger().info("✓ Registered TAB placeholders: %quantum_level%, %quantum_job%, %quantum_job_level%, %quantum_tower%, %quantum_tower_floor%");
    }

    /**
     * Update a player's TAB display
     * Can be called when player data changes
     * Note: TAB 5.x+ automatically updates when placeholders change
     */
    public void updatePlayer(Player player) {
        if (!enabled) return;
        
        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) {
                // TAB 5.x+ automatically refreshes placeholders
                // No manual refresh needed
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update TAB for player " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Update all online players' TAB displays
     */
    public void updateAllPlayers() {
        if (!enabled) return;
        
        Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
    }

    /**
     * Check if TAB integration is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the TAB API instance
     */
    public TabAPI getAPI() {
        return tabAPI;
    }
}
