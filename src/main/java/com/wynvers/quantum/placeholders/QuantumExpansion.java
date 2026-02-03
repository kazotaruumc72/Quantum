package com.wynvers.quantum.placeholders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import com.wynvers.quantum.worldguard.ZoneManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * PlaceholderAPI Expansion pour Quantum
 * Supporte les placeholders de session de création d'ordres, tracking de kills, et système de tours
 */
public class QuantumExpansion extends PlaceholderExpansion {
    
    private final Quantum plugin;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
    
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
            return "";
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
            return String.valueOf(storage.getUniqueItemCount());
        }
        
        if (params.equals("storage_total")) {
            PlayerStorage storage = plugin.getStorageManager().getStorage(player);
            return String.valueOf(storage.getTotalItemCount());
        }
        
        // === TOWER SYSTEM ===
        if (params.startsWith("tower_")) {
            return handleTowerPlaceholder(player, params);
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
                return "";
            }
            
            Map<String, String> placeholders = session.getPlaceholders();
            String key = "quantum_" + params;
            return placeholders.getOrDefault(key, "");
        }
        
        return null;
    }
    
    /**
     * Handle tower-related placeholders
     */
    private String handleTowerPlaceholder(Player player, String params) {
        TowerManager towerManager = plugin.getTowerManager();
        if (towerManager == null) {
            return "";
        }
        
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String currentTowerId = progress.getCurrentTower();
        
        // %quantum_tower_current% - Current tower name
        if (params.equals("tower_current")) {
            if (currentTowerId == null) return "Aucune";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            return tower != null ? tower.getName() : "Inconnue";
        }
        
        // %quantum_tower_floor% - Current floor number
        if (params.equals("tower_floor")) {
            return String.valueOf(progress.getCurrentFloor());
        }
        
        // %quantum_tower_progress% - Current floor progress (5/25)
        if (params.equals("tower_progress")) {
            if (currentTowerId == null) return "0/0";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(currentTowerId);
            return completed + "/" + tower.getTotalFloors();
        }
        
        // %quantum_tower_kills_current% - Total kills in current floor
        if (params.equals("tower_kills_current")) {
            Map<String, Integer> kills = progress.getCurrentKills();
            return String.valueOf(kills.values().stream().mapToInt(Integer::intValue).sum());
        }
        
        // %quantum_tower_kills_required% - Required kills for current floor
        if (params.equals("tower_kills_required")) {
            if (currentTowerId == null) return "0";
            int floor = progress.getCurrentFloor();
            return String.valueOf(getRequiredKills(currentTowerId, floor));
        }
        
        // %quantum_tower_kills_progress% - Kills progress (3/10)
        if (params.equals("tower_kills_progress")) {
            Map<String, Integer> kills = progress.getCurrentKills();
            int current = kills.values().stream().mapToInt(Integer::intValue).sum();
            int required = 0;
            if (currentTowerId != null) {
                required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            }
            return current + "/" + required;
        }
        
        // %quantum_tower_percentage% - Completion percentage
        if (params.equals("tower_percentage")) {
            if (currentTowerId == null) return "0%";
            Map<String, Integer> kills = progress.getCurrentKills();
            int current = kills.values().stream().mapToInt(Integer::intValue).sum();
            int required = getRequiredKills(currentTowerId, progress.getCurrentFloor());
            if (required == 0) return "100%";
            int percentage = (current * 100) / required;
            return Math.min(percentage, 100) + "%";
        }
        
        // %quantum_tower_next_boss% - Next boss floor
        if (params.equals("tower_next_boss")) {
            if (currentTowerId == null) return "Aucun";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "Aucun";
            int currentFloor = progress.getFloorProgress(currentTowerId);
            int nextBoss = tower.getNextBossFloor(currentFloor);
            return nextBoss == -1 ? "Aucun" : "Étage " + nextBoss;
        }
        
        // %quantum_tower_status% - Current status
        if (params.equals("tower_status")) {
            if (currentTowerId == null) return "Hors tour";
            TowerConfig tower = towerManager.getTower(currentTowerId);
            if (tower == null) return "Inconnu";
            int floor = progress.getCurrentFloor();
            if (tower.isFinalBoss(floor)) return "§c§lBOSS FINAL";
            if (tower.isBossFloor(floor)) return "§e§lBOSS D'ÉTAGE";
            return "§aEn cours";
        }
        
        // %quantum_tower_<id>_progress% - Specific tower progress
        if (params.matches("tower_[a-z_]+_progress")) {
            String towerId = params.substring(6, params.lastIndexOf("_progress"));
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) return "0/0";
            int completed = progress.getFloorProgress(towerId);
            return completed + "/" + tower.getTotalFloors();
        }
        
        // %quantum_towers_completed% - Number of completed towers
        if (params.equals("towers_completed")) {
            Map<String, TowerConfig> towers = towerManager.getAllTowers();
            int completed = progress.getCompletedTowersCount(towers);
            return completed + "/" + towers.size();
        }
        
        // %quantum_total_floors_completed% - Total floors across all towers
        if (params.equals("total_floors_completed")) {
            int totalFloors = progress.getTotalFloorsCompleted();
            Map<String, TowerConfig> towers = towerManager.getAllTowers();
            int maxFloors = towers.values().stream()
                    .mapToInt(TowerConfig::getTotalFloors)
                    .sum();
            return totalFloors + "/" + maxFloors;
        }
        
        return "";
    }
    
    /**
     * Get required kills for a floor from zone configuration
     */
    private int getRequiredKills(String towerId, int floor) {
        ZoneManager zoneManager = plugin.getZoneManager();
        if (zoneManager == null) return 0;
        
        String regionName = towerId + "_floor_" + floor;
        ZoneManager.ZoneRestriction zone = zoneManager.getZone(regionName);
        if (zone == null) return 0;
        
        List<ZoneManager.MobRequirement> requirements = zone.getRequirements();
        return requirements.stream()
                .mapToInt(ZoneManager.MobRequirement::getAmount)
                .sum();
    }
}
