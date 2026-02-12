package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageMode;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager {

    private final Quantum plugin;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    
    // Cache for tower kill requirements to avoid repeated YAML file reads
    // Key: towerId + "|" + floor (using | as delimiter to avoid collisions with tower IDs)
    // Value: required kills
    private final Map<String, Integer> killRequirementsCache = new ConcurrentHashMap<>();
    private volatile long lastCacheRefresh = 0;
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds

    public PlaceholderManager(Quantum plugin) {
        this.plugin = plugin;
    }

    /**
     * Parse placeholders in string
     * Internal implementation - no external PlaceholderAPI dependency
     */
    public String parse(Player player, String text) {
        if (text == null) {
            return text;
        }

        // Use regex to find and replace all placeholders
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(player, placeholder);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement != null ? replacement : matcher.group(0)));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Parse placeholders in string avec contexte personnalisé
     * Utile pour les menus dynamiques (vente, etc.)
     */
    public String parse(Player player, String text, Map<String, String> customPlaceholders) {
        if (text == null) {
            return text;
        }
        
        // D'abord remplacer les placeholders personnalisés
        if (customPlaceholders != null) {
            for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
                text = text.replace("%" + entry.getKey() + "%", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        
        // Ensuite utiliser le parseur interne
        return parse(player, text);
    }

    /**
     * Parse placeholders in list
     */
    public List<String> parse(Player player, List<String> texts) {
        if (texts == null) {
            return texts;
        }

        List<String> parsed = new ArrayList<>();
        for (String text : texts) {
            parsed.add(parse(player, text));
        }
        return parsed;
    }
    
    /**
     * Parse placeholders in list avec contexte personnalisé
     */
    public List<String> parse(Player player, List<String> texts, Map<String, String> customPlaceholders) {
        if (texts == null) {
            return texts;
        }

        List<String> parsed = new ArrayList<>();
        for (String text : texts) {
            parsed.add(parse(player, text, customPlaceholders));
        }
        return parsed;
    }

    /**
     * Check if PlaceholderAPI is enabled
     * Always returns true since we have internal implementation
     */
    public boolean isEnabled() {
        return true;
    }
    
    /**
     * Resolve a single placeholder
     * Integrates all QuantumExpansion and QuantumPlaceholderExpansion logic
     * 
     * @param player The player for whom to resolve the placeholder
     * @param placeholder The placeholder text without % signs (e.g., "quantum_mode" or "mode")
     * @return The resolved value, or null to preserve the original placeholder syntax
     */
    private String resolvePlaceholder(Player player, String placeholder) {
        if (player == null) {
            return "0";
        }
        
        // Strip "quantum_" prefix if present to normalize placeholder names
        // After this, 'params' contains the core placeholder name (e.g., "mode", "order_item_name")
        String params = placeholder;
        if (params.startsWith("quantum_")) {
            params = params.substring(8); // Remove "quantum_" prefix
        }
        
        // === STORAGE MODE ===
        if (params.equals("mode")) {
            return StorageMode.getMode(player).name();
        }
        
        if (params.equals("mode_display")) {
            String modeDisplay = StorageMode.getModeDisplay(player);
            return ChatColor.translateAlternateColorCodes('&', modeDisplay);
        }
        
        if (params.equals("mode_simple")) {
            return StorageMode.getSimpleModeDisplay(player);
        }
        
        // === STORAGE STATS ===
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        if (params.equals("storage_items")) {
            return Integer.toString(storage.getUniqueItemCount());
        }
        
        if (params.equals("storage_total")) {
            return Integer.toString(storage.getTotalItemCount());
        }
        
        // === STORAGE AMOUNT PLACEHOLDERS ===
        // %quantum_amt_nexo-custom_sword% or %amt_nexo-custom_sword%
        // %quantum_amt_minecraft-diamond% or %amt_minecraft-diamond%
        if (params.startsWith("amt_")) {
            String itemId = params.substring(4); // Remove "amt_"
            
            // Check if it's a Nexo item (nexo-id format)
            if (itemId.startsWith("nexo-")) {
                String nexoId = itemId.substring(5).replace("-", ":");
                int amount = storage.getNexoAmount(nexoId);
                return String.valueOf(amount);
            }
            
            // Check if it's a Minecraft item (minecraft-id format)
            if (itemId.startsWith("minecraft-")) {
                String materialName = itemId.substring(10).toUpperCase().replace("-", "_");
                try {
                    Material material = Material.valueOf(materialName);
                    int amount = storage.getAmount(material);
                    return String.valueOf(amount);
                } catch (IllegalArgumentException e) {
                    return "0";
                }
            }
            
            // No prefix - try both (priority to Nexo)
            String normalizedId = itemId.replace("-", ":");
            
            // Try Nexo first
            if (storage.getNexoItems().containsKey(normalizedId)) {
                return String.valueOf(storage.getNexoAmount(normalizedId));
            }
            
            // Try Minecraft
            try {
                Material material = Material.valueOf(itemId.toUpperCase().replace("-", "_"));
                return String.valueOf(storage.getAmount(material));
            } catch (IllegalArgumentException e) {
                return "0";
            }
        }
        
        // === JOB SYSTEM ===
        if (params.startsWith("job_")) {
            return handleJobPlaceholder(player, params);
        }
        
        // === TOWER SYSTEM ===
        if (params.startsWith("tower_") || params.startsWith("towers_")) {
            return handleTowerPlaceholder(player, params);
        }
        
        // === KILL TRACKING ===
        if (params.startsWith("killed_")) {
            return handleKillPlaceholder(player, params);
        }
        
        // === ORDER CREATION SESSION ===
        if (params.startsWith("order_")) {
            return handleOrderPlaceholder(player, params);
        }
        
        // Default: return null to preserve the original placeholder syntax (e.g., %unknown_placeholder%)
        return null;
    }
    
    /**
     * Handle job-related placeholders
     */
    private String handleJobPlaceholder(Player player, String params) {
        if (plugin.getJobManager() == null) {
            return "0";
        }
        
        com.wynvers.quantum.jobs.JobManager jobManager = plugin.getJobManager();
        com.wynvers.quantum.jobs.JobData jobData = jobManager.getPlayerJob(player.getUniqueId());
        
        // %quantum_job_name% - Current job display name
        if (params.equals("job_name")) {
            if (jobData == null) return "Aucun";
            com.wynvers.quantum.jobs.Job job = jobManager.getJob(jobData.getJobId());
            return job != null ? ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', job.getDisplayName())
            ) : "Aucun";
        }
        
        // %quantum_job_level% - Current job level
        if (params.equals("job_level")) {
            return jobData != null ? String.valueOf(jobData.getLevel()) : "0";
        }
        
        // %quantum_job_exp% - Current job exp
        if (params.equals("job_exp")) {
            return jobData != null ? String.valueOf(jobData.getExp()) : "0";
        }
        
        // %quantum_job_exp_needed% - Exp needed for next level
        if (params.equals("job_exp_needed")) {
            if (jobData == null) return "0";
            return String.valueOf(jobManager.getRequiredExp(jobData.getLevel()));
        }
        
        // %quantum_job_exp_progress% - Exp progress (50/100)
        if (params.equals("job_exp_progress")) {
            if (jobData == null) return "0/0";
            int exp = jobData.getExp();
            int needed = jobManager.getRequiredExp(jobData.getLevel());
            return exp + "/" + needed;
        }
        
        // %quantum_job_rank% - Player's rank in their job
        if (params.equals("job_rank")) {
            int rank = jobManager.getPlayerRank(player.getUniqueId());
            return rank > 0 ? String.valueOf(rank) : "N/A";
        }
        
        // %quantum_job_booster_exp% - Active exp booster multiplier
        if (params.equals("job_booster_exp")) {
            boolean inDungeon = plugin.getTowerManager() != null && 
                plugin.getTowerManager().getPlayerTower(player) != null;
            double multiplier = jobManager.getExpMultiplier(player.getUniqueId(), inDungeon);
            return multiplier > 1.0 ? String.format("%.1f", multiplier) : "1.0";
        }
        
        // %quantum_job_booster_money% - Active money booster multiplier
        if (params.equals("job_booster_money")) {
            boolean inDungeon = plugin.getTowerManager() != null && 
                plugin.getTowerManager().getPlayerTower(player) != null;
            double multiplier = jobManager.getMoneyMultiplier(player.getUniqueId(), inDungeon);
            return multiplier > 1.0 ? String.format("%.1f", multiplier) : "1.0";
        }
        
        // %quantum_job_boosters_active% - Number of active boosters
        if (params.equals("job_boosters_active")) {
            List<com.wynvers.quantum.jobs.ActiveBooster> boosters = 
                jobManager.getActiveBoosters(player.getUniqueId());
            int activeCount = 0;
            for (com.wynvers.quantum.jobs.ActiveBooster booster : boosters) {
                if (!booster.isExpired()) {
                    activeCount++;
                }
            }
            return String.valueOf(activeCount);
        }
        
        // Handle top placeholders
        if (params.matches("job_top_[a-z_]+_\\d+(_level)?")) {
            return handleJobTopPlaceholder(params);
        }
        
        return "0";
    }
    
    /**
     * Handle job top placeholders
     */
    private String handleJobTopPlaceholder(String params) {
        com.wynvers.quantum.jobs.JobManager jobManager = plugin.getJobManager();
        boolean isLevel = params.endsWith("_level");
        
        String[] parts = params.split("_");
        if (parts.length < 4) return isLevel ? "0" : "N/A";
        
        // Reconstruct job ID
        int endIndex = isLevel ? parts.length - 2 : parts.length - 1;
        StringBuilder jobIdBuilder = new StringBuilder();
        for (int i = 2; i < endIndex; i++) {
            if (i > 2) jobIdBuilder.append("_");
            jobIdBuilder.append(parts[i]);
        }
        String jobId = jobIdBuilder.toString();
        
        try {
            int position = Integer.parseInt(parts[endIndex]);
            List<com.wynvers.quantum.jobs.JobData> topPlayers = 
                jobManager.getTopPlayers(jobId, position);
            
            if (topPlayers.size() >= position) {
                com.wynvers.quantum.jobs.JobData topPlayer = topPlayers.get(position - 1);
                if (isLevel) {
                    return String.valueOf(topPlayer.getLevel());
                } else {
                    org.bukkit.OfflinePlayer offlinePlayer = 
                        org.bukkit.Bukkit.getOfflinePlayer(topPlayer.getPlayerUUID());
                    return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
                }
            }
        } catch (NumberFormatException e) {
            // Invalid position number
        }
        
        return isLevel ? "0" : "N/A";
    }
    
    /**
     * Handle tower-related placeholders
     */
    private String handleTowerPlaceholder(Player player, String params) {
        com.wynvers.quantum.towers.TowerManager towerManager = plugin.getTowerManager();
        if (towerManager == null) {
            return "0";
        }
        
        Map<String, com.wynvers.quantum.towers.TowerConfig> towers = towerManager.getAllTowers();
        if (towers.isEmpty()) {
            return "0";
        }
        
        com.wynvers.quantum.towers.TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String currentTowerId = progress.getCurrentTower();
        
        // Current tower placeholders
        if (params.equals("tower_current")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(currentTowerId);
            return tower != null ? tower.getName() : "0";
        }
        
        if (params.equals("tower_floor")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            return Integer.toString(progress.getCurrentFloor());
        }
        
        if (params.equals("tower_progress")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0/0";
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(currentTowerId);
            int total = tower.getTotalFloors();
            return completed + "/" + total;
        }
        
        // Additional tower placeholders
        if (params.equals("tower_kills_current") || params.equals("tower_kills_required") || 
            params.equals("tower_kills_progress") || params.equals("tower_percentage") ||
            params.equals("tower_next_boss") || params.equals("tower_status")) {
            return handleCurrentTowerPlaceholder(player, params, currentTowerId, progress, towerManager);
        }
        
        // Specific tower placeholders
        if (params.matches("tower_[a-z_]+_(progress|percentage|completed)")) {
            return handleSpecificTowerPlaceholder(params, progress, towerManager);
        }
        
        // Global towers placeholders
        if (params.equals("towers_completed")) {
            return Integer.toString(progress.getCompletedTowersCount(towers));
        }
        
        if (params.equals("towers_total")) {
            return Integer.toString(towers.size());
        }
        
        if (params.equals("towers_percentage")) {
            if (towers.isEmpty()) return "0";
            
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
        
        if (params.equals("total_floors_completed")) {
            int totalFloors = progress.getTotalFloorsCompleted();
            int maxFloors = towers.values().stream()
                    .mapToInt(com.wynvers.quantum.towers.TowerConfig::getTotalFloors)
                    .sum();
            return totalFloors + "/" + maxFloors;
        }
        
        return "0";
    }
    
    /**
     * Handle current tower specific placeholders
     */
    private String handleCurrentTowerPlaceholder(Player player, String params, String currentTowerId, 
            com.wynvers.quantum.towers.TowerProgress progress, com.wynvers.quantum.towers.TowerManager towerManager) {
        if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
        
        Map<String, Integer> kills = progress.getCurrentKills();
        int currentKills = kills.values().stream().mapToInt(Integer::intValue).sum();
        
        if (params.equals("tower_kills_current")) {
            return Integer.toString(currentKills);
        }
        
        if (params.equals("tower_kills_required")) {
            int floor = progress.getCurrentFloor();
            return Integer.toString(getRequiredKills(currentTowerId, floor));
        }
        
        if (params.equals("tower_kills_progress")) {
            int required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            return currentKills + "/" + required;
        }
        
        if (params.equals("tower_percentage")) {
            int required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            if (required == 0) return "0";
            int percentage = (currentKills * 100) / required;
            return Integer.toString(Math.min(percentage, 100));
        }
        
        if (params.equals("tower_next_boss")) {
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0";
            int currentFloor = progress.getFloorProgress(currentTowerId);
            int nextBoss = tower.getNextBossFloor(currentFloor);
            return nextBoss == -1 ? "0" : Integer.toString(nextBoss);
        }
        
        if (params.equals("tower_status")) {
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0";
            int floor = progress.getCurrentFloor();
            if (tower.isFinalBoss(floor)) return "§c§lBOSS FINAL";
            if (tower.isBossFloor(floor)) return "§e§lBOSS D'ÉTAGE";
            return "§aEn cours";
        }
        
        return "0";
    }
    
    /**
     * Handle specific tower placeholders (tower_<id>_xxx)
     */
    private String handleSpecificTowerPlaceholder(String params, com.wynvers.quantum.towers.TowerProgress progress, 
            com.wynvers.quantum.towers.TowerManager towerManager) {
        if (params.matches("tower_[a-z_]+_progress")) {
            String towerId = params.substring(6, params.lastIndexOf("_progress"));
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(towerId);
            return completed + "/" + tower.getTotalFloors();
        }
        
        if (params.matches("tower_[a-z_]+_percentage")) {
            String towerId = params.substring(6, params.lastIndexOf("_percentage"));
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "0";
            int completed = progress.getFloorProgress(towerId);
            int total = tower.getTotalFloors();
            if (total == 0) return "0";
            double percentage = (completed * 100.0) / total;
            return String.format("%.1f", percentage);
        }
        
        if (params.matches("tower_[a-z_]+_completed")) {
            String towerId = params.substring(6, params.lastIndexOf("_completed"));
            com.wynvers.quantum.towers.TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "false";
            int completed = progress.getFloorProgress(towerId);
            boolean isCompleted = completed >= tower.getTotalFloors();
            return isCompleted ? "true" : "false";
        }
        
        return "0";
    }
    
    /**
     * Get required kills for a floor from towers.yml configuration
     * Uses caching to avoid repeated YAML file reads
     */
    private int getRequiredKills(String towerId, int floor) {
        // Check cache first - using | as delimiter to prevent collisions
        String cacheKey = towerId + "|" + floor;
        
        // Refresh cache if TTL has expired
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheRefresh > CACHE_TTL) {
            killRequirementsCache.clear();
            lastCacheRefresh = currentTime;
        }
        
        // Return cached value if available
        Integer cached = killRequirementsCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Calculate and cache
        int total = calculateRequiredKills(towerId, floor);
        killRequirementsCache.put(cacheKey, total);
        return total;
    }
    
    /**
     * Calculate required kills from YAML configuration
     */
    private int calculateRequiredKills(String towerId, int floor) {
        try {
            java.io.File towersFile = new java.io.File(plugin.getDataFolder(), "towers.yml");
            if (!towersFile.exists()) return 0;
            
            org.bukkit.configuration.file.YamlConfiguration config = 
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(towersFile);
            
            String path = "towers." + towerId + ".floors." + floor + ".spawners";
            org.bukkit.configuration.ConfigurationSection spawnersSection = config.getConfigurationSection(path);
            
            if (spawnersSection == null) return 0;
            
            int total = 0;
            for (String spawnerKey : spawnersSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection spawner = spawnersSection.getConfigurationSection(spawnerKey);
                if (spawner != null) {
                    int amount = spawner.getInt("amount", 0);
                    total += amount;
                }
            }
            
            return total;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Handle kill tracking placeholders
     */
    private String handleKillPlaceholder(Player player, String params) {
        if (plugin.getKillTracker() == null) {
            return "false";
        }
        
        // Extract mob_id and amount
        String[] parts = params.substring(7).split("_");
        if (parts.length < 2) {
            return "false";
        }
        
        // Reconstruct the mob_id (may contain underscores)
        StringBuilder mobIdBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) mobIdBuilder.append("_");
            mobIdBuilder.append(parts[i]);
        }
        String mobId = mobIdBuilder.toString();
        
        // Last element is the amount
        int requiredAmount;
        try {
            requiredAmount = Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return "false";
        }
        
        // Check if player has reached the quota
        boolean hasReached = plugin.getKillTracker().hasReachedQuota(
            player.getUniqueId(), 
            mobId, 
            requiredAmount
        );
        
        return hasReached ? "true" : "false";
    }
    
    /**
     * Handle order creation session placeholders
     * Params format: "order_<key>" (e.g., "order_item_name")
     * Session keys format: "quantum_order_<key>" (e.g., "quantum_order_item_name")
     */
    private String handleOrderPlaceholder(Player player, String params) {
        com.wynvers.quantum.orders.OrderCreationSession session = 
            plugin.getOrderCreationManager().getSession(player);
        if (session == null) {
            return "0";
        }
        
        Map<String, String> placeholders = session.getPlaceholders();
        // Reconstruct the full session key: params is "order_xxx", we need "quantum_order_xxx"
        String key = "quantum_" + params;
        return placeholders.getOrDefault(key, "0");
    }
}
