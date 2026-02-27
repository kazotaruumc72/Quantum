package com.wynvers.quantum.placeholderapi;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * PlaceholderAPI Expansion for Quantum Plugin
 * Provides custom placeholders for use with PlaceholderAPI
 * Reference: https://github.com/PlaceholderAPI/PlaceholderAPI
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
        // This expansion should persist through reloads
        return true;
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

        // Normalisation en minuscules
        String p = params.toLowerCase();
        Player player = offlinePlayer.getPlayer();

        // Delegate to internal PlaceholderManager for online players
        // This ensures ALL placeholders work consistently with the internal system
        if (plugin.getPlaceholderManager() != null && player != null) {
            // Build the placeholder with "quantum_" prefix if not present
            String fullPlaceholder = p.startsWith("quantum_") ? "%" + p + "%" : "%quantum_" + p + "%";
            String internalResult = plugin.getPlaceholderManager().parse(player, fullPlaceholder);

            // If placeholder was resolved (not returned as-is), use that result
            if (internalResult != null && !internalResult.equals(fullPlaceholder)) {
                return internalResult;
            }
        }

        // ==========================
        // FALLBACK FOR OFFLINE PLAYERS
        // These placeholders work for offline players too
        // ==========================

        // ==========================
        // SERVER INFO
        // ==========================
        if (p.equals("online")) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }

        if (p.equals("max_players")) {
            return String.valueOf(Bukkit.getMaxPlayers());
        }

        // ==========================
        // NIVEAU / EXP
        // ==========================
        // Aliases :
        // %quantum_level%            -> level
        // %quantum_player_level%     -> player_level
        // %quantum_exp%              -> exp
        // %quantum_player_exp%       -> player_exp
        // %quantum_exp_required%     -> exp_required
        // %quantum_player_exp_required% -> player_exp_required

        if (p.equals("level") || p.equals("player_level")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId()));
        }

        if (p.equals("exp") || p.equals("player_exp")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getExp(offlinePlayer.getUniqueId()));
        }

        if (p.equals("exp_required") || p.equals("player_exp_required")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            int level = plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId());
            return String.valueOf(plugin.getPlayerLevelManager().getExpForLevel(level + 1));
        }

        if (p.equals("exp_progress")) {
            if (plugin.getPlayerLevelManager() == null) return "0";
            int level = plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId());
            int currentExp = plugin.getPlayerLevelManager().getExp(offlinePlayer.getUniqueId());
            int requiredExp = plugin.getPlayerLevelManager().getExpForLevel(level + 1);
            if (requiredExp <= 0) return "100";
            int progress = (int) ((currentExp / (double) requiredExp) * 100);
            return String.valueOf(progress);
        }

        if (p.equals("max_level")) {
            if (plugin.getPlayerLevelManager() == null) return "1000";
            return String.valueOf(plugin.getPlayerLevelManager().getMaxLevel());
        }

        // ============
        // JOB SYSTEM
        // ============
        if (p.equals("job")) {
            if (plugin.getJobManager() == null) return "None";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            if (jobData == null) return "None";
            var job = plugin.getJobManager().getJob(jobData.getJobId());
            return job != null ? job.getDisplayName() : "Unknown";
        }

        if (p.equals("job_level")) {
            if (plugin.getJobManager() == null) return "0";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getLevel()) : "0";
        }

        if (p.equals("job_exp")) {
            if (plugin.getJobManager() == null) return "0";
            var jobData = plugin.getJobManager().getPlayerJob(offlinePlayer.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getExperience()) : "0";
        }

        // =================
        // TOWER (joueur)
        // =================
        if (player != null) {
            if (p.equals("tower")) {
                if (plugin.getTowerManager() == null) return "None";
                var tower = plugin.getTowerManager().getPlayerTower(player);
                return tower != null ? tower.getId() : "None";
            }

            if (p.equals("tower_name")) {
                if (plugin.getTowerManager() == null) return "None";
                var tower = plugin.getTowerManager().getPlayerTower(player);
                return tower != null ? tower.getName() : "None";
            }

            if (p.equals("tower_floor")) {
                if (plugin.getTowerManager() == null) return "0";
                return String.valueOf(plugin.getTowerManager().getPlayerFloor(player.getUniqueId()));
            }

            if (p.equals("tower_kills") || p.equals("tower_kills_current")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                int floor = progress.getCurrentFloor();
                if (towerId == null || floor <= 0) return "0";
                return String.valueOf(progress.getFloorMobKills(towerId, floor));
            }

            if (p.equals("tower_kills_required")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                int floor = progress.getCurrentFloor();
                if (towerId == null || floor <= 0) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                return String.valueOf(tower.getFloorMobKillsRequired(floor));
            }

            if (p.equals("tower_percentage")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                int floor = progress.getCurrentFloor();
                if (towerId == null || floor <= 0) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                int required = tower.getFloorMobKillsRequired(floor);
                if (required <= 0) return "100";
                int kills = progress.getFloorMobKills(towerId, floor);
                int pct = (int) Math.min(100, (kills * 100.0) / required);
                return String.valueOf(pct);
            }

            if (p.equals("tower_mob_list")) {
                if (plugin.getTowerManager() == null) return "";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                int floor = progress.getCurrentFloor();
                if (towerId == null || floor <= 0) return "";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "";
                var reqs = tower.getFloorMobRequirements(floor);
                if (reqs.isEmpty()) return "";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < reqs.size(); i++) {
                    var req = reqs.get(i);
                    String mobName = req.getSource() == com.wynvers.quantum.towers.FloorMobRequirement.MobSource.MYTHICMOBS
                            ? req.getMythicId()
                            : req.getEntityType().name();
                    int killed = progress.getFloorMobKills(towerId, floor, req.getKey());
                    int remaining = Math.max(0, req.getAmount() - killed);
                    sb.append(mobName).append(" x").append(remaining);
                    if (i < reqs.size() - 1) sb.append(", ");
                }
                return sb.toString();
            }

            if (p.equals("tower_mob_remaining_percentage")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                int floor = progress.getCurrentFloor();
                if (towerId == null || floor <= 0) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                var reqs = tower.getFloorMobRequirements(floor);
                if (reqs.isEmpty()) return "0";
                int totalRequired = 0;
                int totalRemaining = 0;
                for (var req : reqs) {
                    totalRequired += req.getAmount();
                    int killed = progress.getFloorMobKills(towerId, floor, req.getKey());
                    totalRemaining += Math.max(0, req.getAmount() - killed);
                }
                if (totalRequired <= 0) return "0";
                int pct = (int) (((totalRequired - totalRemaining) * 100.0) / totalRequired);
                return String.valueOf(pct);
            }

            if (p.equals("tower_next_semi_boss")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                if (towerId == null) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                int currentFloor = progress.getCurrentFloor();
                int nextSemiBoss = tower.getNextSemiBossFloor(currentFloor);
                return nextSemiBoss == -1 ? "0" : String.valueOf(nextSemiBoss);
            }

            if (p.equals("tower_next_boss")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                if (towerId == null) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                int currentFloor = progress.getCurrentFloor();
                int nextBoss = tower.getNextBossFloor(currentFloor);
                return nextBoss == -1 ? "0" : String.valueOf(nextBoss);
            }

            if (p.equals("tower_floors_percentage")) {
                if (plugin.getTowerManager() == null) return "0";
                var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
                String towerId = progress.getCurrentTower();
                if (towerId == null) return "0";
                var tower = plugin.getTowerManager().getTower(towerId);
                if (tower == null) return "0";
                int total = tower.getTotalFloors();
                if (total <= 0) return "0";
                int completed = progress.getFloorProgress(towerId);
                int pct = (int) Math.min(100, (completed * 100.0) / total);
                return String.valueOf(pct);
            }
        }

        // ===========================
        // TOWERS GLOBAL
        // ===========================
        if (p.equals("towers_completed") || p.equals("towers_total") || p.equals("towers_percentage")) {
            if (plugin.getTowerManager() == null) return "0";

            var towerManager = plugin.getTowerManager();
            Map<String, com.wynvers.quantum.towers.TowerConfig> towers = towerManager.getAllTowers();
            if (towers.isEmpty()) return "0";

            com.wynvers.quantum.towers.TowerProgress progress =
                    towerManager.getProgress(offlinePlayer.getUniqueId());

            if (p.equals("towers_completed")) {
                return String.valueOf(progress.getCompletedTowersCount(towers));
            }

            if (p.equals("towers_total")) {
                return String.valueOf(towers.size());
            }

            // towers_percentage
            int totalFloors = 0;
            int completedFloors = 0;

            for (Map.Entry<String, com.wynvers.quantum.towers.TowerConfig> entry : towers.entrySet()) {
                String towerId = entry.getKey();
                com.wynvers.quantum.towers.TowerConfig tower = entry.getValue();
                totalFloors += tower.getTotalFloors();
                completedFloors += progress.getFloorProgress(towerId);
            }

            if (totalFloors == 0) return "0";
            double percentage = (completedFloors * 100.0) / totalFloors;
            return String.format("%.1f", percentage);
        }

        // ===========================
        // TOWER TOP POSITION
        // ===========================
        if (p.matches("tower_top_pos_\\d+")) {
            if (plugin.getTowerManager() == null) return "N/A";
            int position;
            try {
                position = Integer.parseInt(p.substring("tower_top_pos_".length()));
            } catch (NumberFormatException e) {
                return "N/A";
            }
            java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> topPlayers =
                    plugin.getTowerManager().getTopPlayers(position);
            if (topPlayers.size() >= position) {
                java.util.UUID topUuid = topPlayers.get(position - 1).getKey();
                org.bukkit.OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(topUuid);
                return topPlayer.getName() != null ? topPlayer.getName() : "Unknown";
            }
            return "N/A";
        }

        // ===========================
        // FLOOR CLEAR TIME LEADERBOARD (podium)
        // %quantum_floor_top_<towerId>_<floor>_name_<pos>%  -> player name
        // %quantum_floor_top_<towerId>_<floor>_time_<pos>%  -> formatted time
        // ===========================
        if (p.startsWith("floor_top_")) {
            if (plugin.getFloorClearTimeManager() == null) return "N/A";
            // Expected format: floor_top_<towerId>_<floor>_name_<pos> or floor_top_<towerId>_<floor>_time_<pos>
            // We parse from the end: last segment is position, second-to-last is "name" or "time",
            // third-to-last is floor number, and everything before that is the tower ID.
            String remainder = p.substring("floor_top_".length()); // e.g. "tower1_3_name_1"
            String[] parts = remainder.split("_");
            // Minimum parts: towerId(1) + floor(1) + type(1) + pos(1) = 4
            if (parts.length < 4) return "N/A";

            try {
                int pos = Integer.parseInt(parts[parts.length - 1]);
                String type = parts[parts.length - 2]; // "name" or "time"
                int floor = Integer.parseInt(parts[parts.length - 3]);
                // Tower ID = everything before the last 3 segments (supports tower IDs with underscores)
                StringBuilder towerIdBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 3; i++) {
                    if (i > 0) towerIdBuilder.append("_");
                    towerIdBuilder.append(parts[i]);
                }
                String towerId = towerIdBuilder.toString();

                if (pos <= 0 || pos > 30) return "N/A";

                java.util.List<com.wynvers.quantum.towers.FloorClearTimeManager.LeaderboardEntry> top =
                        plugin.getFloorClearTimeManager().getTopPlayers(towerId, floor, pos);

                if (top.size() < pos) return "N/A";

                com.wynvers.quantum.towers.FloorClearTimeManager.LeaderboardEntry entry = top.get(pos - 1);

                if ("name".equals(type)) {
                    return entry.playerName() != null ? entry.playerName() : "Unknown";
                } else if ("time".equals(type)) {
                    return com.wynvers.quantum.towers.FloorClearTimeManager.formatTime(entry.clearTimeMs());
                }
            } catch (NumberFormatException ignored) {
                // Invalid format
            }
            return "N/A";
        }

        // ==============
        // STORAGE
        // ==============
        if (p.equals("storage_items")) {
            if (plugin.getStorageManager() == null) return "0";
            var storage = plugin.getStorageManager().getPlayerStorage(offlinePlayer.getUniqueId());
            return storage != null ? String.valueOf(storage.getTotalItemCount()) : "0";
        }

        // utilisé pour la 2ᵉ valeur du storage
        if (p.equals("storage_capacity")) {
            if (plugin.getStorageManager() == null || plugin.getStorageUpgradeManager() == null) return "0";
            if (player == null) return "0"; // nécessite un joueur en ligne
            var state = plugin.getStorageUpgradeManager().getState(player);
            return String.valueOf(plugin.getStorageUpgradeManager().getMaxStacks(state));
        }

        if (p.equals("storage_used_percent")) {
            if (plugin.getStorageManager() == null || plugin.getStorageUpgradeManager() == null) return "0%";
            var storage = plugin.getStorageManager().getPlayerStorage(offlinePlayer.getUniqueId());
            if (storage == null || player == null) return "0%";
            var state = plugin.getStorageUpgradeManager().getState(player);
            int capacity = plugin.getStorageUpgradeManager().getMaxStacks(state);
            if (capacity <= 0) return "0%";
            int percent = (int) ((storage.getTotalItemCount() / (double) capacity) * 100);
            return percent + "%";
        }

        // ====================
        // STATISTIQUES ORDERS
        // ====================
        if (p.equals("orders_created")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getOrdersCreated(offlinePlayer.getUniqueId()));
        }

        if (p.equals("orders_filled")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getOrdersFilled(offlinePlayer.getUniqueId()));
        }

        if (p.equals("items_sold")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getItemsSold(offlinePlayer.getUniqueId()));
        }

        if (p.equals("items_bought")) {
            if (plugin.getStatisticsManager() == null) return "0";
            return String.valueOf(plugin.getStatisticsManager().getItemsBought(offlinePlayer.getUniqueId()));
        }

        // =========
        // ECONOMIE
        // =========
        if (p.equals("eco_balance")) {
            if (plugin.getVaultManager() == null || !plugin.getVaultManager().isEnabled()) return "0";
            return String.valueOf(plugin.getVaultManager().getBalance(offlinePlayer));
        }

        if (p.equals("eco_balance_formatted")) {
            if (plugin.getVaultManager() == null || !plugin.getVaultManager().isEnabled()) return "0.00";
            return plugin.getVaultManager().format(plugin.getVaultManager().getBalance(offlinePlayer));
        }

        if (p.equals("eco_currency")) {
            if (plugin.getVaultManager() == null || !plugin.getVaultManager().isEnabled()) return "Dollar";
            return plugin.getVaultManager().getCurrencyName();
        }

        if (p.equals("eco_currency_plural")) {
            if (plugin.getVaultManager() == null || !plugin.getVaultManager().isEnabled()) return "Dollars";
            return plugin.getVaultManager().getCurrencyNamePlural();
        }

        if (p.equals("eco_symbol")) {
            if (plugin.getVaultManager() == null || !plugin.getVaultManager().isEnabled()) return "$";
            return plugin.getVaultManager().getSymbol();
        }

        if (player != null) {
            if (p.equals("eco_total_buy")) {
                if (plugin.getTransactionHistoryManager() == null) return "0.00";
                return String.format("%.2f", plugin.getTransactionHistoryManager().getTotalBuyAmount(player));
            }

            if (p.equals("eco_total_sell")) {
                if (plugin.getTransactionHistoryManager() == null) return "0.00";
                return String.format("%.2f", plugin.getTransactionHistoryManager().getTotalSellAmount(player));
            }

            if (p.equals("eco_net_profit")) {
                if (plugin.getTransactionHistoryManager() == null) return "0.00";
                return String.format("%.2f", plugin.getTransactionHistoryManager().getNetProfit(player));
            }

            if (p.equals("eco_transactions")) {
                if (plugin.getTransactionHistoryManager() == null) return "0";
                return String.valueOf(plugin.getTransactionHistoryManager().getTotalTransactionCount(player));
            }
        }

        // ==========================
        // ECONOMIE MULTI‑DEVISES
        // ==========================
        if (plugin.getVaultManager() != null) {
            String lowerParams = p;
            for (String currencyId : plugin.getVaultManager().getCurrencyIds()) {
                String prefix = "eco_" + currencyId.toLowerCase() + "_";
                if (lowerParams.startsWith(prefix)) {
                    String subParam = lowerParams.substring(prefix.length());
                    com.wynvers.quantum.economy.QuantumEconomy eco =
                            plugin.getVaultManager().getCurrency(currencyId);
                    if (eco == null) return "0";

                    switch (subParam) {
                        case "balance":
                            return String.valueOf(eco.getBalance(offlinePlayer));
                        case "balance_formatted":
                            return eco.format(eco.getBalance(offlinePlayer));
                        case "symbol":
                            return eco.getSymbol();
                        case "currency":
                            return eco.currencyNameSingular();
                        case "currency_plural":
                            return eco.currencyNamePlural();
                        default:
                            return "0";
                    }
                }
            }
        }


        // =====================
        // COORDINATES
        // =====================
        if (player != null) {
            if (p.equals("player_x")) {
                return String.valueOf(player.getLocation().getBlockX());
            }
            if (p.equals("player_y")) {
                return String.valueOf(player.getLocation().getBlockY());
            }
            if (p.equals("player_z")) {
                return String.valueOf(player.getLocation().getBlockZ());
            }
        }

        // =========
        // HOMES
        // =========
        if (p.equals("homes")) {
            if (plugin.getHomeManager() == null) return "0";
            return String.valueOf(plugin.getHomeManager().getHomeCount(offlinePlayer.getUniqueId()));
        }

        if (p.equals("homes_max")) {
            if (player == null || plugin.getHomeManager() == null) return "0";
            return String.valueOf(plugin.getHomeManager().getMaxHomes(player));
        }

        return null; // Placeholder not recognized
    }
}
