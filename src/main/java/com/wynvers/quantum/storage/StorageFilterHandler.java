package com.wynvers.quantum.storage;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère les filtres et la recherche dans le storage
 * 
 * Fonctionnalités:
 * - Recherche par nom d'item (partielle, insensible à la casse)
 * - Filtrage par type (Nexo / Minecraft)
 * - Tri par quantité (croissant / décroissant)
 * - Tri alphabétique
 * - Cache des filtres actifs par joueur
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class StorageFilterHandler {
    
    private final Quantum plugin;
    
    // Cache des filtres actifs par joueur
    private final Map<UUID, FilterSettings> activeFilters = new HashMap<>();
    
    public StorageFilterHandler(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Récupère les paramètres de filtre pour un joueur
     */
    public FilterSettings getFilterSettings(Player player) {
        return activeFilters.computeIfAbsent(player.getUniqueId(), k -> new FilterSettings());
    }
    
    /**
     * Applique un filtre de recherche par nom
     */
    public void setSearchQuery(Player player, String query) {
        FilterSettings settings = getFilterSettings(player);
        settings.searchQuery = query == null ? "" : query.toLowerCase();
    }
    
    /**
     * Définit le filtre de type
     */
    public void setTypeFilter(Player player, ItemTypeFilter filter) {
        getFilterSettings(player).typeFilter = filter;
    }
    
    /**
     * Définit le mode de tri
     */
    public void setSortMode(Player player, SortMode mode) {
        getFilterSettings(player).sortMode = mode;
    }
    
    /**
     * Réinitialise tous les filtres pour un joueur
     */
    public void resetFilters(Player player) {
        activeFilters.put(player.getUniqueId(), new FilterSettings());
    }
    
    /**
     * Nettoie le cache pour un joueur (déconnexion)
     */
    public void clearCache(Player player) {
        activeFilters.remove(player.getUniqueId());
    }
    
    /**
     * Applique les filtres et trie les items du storage
     * 
     * @param storage Storage du joueur
     * @param player Joueur (pour récupérer les filtres)
     * @return Liste filtrée et triée des entrées du storage
     */
    public List<StorageEntry> applyFilters(PlayerStorage storage, Player player) {
        FilterSettings settings = getFilterSettings(player);
        
        // Récupérer tous les items avec la nouvelle méthode getAllStorageItems()
        Map<String, Integer> items = storage.getAllStorageItems();
        List<StorageEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            entries.add(new StorageEntry(entry.getKey(), entry.getValue()));
        }
        
        // Appliquer les filtres
        List<StorageEntry> filtered = entries.stream()
            .filter(entry -> matchesSearch(entry, settings.searchQuery))
            .filter(entry -> matchesTypeFilter(entry, settings.typeFilter))
            .collect(Collectors.toList());
        
        // Appliquer le tri
        sortEntries(filtered, settings.sortMode);
        
        return filtered;
    }
    
    /**
     * Vérifie si un item correspond à la recherche
     */
    private boolean matchesSearch(StorageEntry entry, String query) {
        if (query == null || query.isEmpty()) return true;
        
        String itemName = formatItemName(entry.itemId).toLowerCase();
        return itemName.contains(query);
    }
    
    /**
     * Vérifie si un item correspond au filtre de type
     */
    private boolean matchesTypeFilter(StorageEntry entry, ItemTypeFilter filter) {
        switch (filter) {
            case ALL:
                return true;
            case NEXO_ONLY:
                return entry.itemId.startsWith("nexo:");
            case MINECRAFT_ONLY:
                return entry.itemId.startsWith("minecraft:");
            default:
                return true;
        }
    }
    
    /**
     * Trie la liste d'entrées selon le mode de tri
     */
    private void sortEntries(List<StorageEntry> entries, SortMode mode) {
        switch (mode) {
            case QUANTITY_DESC:
                entries.sort((a, b) -> Integer.compare(b.quantity, a.quantity));
                break;
            case QUANTITY_ASC:
                entries.sort(Comparator.comparingInt(e -> e.quantity));
                break;
            case ALPHABETICAL:
                entries.sort(Comparator.comparing(e -> formatItemName(e.itemId)));
                break;
            case RECENT:
                // Par défaut, garde l'ordre d'insertion
                break;
        }
    }
    
    /**
     * Formate le nom d'un item pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId.replace("_", " ");
    }
    
    /**
     * Obtient un placeholder pour afficher les filtres actifs
     */
    public String getFilterStatusPlaceholder(Player player) {
        FilterSettings settings = getFilterSettings(player);
        List<String> activeFiltersList = new ArrayList<>();
        
        if (!settings.searchQuery.isEmpty()) {
            activeFiltersList.add("§eRecherche: " + settings.searchQuery);
        }
        
        if (settings.typeFilter != ItemTypeFilter.ALL) {
            activeFiltersList.add("§eType: " + settings.typeFilter.getDisplayName());
        }
        
        if (settings.sortMode != SortMode.RECENT) {
            activeFiltersList.add("§eTri: " + settings.sortMode.getDisplayName());
        }
        
        if (activeFiltersList.isEmpty()) {
            return "§7Aucun filtre actif";
        }
        
        return String.join("§7, ", activeFiltersList);
    }
    
    // ============================================================
    // CLASSES INTERNES
    // ============================================================
    
    /**
     * Entrée de storage (itemId + quantité)
     */
    public static class StorageEntry {
        public final String itemId;
        public final int quantity;
        
        public StorageEntry(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }
    }
    
    /**
     * Paramètres de filtre pour un joueur
     */
    public static class FilterSettings {
        public String searchQuery = "";
        public ItemTypeFilter typeFilter = ItemTypeFilter.ALL;
        public SortMode sortMode = SortMode.RECENT;
    }
    
    /**
     * Types de filtres par type d'item
     */
    public enum ItemTypeFilter {
        ALL("§fTous"),
        NEXO_ONLY("§bNexo uniquement"),
        MINECRAFT_ONLY("§6Minecraft uniquement");
        
        private final String displayName;
        
        ItemTypeFilter(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Modes de tri
     */
    public enum SortMode {
        RECENT("§7Récent"),
        QUANTITY_DESC("§eQuantité (décroissant)"),
        QUANTITY_ASC("§eQuantité (croissant)"),
        ALPHABETICAL("§bAlphabétique");
        
        private final String displayName;
        
        SortMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
