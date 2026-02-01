package com.wynvers.quantum.orders;

import java.util.UUID;

/**
 * Représente un ordre d'achat
 */
public class Order {
    private final UUID orderId;
    private final UUID playerId;
    private final String playerName;
    private final String itemId;
    private final String category;
    private final int quantity;
    private final double pricePerUnit;
    private final long createdAt;
    private final long expiresAt;
    private OrderStatus status;
    private int remainingQuantity;

    public Order(UUID playerId, String playerName, String itemId, String category, int quantity, double pricePerUnit, long durationDays) {
        this.orderId = UUID.randomUUID();
        this.playerId = playerId;
        this.playerName = playerName;
        this.itemId = itemId;
        this.category = category;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + (durationDays * 24 * 60 * 60 * 1000);
        this.status = OrderStatus.ACTIVE;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getItemId() {
        return itemId;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getTotalPrice() {
        return remainingQuantity * pricePerUnit;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getTimeRemaining() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    /**
     * Formate le temps restant
     */
    public String getFormattedTimeRemaining(String format) {
        long remaining = getTimeRemaining();
        
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        return format
            .replace("%days%", String.valueOf(days))
            .replace("%hours%", String.valueOf(hours))
            .replace("%minutes%", String.valueOf(minutes))
            .replace("%seconds%", String.valueOf(seconds));
    }
}
