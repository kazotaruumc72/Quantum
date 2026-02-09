package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.upgrades.StorageUpgradeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class StorageSettingsMenuListener implements Listener {

    private final Quantum plugin;
    private final StorageUpgradeManager upgradeManager;

    public StorageSettingsMenuListener(Quantum plugin, StorageUpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        String title = event.getView().getTitle();
        if (title == null || !title.contains("Paramètres du Quantum Storage")) {
            return; // ce n'est pas le menu settings
        }

        event.setCancelled(true);

        int slot = event.getSlot();
        ClickType click = event.getClick();

        switch (slot) {
            case 11 -> { // toggle autovente
                upgradeManager.toggleAutoSell(player);
                reopenSettings(player);
            }
            case 15 -> { // changer la limite
                int delta = 0;
                if (click.isLeftClick() && !click.isShiftClick())       delta = 10;
                else if (click.isRightClick() && !click.isShiftClick()) delta = -10;
                else if (click.isLeftClick() && click.isShiftClick())   delta = 1;
                else if (click.isRightClick() && click.isShiftClick())  delta = -1;

                if (delta != 0) {
                    upgradeManager.changeAutoSellLimit(player, delta);
                    reopenSettings(player);
                }
            }
            case 22 -> { // retour au menu principal
                openStorageMain(player);
            }
            default -> {
                // rien
            }
        }
    }

    private void reopenSettings(Player player) {
        var menu = plugin.getMenuManager().getMenu("storage_settings");
        if (menu == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> menu.open(player, plugin));
    }

    private void openStorageMain(Player player) {
        var menu = plugin.getMenuManager().getMenu("storage");
        if (menu == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> menu.open(player, plugin));
    }
}
