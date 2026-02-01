package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menus.MenuManager;
import com.wynvers.quantum.orders.OrderButtonHandler;
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
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
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
            player.sendMessage("§cNo items available!");
            return;
        }
        
        // Check inventory space
        if (!hasSpace(player, toWithdraw)) {
            player.sendMessage("§cYour inventory is full!");
            return;
        }
        
        // Remove from storage
        storage.removeItem(material, toWithdraw);
        
        // Give to player
        giveItems(player, material, toWithdraw);
        
        player.sendMessage("§a§l✓ §aWithdrawn §e" + toWithdraw + "x §f" + material.name());
        
        // Save and refresh GUI
        storage.save(plugin);
        
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("storage");
        }, 1L);
    }
    
    /**
     * Gère la vente d'items en mode SELL
     * TODO: Implémenter la logique de vente avec prix
     */
    private void handleSell(Player player, ItemStack displayItem, boolean shiftClick, boolean rightClick) {
        Material material = displayItem.getType();
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage("§e⚠ Système de vente en développement pour " + material.name());
        // TODO: Intégrer le système de prix et vente
    }
    
    /**
     * Gère la création d'offre d'achat en mode RECHERCHE
     * IMPORTANT: NE RETIRE PAS LES ITEMS DU STORAGE !
     * Crée la session et ouvre le menu avec placeholders appliqués
     */
    private void handleCreateOrder(Player player, ItemStack displayItem) {
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        // Convertir l'ItemStack en itemId (minecraft:xxx ou nexo:xxx)
        String itemId = OrderCreationManager.getItemId(displayItem);
        if (itemId == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Item invalide!");
            return;
        }
        
        // Récupérer la quantité en stock (SANS LA RETIRER)
        int stockQuantity = storage.getAmountByItemId(itemId);
        
        // Démarrer la création d'offre via OrderCreationManager
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
        
        // Récupérer la session pour récupérer l'item d'origine
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§c⚠ Erreur lors de la création de la session!");
            return;
        }
        
        // Fermer l'inventaire
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        
        // Ouvrir le menu order_quantity avec l'item et les placeholders
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MenuManager menuManager = plugin.getMenuManager();
            if (menuManager != null) {
                // Ouvrir le menu avec la session et l'item
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
