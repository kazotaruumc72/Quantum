package com.wynvers.quantum.placeholders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlaceholderAPI Expansion pour Quantum
 * Supporte les placeholders de session de création d'ordres, tracking de kills, et système de tours
 */
public class QuantumExpansion extends PlaceholderExpansion {
    
    private final Quantum plugin;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0");
    
    // Cache for tower kill requirements to avoid repeated YAML file reads
    // Key: towerId + "_" + floor, Value: required kills
    private final Map<String, Integer> killRequirementsCache = new ConcurrentHashMap<>();
    private long lastCacheRefresh = 0;
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    
    public QuantumExpansion(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "quantum";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "Wynvers";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "2.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "0";
        }
        
        // === STORAGE MODE ===
        if (params.equals("mode")) {
            return StorageMode.getMode(player).name();
        }
        
        if (params.equals("mode_display")) {
            StorageMode.Mode mode = StorageMode.getMode(player);
            switch (mode) {
                case STORAGE: return "§aSTOCKAGE";
                case SELL: return "§6VENTE";
                case RECHERCHE: return "§bRECHERCHE";
                default: return mode.name();
            }
        }
        
        // === STORAGE STATS ===
        if (params.equals("storage_items")) {
            PlayerStorage storage = plugin.getStorageManager().getStorage(player);
            return Integer.toString(storage.getUniqueItemCount());
        }
        
        if (params.equals("storage_total")) {
            PlayerStorage storage = plugin.getStorageManager().getStorage(player);
            return Integer.toString(storage.getTotalItemCount());
        }
        
        // === TOWER SYSTEM ===
        if (params.startsWith("tower_") || params.startsWith("towers_")) {            return handleTowerPlaceholder(player, params);
        }
        
        // === KILL TRACKING ===
        // Format: %quantum_killed_<mob_id>_<amount>%
        // Retourne "true" si le joueur a tué assez de mobs, "false" sinon
        if (params.startsWith("killed_")) {
            if (plugin.getKillTracker() == null) {
                return "false";
            }
            
            // Extraire mob_id et amount
            String[] parts = params.substring(7).split("_");
            if (parts.length < 2) {
                return "false";
            }
            
            // Reconstruire le mob_id (peut contenir des underscores)
            StringBuilder mobIdBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) mobIdBuilder.append("_");
                mobIdBuilder.append(parts[i]);
            }
            String mobId = mobIdBuilder.toString();
            
            // Le dernier élément est l'amount
            int requiredAmount;
            try {
                requiredAmount = Integer.parseInt(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                return "false";
            }
            
            // Vérifier si le joueur a atteint le quota
            boolean hasReached = plugin.getKillTracker().hasReachedQuota(
                player.getUniqueId(), 
                mobId, 
                requiredAmount
            );
            
            return hasReached ? "true" : "false";
        }
        
        // === ORDER CREATION SESSION ===
        // Tous les placeholders %quantum_order_*%
        if (params.startsWith("order_")) {
            OrderCreationSession session = plugin.getOrderCreationManager().getSession(player);
            if (session == null) {
                return "0";
            }
            
            Map<String, String> placeholders = session.getPlaceholders();
            String key = "quantum_" + params;
            return placeholders.getOrDefault(key, "0");
        }
        
        return "0";
    }
    
    /**
     * Handle tower-related placeholders
     * Optimized with caching to reduce repeated calculations
     */
    private String handleTowerPlaceholder(Player player, String params) {
        TowerManager towerManager = plugin.getTowerManager();
        if (towerManager == null) {
            // Pas de WorldGuard / Tours non configurées
            return "0";
        }
        
        Map<String, TowerConfig> towers = towerManager.getAllTowers();
        
        // Cas spécial : Aucune tour configurée
        if (towers.isEmpty()) {
            return "0";
        }
        
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String currentTowerId = progress.getCurrentTower();
        
        // %quantum_tower_current% - Current tower name
        if (params.equals("tower_current")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            return tower != null ? tower.getName() : "0";
        }
        
        // %quantum_tower_floor% - Current floor number
        if (params.equals("tower_floor")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            return Integer.toString(progress.getCurrentFloor());
        }
        
        // %quantum_tower_progress% - Current floor progress (5/25)
        if (params.equals("tower_progress")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0/0";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(currentTowerId);
            int total = tower.getTotalFloors();
            return completed + "/" + total;
        }
        
        // %quantum_tower_kills_current% - Total kills in current floor
        if (params.equals("tower_kills_current")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            Map<String, Integer> kills = progress.getCurrentKills();
            int sum = 0;
            for (int count : kills.values()) {
                sum += count;
            }
            return Integer.toString(sum);
        }
        
        // %quantum_tower_kills_required% - Required kills for current floor
        if (params.equals("tower_kills_required")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            int floor = progress.getCurrentFloor();
            return Integer.toString(getRequiredKills(currentTowerId, floor));
        }
        
        // %quantum_tower_kills_progress% - Kills progress (3/10)
        if (params.equals("tower_kills_progress")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0/0";
            Map<String, Integer> kills = progress.getCurrentKills();
            int current = 0;
            for (int count : kills.values()) {
                current += count;
            }
            int required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            return current + "/" + required;
        }
        
        // %quantum_tower_percentage% - Completion percentage
        if (params.equals("tower_percentage")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            Map<String, Integer> kills = progress.getCurrentKills();
            int current = 0;
            for (int count : kills.values()) {
                current += count;
            }
            int required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            if (required == 0) return "0";
            int percentage = (current * 100) / required;
            return Integer.toString(Math.min(percentage, 100));
        }
        
        // %quantum_tower_next_boss% - Next boss floor
        if (params.equals("tower_next_boss")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0";
            int currentFloor = progress.getFloorProgress(currentTowerId);
            int nextBoss = tower.getNextBossFloor(currentFloor);
            return nextBoss == -1 ? "0" : Integer.toString(nextBoss);
        }
        
        // %quantum_tower_status% - Current status
        if (params.equals("tower_status")) {
            if (currentTowerId == null || currentTowerId.isEmpty()) return "0";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0";
            int floor = progress.getCurrentFloor();
            if (tower.isFinalBoss(floor)) return "§c§lBOSS FINAL";
            if (tower.isBossFloor(floor)) return "§e§lBOSS D'ÉTAGE";
            return "§aEn cours";
        }
        
        // %quantum_tower_<id>_progress% - Specific tower progress (5/25)
        if (params.matches("tower_[a-z_]+_progress")) {
            String towerId = params.substring(6, params.lastIndexOf("_progress"));
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(towerId);
            return completed + "/" + tower.getTotalFloors();
        }
        
        // %quantum_tower_<id>_percentage% - Specific tower completion percentage (20.5%)
        if (params.matches("tower_[a-z_]+_percentage")) {
            String towerId = params.substring(6, params.lastIndexOf("_percentage"));
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "0";
            int completed = progress.getFloorProgress(towerId);
            int total = tower.getTotalFloors();
            if (total == 0) return "0";
            double percentage = (completed * 100.0) / total;
            return percentFormat.format(percentage);
        }
        
        // %quantum_tower_<id>_completed% - Is specific tower completed? (true/false)
        if (params.matches("tower_[a-z_]+_completed")) {
            String towerId = params.substring(6, params.lastIndexOf("_completed"));
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "false";
            int completed = progress.getFloorProgress(towerId);
            boolean isCompleted = completed >= tower.getTotalFloors();
            return isCompleted ? "true" : "false";
        }
        
        // %quantum_towers_completed% - Number of completed towers (format: "2" or "Aucune tour configurée")
        if (params.equals("towers_completed")) {
            if (towers.isEmpty()) return "0";
            int completed = progress.getCompletedTowersCount(towers);
            return Integer.toString(completed);
        }
        
        // %quantum_towers_total% - Total number of towers configured (format: "4")
        if (params.equals("towers_total")) {
            return Integer.toString(towers.size());
        }
        
        // %quantum_towers_percentage% - Overall towers completion percentage (50.0 or 0.0)
        if (params.equals("towers_percentage")) {
            if (towers.isEmpty()) return "0";
            
            int totalFloors = 0;
            int completedFloors = 0;
            
            for (Map.Entry<String, TowerConfig> entry : towers.entrySet()) {
                String towerId = entry.getKey();
                TowerConfig tower = entry.getValue();
                totalFloors += tower.getTotalFloors();
                completedFloors += progress.getFloorProgress(towerId);
            }
            
            if (totalFloors == 0) return "0";
            double percentage = (completedFloors * 100.0) / totalFloors;
            return percentFormat.format(percentage);
        }
        
        // %quantum_total_floors_completed% - Total floors across all towers (45/100)
        if (params.equals("total_floors_completed")) {
            int totalFloors = progress.getTotalFloorsCompleted();
            int maxFloors = towers.values().stream()
                    .mapToInt(TowerConfig::getTotalFloors)
                    .sum();
            return totalFloors + "/" + maxFloors;
        }
        
        return "0";
    }
    
    /**
     * Get required kills for a floor from towers.yml configuration
     * Additionne le 'amount' de tous les spawners de l'étage
     * Uses caching to avoid repeated YAML file reads
     */
    private int getRequiredKills(String towerId, int floor) {
        // Check cache first
        String cacheKey = towerId + "_" + floor;
        
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
            // Charger depuis towers.yml au lieu de zones.yml
            File towersFile = new File(plugin.getDataFolder(), "towers.yml");
            if (!towersFile.exists()) return 0;
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
            
            // Récupérer le chemin : towers.<towerId>.floors.<floor>.spawners
            String path = "towers." + towerId + ".floors." + floor + ".spawners";
            ConfigurationSection spawnersSection = config.getConfigurationSection(path);
            
            if (spawnersSection == null) return 0;
            
            // Calculer le total de mobs à tuer (somme des 'amount' de tous les spawners)
            int total = 0;
            for (String spawnerKey : spawnersSection.getKeys(false)) {
                ConfigurationSection spawner = spawnersSection.getConfigurationSection(spawnerKey);
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
}
