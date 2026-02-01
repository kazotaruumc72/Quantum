package com.wynvers.quantum.sell;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Repr\u00e9sente une session de vente pour un joueur
 * Contient l'item \u00e0 vendre et la quantit\u00e9
 */
public class SellSession {
    
    private final UUID playerUUID;
    private ItemStack itemToSell;
    private int quantity;
    private int maxQuantity;
    private double pricePerUnit;
    
    public SellSession(UUID playerUUID, ItemStack itemToSell, int maxQuantity, double pricePerUnit) {
        this.playerUUID = playerUUID;
        this.itemToSell = itemToSell.clone();
        this.maxQuantity = maxQuantity;
        this.pricePerUnit = pricePerUnit;
        this.quantity = Math.min(maxQuantity / 2, 1); // Par d\u00e9faut: moiti\u00e9 ou 1 minimum
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
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
    
    /**
     * Modifie la quantit\u00e9 (ajoute ou retire)
     */
    public void changeQuantity(int amount) {
        setQuantity(quantity + amount);
    }
}
