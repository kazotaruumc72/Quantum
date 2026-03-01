package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.MenuManager;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
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

public class StorageListener implements Listener {
    
    private final Quantum plugin;
    
    public StorageListener(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStorageClick(InventoryClickEvent event) {
        // Check if it's storage GUI using view title
        String title = event.getView().getTitle();
        if (title == null || !title.contains("Quantum Storage")) {
            return;
        }
        
        Inventory topInv = event.getView().getTopInventory();
        
        // If clicking in storage inventory
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInv)) {
            event.setCancelled(true);
            
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            // If MenuListener is already managing this menu, defer to it
            Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
            if (activeMenu != null && activeMenu.getId().equals("storage")) {
                return;
            }
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // === FILTRE: Vérifier si c'est un bouton/item statique ===
            if (activeMenu != null) {
                MenuItem menuItem = activeMenu.getItemAt(event.getSlot());
                
                // Si l'item existe dans la config et qu'il est marqué comme statique, on ignore
                if (menuItem != null && menuItem.isStatic()) {
                    // C'est un bouton/vitre statique, on laisse MenuListener gérer
                    return;
                }
                
                // Si ce n'est pas un slot de storage dynamique (quantum_storage), on ignore aussi
                if (menuItem != null && !menuItem.isQuantumStorage()) {
                    // C'est un bouton (mode, settings, etc.), on laisse MenuListener gérer
                    return;
                }
            }
            // === FIN DU FILTRE ===
            
            // Vérifier le mode du joueur
            StorageMode.Mode mode = StorageMode.getMode(player);
            
            switch (mode) {
                case STORAGE:
                    // Mode normal: retirer les items
                    handleWithdraw(player, clicked, event.isShiftClick(), event.isRightClick());
                    break;
                    
                case SELL:
                    // Mode vente: vendre les items
                    handleSell(player, clicked, event.isShiftClick(), event.isRightClick());
                    break;
                    
                case RECHERCHE:
                    // Mode recherche: créer une offre d'achat (NE PAS RETIRER LES ITEMS)
                    handleCreateOrder(player, clicked);
                    break;
            }
        }
        // If clicking in player inventory while storage open, prevent shift-click
        else if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStorageDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.contains("Quantum Storage")) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Gère le retrait d'items en mode STORAGE
     */
    private void handleWithdraw(Player player, ItemStack displayItem, boolean shiftClick, boolean rightClick) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
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
        
        // Check inventory space
        if (!hasSpace(player, toWithdraw)) {
            plugin.getMessageManager().sendMessage(player, "storage-advanced.inventory-full");
            return;
        }
        
        // Remove from storage
        storage.removeItem(material, toWithdraw);
        
        // Give to player
        giveItems(player, material, toWithdraw);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantity", String.valueOf(toWithdraw));
        placeholders.put("item", material.name());
        plugin.getMessageManager().sendMessage(player, "storage-advanced.withdrawn", placeholders);
        
        // Save and refresh GUI
        storage.save(plugin);
        
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("storage");
        }, 1L);
    }
    
    /**
     * Gère la vente d'items en mode SELL
     * Supporte à la fois les items Minecraft vanilla et Nexo
     */
    private void handleSell(Player player, ItemStack displayItem, boolean shiftClick, boolean rightClick) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        // Convertir l'ItemStack en itemId unifié (minecraft:xxx ou nexo:xxx)
        String itemId = OrderCreationManager.getItemId(displayItem);
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.invalid-item");
            return;
        }
        
        // Déterminer la quantité à vendre
        int sellAmount;
        int available = storage.getAmountByItemId(itemId);
        
        if (shiftClick) {
            // Shift-clic: vendre tout
            sellAmount = available;
        } else if (rightClick) {
            // Clic-droit: vendre 64
            sellAmount = 64;
        } else {
            // Clic-gauche: vendre 1
            sellAmount = 1;
        }
        
        int toSell = Math.min(sellAmount, available);
        
        if (toSell <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.no-items-to-sell");
            return;
        }
        
        // Obtenir le prix depuis le PriceManager
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
        
        // Calculer le montant total (avec multiplicateur de vente)
        double multiplier = plugin.getStorageUpgradeManager().getSellMultiplier(player);
        double totalPrice = pricePerItem * toSell * multiplier;
        
        // Retirer les items du storage
        storage.removeItemById(itemId, toSell);
        
        // Donner l'argent au joueur via VaultManager
        if (plugin.getVaultManager().isEnabled()) {
            boolean success = plugin.getVaultManager().deposit(player, totalPrice);
            if (!success) {
                plugin.getMessageManager().sendMessage(player, "storage-advanced.deposit-error");
                plugin.getQuantumLogger().error("Erreur deposit Vault pour " + player.getName() + ": " + totalPrice + "$");
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "storage-advanced.economy-unavailable");
            plugin.getQuantumLogger().error("Vault non disponible pour la vente!");
        }
        
        // Formater le nom d'affichage
        String displayName = formatItemName(itemId);
        
        // Messages et son
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantity", String.valueOf(toSell));
        placeholders.put("display_name", displayName);
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        plugin.getMessageManager().sendMessage(player, "storage-advanced.sold", placeholders);
        
        // Sauvegarder et rafraîchir
        storage.save(plugin);
        
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("storage");
        }, 1L);
    }
    
    /**
     * Formate un itemId pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return "[Nexo] " + itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }
    
    /**
     * Gère la création d'offre d'achat en mode RECHERCHE
     * IMPORTANT: NE RETIRE PAS LES ITEMS DU STORAGE !
     */
    private void handleCreateOrder(Player player, ItemStack displayItem) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        // Convertir l'ItemStack en itemId (minecraft:xxx ou nexo:xxx)
        String itemId = OrderCreationManager.getItemId(displayItem);
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            plugin.getMessageManager().sendMessage(player, "storage-advanced.invalid-item");
            return;
        }
        
        // Récupérer la quantité en stock (SANS LA RETIRER)
        int stockQuantity = storage.getAmountByItemId(itemId);
        
        // Démarrer la création d'offre via OrderCreationManager
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
        
        // Récupérer la session pour stocker l'ItemStack
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            plugin.getMessageManager().sendMessage(player, "order-creation.session-creation-error");
            return;
        }
        
        // Stocker l'ItemStack dans la session
        session.setDisplayItem(displayItem.clone());
        
        // Fermer l'inventaire et ouvrir le menu order_quantity
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
