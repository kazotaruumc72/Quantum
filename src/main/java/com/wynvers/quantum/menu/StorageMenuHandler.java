package com.wynvers.quantum.menu;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles click events in the storage menu
 */
public class StorageMenuHandler {

    private final Quantum plugin;

    public StorageMenuHandler(Quantum plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle storage menu click
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param clickType The type of click
     * @param cursorItem The item on the cursor
     */
    public void handleClick(Player player, int slot, ClickType clickType, ItemStack cursorItem) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);

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

        // V\u00e9rifier le mode du joueur
        StorageMode.Mode mode = StorageMode.getMode(player);
        
        if (mode == StorageMode.Mode.SELL) {
            // Mode VENTE : ouvrir le menu de vente
            handleSell(player, storage, clickedItem);
        } else {
            // Mode STORAGE : retirer l'item
            handleWithdraw(player, storage, clickedItem, clickType);
        }
    }

    /**
     * Handle depositing items into storage
     */
    private void handleDeposit(Player player, PlayerStorage storage, ItemStack cursorItem, ClickType clickType) {
        // Determine amount to deposit
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

        // Check if it's a Nexo item
        String nexoId = NexoItems.idFromItem(cursorItem);
        String itemName;
        
        if (nexoId != null) {
            storage.addNexoItem(nexoId, amount);
            itemName = nexoId;
        } else {
            storage.addItem(cursorItem.getType(), amount);
            itemName = cursorItem.getType().name();
        }
        
        // Send message from messages.yml
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("item", itemName);
        player.sendMessage(plugin.getMessagesManager().get("storage.deposited", placeholders, false));

        // Remove from cursor
        cursorItem.setAmount(cursorItem.getAmount() - amount);
        player.setItemOnCursor(cursorItem);

        // Save and refresh
        storage.save(plugin);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        refreshMenu(player);
    }

    /**
     * Handle withdrawing items from storage
     */
    private void handleWithdraw(Player player, PlayerStorage storage, ItemStack clickedItem, ClickType clickType) {
        // Determine the item type
        String nexoId = NexoItems.idFromItem(clickedItem);
        Material material = clickedItem.getType();

        // Determine amount to withdraw
        int amount;
        switch (clickType) {
            case LEFT:
                amount = clickedItem.getMaxStackSize();
                break;
            case RIGHT:
                amount = 1;
                break;
            case SHIFT_LEFT:
                amount = Integer.MAX_VALUE; // Take all
                break;
            default:
                return;
        }

        // Check available amount in storage
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

        // Calculate actual withdrawal amount
        int toWithdraw = Math.min(amount, available);

        // Check if player has space
        if (!hasSpace(player, toWithdraw)) {
            player.sendMessage(plugin.getMessagesManager().get("storage.inventory-full", false));
            return;
        }

        String itemName;
        
        // Withdraw from storage
        if (nexoId != null) {
            storage.removeNexoItem(nexoId, toWithdraw);
            giveNexoItems(player, nexoId, toWithdraw);
            itemName = nexoId;
        } else {
            storage.removeItem(material, toWithdraw);
            giveVanillaItems(player, material, toWithdraw);
            itemName = material.name();
        }
        
        // Send message from messages.yml
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(toWithdraw));
        placeholders.put("item", itemName);
        player.sendMessage(plugin.getMessagesManager().get("storage.withdrawn", placeholders, false));

        // Save and refresh
        storage.save(plugin);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        refreshMenu(player);
    }
    
    /**
     * Handle selling items from storage
     * Ouvre le menu de vente avec l'item s\u00e9lectionn\u00e9
     */
    private void handleSell(Player player, PlayerStorage storage, ItemStack clickedItem) {
        // V\u00e9rifier que Vault est activ\u00e9
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage("\u00a7cLe syst\u00e8me de vente n'est pas disponible (Vault requis).");
            return;
        }
        
        // D\u00e9terminer le type d'item
        String nexoId = NexoItems.idFromItem(clickedItem);
        Material material = clickedItem.getType();
        
        // V\u00e9rifier la quantit\u00e9 disponible
        int available;
        if (nexoId != null) {
            available = storage.getNexoAmount(nexoId);
        } else {
            available = storage.getAmount(material);
        }
        
        if (available <= 0) {
            player.sendMessage("\u00a7cVous n'avez pas cet item en stock.");
            return;
        }
        
        // R\u00e9cup\u00e9rer le prix de l'item
        double pricePerUnit;
        if (nexoId != null) {
            pricePerUnit = plugin.getPriceManager().getPrice(nexoId);
        } else {
            pricePerUnit = plugin.getPriceManager().getPrice(material.name());
        }
        
        if (pricePerUnit <= 0) {
            player.sendMessage("\u00a7cCet item ne peut pas \u00eatre vendu.");
            return;
        }
        
        // Cr\u00e9er une session de vente
        SellSession session = plugin.getSellManager().createSession(player, clickedItem, available, pricePerUnit);
        
        // Ouvrir le menu de vente
        Menu sellMenu = plugin.getMenuManager().getMenu("sell");
        if (sellMenu != null) {
            player.closeInventory();
            
            // Attendre 2 ticks avant d'ouvrir le menu de vente
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sellMenu.open(player, plugin);
            }, 2L);
        } else {
            player.sendMessage("\u00a7cErreur: Menu de vente introuvable.");
            plugin.getSellManager().removeSession(player);
        }
    }

    /**
     * Refresh the storage menu for the player
     */
    private void refreshMenu(Player player) {
        Menu storageMenu = plugin.getMenuManager().getMenu("storage");
        if (storageMenu != null) {
            storageMenu.open(player, plugin);
        }
    }

    /**
     * Check if player has space in inventory
     */
    private boolean hasSpace(Player player, int amount) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots * 64 >= amount;
    }

    /**
     * Give Nexo items to player
     */
    private void giveNexoItems(Player player, String nexoId, int amount) {
        int remaining = amount;
        ItemStack nexoItem = NexoItems.itemFromId(nexoId).build();
        int maxStackSize = nexoItem.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = NexoItems.itemFromId(nexoId).build();
            stack.setAmount(stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }

    /**
     * Give vanilla items to player
     */
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
