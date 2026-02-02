package com.wynvers.quantum.menu;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles click events in the storage menu
 */
public class StorageMenuHandler {

    private final Quantum plugin;
    private final NamespacedKey itemIdKey;

    public StorageMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "quantum_item_id");
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

        // If player has item on cursor - deposit it (ALLOWED ON ALL SLOTS)
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            handleDeposit(player, storage, cursorItem, clickType);
            return;
        }

        // If clicking empty slot - do nothing
        ItemStack clickedItem = player.getOpenInventory().getTopInventory().getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // IMPORTANT: Pour les RETRAITS uniquement, vérifier que le slot fait partie des storage_slots (9-44)
        // Les boutons de mode (0, 4, 8) et autres UI ne doivent pas déclencher d'actions
        if (!isStorageSlot(slot)) {
            return;
        }

        // Vérifier le mode du joueur
        StorageMode.Mode mode = StorageMode.getMode(player);
        
        switch (mode) {
            case STORAGE:
                // Mode STORAGE : retirer l'item
                handleWithdraw(player, storage, clickedItem, clickType);
                break;
                
            case SELL:
                // Mode VENTE : ouvrir le menu de vente
                handleSell(player, storage, clickedItem);
                break;
                
            case RECHERCHE:
                // Mode RECHERCHE : ouvrir le menu order_quantity
                handleCreateOrder(player, storage, clickedItem);
                break;
        }
    }

    /**
     * Vérifie si un slot fait partie des storage_slots (slots 9 à 44)
     * @param slot Le numéro du slot
     * @return true si c'est un slot de storage, false sinon
     */
    private boolean isStorageSlot(int slot) {
        // Les storage_slots vont du slot 9 au slot 44 (4 lignes complètes du milieu)
        return slot >= 9 && slot <= 44;
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
     * Ouvre le menu de vente avec l'item sélectionné
     */
    private void handleSell(Player player, PlayerStorage storage, ItemStack clickedItem) {
        // Vérifier que Vault est activé
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage("§cLe système de vente n'est pas disponible (Vault requis).");
            return;
        }
        
        // Déterminer le type d'item
        String nexoId = NexoItems.idFromItem(clickedItem);
        Material material = clickedItem.getType();
        
        // Vérifier la quantité disponible
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
        
        // Récupérer le prix de l'item
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
        
        // Créer une session de vente
        SellSession session = plugin.getSellManager().createSession(player, clickedItem, available, pricePerUnit);
        
        // Ouvrir le menu de vente
        Menu sellMenu = plugin.getMenuManager().getMenu("sell");
        if (sellMenu != null) {
            player.closeInventory();
            
            // Attendre 2 ticks avant d'ouvrir le menu de vente
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Passer les placeholders au menu
                sellMenu.open(player, plugin, session.getPlaceholders());
            }, 2L);
        } else {
            player.sendMessage("§cErreur: Menu de vente introuvable.");
            plugin.getSellManager().removeSession(player);
        }
    }
    
    /**
     * Handle creating a purchase order from storage
     * IMPORTANT: Ouvre le menu order_quantity pour configurer l'offre
     */
    private void handleCreateOrder(Player player, PlayerStorage storage, ItemStack clickedItem) {
        // Récupérer l'itemId depuis le PersistentDataContainer si disponible
        String itemId = null;
        
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta() != null) {
            itemId = clickedItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        }
        
        // Fallback: Utiliser OrderCreationManager.getItemId() si pas de PDC
        if (itemId == null) {
            itemId = OrderCreationManager.getItemId(clickedItem);
        }
        
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Item invalide!");
            return;
        }
        
        // Récupérer la quantité en stock (SANS LA RETIRER)
        int stockQuantity = storage.getAmountByItemId(itemId);
        
        // Vérifier qu'il y a au moins 1 item en stock
        if (stockQuantity <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Vous devez avoir au moins 1 item en stock!");
            return;
        }
        
        // Démarrer la création d'offre via OrderCreationManager
        OrderCreationManager orderManager = plugin.getOrderCreationManager();
        if (orderManager == null) {
            player.sendMessage("§c⚠ OrderCreationManager non initialisé!");
            return;
        }
        
        boolean started = orderManager.startOrderCreation(player, itemId, stockQuantity);
        if (!started) {
            // Le message d'erreur est géré par OrderCreationManager si nécessaire
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Ouvrir le menu order_quantity pour configurer la quantité
        Menu orderQuantityMenu = plugin.getMenuManager().getMenu("order_quantity");
        if (orderQuantityMenu != null) {
            player.closeInventory();
            
            // Attendre 2 ticks avant d'ouvrir le menu order_quantity
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                orderQuantityMenu.open(player, plugin);
            }, 2L);
        } else {
            player.sendMessage("§c⚠ Menu order_quantity introuvable!");
            // Annuler la création d'offre
            orderManager.cancelOrder(player);
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
