package com.wynvers.quantum.statistics;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Gestionnaire des statistiques globales du storage
 * Track le nombre total d'items stockés et vendus depuis le storage
 */
public class StorageStatsManager {
    
    private final Quantum plugin;
    
    public StorageStatsManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Incrémenter le nombre d'items ajoutés au storage
     * @param amount Quantité ajoutée
     */
    public void incrementItemsStored(long amount) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "UPDATE storage_stats SET stat_value = stat_value + ? WHERE stat_key = 'total_items_stored'";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setLong(1, amount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to increment items stored: " + e.getMessage());
        }
    }
    
    /**
     * Incrémenter le nombre d'items vendus depuis le storage
     * @param amount Quantité vendue
     */
    public void incrementItemsSold(long amount) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "UPDATE storage_stats SET stat_value = stat_value + ? WHERE stat_key = 'total_items_sold'";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setLong(1, amount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to increment items sold: " + e.getMessage());
        }
    }
    
    /**
     * Récupérer le nombre total d'items stockés historiquement
     * @return Nombre total d'items stockés depuis toujours
     */
    public long getTotalItemsStored() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "SELECT stat_value FROM storage_stats WHERE stat_key = 'total_items_stored'";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("stat_value");
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get total items stored: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Récupérer le nombre total d'items vendus depuis le storage
     * @return Nombre total d'items vendus
     */
    public long getTotalItemsSold() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "SELECT stat_value FROM storage_stats WHERE stat_key = 'total_items_sold'";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("stat_value");
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get total items sold: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Récupérer le nombre actuel d'items en stock (de tous les joueurs)
     * @return Nombre d'items actuellement stockés
     */
    public long getCurrentStoredItems() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "SELECT SUM(amount) as total FROM player_storage";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get current stored items: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Calculer le nombre d'items stockés pour un joueur spécifique
     * @param uuid UUID du joueur
     * @return Nombre d'items en stock pour ce joueur
     */
    public long getPlayerStoredItems(UUID uuid) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(uuid);
        if (storage != null) {
            return storage.getTotalItemCount();
        }
        return 0;
    }
    
    /**
     * Récupérer toutes les statistiques du storage
     * @return Objet contenant toutes les stats
     */
    public StorageStats getStorageStats() {
        return new StorageStats(
            getTotalItemsStored(),
            getTotalItemsSold(),
            getCurrentStoredItems()
        );
    }
    
    /**
     * Reset toutes les statistiques (ADMIN SEULEMENT)
     * @param resetHistorical Si true, reset aussi les stats historiques (items stockés/vendus)
     */
    public void resetStats(boolean resetHistorical) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            if (resetHistorical) {
                String query = "UPDATE storage_stats SET stat_value = 0";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.executeUpdate();
                }
                plugin.getQuantumLogger().success("✓ Toutes les statistiques de storage ont été réinitialisées");
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to reset storage stats: " + e.getMessage());
        }
    }
    
    /**
     * Classe pour stocker les statistiques du storage
     */
    public static class StorageStats {
        public final long totalItemsStored;     // Total historique d'items ajoutés
        public final long totalItemsSold;       // Total d'items vendus depuis le storage
        public final long currentStoredItems;   // Nombre actuel d'items en stock
        
        public StorageStats(long totalItemsStored, long totalItemsSold, long currentStoredItems) {
            this.totalItemsStored = totalItemsStored;
            this.totalItemsSold = totalItemsSold;
            this.currentStoredItems = currentStoredItems;
        }
    }
}
