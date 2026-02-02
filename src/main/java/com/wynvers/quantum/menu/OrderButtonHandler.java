package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderTransaction;
import com.wynvers.quantum.storage.PlayerStorage;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère les clics sur les ordres dans les menus orders_* et order_confirm
 */
public class OrderButtonHandler {
    
    private final Quantum plugin;
    private final OrderTransaction orderTransaction;
    
    // Cache des données d'ordre pour chaque joueur
    private final Map<Player, OrderClickData> orderClickCache = new HashMap<>();
    
    public OrderButtonHandler(Quantum plugin) {
        this.plugin = plugin;
        this.orderTransaction = new OrderTransaction(plugin);
    }
    
    /**
     * Gère le clic sur un ordre dans orders_* menus
     * Ouvre le menu de confirmation avec les détails de l'ordre
     * 
     * @param player Le joueur qui clique
     * @param category La catégorie de l'ordre (cultures, loots, items, etc.)
     * @param orderId L'ID de l'ordre dans orders.yml
     */
    public void handleOrderClick(Player player, String category, String orderId) {
        // Charger l'ordre depuis orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            player.sendMessage("§c⚠ Fichier orders.yml introuvable!");
            return;
        }
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        String orderPath = category + "." + orderId;
        
        if (!ordersConfig.contains(orderPath)) {
            player.sendMessage("§c⚠ Cet ordre n'existe plus!");
            return;
        }
        
        // Récupérer les infos de l'ordre
        String buyerName = ordersConfig.getString(orderPath + ".orderer");
        String itemId = ordersConfig.getString(orderPath + ".item");
        int quantity = ordersConfig.getInt(orderPath + ".quantity");
        double pricePerUnit = ordersConfig.getDouble(orderPath + ".price_per_unit");
        double totalPrice = ordersConfig.getDouble(orderPath + ".total_price");
        
        // Récupérer le stock du vendeur
        PlayerStorage sellerStorage = plugin.getStorageManager().getStorage(player);
        int sellerStock = sellerStorage.getAmountByItemId(itemId);
        
        // Sauvegarder les données pour le menu de confirmation
        OrderClickData data = new OrderClickData(category, orderId, buyerName, itemId, quantity, pricePerUnit, totalPrice, sellerStock);
        orderClickCache.put(player, data);
        
        // Créer les placeholders pour le menu
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantum_order_buyer", buyerName);
        placeholders.put("quantum_order_item_name", formatItemName(itemId));
        placeholders.put("quantum_order_quantity", String.valueOf(quantity));
        placeholders.put("quantum_order_price_per_unit", String.format("%.2f", pricePerUnit));
        placeholders.put("quantum_order_total_price", String.format("%.2f", totalPrice));
        placeholders.put("quantum_order_seller_stock", String.valueOf(sellerStock));
        
        // NOUVEAU: Créer l'ItemStack Nexo réel pour le display
        ItemStack displayItem = createDisplayItem(itemId, quantity);
        
        // Ouvrir le menu order_confirm
        Menu confirmMenu = plugin.getMenuManager().getMenu("order_confirm");
        if (confirmMenu != null) {
            player.closeInventory();
            
            // Attendre 2 ticks avant d'ouvrir le menu
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // NOUVEAU: Remplacer l'item PAPER par l'item Nexo réel
                confirmMenu.open(player, plugin, placeholders);
                
                // Remplacer l'item du slot 2 (order_item) par l'item réel
                if (displayItem != null) {
                    player.getOpenInventory().getTopInventory().setItem(2, displayItem);
                }
            }, 2L);
        } else {
            player.sendMessage("§c⚠ Menu order_confirm introuvable!");
        }
    }
    
    /**
     * NOUVEAU: Crée l'ItemStack Nexo réel pour affichage dans order_confirm
     * 
     * @param itemId L'ID de l'item (nexo:xxx ou minecraft:xxx)
     * @param quantity La quantité à afficher
     * @return L'ItemStack créé, ou null si erreur
     */
    private ItemStack createDisplayItem(String itemId, int quantity) {
        try {
            ItemStack displayItem;
            
            if (itemId.startsWith("nexo:")) {
                // Item Nexo
                String nexoId = itemId.substring(5); // Enlever "nexo:"
                
                var itemBuilder = NexoItems.itemFromId(nexoId);
                if (itemBuilder != null) {
                    displayItem = itemBuilder.build();
                    displayItem.setAmount(Math.min(quantity, 64)); // Max 64 pour l'affichage
                    return displayItem;
                } else {
                    plugin.getLogger().warning("[ORDER_CONFIRM] Item Nexo introuvable: " + nexoId);
                    return null;
                }
            } else if (itemId.startsWith("minecraft:")) {
                // Item Minecraft vanilla
                String materialName = itemId.substring(10).toUpperCase(); // Enlever "minecraft:"
                
                try {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialName);
                    displayItem = new ItemStack(material, Math.min(quantity, 64));
                    return displayItem;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[ORDER_CONFIRM] Material Minecraft invalide: " + materialName);
                    return null;
                }
            } else {
                plugin.getLogger().warning("[ORDER_CONFIRM] Format d'itemId invalide: " + itemId);
                return null;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("[ORDER_CONFIRM] Erreur lors de la création de l'item: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gère le clic sur le bouton VENDRE (confirmation de vente)
     * Exécute la transaction
     * 
     * @param player Le joueur qui vend
     */
    public void handleConfirmSell(Player player) {
        OrderClickData data = orderClickCache.get(player);
        
        if (data == null) {
            player.sendMessage("§c⚠ Données d'ordre introuvables! Veuillez réessayer.");
            player.closeInventory();
            return;
        }
        
        // Exécuter la transaction
        boolean success = orderTransaction.executeTransaction(player, data.category, data.orderId);
        
        if (success) {
            // Nettoyer le cache
            orderClickCache.remove(player);
            
            // Retourner au menu de catégorie
            String categoryMenuName = "orders_" + data.category;
            Menu categoryMenu = plugin.getMenuManager().getMenu(categoryMenuName);
            
            player.closeInventory();
            
            if (categoryMenu != null) {
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    categoryMenu.open(player, plugin);
                }, 5L);
            }
        } else {
            // La transaction a échoué, le message d'erreur a déjà été envoyé par OrderTransaction
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            
            // Rester sur le menu actuel pour que le joueur puisse réessayer
        }
    }
    
    /**
     * Gère le clic sur le bouton REFUSER (annulation de vente)
     * Retourne au menu de catégorie sans transaction
     * 
     * @param player Le joueur qui refuse
     */
    public void handleCancelConfirm(Player player) {
        OrderClickData data = orderClickCache.get(player);
        
        if (data == null) {
            player.sendMessage("§c⚠ Données d'ordre introuvables!");
            player.closeInventory();
            return;
        }
        
        // Nettoyer le cache
        orderClickCache.remove(player);
        
        // Retourner au menu de catégorie
        String categoryMenuName = "orders_" + data.category;
        Menu categoryMenu = plugin.getMenuManager().getMenu(categoryMenuName);
        
        player.closeInventory();
        
        if (categoryMenu != null) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                categoryMenu.open(player, plugin);
            }, 2L);
        }
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
    
    /**
     * Nettoie le cache pour un joueur
     * Appelé quand le joueur se déconnecte ou ferme le menu
     */
    public void clearCache(Player player) {
        orderClickCache.remove(player);
    }
    
    /**
     * Formate joliment un itemId pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }
    
    /**
     * Classe interne pour stocker les données d'un clic sur un ordre
     */
    private static class OrderClickData {
        final String category;
        final String orderId;
        final String buyerName;
        final String itemId;
        final int quantity;
        final double pricePerUnit;
        final double totalPrice;
        final int sellerStock;
        
        OrderClickData(String category, String orderId, String buyerName, String itemId, 
                      int quantity, double pricePerUnit, double totalPrice, int sellerStock) {
            this.category = category;
            this.orderId = orderId;
            this.buyerName = buyerName;
            this.itemId = itemId;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.totalPrice = totalPrice;
            this.sellerStock = sellerStock;
        }
    }
}
