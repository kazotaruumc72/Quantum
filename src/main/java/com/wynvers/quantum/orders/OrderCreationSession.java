package com.wynvers.quantum.orders;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente une session de création d'ordre pour un joueur
 */
public class OrderCreationSession {
    
    public enum Step {
        QUANTITY,
        PRICE
    }
    
    private final String itemId;
    private final String itemName;
    private final int stockQuantity;
    private ItemStack displayItem; // PATCH: Item à afficher dans les menus
    
    private Step currentStep;
    private int quantity;
    private double price;
    private double minPrice;
    private double maxPrice;
    
    public OrderCreationSession(String itemId, String itemName, int stockQuantity, double minPrice, double maxPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.stockQuantity = stockQuantity;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        
        this.currentStep = Step.QUANTITY;
        this.quantity = 1;
        this.price = (minPrice + maxPrice) / 2.0; // Prix moyen par défaut
        this.displayItem = null; // Sera défini plus tard
    }
    
    // === DISPLAY ITEM METHODS ===
    
    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }
    
    public ItemStack getDisplayItem() {
        return displayItem;
    }
    
    // === QUANTITY METHODS ===
    
    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }
    
    public void adjustQuantity(int amount) {
        this.quantity = Math.max(1, this.quantity + amount);
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public int getMaxQuantity() {
        return stockQuantity;
    }
    
    public String getQuantityPercentage() {
        if (stockQuantity <= 0) return "0%";
        int percentage = (int) ((quantity * 100.0) / stockQuantity);
        return percentage + "%";
    }
    
    // === PRICE METHODS ===
    
    public void setPrice(double price) {
        this.price = Math.max(0.01, Math.min(price, maxPrice));
    }
    
    public void adjustPrice(int percentage) {
        double range = maxPrice - minPrice;
        double adjustment = (range * percentage) / 100.0;
        this.price = Math.max(minPrice, Math.min(maxPrice, this.price + adjustment));
    }
    
    public double getPrice() {
        return price;
    }
    
    public double getMinPrice() {
        return minPrice;
    }
    
    public double getMaxPrice() {
        return maxPrice;
    }
    
    public double getTotalPrice() {
        return quantity * price;
    }
    
    public String getPricePercentage() {
        if (maxPrice <= minPrice) return "50%";
        double range = maxPrice - minPrice;
        double current = price - minPrice;
        int percentage = (int) ((current * 100.0) / range);
        return percentage + "%";
    }
    
    // === STEP METHODS ===
    
    public Step getStep() {
        return currentStep;
    }
    
    public void setStep(Step step) {
        this.currentStep = step;
    }
    
    // === ITEM INFO ===
    
    public String getItemId() {
        return itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    // === PLACEHOLDERS ===
    
    /**
     * Génère les placeholders pour les menus
     */
    public Map<String, String> getPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        
        // Item
        placeholders.put("quantum_order_item_name", itemName);
        placeholders.put("quantum_order_item_id", itemId);
        
        // Quantité
        placeholders.put("quantum_order_quantity", String.valueOf(quantity));
        placeholders.put("quantum_order_max_quantity", String.valueOf(stockQuantity));
        placeholders.put("quantum_order_quantity_percentage", getQuantityPercentage());
        
        // Prix
        placeholders.put("quantum_order_price", String.format("%.2f", price));
        placeholders.put("quantum_order_min_price", String.format("%.2f", minPrice));
        placeholders.put("quantum_order_max_price", String.format("%.2f", maxPrice));
        placeholders.put("quantum_order_total_price", String.format("%.2f", getTotalPrice()));
        placeholders.put("quantum_order_price_percentage", getPricePercentage());
        
        return placeholders;
    }
    
    @Override
    public String toString() {
        return String.format("OrderCreationSession[item=%s, qty=%d, price=%.2f, step=%s]",
                itemName, quantity, price, currentStep);
    }
}
