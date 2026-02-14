package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-world inventories for tower system.
 * When entering a tower world, saves the player's current inventory
 * and loads their tower inventory. When leaving, restores original inventory.
 * Also unequips dungeon armor when leaving the tower world.
 */
public class TowerInventoryManager {

    private final Quantum plugin;

    // Saved main world inventories (contents + armor)
    private final Map<UUID, SavedInventory> savedInventories = new HashMap<>();

    // Saved tower inventories (per tower ID)
    private final Map<UUID, Map<String, SavedInventory>> towerInventories = new HashMap<>();

    public TowerInventoryManager(Quantum plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player enters a tower.
     * Saves their current inventory and loads tower inventory.
     */
    public void onEnterTower(Player player, String towerId) {
        UUID uuid = player.getUniqueId();
        PlayerInventory inv = player.getInventory();

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

        plugin.getQuantumLogger().info("Tower inventory loaded for " + player.getName() + " (tower: " + towerId + ")");
    }

    /**
     * Called when a player leaves a tower.
     * Saves their tower inventory, unequips dungeon armor, and restores main inventory.
     */
    public void onLeaveTower(Player player, String towerId) {
        UUID uuid = player.getUniqueId();
        PlayerInventory inv = player.getInventory();

        // Save current tower inventory
        if (towerId != null) {
            towerInventories
                    .computeIfAbsent(uuid, k -> new HashMap<>())
                    .put(towerId, new SavedInventory(inv));
        }

        // Unequip all armor (including dungeon armor)
        inv.setArmorContents(new ItemStack[4]);

        // Restore saved main world inventory
        SavedInventory saved = savedInventories.remove(uuid);
        if (saved != null) {
            saved.restoreTo(inv);
        } else {
            inv.clear();
        }

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
     * Simple inventory snapshot class
     */
    private static class SavedInventory {
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        SavedInventory(PlayerInventory inv) {
            this.contents = cloneArray(inv.getContents());
            this.armor = cloneArray(inv.getArmorContents());
            this.offhand = inv.getItemInOffHand().clone();
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
