package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
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
import java.util.UUID;

public class StorageListener implements Listener {
    
    private final Quantum plugin;
    
    // Stockage temporaire des offres en cours de création
    private final Map<UUID, PendingOrder> pendingOrders = new HashMap<>();
    
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
                    // Mode recherche: créer une offre d'achat
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
     * Étape 1: Vérifier qu'au moins 1 item est stocké
     * Étape 2: Ouvrir le menu de sélection de quantité
     */
    private void handleCreateOrder(Player player, ItemStack displayItem) {
        Material material = displayItem.getType();
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        int stockQuantity = storage.getAmount(material);
        if (stockQuantity <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage("§c⚠ Vous devez avoir au moins 1 item en stock pour créer une offre!");
            return;
        }
        
        // Créer l'offre en attente
        PendingOrder order = new PendingOrder(player.getUniqueId(), material, stockQuantity);
        pendingOrders.put(player.getUniqueId(), order);
        
        // Ouvrir le menu de sélection de quantité
        player.closeInventory();
        openQuantitySelectionMenu(player, material, stockQuantity);
    }
    
    /**
     * Ouvre le menu de sélection de quantité
     */
    private void openQuantitySelectionMenu(Player player, Material material, int stockQuantity) {
        Inventory inv = Bukkit.createInventory(null, 27, "§b§lQuantité recherchée");
        
        // TODO: Créer les boutons de sélection (1, 8, 16, 32, 64, etc.)
        // Pour l'instant, message temporaire
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        player.sendMessage("§b§l✓ Création d'offre pour: §f" + material.name());
        player.sendMessage("§7Stock disponible: §e" + stockQuantity);
        player.sendMessage("§e⚠ Menu de sélection de quantité en développement");
        player.sendMessage("§7Tapez la quantité dans le chat (ou 'cancel' pour annuler)");
        
        // TODO: Ouvrir le menu graphique au lieu du chat
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
    
    /**
     * Classe interne pour stocker les offres en cours de création
     */
    private static class PendingOrder {
        private final UUID playerUUID;
        private final Material material;
        private final int maxQuantity;
        private int quantity = 0;
        private double pricePerUnit = 0.0;
        
        public PendingOrder(UUID playerUUID, Material material, int maxQuantity) {
            this.playerUUID = playerUUID;
            this.material = material;
            this.maxQuantity = maxQuantity;
        }
        
        public UUID getPlayerUUID() { return playerUUID; }
        public Material getMaterial() { return material; }
        public int getMaxQuantity() { return maxQuantity; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    }
}
