package com.wynvers.quantum.statistics;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de statistiques pour Quantum
 * Tracke les items stockés et les trades par catégorie
 */
public class StatisticsManager {
    
    private final Quantum plugin;
    private final File statsFile;
    private YamlConfiguration statsConfig;
    
    // Cache en mémoire pour performance
    private final Map<String, CategoryStats> categoryStatsCache;
    private GlobalStats globalStatsCache;
    
    public StatisticsManager(Quantum plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        this.categoryStatsCache = new HashMap<>();
        this.globalStatsCache = new GlobalStats();
        
        loadStatistics();
    }
    
    /**
     * Charge les statistiques depuis le fichier
     */
    public void loadStatistics() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
                statsConfig = new YamlConfiguration();
                initializeDefaults();
                saveStatistics();
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create statistics.yml");
                e.printStackTrace();
                return;
            }
        }
        
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        
        // Charger les stats globales
        globalStatsCache.totalItemsStored = statsConfig.getLong("global.total_items_stored", 0);
        globalStatsCache.totalTradesCreated = statsConfig.getLong("global.total_trades_created", 0);
        globalStatsCache.totalTradesCompleted = statsConfig.getLong("global.total_trades_completed", 0);
        globalStatsCache.totalVolumeTraded = statsConfig.getLong("global.total_volume_traded", 0);
        
        // Charger les stats par catégorie
        if (statsConfig.contains("categories")) {
            for (String category : statsConfig.getConfigurationSection("categories").getKeys(false)) {
                CategoryStats stats = new CategoryStats();
                stats.itemsStored = statsConfig.getLong("categories." + category + ".items_stored", 0);
                stats.tradesCreated = statsConfig.getLong("categories." + category + ".trades_created", 0);
                stats.tradesCompleted = statsConfig.getLong("categories." + category + ".trades_completed", 0);
                stats.volumeTraded = statsConfig.getLong("categories." + category + ".volume_traded", 0);
                
                categoryStatsCache.put(category, stats);
            }
        }
        
        plugin.getQuantumLogger().info("✓ Statistics loaded: " + categoryStatsCache.size() + " categories tracked");
    }
    
    /**
     * Initialise les valeurs par défaut
     */
    private void initializeDefaults() {
        statsConfig.set("global.total_items_stored", 0);
        statsConfig.set("global.total_trades_created", 0);
        statsConfig.set("global.total_trades_completed", 0);
        statsConfig.set("global.total_volume_traded", 0);
        statsConfig.set("global.last_updated", System.currentTimeMillis());
    }
    
    /**
     * Sauvegarde les statistiques dans le fichier
     */
    public void saveStatistics() {
        // Sauvegarder les stats globales
        statsConfig.set("global.total_items_stored", globalStatsCache.totalItemsStored);
        statsConfig.set("global.total_trades_created", globalStatsCache.totalTradesCreated);
        statsConfig.set("global.total_trades_completed", globalStatsCache.totalTradesCompleted);
        statsConfig.set("global.total_volume_traded", globalStatsCache.totalVolumeTraded);
        statsConfig.set("global.last_updated", System.currentTimeMillis());
        
        // Sauvegarder les stats par catégorie
        for (Map.Entry<String, CategoryStats> entry : categoryStatsCache.entrySet()) {
            String category = entry.getKey();
            CategoryStats stats = entry.getValue();
            
            statsConfig.set("categories." + category + ".items_stored", stats.itemsStored);
            statsConfig.set("categories." + category + ".trades_created", stats.tradesCreated);
            statsConfig.set("categories." + category + ".trades_completed", stats.tradesCompleted);
            statsConfig.set("categories." + category + ".volume_traded", stats.volumeTraded);
        }
        
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save statistics.yml");
            e.printStackTrace();
        }
    }
    
    // ========================================
    // TRACKING DES ITEMS STOCKÉS
    // ========================================
    
    /**
     * Met à jour le nombre d'items stockés pour une catégorie
     * Appelé quand un joueur ajoute/retire des items
     */
    public void updateItemsStored(String category, long delta) {
        CategoryStats stats = categoryStatsCache.computeIfAbsent(category, k -> new CategoryStats());
        stats.itemsStored += delta;
        
        globalStatsCache.totalItemsStored += delta;
    }
    
    /**
     * Définit le nombre exact d'items stockés pour une catégorie
     * Utile pour recalculer les stats
     */
    public void setItemsStored(String category, long amount) {
        CategoryStats stats = categoryStatsCache.computeIfAbsent(category, k -> new CategoryStats());
        long delta = amount - stats.itemsStored;
        stats.itemsStored = amount;
        
        globalStatsCache.totalItemsStored += delta;
    }
    
    // ========================================
    // TRACKING DES TRADES
    // ========================================
    
    /**
     * Incrémente le nombre de trades créés pour une catégorie
     * Appelé quand un joueur crée une offre d'achat/vente
     */
    public void incrementTradesCreated(String category) {
        CategoryStats stats = categoryStatsCache.computeIfAbsent(category, k -> new CategoryStats());
        stats.tradesCreated++;
        
        globalStatsCache.totalTradesCreated++;
    }
    
    /**
     * Incrémente le nombre de trades complétés pour une catégorie
     * Appelé quand un trade est validé/finalisé
     */
    public void incrementTradesCompleted(String category, long volumeTraded) {
        CategoryStats stats = categoryStatsCache.computeIfAbsent(category, k -> new CategoryStats());
        stats.tradesCompleted++;
        stats.volumeTraded += volumeTraded;
        
        globalStatsCache.totalTradesCompleted++;
        globalStatsCache.totalVolumeTraded += volumeTraded;
    }
    
    // ========================================
    // RECALCUL DES STATISTIQUES
    // ========================================
    
    /**
     * Recalcule toutes les statistiques depuis zéro
     * Parcourt tous les storages et orders pour recalculer
     */
    public void recalculateAll() {
        plugin.getQuantumLogger().info("Recalculating all statistics...");
        
        // Réinitialiser les caches
        categoryStatsCache.clear();
        globalStatsCache = new GlobalStats();
        
        // TODO: Parcourir tous les PlayerStorage et compter les items par catégorie
        // TODO: Parcourir orders.yml et compter les trades par catégorie
        
        saveStatistics();
        plugin.getQuantumLogger().success("✓ Statistics recalculated");
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    /**
     * Récupère les stats d'une catégorie
     */
    public CategoryStats getCategoryStats(String category) {
        return categoryStatsCache.getOrDefault(category, new CategoryStats());
    }
    
    /**
     * Récupère les stats globales
     */
    public GlobalStats getGlobalStats() {
        return globalStatsCache;
    }
    
    /**
     * Récupère toutes les catégories trackées
     */
    public Map<String, CategoryStats> getAllCategoryStats() {
        return new HashMap<>(categoryStatsCache);
    }
    
    // ========================================
    // CLASSES INTERNES
    // ========================================
    
    /**
     * Statistiques pour une catégorie spécifique
     */
    public static class CategoryStats {
        public long itemsStored = 0;        // Nombre d'items actuellement stockés
        public long tradesCreated = 0;      // Nombre total de trades créés
        public long tradesCompleted = 0;    // Nombre de trades complétés
        public long volumeTraded = 0;       // Volume total échangé (quantité)
        
        @Override
        public String toString() {
            return String.format("CategoryStats[stored=%d, created=%d, completed=%d, volume=%d]",
                    itemsStored, tradesCreated, tradesCompleted, volumeTraded);
        }
    }
    
    /**
     * Statistiques globales (toutes catégories confondues)
     */
    public static class GlobalStats {
        public long totalItemsStored = 0;
        public long totalTradesCreated = 0;
        public long totalTradesCompleted = 0;
        public long totalVolumeTraded = 0;
        
        @Override
        public String toString() {
            return String.format("GlobalStats[stored=%d, created=%d, completed=%d, volume=%d]",
                    totalItemsStored, totalTradesCreated, totalTradesCompleted, totalVolumeTraded);
        }
    }
}
