package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.dungeonutis.DungeonUtilsType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages per-world inventories for tower system.
 * When entering a tower world, saves the player's current inventory
 * and loads their tower inventory. When leaving, restores original inventory.
 * Preserves dungeon armor and weapons when entering tower zones.
 */
public class TowerInventoryManager {

    private final Quantum plugin;
    private YamlConfiguration dungeonItemsConfig;

    // Saved main world inventories (contents + armor)
    private final Map<UUID, SavedInventory> savedInventories = new HashMap<>();

    // Saved tower inventories (per tower ID)
    private final Map<UUID, Map<String, SavedInventory>> towerInventories = new HashMap<>();

    public TowerInventoryManager(Quantum plugin) {
        this.plugin = plugin;
        loadDungeonItemsConfig();
    }

    /**
     * Load the dungeon_items.yml configuration
     */
    private void loadDungeonItemsConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon_items.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon_items.yml", false);
            }
            dungeonItemsConfig = YamlConfiguration.loadConfiguration(configFile);
            plugin.getQuantumLogger().info("Dungeon items configuration loaded");
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Error loading dungeon_items.yml: " + e.getMessage());
        }
    }

    /**
     * Reload the dungeon items configuration
     */
    public void reload() {
        loadDungeonItemsConfig();
    }

    /**
     * Called when a player enters a tower.
     * Saves their current inventory and loads tower inventory.
     * Preserves dungeon armor and weapons when entering.
     */
    public void onEnterTower(Player player, String towerId) {
        UUID uuid = player.getUniqueId();
        PlayerInventory inv = player.getInventory();

        // Extract dungeon armor and weapons before saving
        List<ItemStack> dungeonItems = extractDungeonItems(inv);

        // Save current (main world) inventory if not already saved
        if (!savedInventories.containsKey(uuid)) {
            savedInventories.put(uuid, new SavedInventory(inv));
        }

        // Load tower inventory if one exists
        Map<String, SavedInventory> playerTowerInvs = towerInventories.get(uuid);
        if (playerTowerInvs != null && playerTowerInvs.containsKey(towerId)) {
            playerTowerInvs.get(towerId).restoreTo(inv);
        } else {
            // Clear inventory for fresh tower entry
            inv.clear();
            inv.setArmorContents(new ItemStack[4]);
            inv.setItemInOffHand(null);
        }

        // Restore dungeon armor and weapons
        restoreDungeonItems(inv, dungeonItems);

        plugin.getQuantumLogger().info("Tower inventory loaded for " + player.getName() + " (tower: " + towerId + ")");
    }

    /**
     * Called when a player leaves a tower.
     * Saves their tower inventory and restores main inventory.
     * Preserves dungeon armor and weapons when leaving.
     */
    public void onLeaveTower(Player player, String towerId) {
        UUID uuid = player.getUniqueId();
        PlayerInventory inv = player.getInventory();

        // Extract dungeon armor and weapons before saving tower inventory
        List<ItemStack> dungeonItems = extractDungeonItems(inv);

        // Save current tower inventory
        if (towerId != null) {
            towerInventories
                    .computeIfAbsent(uuid, k -> new HashMap<>())
                    .put(towerId, new SavedInventory(inv));
        }

        // Restore saved main world inventory
        SavedInventory saved = savedInventories.remove(uuid);
        if (saved != null) {
            saved.restoreTo(inv);
        } else {
            inv.clear();
        }

        // Restore dungeon armor and weapons
        restoreDungeonItems(inv, dungeonItems);

        plugin.getQuantumLogger().info("Main inventory restored for " + player.getName());
    }

    /**
     * Cleanup when a player disconnects
     */
    public void cleanup(UUID uuid) {
        savedInventories.remove(uuid);
        towerInventories.remove(uuid);
    }

    /**
     * Check if a player has a saved main inventory (i.e., is in a tower)
     */
    public boolean isInTower(UUID uuid) {
        return savedInventories.containsKey(uuid);
    }

    /**
     * Extracts and removes dungeon armor and weapons from inventory
     */
    private List<ItemStack> extractDungeonItems(PlayerInventory inv) {
        List<ItemStack> dungeonItems = new ArrayList<>();

        // Check armor slots
        ItemStack[] armorContents = inv.getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack item = armorContents[i];
            if (item != null && isDungeonItem(item)) {
                dungeonItems.add(item.clone());
                armorContents[i] = null;
            }
        }
        inv.setArmorContents(armorContents);

        // Check main inventory for dungeon items
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && isDungeonItem(item)) {
                dungeonItems.add(item.clone());
                contents[i] = null;
            }
        }
        inv.setContents(contents);

        // Check offhand
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && isDungeonItem(offhand)) {
            dungeonItems.add(offhand.clone());
            inv.setItemInOffHand(null);
        }

        return dungeonItems;
    }

    /**
     * Restores dungeon armor and weapons to inventory
     */
    private void restoreDungeonItems(PlayerInventory inv, List<ItemStack> dungeonItems) {
        for (ItemStack item : dungeonItems) {
            if (item == null) continue;

            // Try to place armor in armor slots
            if (isDungeonArmor(item)) {
                String type = item.getType().name();
                if (type.contains("HELMET")) {
                    if (inv.getHelmet() == null) {
                        inv.setHelmet(item);
                        continue;
                    }
                } else if (type.contains("CHESTPLATE")) {
                    if (inv.getChestplate() == null) {
                        inv.setChestplate(item);
                        continue;
                    }
                } else if (type.contains("LEGGINGS")) {
                    if (inv.getLeggings() == null) {
                        inv.setLeggings(item);
                        continue;
                    }
                } else if (type.contains("BOOTS")) {
                    if (inv.getBoots() == null) {
                        inv.setBoots(item);
                        continue;
                    }
                }
            }

            // For weapons or if armor slots are full, add to main inventory
            inv.addItem(item);
        }
    }

    /**
     * Checks if an item is a dungeon item (armor or util)
     */
    private boolean isDungeonItem(ItemStack item) {
        // Check if it's dungeon armor
        if (shouldKeepDungeonArmor() && isDungeonArmor(item)) {
            return true;
        }

        // Check if it's a dungeon util (tool/weapon)
        if (shouldKeepDungeonUtils() && isDungeonUtil(item)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if dungeon armor should be kept in dungeon
     */
    private boolean shouldKeepDungeonArmor() {
        if (dungeonItemsConfig == null) return true;
        return dungeonItemsConfig.getBoolean("keep_dungeon_armor", true);
    }

    /**
     * Checks if dungeon utils should be kept in dungeon
     */
    private boolean shouldKeepDungeonUtils() {
        if (dungeonItemsConfig == null) return true;
        return dungeonItemsConfig.getBoolean("keep_dungeon_utils", true);
    }

    /**
     * Checks if an item is dungeon armor
     */
    private boolean isDungeonArmor(ItemStack item) {
        if (plugin.getDungeonArmor() != null) {
            return plugin.getDungeonArmor().isDungeonArmor(item);
        }
        return false;
    }

    /**
     * Checks if an item is a dungeon util
     */
    private boolean isDungeonUtil(ItemStack item) {
        if (plugin.getDungeonUtils() == null) return false;

        // Check if item is a dungeon util
        if (!plugin.getDungeonUtils().isDungeonUtil(item)) {
            return false;
        }

        // Check if this type should be kept in dungeon
        DungeonUtilsType type = plugin.getDungeonUtils().getType(item);
        if (type == null) return false;

        if (dungeonItemsConfig != null) {
            String path = "item_types." + type.name();
            if (!dungeonItemsConfig.getBoolean(path, true)) {
                return false;
            }
        }

        // Check exception list (items that can leave dungeon)
        if (dungeonItemsConfig != null && dungeonItemsConfig.getBoolean("exceptions.enabled", false)) {
            List<String> exceptions = dungeonItemsConfig.getStringList("exceptions.nexo_ids");
            String nexoId = com.nexomc.nexo.api.NexoItems.idFromItem(item);
            if (nexoId != null && exceptions.contains(nexoId)) {
                return false; // This item is an exception and can leave the dungeon
            }
        }

        return true;
    }

    /**
     * Simple inventory snapshot class
     */
    private static class SavedInventory {
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        SavedInventory(PlayerInventory inv) {
            this.contents = cloneArray(inv.getContents());
            this.armor = cloneArray(inv.getArmorContents());
            ItemStack offhandItem = inv.getItemInOffHand();
            this.offhand = offhandItem != null ? offhandItem.clone() : null;
        }

        void restoreTo(PlayerInventory inv) {
            inv.setContents(cloneArray(contents));
            inv.setArmorContents(cloneArray(armor));
            inv.setItemInOffHand(offhand != null ? offhand.clone() : null);
        }

        private static ItemStack[] cloneArray(ItemStack[] items) {
            ItemStack[] cloned = new ItemStack[items.length];
            for (int i = 0; i < items.length; i++) {
                cloned[i] = items[i] != null ? items[i].clone() : null;
            }
            return cloned;
        }
    }
}
