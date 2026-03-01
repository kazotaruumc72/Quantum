package com.wynvers.quantum.menu;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.towers.storage.PlayerTowerStorage;
import com.wynvers.quantum.towers.storage.TowerStorageMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles click events in the tower storage menu
 */
public class TowerStorageMenuHandler {

    private final Quantum plugin;
    private final NamespacedKey itemIdKey;
    private final NamespacedKey buttonTypeKey;

    public TowerStorageMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "tower_quantum_item_id");
        this.buttonTypeKey = new NamespacedKey(plugin, "button_type");
    }

    /**
     * Handle tower storage menu click
     */
    public void handleClick(Player player, int slot, ClickType clickType, ItemStack cursorItem) {
        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(player);

        // If player has item on cursor - deposit it
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            handleDeposit(player, storage, cursorItem, clickType);
            return;
        }

        // If clicking empty slot - do nothing
        ItemStack clickedItem = player.getOpenInventory().getTopInventory().getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Item MUST have quantum_item_id tag to be treated as a tower storage item
        if (!hasQuantumItemId(clickedItem)) {
            return;
        }

        TowerStorageMode.Mode mode = TowerStorageMode.getMode(player);

        switch (mode) {
            case STORAGE:
                handleWithdraw(player, storage, clickedItem, clickType);
                break;

            case SELL:
                handleSell(player, storage, clickedItem);
                break;

            case RECHERCHE:
                handleCreateOrder(player, storage, clickedItem);
                break;
        }
    }

    private boolean hasQuantumItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        String itemId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return itemId != null && !itemId.isEmpty();
    }

    private void handleDeposit(Player player, PlayerTowerStorage storage, ItemStack cursorItem, ClickType clickType) {
        int amount;
        switch (clickType) {
            case LEFT:
                amount = cursorItem.getAmount();
                break;
            case RIGHT:
                amount = 1;
                break;
            case SHIFT_LEFT:
                amount = cursorItem.getAmount();
                break;
            default:
                return;
        }

        amount = Math.min(amount, cursorItem.getAmount());

        String nexoId = NexoItems.idFromItem(cursorItem);
        String itemName;
        boolean success;

        if (nexoId != null) {
            success = storage.addNexoItem(plugin, player, nexoId, amount);
            itemName = nexoId;
        } else {
            success = storage.addItem(plugin, player, cursorItem.getType(), amount);
            itemName = cursorItem.getType().name();
        }

        if (success) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("item", itemName);
            player.sendMessage(plugin.getMessagesManager().get("storage.deposited", placeholders, false));

            cursorItem.setAmount(cursorItem.getAmount() - amount);
            player.setItemOnCursor(cursorItem);

            storage.save(plugin);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            refreshMenu(player);
        }
    }

    private void handleWithdraw(Player player, PlayerTowerStorage storage, ItemStack clickedItem, ClickType clickType) {
        String nexoId = NexoItems.idFromItem(clickedItem);
        Material material = clickedItem.getType();

        int amount;
        switch (clickType) {
            case LEFT:
                amount = clickedItem.getMaxStackSize();
                break;
            case RIGHT:
                amount = 1;
                break;
            case SHIFT_LEFT:
                amount = Integer.MAX_VALUE;
                break;
            default:
                return;
        }

        int available;
        if (nexoId != null) {
            available = storage.getNexoAmount(nexoId);
        } else {
            available = storage.getAmount(material);
        }

        if (available <= 0) {
            player.sendMessage(plugin.getMessagesManager().get("storage.not-in-storage", false));
            return;
        }

        int toWithdraw = Math.min(amount, available);

        if (!hasSpace(player, toWithdraw)) {
            player.sendMessage(plugin.getMessagesManager().get("storage.inventory-full", false));
            return;
        }

        String itemName;

        if (nexoId != null) {
            storage.removeNexoItem(nexoId, toWithdraw);
            giveNexoItems(player, nexoId, toWithdraw);
            itemName = nexoId;
        } else {
            storage.removeItem(material, toWithdraw);
            giveVanillaItems(player, material, toWithdraw);
            itemName = material.name();
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(toWithdraw));
        placeholders.put("item", itemName);
        player.sendMessage(plugin.getMessagesManager().get("storage.withdrawn", placeholders, false));

        storage.save(plugin);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        refreshMenu(player);
    }

    private void handleSell(Player player, PlayerTowerStorage storage, ItemStack clickedItem) {
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage("§cLe système de vente n'est pas disponible (Vault requis).");
            return;
        }

        String nexoId = NexoItems.idFromItem(clickedItem);
        Material material = clickedItem.getType();

        int available;
        if (nexoId != null) {
            available = storage.getNexoAmount(nexoId);
        } else {
            available = storage.getAmount(material);
        }

        if (available <= 0) {
            player.sendMessage("§cVous n'avez pas cet item en stock.");
            return;
        }

        double pricePerUnit;
        if (nexoId != null) {
            pricePerUnit = plugin.getPriceManager().getPrice(nexoId);
        } else {
            pricePerUnit = plugin.getPriceManager().getPrice(material.name());
        }

        if (pricePerUnit <= 0) {
            player.sendMessage("§cCet item ne peut pas être vendu.");
            return;
        }

        double multiplier = plugin.getTowerStorageUpgradeManager().getSellMultiplier(player);
        SellSession session = plugin.getSellManager().createSession(player, clickedItem, available, pricePerUnit * multiplier);
        session.setTowerStorage(true);

        Menu sellMenu = plugin.getMenuManager().getMenu("sell");
        if (sellMenu != null) {
            player.closeInventory();

            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sellMenu.open(player, plugin, session.getPlaceholders());
            }, 2L);
        } else {
            player.sendMessage("§cErreur: Menu de vente introuvable.");
            plugin.getSellManager().removeSession(player);
        }
    }

    private void handleCreateOrder(Player player, PlayerTowerStorage storage, ItemStack clickedItem) {
        String itemId = null;

        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta() != null) {
            itemId = clickedItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        }

        if (itemId == null) {
            itemId = OrderCreationManager.getItemId(clickedItem);
        }

        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Item invalide!");
            return;
        }

        int stockQuantity = storage.getAmountByItemId(itemId);

        if (stockQuantity <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Vous devez avoir au moins 1 item en stock!");
            return;
        }

        OrderCreationManager orderManager = plugin.getOrderCreationManager();
        if (orderManager == null) {
            player.sendMessage("§c⚠ OrderCreationManager non initialisé!");
            return;
        }

        boolean started = orderManager.startOrderCreation(player, itemId, stockQuantity);
        if (!started) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ItemStack displayItem = createItemStackFromId(itemId);

        if (displayItem != null) {
            orderManager.getSession(player).setDisplayItem(displayItem.clone());
        }

        player.closeInventory();

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            OrderCreationSession session = orderManager.getSession(player);
            if (session != null) {
                plugin.getMenuManager().openMenuWithSession(player, "order_quantity", session, displayItem);
            } else {
                player.sendMessage("§c⚠ Session de création d'ordre perdue!");
                orderManager.cancelOrder(player);
            }
        }, 2L);
    }

    private ItemStack createItemStackFromId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }

        if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            try {
                com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(nexoId);
                if (itemBuilder != null) {
                    return itemBuilder.build();
                }
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("Failed to create Nexo item: " + nexoId + " - " + e.getMessage());
            }
        } else if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                return new ItemStack(material, 1);
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid material: " + materialName);
            }
        }

        return null;
    }

    private void refreshMenu(Player player) {
        Menu towerStorageMenu = plugin.getMenuManager().getMenu("tower_storage");
        if (towerStorageMenu != null) {
            towerStorageMenu.open(player, plugin);
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

    private void giveNexoItems(Player player, String nexoId, int amount) {
        int remaining = amount;
        com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(nexoId);
        if (itemBuilder == null) {
            plugin.getQuantumLogger().warning("Cannot give Nexo item " + nexoId + " - ItemBuilder is null");
            return;
        }

        ItemStack nexoItem = itemBuilder.build();
        int maxStackSize = nexoItem.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = NexoItems.itemFromId(nexoId).build();
            stack.setAmount(stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }

    private void giveVanillaItems(Player player, Material material, int amount) {
        int remaining = amount;
        int maxStackSize = material.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(material, stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }
}
