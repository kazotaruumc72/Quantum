package com.wynvers.quantum.towers.storage;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTowerStorage {

    private final UUID uuid;
    private final Map<Material, Integer> vanillaItems;
    private final Map<String, Integer> nexoItems;

    public PlayerTowerStorage(UUID uuid) {
        this.uuid = uuid;
        this.vanillaItems = new ConcurrentHashMap<>();
        this.nexoItems = new ConcurrentHashMap<>();
    }

    // === VANILLA ITEMS ===

    private void addItemInternal(Material material, int amount) {
        vanillaItems.merge(material, amount, Integer::sum);
    }

    public boolean addItem(Quantum plugin, Player player, Material material, int amount) {
        int currentAmount = getAmount(material);
        int newAmount = currentAmount + amount;
        int limit = plugin.getTowerStorageUpgradeManager().getMaxStacks(
            plugin.getTowerStorageUpgradeManager().getState(player)
        );

        if (newAmount > limit) {
            String itemDisplayName = formatMaterialName(material);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_display_name", itemDisplayName);
            placeholders.put("limite", String.valueOf(limit));

            player.sendMessage(plugin.getMessagesManager().get("storage.limit-reached-1", placeholders, false));
            player.sendMessage(plugin.getMessagesManager().get("storage.limit-reached-2", placeholders, false));
            return false;
        }

        addItemInternal(material, amount);
        return true;
    }

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

    private void addNexoItemInternal(String nexoId, int amount) {
        nexoItems.merge(nexoId, amount, Integer::sum);
    }

    public boolean addNexoItem(Quantum plugin, Player player, String nexoId, int amount) {
        int currentAmount = getNexoAmount(nexoId);
        int newAmount = currentAmount + amount;
        int limit = plugin.getTowerStorageUpgradeManager().getMaxStacks(
            plugin.getTowerStorageUpgradeManager().getState(player)
        );

        if (newAmount > limit) {
            String itemDisplayName = nexoId;
            try {
                com.nexomc.nexo.items.ItemBuilder itemBuilder = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId);
                if (itemBuilder != null) {
                    org.bukkit.inventory.ItemStack nexoItem = itemBuilder.build();
                    if (nexoItem != null && nexoItem.hasItemMeta() && nexoItem.getItemMeta().hasDisplayName()) {
                        itemDisplayName = nexoItem.getItemMeta().getDisplayName();
                    }
                }
            } catch (Exception e) {
                // Fallback to nexoId
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_display_name", itemDisplayName);
            placeholders.put("limite", String.valueOf(limit));

            player.sendMessage(plugin.getMessagesManager().get("storage.limit-reached-1", placeholders, false));
            player.sendMessage(plugin.getMessagesManager().get("storage.limit-reached-2", placeholders, false));
            return false;
        }

        addNexoItemInternal(nexoId, amount);
        return true;
    }

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

    public Map<String, Integer> getAllStorageItems() {
        Map<String, Integer> allItems = new HashMap<>();

        for (Map.Entry<Material, Integer> entry : vanillaItems.entrySet()) {
            allItems.put("minecraft:" + entry.getKey().name().toLowerCase(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : nexoItems.entrySet()) {
            allItems.put("nexo:" + entry.getKey(), entry.getValue());
        }

        return allItems;
    }

    // === STATISTICS ===

    public int getUniqueItemCount() {
        return vanillaItems.size() + nexoItems.size();
    }

    public int getTotalItemCount() {
        int total = 0;
        for (int amount : vanillaItems.values()) {
            total += amount;
        }
        for (int amount : nexoItems.values()) {
            total += amount;
        }
        return total;
    }

    // === UNIFIED ITEM ID ===

    public int getAmountByItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return 0;
        }

        if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                return getAmount(material);
            } catch (IllegalArgumentException e) {
                return 0;
            }
        } else if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            return getNexoAmount(nexoId);
        }

        return 0;
    }

    public boolean hasItemById(String itemId, int amount) {
        return getAmountByItemId(itemId) >= amount;
    }

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

    public void load(Quantum plugin) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {

            String query = "SELECT material, nexo_id, amount FROM tower_storage WHERE player_uuid = ?";

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
                            plugin.getQuantumLogger().warning("Invalid material in tower_storage: " + materialStr);
                        }
                    } else if (nexoId != null && !nexoId.isEmpty()) {
                        nexoItems.put(nexoId, amount);
                    }
                }
            }

            // Cap items at the upgrade stack limit (200 by default, more with stack upgrades)
            if (plugin.getTowerStorageUpgradeManager() != null) {
                int maxStack = plugin.getTowerStorageUpgradeManager().getUpgradeStackMax(uuid);
                vanillaItems.replaceAll((mat, amt) -> Math.min(amt, maxStack));
                nexoItems.replaceAll((id, amt) -> Math.min(amt, maxStack));
            }

        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load tower storage for " + uuid + ": " + e.getMessage());
        }
    }

    public void save(Quantum plugin) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {

            // Delete old entries
            try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM tower_storage WHERE player_uuid = ?"
            )) {
                delete.setString(1, uuid.toString());
                delete.executeUpdate();
            }

            // Insert vanilla items
            if (!vanillaItems.isEmpty()) {
                try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO tower_storage (player_uuid, material, nexo_id, amount) VALUES (?, ?, '', ?)"
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
                    "INSERT INTO tower_storage (player_uuid, material, nexo_id, amount) VALUES (?, '', ?, ?)"
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
            plugin.getQuantumLogger().error("Failed to save tower storage for " + uuid + ": " + e.getMessage());
        }
    }

    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                formatted.append(word.substring(1));
            }
        }
        return formatted.toString();
    }

    public UUID getUuid() {
        return uuid;
    }
}
