package com.wynvers.quantum.transactions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a transaction in the Quantum trading system
 */
public class Transaction {
    private final String id;
    private final String buyer;
    private final String seller;
    private final String itemId;
    private final int quantity;
    private final double unitPrice;
    private final double totalPrice;
    private final long timestamp;
    private final String date;
    private final TransactionRole playerRole;
    
    public Transaction(String id, String buyer, String seller, String itemId, int quantity,
                      double unitPrice, double totalPrice, long timestamp, String date, TransactionRole playerRole) {
        this.id = id;
        this.buyer = buyer;
        this.seller = seller;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.date = date;
        this.playerRole = playerRole;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getBuyer() {
        return buyer;
    }
    
    public String getSeller() {
        return seller;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getDateString() {
        return date;
    }
    
    public LocalDateTime getDate() {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }
    
    public TransactionRole getPlayerRole() {
        return playerRole;
    }
    
    /**
     * Check if this is a buy order for the player
     */
    public boolean isBuyOrder() {
        return playerRole == TransactionRole.BUYER;
    }
    
    /**
     * Check if this is a sell order for the player
     */
    public boolean isSellOrder() {
        return playerRole == TransactionRole.SELLER;
    }
    
    /**
     * Get partner name (other player in transaction)
     */
    public String getPartnerName() {
        return isBuyOrder() ? seller : buyer;
    }
    
    /**
     * Get item material for display
     */
    public String getItemMaterial() {
        if (itemId.contains(":")) {
            return itemId.split(":")[1].toUpperCase();
        }
        return itemId.toUpperCase();
    }
    
    /**
     * Get formatted item name
     */
    public String getItemName() {
        String name = itemId;
        if (name.startsWith("nexo:")) {
            name = name.substring(5);
        } else if (name.startsWith("minecraft:")) {
            name = name.substring(10);
        }
        return formatName(name);
    }
    
    /**
     * Format item name for display
     */
    private String formatName(String name) {
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase())
                  .append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }
    
    /**
     * Transaction role enum
     */
    public enum TransactionRole {
        BUYER,
        SELLER
    }
}
