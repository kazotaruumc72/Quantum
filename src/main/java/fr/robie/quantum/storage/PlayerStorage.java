package fr.robie.quantum.storage;

import fr.robie.quantum.Quantum;
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
