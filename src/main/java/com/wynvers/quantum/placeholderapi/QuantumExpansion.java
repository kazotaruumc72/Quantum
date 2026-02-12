package com.wynvers.quantum.placeholderapi;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI Expansion for Quantum Plugin
 * Provides custom placeholders for use with PlaceholderAPI
 * Reference: https://github.com/PlaceholderAPI/PlaceholderAPI
 * Version: 2.11.6 (Compatible with Minecraft 1.21.11)
 */
public class QuantumExpansion extends PlaceholderExpansion {

    private final Quantum plugin;

    public QuantumExpansion(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quantum";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This expansion should persist through reloads
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return null;
        }

        // Get online player if available
        Player player = offlinePlayer.getPlayer();
        
        // Level placeholders
        if (params.equalsIgnoreCase("level")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("exp")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getExp(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("exp_required")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            int level = plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId());
            return String.valueOf(plugin.getPlayerLevelManager().getExpForLevel(level + 1));
        }
        
        if (params.equalsIgnoreCase("exp_progress")) {
            if (plugin.getPlayerLevelManager() == null) return "0%";
            int level = plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId());
            int currentExp = plugin.getPlayerLevelManager().getExp(offlinePlayer.getUniqueId());
            int requiredExp = plugin.getPlayerLevelManager().getExpForLevel(level + 1);
            if (requiredExp <= 0) return "100%";
            int progress = (int) ((currentExp / (double) requiredExp) * 100);
            return progress + "%";
        }

        // Job placeholders
        if (params.equalsIgnoreCase("job")) {
            if (plugin.getJobManager() == null) return "None";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            if (jobData == null) return "None";
            var job = plugin.getJobManager().getJob(jobData.getJobId());
            return job != null ? job.getDisplayName() : "Unknown";
        }
        
        if (params.equalsIgnoreCase("job_level")) {
            if (plugin.getJobManager() == null) return "0";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getLevel()) : "0";
        }
        
        if (params.equalsIgnoreCase("job_exp")) {
            if (plugin.getJobManager() == null) return "0";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getExperience()) : "0";
        }

        // Tower placeholders (require online player)
        if (player != null) {
            if (params.equalsIgnoreCase("tower")) {
                if (plugin.getTowerManager() == null) return "None";
                var tower = plugin.getTowerManager().getPlayerTower(player);
                return tower != null ? tower.getId() : "None";
            }
            
            if (params.equalsIgnoreCase("tower_name")) {
                if (plugin.getTowerManager() == null) return "None";
                var tower = plugin.getTowerManager().getPlayerTower(player);
                return tower != null ? tower.getName() : "None";
            }
            
            if (params.equalsIgnoreCase("tower_floor")) {
                if (plugin.getTowerManager() == null) return "0";
                return String.valueOf(plugin.getTowerManager().getPlayerFloor(player.getUniqueId()));
            }
        }

        // Storage placeholders
        if (params.equalsIgnoreCase("storage_items")) {
            if (plugin.getStorageManager() == null) return "0";
            var storage = plugin.getStorageManager().getPlayerStorage(offlinePlayer.getUniqueId());
            return storage != null ? String.valueOf(storage.getTotalItemCount()) : "0";
        }
        
        if (params.equalsIgnoreCase("storage_capacity")) {
            if (plugin.getStorageManager() == null || plugin.getStorageUpgradeManager() == null) return "0";
            if (player == null) return "0"; // Need online player to get capacity
            var state = plugin.getStorageUpgradeManager().getState(player);
            return String.valueOf(plugin.getStorageUpgradeManager().getMaxStacks(state));
        }
        
        if (params.equalsIgnoreCase("storage_used_percent")) {
            if (plugin.getStorageManager() == null || plugin.getStorageUpgradeManager() == null) return "0%";
            var storage = plugin.getStorageManager().getPlayerStorage(offlinePlayer.getUniqueId());
            if (storage == null || player == null) return "0%";
            var state = plugin.getStorageUpgradeManager().getState(player);
            int capacity = plugin.getStorageUpgradeManager().getMaxStacks(state);
            if (capacity <= 0) return "0%";
            int percent = (int) ((storage.getTotalItemCount() / (double) capacity) * 100);
            return percent + "%";
        }

        // Statistics placeholders
        if (params.equalsIgnoreCase("orders_created")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getOrdersCreated(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("orders_filled")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getOrdersFilled(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("items_sold")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getItemsSold(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("items_bought")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getItemsBought(offlinePlayer.getUniqueId()));
        }

        // Home placeholders
        if (params.equalsIgnoreCase("homes")) {
            if (plugin.getHomeManager() == null) return "0";
            return String.valueOf(plugin.getHomeManager().getHomeCount(offlinePlayer.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("homes_max")) {
            if (player == null || plugin.getHomeManager() == null) return "0";
            return String.valueOf(plugin.getHomeManager().getMaxHomes(player));
        }

        return null; // Placeholder not recognized
    }
}
