package com.wynvers.quantum.sell;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Représente une session de vente pour un joueur
 * Contient l'item à vendre et la quantité
 */
public class SellSession {
    
    private final UUID playerUUID;
    private ItemStack itemToSell;
    private int quantity;
    private int maxQuantity;
    private double pricePerUnit;
    private boolean towerStorage;
    
    public SellSession(UUID playerUUID, ItemStack itemToSell, int maxQuantity, double pricePerUnit) {
        this.playerUUID = playerUUID;
        this.itemToSell = itemToSell.clone();
        this.maxQuantity = maxQuantity;
        this.pricePerUnit = pricePerUnit;
        this.quantity = Math.max(maxQuantity / 2, 1); // Par défaut: moitié ou 1 minimum
        this.towerStorage = false;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public boolean isTowerStorage() {
        return towerStorage;
    }

    public void setTowerStorage(boolean towerStorage) {
        this.towerStorage = towerStorage;
    }
    
    public ItemStack getItemToSell() {
        return itemToSell;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, Math.min(quantity, maxQuantity));
    }
    
    public int getMaxQuantity() {
        return maxQuantity;
    }
    
    public double getPricePerUnit() {
        return pricePerUnit;
    }
    
    public double getTotalPrice() {
        return pricePerUnit * quantity;
    }
    
    public double getMaxTotalPrice() {
        return pricePerUnit * maxQuantity;
    }
    
    /**
     * Modifie la quantité (ajoute ou retire)
     */
    public void changeQuantity(int amount) {
        setQuantity(quantity + amount);
    }
    
    /**
     * Génère les placeholders pour le menu de vente
     */
    public Map<String, String> getPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        
        // Nom de l'item (avec couleurs)
        String itemName;
        if (itemToSell.hasItemMeta() && itemToSell.getItemMeta().hasDisplayName()) {
            itemName = itemToSell.getItemMeta().getDisplayName();
        } else {
            // Vérifier si c'est un item Nexo
            String nexoId = NexoItems.idFromItem(itemToSell);
            if (nexoId != null) {
                // Essayer de récupérer le display name depuis Nexo
                try {
                    ItemStack nexoItem = NexoItems.itemFromId(nexoId).build();
                    if (nexoItem.hasItemMeta() && nexoItem.getItemMeta().hasDisplayName()) {
                        itemName = nexoItem.getItemMeta().getDisplayName();
                    } else {
                        itemName = nexoId;
                    }
                } catch (Exception e) {
                    itemName = nexoId;
                }
            } else {
                itemName = itemToSell.getType().name().toLowerCase().replace('_', ' ');
            }
        }
        placeholders.put("item_name", itemName);
        
        // Material de l'item (pour l'affichage dans le menu)
        // Si c'est un item Nexo, on garde le type vanilla
        placeholders.put("item", itemToSell.getType().name());
        
        // Amount à afficher (max 64 si quantité > 64)
        int displayAmount = Math.min(quantity, 64);
        placeholders.put("amount", String.valueOf(displayAmount));
        
        // Quantités
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("max_quantity", String.valueOf(maxQuantity));
        
        // Prix
        placeholders.put("price_per_unit", String.format("%.2f$", pricePerUnit));
        placeholders.put("total_price", String.format("%.2f$", getTotalPrice()));
        placeholders.put("max_total_price", String.format("%.2f$", getMaxTotalPrice()));
        
        return placeholders;
    }
}
