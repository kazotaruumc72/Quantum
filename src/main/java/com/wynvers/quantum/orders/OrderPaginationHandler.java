package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Gère la pagination des menus d'ordres
 * 
 * Fonctionnalités:
 * - Navigation entre plusieurs pages (Previous/Next)
 * - 21 ordres par page (slots 10-34 dans un inventaire 6 lignes)
 * - Cache des pages par joueur et par catégorie
 * - Auto-refresh quand un ordre est vendu
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class OrderPaginationHandler {
    
    private final Quantum plugin;
    
    // Nombre d'ordres par page (3 rangées de 7 items)
    private static final int ORDERS_PER_PAGE = 21;
    
    // Slots pour les ordres (rangées 2-4 : slots 10-16, 19-25, 28-34)
    private static final int[] ORDER_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,  // Rangée 2
        19, 20, 21, 22, 23, 24, 25,  // Rangée 3
        28, 29, 30, 31, 32, 33, 34   // Rangée 4
    };
    
    // Cache des pages par joueur (playerUUID -> category -> currentPage)
    private final Map<UUID, Map<String, Integer>> playerPages = new HashMap<>();
    
    public OrderPaginationHandler(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Récupère le numéro de page actuel pour un joueur et une catégorie
     * 
     * @param player Joueur
     * @param category Catégorie (cultures, loots, etc.)
     * @return Numéro de page (démarre à 0)
     */
    public int getCurrentPage(Player player, String category) {
        return playerPages
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .getOrDefault(category, 0);
    }
    
    /**
     * Définit la page actuelle pour un joueur et une catégorie
     */
    public void setCurrentPage(Player player, String category, int page) {
        playerPages
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(category, Math.max(0, page));
    }
    
    /**
     * Réinitialise la page à 0 pour une catégorie
     */
    public void resetPage(Player player, String category) {
        setCurrentPage(player, category, 0);
    }
    
    /**
     * Nettoie le cache pour un joueur (déconnexion)
     */
    public void clearCache(Player player) {
        playerPages.remove(player.getUniqueId());
    }
    
    /**
     * Passe à la page suivante
     * 
     * @return true si changement effectué, false si déjà sur la dernière page
     */
    public boolean nextPage(Player player, String category) {
        int currentPage = getCurrentPage(player, category);
        int totalPages = getTotalPages(category);
        
        if (currentPage >= totalPages - 1) {
            player.sendMessage("§cVous êtes déjà sur la dernière page!");
            return false;
        }
        
        setCurrentPage(player, category, currentPage + 1);
        return true;
    }
    
    /**
     * Passe à la page précédente
     * 
     * @return true si changement effectué, false si déjà sur la première page
     */
    public boolean previousPage(Player player, String category) {
        int currentPage = getCurrentPage(player, category);
        
        if (currentPage <= 0) {
            player.sendMessage("§cVous êtes déjà sur la première page!");
            return false;
        }
        
        setCurrentPage(player, category, currentPage - 1);
        return true;
    }
    
    /**
     * Calcule le nombre total de pages pour une catégorie
     */
    public int getTotalPages(String category) {
        int totalOrders = getTotalOrders(category);
        return Math.max(1, (int) Math.ceil((double) totalOrders / ORDERS_PER_PAGE));
    }
    
    /**
     * Compte le nombre total d'ordres ACTIFS dans une catégorie
     */
    private int getTotalOrders(String category) {
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) return 0;
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        
        if (!ordersConfig.contains(category)) return 0;
        
        Set<String> orderIds = ordersConfig.getConfigurationSection(category).getKeys(false);
        
        // Compter uniquement les ordres ACTIFS
        int count = 0;
        for (String orderId : orderIds) {
            String status = ordersConfig.getString(category + "." + orderId + ".status", "ACTIVE");
            if ("ACTIVE".equals(status)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Récupère les ordres ACTIFS pour une page spécifique
     * 
     * @param category Catégorie
     * @param page Numéro de page (démarre à 0)
     * @return Liste des orderIds pour cette page
     */
    public List<String> getOrdersForPage(String category, int page) {
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) return new ArrayList<>();
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        
        if (!ordersConfig.contains(category)) return new ArrayList<>();
        
        Set<String> orderIds = ordersConfig.getConfigurationSection(category).getKeys(false);
        
        // Filtrer les ordres ACTIFS et les trier par timestamp
        List<OrderEntry> activeOrders = new ArrayList<>();
        for (String orderId : orderIds) {
            String path = category + "." + orderId;
            String status = ordersConfig.getString(path + ".status", "ACTIVE");
            
            if ("ACTIVE".equals(status)) {
                long timestamp = ordersConfig.getLong(path + ".timestamp", 0);
                activeOrders.add(new OrderEntry(orderId, timestamp));
            }
        }
        
        // Trier par timestamp (plus récent en premier)
        activeOrders.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        
        // Pagination
        int start = page * ORDERS_PER_PAGE;
        int end = Math.min(start + ORDERS_PER_PAGE, activeOrders.size());
        
        if (start >= activeOrders.size()) return new ArrayList<>();
        
        List<String> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(activeOrders.get(i).orderId);
        }
        
        return result;
    }
    
    /**
     * Récupère les slots à utiliser pour les ordres
     */
    public static int[] getOrderSlots() {
        return ORDER_SLOTS;
    }
    
    /**
     * Obtient le placeholder pour la page actuelle
     * Format: "Page X/Y"
     */
    public String getPagePlaceholder(Player player, String category) {
        int currentPage = getCurrentPage(player, category) + 1; // +1 pour affichage (commence à 1)
        int totalPages = getTotalPages(category);
        return currentPage + "/" + totalPages;
    }
    
    /**
     * Obtient le placeholder pour le nombre d'ordres total
     */
    public String getTotalOrdersPlaceholder(String category) {
        return String.valueOf(getTotalOrders(category));
    }
    
    /**
     * Classe interne pour stocker un ordre avec son timestamp
     */
    private static class OrderEntry {
        final String orderId;
        final long timestamp;
        
        OrderEntry(String orderId, long timestamp) {
            this.orderId = orderId;
            this.timestamp = timestamp;
        }
    }
}
