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
        // NIVEAU / EXP (scoreboard)
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
            if (plugin.getPlayerLevelManager() == null) return "0%";
            int level = plugin.getPlayerLevelManager().getLevel(offlinePlayer.getUniqueId());
            int currentExp = plugin.getPlayerLevelManager().getExp(offlinePlayer.getUniqueId());
            int requiredExp = plugin.getPlayerLevelManager().getExpForLevel(level + 1);
            if (requiredExp <= 0) return "100%";
            int progress = (int) ((currentExp / (double) requiredExp) * 100);
            return progress + "%";
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
        }

        // ===========================
        // TOWERS GLOBAL (scoreboard)
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

        // ==============
        // STORAGE
        // ==============
        if (p.equals("storage_items")) {
            if (plugin.getStorageManager() == null) return "0";
            var storage = plugin.getStorageManager().getPlayerStorage(offlinePlayer.getUniqueId());
            return storage != null ? String.valueOf(storage.getTotalItemCount()) : "0";
        }

        // utilisé pour la 2ᵉ valeur du scoreboard
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
        // APARTMENT
        // =====================
        if (p.equals("apartment_zone")) {
            if (plugin.getApartmentManager() == null) return "Aucun";
            var apt = plugin.getApartmentManager().getPlayerApartment(offlinePlayer.getUniqueId());
            return apt != null && !apt.getZoneName().isEmpty() ? apt.getZoneName() : "Aucun";
        }

        if (p.equals("apartment_name")) {
            if (plugin.getApartmentManager() == null) return "Aucun";
            var apt = plugin.getApartmentManager().getPlayerApartment(offlinePlayer.getUniqueId());
            return apt != null ? apt.getApartmentName() : "Aucun";
        }

        if (p.equals("apartment_size")) {
            if (plugin.getApartmentManager() == null) return "Aucun";
            var apt = plugin.getApartmentManager().getPlayerApartment(offlinePlayer.getUniqueId());
            return apt != null ? apt.getSize().getDisplayName() : "Aucun";
        }

        if (p.equals("apartment_deadline")) {
            if (plugin.getApartmentManager() == null) return "Aucun";
            var apt = plugin.getApartmentManager().getPlayerApartment(offlinePlayer.getUniqueId());
            return apt != null ? plugin.getApartmentManager().getFormattedDeadline(apt) : "Aucun contrat";
        }

        if (p.equals("apartment_furniture_count")) {
            if (plugin.getApartmentManager() == null) return "0";
            var apt = plugin.getApartmentManager().getPlayerApartment(offlinePlayer.getUniqueId());
            return apt != null ? String.valueOf(apt.getFurniture().size()) : "0";
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
