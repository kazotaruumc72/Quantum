package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Handles clicks in the storage selector menu opened by /quantum storage (no type argument).
 * Slot 11 → Classic Storage, Slot 15 → Tower Storage.
 */
public class StorageSelectorMenuListener implements Listener {

    private static final String MENU_TITLE = "Sélecteur de Storage";

    private final Quantum plugin;

    public StorageSelectorMenuListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        String title = event.getView().getTitle();
        if (title == null || !title.contains(MENU_TITLE)) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        switch (slot) {
            case 11 -> openClassicStorage(player);
            case 15 -> openTowerStorage(player);
            default -> { /* border / filler — do nothing */ }
        }
    }

    private void openClassicStorage(Player player) {
        if (!player.hasPermission("quantum.storage.use")) {
            plugin.getMessageManager().sendMessage(player, "system.no-permission");
            return;
        }
        Menu menu = plugin.getMenuManager().getMenu("storage");
        if (menu != null) {
            Bukkit.getScheduler().runTask(plugin, () -> menu.open(player, plugin));
        } else {
            plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open");
        }
    }

    private void openTowerStorage(Player player) {
        if (!player.hasPermission("quantum.tower.storage")) {
            plugin.getMessageManager().sendMessage(player, "system.no-permission");
            return;
        }
        Menu menu = plugin.getMenuManager().getMenu("tower_storage");
        if (menu != null) {
            Bukkit.getScheduler().runTask(plugin, () -> menu.open(player, plugin));
        } else {
            plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open");
        }
    }
}
