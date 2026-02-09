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
import org.bukkit.inventory.ItemStack;

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

        // On ne s'intéresse qu'à l'inventaire du haut (menu)
        if (!event.getView().getTitle().contains("Paramètres du Quantum Storage")) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        ClickType click = event.getClick();

        switch (slot) {
            case 11 -> { // autosell_toggle
                upgradeManager.toggleAutoSell(player);
                // On rouvre pour rafraîchir les placeholders
                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> plugin.getMenuManager().openMenu(player, "storage_settings"),
                        1L);
            }
            case 15 -> { // autosell_limit
                int delta = 0;
                if (click.isLeftClick() && !click.isShiftClick()) {
                    delta = 10;
                } else if (click.isRightClick() && !click.isShiftClick()) {
                    delta = -10;
                } else if (click.isLeftClick() && click.isShiftClick()) {
                    delta = 1;
                } else if (click.isRightClick() && click.isShiftClick()) {
                    delta = -1;
                }
                if (delta != 0) {
                    upgradeManager.changeAutoSellLimit(player, delta);
                    Bukkit.getScheduler().runTaskLater(plugin,
                            () -> plugin.getMenuManager().openMenu(player, "storage_settings"),
                            1L);
                }
            }
            case 22 -> { // bouton "Retour" (on laisse aussi quantum_open_menu faire le job si tu l'as)
                plugin.getMenuManager().openMenu(player, "storage");
            }
            default -> {
                // rien
            }
        }
    }
}
