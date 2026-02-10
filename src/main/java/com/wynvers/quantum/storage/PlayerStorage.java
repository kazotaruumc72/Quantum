package com.wynvers.quantum.storage;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStorage {
    
    private final UUID uuid;
    private final Map<Material, Integer> vanillaItems;
    private final Map<String, Integer> nexoItems;
    
    public PlayerStorage(UUID uuid) {
        this.uuid = uuid;
        this.vanillaItems = new ConcurrentHashMap<>();
        this.nexoItems = new ConcurrentHashMap<>();
    }
    
    // === VANILLA ITEMS ===
    
    /**
     * Add item without limit checking (for internal use)
     */
    private void addItemInternal(Material material, int amount) {
        vanillaItems.merge(material, amount, Integer::sum);
    }
    
    /**
     * Add item with limit checking and messaging
     * @param plugin Quantum plugin instance
     * @param player Player adding the item
     * @param material Material type
     * @param amount Amount to add
     * @return true if successful, false if limit reached
     */
    public boolean addItem(Quantum plugin, org.bukkit.entity.Player player, Material material, int amount) {
        int currentAmount = getAmount(material);
        int newAmount = currentAmount + amount;
        int limit = plugin.getStorageUpgradeManager().getMaxStacks(
            plugin.getStorageUpgradeManager().getState(player)
        );
        
        if (newAmount > limit) {
            // Send both limit messages
            String itemDisplayName = formatMaterialName(material);
            player.sendMessage("§cL'item " + itemDisplayName + " §cest arrivé à la limite de " + limit);
            player.sendMessage("§cVous ne pouver pas stocker plus de " + limit + " §citems pour: " + itemDisplayName);
            return false;
        }
        
        addItemInternal(material, amount);
        return true;
    }
    
    /**
     * Add item without limit checking (legacy method for backwards compatibility)
     */
    public void addItem(Material material, int amount) {
        addItemInternal(material, amount);
    }
    
    public void removeItem(Material material, int amount) {
        vanillaItems.computeIfPresent(material, (m, current) -> {
            int newAmount = current - amount;
            return newAmount <= 0 ? null : newAmount;
        });
    }
    
    public int getAmount(Material material) {
        return vanillaItems.getOrDefault(material, 0);
    }
    
    public boolean hasItem(Material material, int amount) {
        return getAmount(material) >= amount;
    }
    
    public Map<Material, Integer> getVanillaItems() {
        return vanillaItems;
    }
    
    // === NEXO ITEMS ===
    
    /**
     * Add Nexo item without limit checking (for internal use)
     */
    private void addNexoItemInternal(String nexoId, int amount) {
        nexoItems.merge(nexoId, amount, Integer::sum);
    }
    
    /**
     * Add Nexo item with limit checking and messaging
     * @param plugin Quantum plugin instance
     * @param player Player adding the item
     * @param nexoId Nexo item ID
     * @param amount Amount to add
     * @return true if successful, false if limit reached
     */
    public boolean addNexoItem(Quantum plugin, org.bukkit.entity.Player player, String nexoId, int amount) {
        int currentAmount = getNexoAmount(nexoId);
        int newAmount = currentAmount + amount;
        int limit = plugin.getStorageUpgradeManager().getMaxStacks(
            plugin.getStorageUpgradeManager().getState(player)
        );
        
        if (newAmount > limit) {
            // Send both limit messages
            String itemDisplayName = nexoId;
            player.sendMessage("§cL'item " + itemDisplayName + " §cest arrivé à la limite de " + limit);
            player.sendMessage("§cVous ne pouver pas stocker plus de " + limit + " §citems pour: " + itemDisplayName);
            return false;
        }
        
        addNexoItemInternal(nexoId, amount);
        return true;
    }
    
    /**
     * Add Nexo item without limit checking (legacy method for backwards compatibility)
     */
    public void addNexoItem(String nexoId, int amount) {
        addNexoItemInternal(nexoId, amount);
    }
    
    public void removeNexoItem(String nexoId, int amount) {
        nexoItems.computeIfPresent(nexoId, (id, current) -> {
            int newAmount = current - amount;
            return newAmount <= 0 ? null : newAmount;
        });
    }
    
    public int getNexoAmount(String nexoId) {
        return nexoItems.getOrDefault(nexoId, 0);
    }
    
    public boolean hasNexoItem(String nexoId, int amount) {
        return getNexoAmount(nexoId) >= amount;
    }
    
    public Map<String, Integer> getNexoItems() {
        return nexoItems;
    }
    
    // === COMBINED ITEMS ACCESS ===
    
    /**
     * Récupère tous les items (Minecraft + Nexo) sous forme unifiée
     * Format des clés: "minecraft:material" ou "nexo:id"
     * 
     * @return Map avec tous les items et leurs quantités
     */
    public Map<String, Integer> getAllStorageItems() {
        Map<String, Integer> allItems = new HashMap<>();
        
        // Ajouter items vanilla avec préfixe minecraft:
        for (Map.Entry<Material, Integer> entry : vanillaItems.entrySet()) {
            allItems.put("minecraft:" + entry.getKey().name().toLowerCase(), entry.getValue());
        }
        
        // Ajouter items Nexo avec préfixe nexo:
        for (Map.Entry<String, Integer> entry : nexoItems.entrySet()) {
            allItems.put("nexo:" + entry.getKey(), entry.getValue());
        }
        
        return allItems;
    }
    
    // === STATISTICS ===
    
    /**
     * Obtenir le nombre d'items uniques (types différents)
     * @return Nombre de types d'items différents en stock
     */
    public int getUniqueItemCount() {
        return vanillaItems.size() + nexoItems.size();
    }
    
    /**
     * Obtenir le nombre total d'items (toutes quantités)
     * @return Somme de toutes les quantités
     */
    public int getTotalItemCount() {
        int total = 0;
        
        // Somme des items vanilla
        for (int amount : vanillaItems.values()) {
            total += amount;
        }
        
        // Somme des items Nexo
        for (int amount : nexoItems.values()) {
            total += amount;
        }
        
        return total;
    }
    
    // === UNIFIED ITEM ID ===
    
    /**
     * Récupère la quantité d'un item par son ID unifié
     * Format supporté:
     * - minecraft:stone (item vanilla)
     * - nexo:custom_sword (item Nexo)
     * 
     * @param itemId L'ID de l'item au format minecraft:xxx ou nexo:xxx
     * @return La quantité en stock, ou 0 si aucun
     */
    public int getAmountByItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return 0;
        }
        
        if (itemId.startsWith("minecraft:")) {
            // Item Minecraft vanilla
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                return getAmount(material);
            } catch (IllegalArgumentException e) {
                return 0;
            }
        } else if (itemId.startsWith("nexo:")) {
            // Item Nexo
            String nexoId = itemId.substring(5);
            return getNexoAmount(nexoId);
        }
        
        return 0;
    }
    
    /**
     * Vérifie si le joueur possède au moins une certaine quantité d'un item par son ID unifié
     * 
     * @param itemId L'ID de l'item au format minecraft:xxx ou nexo:xxx
     * @param amount La quantité requise
     * @return true si le joueur a au moins cette quantité, false sinon
     */
    public boolean hasItemById(String itemId, int amount) {
        return getAmountByItemId(itemId) >= amount;
    }
    
    /**
     * Retire un item par son ID unifié
     * 
     * @param itemId L'ID de l'item au format minecraft:xxx ou nexo:xxx
     * @param amount La quantité à retirer
     */
    public void removeItemById(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) {
            return;
        }
        
        if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                removeItem(material, amount);
            } catch (IllegalArgumentException e) {
                // Material invalide, ignorer
            }
        } else if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            removeNexoItem(nexoId, amount);
        }
    }
    
    /**
     * Ajoute un item par son ID unifié avec tracking statistiques
     * 
     * @param itemId L'ID de l'item au format minecraft:xxx ou nexo:xxx
     * @param amount La quantité à ajouter
     */
    public void addItemById(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) {
            return;
        }
        
        // Déterminer la catégorie depuis l'item ID
        String category = determineCategoryFromItemId(itemId);
        
        if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                addItem(material, amount);
                
                // Track statistics par catégorie (utilise updateItemsStored avec delta positif)
                Quantum plugin = Quantum.getInstance();
                if (plugin != null && plugin.getStatisticsManager() != null) {
                    plugin.getStatisticsManager().updateItemsStored(category, (long) amount);
                }
                
                // Track statistiques globales du storage
                if (plugin != null && plugin.getStorageStatsManager() != null) {
                    plugin.getStorageStatsManager().incrementItemsStored((long) amount);
                }
            } catch (IllegalArgumentException e) {
                // Material invalide, ignorer
            }
        } else if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            addNexoItem(nexoId, amount);
            
            // Track statistics par catégorie (utilise updateItemsStored avec delta positif)
            Quantum plugin = Quantum.getInstance();
            if (plugin != null && plugin.getStatisticsManager() != null) {
                plugin.getStatisticsManager().updateItemsStored(category, (long) amount);
            }
            
            // Track statistiques globales du storage
            if (plugin != null && plugin.getStorageStatsManager() != null) {
                plugin.getStorageStatsManager().incrementItemsStored((long) amount);
            }
        }
    }
    
    /**
     * Détermine la catégorie d'un item depuis son ID
     * Utilise les mêmes catégories que le système d'ordres
     * 
     * @param itemId L'ID de l'item
     * @return La catégorie (cultures, loots, items, potions, armures, outils, autre)
     */
    private String determineCategoryFromItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return "autre";
        }
        
        // Pour les items Nexo, essayer de deviner depuis le nom
        if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5).toLowerCase();
            
            // Cultures
            if (nexoId.contains("wheat") || nexoId.contains("carrot") || nexoId.contains("potato") ||
                nexoId.contains("beetroot") || nexoId.contains("crop") || nexoId.contains("seed")) {
                return "cultures";
            }
            
            // Potions
            if (nexoId.contains("potion") || nexoId.contains("elixir")) {
                return "potions";
            }
            
            // Armures
            if (nexoId.contains("helmet") || nexoId.contains("chestplate") || nexoId.contains("leggings") ||
                nexoId.contains("boots") || nexoId.contains("armor") || nexoId.contains("armure")) {
                return "armures";
            }
            
            // Outils
            if (nexoId.contains("pickaxe") || nexoId.contains("axe") || nexoId.contains("shovel") ||
                nexoId.contains("hoe") || nexoId.contains("sword") || nexoId.contains("tool")) {
                return "outils";
            }
            
            // Loots (default pour items spéciaux)
            return "loots";
        }
        
        // Pour les items Minecraft vanilla
        if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toLowerCase();
            
            // Cultures
            if (materialName.contains("wheat") || materialName.contains("carrot") || materialName.contains("potato") ||
                materialName.contains("beetroot") || materialName.contains("seeds") || materialName.contains("melon")) {
                return "cultures";
            }
            
            // Potions
            if (materialName.contains("potion")) {
                return "potions";
            }
            
            // Armures
            if (materialName.contains("helmet") || materialName.contains("chestplate") || materialName.contains("leggings") ||
                materialName.contains("boots")) {
                return "armures";
            }
            
            // Outils
            if (materialName.contains("pickaxe") || materialName.contains("axe") || materialName.contains("shovel") ||
                materialName.contains("hoe") || materialName.contains("sword")) {
                return "outils";
            }
            
            // Items généraux
            return "items";
        }
        
        return "autre";
    }
    
    // === DATABASE ===
    
    /**
     * Load storage from database
     */
    public void load(Quantum plugin) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            
            String query = "SELECT material, nexo_id, amount FROM player_storage WHERE player_uuid = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String materialStr = rs.getString("material");
                    String nexoId = rs.getString("nexo_id");
                    int amount = rs.getInt("amount");
                    
                    if (materialStr != null && !materialStr.isEmpty()) {
                        try {
                            Material material = Material.valueOf(materialStr);
                            vanillaItems.put(material, amount);
                        } catch (IllegalArgumentException e) {
                            plugin.getQuantumLogger().warning("Invalid material: " + materialStr);
                        }
                    } else if (nexoId != null && !nexoId.isEmpty()) {
                        nexoItems.put(nexoId, amount);
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load storage for " + uuid + ": " + e.getMessage());
        }
    }
    
    /**
     * Save storage to database
     */
    public void save(Quantum plugin) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            
            // Delete old entries
            try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM player_storage WHERE player_uuid = ?"
            )) {
                delete.setString(1, uuid.toString());
                delete.executeUpdate();
            }
            
            // Insert vanilla items
            if (!vanillaItems.isEmpty()) {
                try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO player_storage (player_uuid, material, nexo_id, amount) VALUES (?, ?, NULL, ?)"
                )) {
                    for (Map.Entry<Material, Integer> entry : vanillaItems.entrySet()) {
                        insert.setString(1, uuid.toString());
                        insert.setString(2, entry.getKey().name());
                        insert.setInt(3, entry.getValue());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            
            // Insert Nexo items
            if (!nexoItems.isEmpty()) {
                try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO player_storage (player_uuid, material, nexo_id, amount) VALUES (?, NULL, ?, ?)"
                )) {
                    for (Map.Entry<String, Integer> entry : nexoItems.entrySet()) {
                        insert.setString(1, uuid.toString());
                        insert.setString(2, entry.getKey());
                        insert.setInt(3, entry.getValue());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to save storage for " + uuid + ": " + e.getMessage());
        }
    }
    
    /**
     * Format material name to display name (e.g., DIAMOND_SWORD -> Diamond Sword)
     */
    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(Character.toUpperCase(word.charAt(0)));
            formatted.append(word.substring(1));
        }
        return formatted.toString();
    }
    
    public UUID getUuid() {
        return uuid;
    }
}
