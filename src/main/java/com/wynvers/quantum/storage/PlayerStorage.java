package com.wynvers.quantum.storage;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    public void addItem(Material material, int amount) {
        vanillaItems.merge(material, amount, Integer::sum);
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
    
    public void addNexoItem(String nexoId, int amount) {
        nexoItems.merge(nexoId, amount, Integer::sum);
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
     * Ajoute un item par son ID unifié
     * 
     * @param itemId L'ID de l'item au format minecraft:xxx ou nexo:xxx
     * @param amount La quantité à ajouter
     */
    public void addItemById(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) {
            return;
        }
        
        if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                addItem(material, amount);
            } catch (IllegalArgumentException e) {
                // Material invalide, ignorer
            }
        } else if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            addNexoItem(nexoId, amount);
        }
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
    
    public UUID getUuid() {
        return uuid;
    }
}
