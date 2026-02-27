package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.MenuManager;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.towers.storage.PlayerTowerStorage;
import com.wynvers.quantum.towers.storage.TowerStorageMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TowerStorageListener implements Listener {

    private final Quantum plugin;

    public TowerStorageListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTowerStorageClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title == null || !title.contains("Tower Storage")) {
            return;
        }

        Inventory topInv = event.getView().getTopInventory();

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInv)) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
            if (activeMenu != null) {
                MenuItem menuItem = activeMenu.getItemAt(event.getSlot());

                if (menuItem != null && menuItem.isStatic()) {
                    return;
                }

                if (menuItem != null && !menuItem.isQuantumTowerStorage()) {
                    return;
                }
            }

            TowerStorageMode.Mode mode = TowerStorageMode.getMode(player);

            switch (mode) {
                case STORAGE:
                    handleWithdraw(player, clicked, event.isShiftClick(), event.isRightClick());
                    break;

                case SELL:
                    handleSell(player, clicked, event.isShiftClick(), event.isRightClick());
                    break;

                case RECHERCHE:
                    handleCreateOrder(player, clicked);
                    break;
            }
        } else if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTowerStorageDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.contains("Tower Storage")) {
            event.setCancelled(true);
        }
    }

    private void handleWithdraw(Player player, ItemStack displayItem, boolean shiftClick, boolean rightClick) {
        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(player);
        Material material = displayItem.getType();

        int withdrawAmount;
        if (shiftClick) {
            withdrawAmount = storage.getAmount(material);
        } else if (rightClick) {
            withdrawAmount = 64;
        } else {
            withdrawAmount = 1;
        }

        int available = storage.getAmount(material);
        int toWithdraw = Math.min(withdrawAmount, available);

        if (toWithdraw <= 0) {
            plugin.getMessageManager().sendMessage(player, "storage-advanced.no-items-available");
            return;
        }

        if (!hasSpace(player, toWithdraw)) {
            plugin.getMessageManager().sendMessage(player, "storage-advanced.inventory-full");
            return;
        }

        storage.removeItem(material, toWithdraw);
        giveItems(player, material, toWithdraw);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantity", String.valueOf(toWithdraw));
        placeholders.put("item", material.name());
        plugin.getMessageManager().sendMessage(player, "storage-advanced.withdrawn", placeholders);

        storage.save(plugin);

        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Menu towerStorageMenu = plugin.getMenuManager().getMenu("tower_storage");
            if (towerStorageMenu != null) {
                towerStorageMenu.open(player, plugin);
            }
        }, 1L);
    }

    private void handleSell(Player player, ItemStack displayItem, boolean shiftClick, boolean rightClick) {
        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(player);

        String itemId = OrderCreationManager.getItemId(displayItem);
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.invalid-item");
            return;
        }

        int sellAmount;
        int available = storage.getAmountByItemId(itemId);

        if (shiftClick) {
            sellAmount = available;
        } else if (rightClick) {
            sellAmount = 64;
        } else {
            sellAmount = 1;
        }

        int toSell = Math.min(sellAmount, available);

        if (toSell <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.no-items-to-sell");
            return;
        }

        String priceKey;
        if (itemId.startsWith("nexo:")) {
            priceKey = itemId.substring(5);
        } else if (itemId.startsWith("minecraft:")) {
            priceKey = itemId.substring(10).toUpperCase();
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.unrecognized-format");
            return;
        }

        double pricePerItem = plugin.getPriceManager().getPrice(priceKey);

        if (pricePerItem <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("price_key", priceKey);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.item-not-sellable", placeholders);
            return;
        }

        double totalPrice = pricePerItem * toSell;

        storage.removeItemById(itemId, toSell);

        if (plugin.getVaultManager().isEnabled()) {
            boolean success = plugin.getVaultManager().deposit(player, totalPrice);
            if (!success) {
                plugin.getMessageManager().sendMessage(player, "storage-advanced.deposit-error");
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "storage-advanced.economy-unavailable");
        }

        String displayName = formatItemName(itemId);

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantity", String.valueOf(toSell));
        placeholders.put("display_name", displayName);
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        plugin.getMessageManager().sendMessage(player, "storage-advanced.sold", placeholders);

        storage.save(plugin);

        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Menu towerStorageMenu = plugin.getMenuManager().getMenu("tower_storage");
            if (towerStorageMenu != null) {
                towerStorageMenu.open(player, plugin);
            }
        }, 1L);
    }

    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return "[Nexo] " + itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }

    private void handleCreateOrder(Player player, ItemStack displayItem) {
        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(player);

        String itemId = OrderCreationManager.getItemId(displayItem);
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.invalid-item");
            return;
        }

        int stockQuantity = storage.getAmountByItemId(itemId);

        OrderCreationManager orderManager = plugin.getOrderCreationManager();
        if (orderManager == null) {
            plugin.getMessageManager().sendMessage(player, "order-creation.manager-not-initialized");
            return;
        }

        boolean started = orderManager.startOrderCreation(player, itemId, stockQuantity);
        if (!started) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            plugin.getMessageManager().sendMessage(player, "order-creation.session-creation-error");
            return;
        }

        session.setDisplayItem(displayItem.clone());

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MenuManager menuManager = plugin.getMenuManager();
            if (menuManager != null) {
                menuManager.openMenuWithSession(player, "order_quantity", session, displayItem);
            }
        }, 2L);
    }

    private void giveItems(Player player, Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int remaining = amount;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(material, stackSize);

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }

            remaining -= stackSize;
        }
    }

    private boolean hasSpace(Player player, int amount) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots * 64 >= amount;
    }
}
