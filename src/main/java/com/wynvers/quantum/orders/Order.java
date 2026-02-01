package com.wynvers.quantum.orders;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Représente un ordre d'achat actif
 */
public class Order {
    private final UUID orderId;
    private final UUID playerId;
    private final String playerName;
    private final String itemId;
    private final int quantity;
    private final double pricePerUnit;
    private final Instant createdAt;
    private final Instant expiresAt;
    private OrderStatus status;

    public Order(UUID playerId, String playerName, String itemId, int quantity, double pricePerUnit, long durationDays) {
        this.orderId = UUID.randomUUID();
        this.playerId = playerId;
        this.playerName = playerName;
        this.itemId = itemId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plus(Duration.ofDays(durationDays));
        this.status = OrderStatus.ACTIVE;
    }

    // Constructor pour charger depuis DB
    public Order(UUID orderId, UUID playerId, String playerName, String itemId, int quantity, 
                 double pricePerUnit, Instant createdAt, Instant expiresAt, OrderStatus status) {
        this.orderId = orderId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.itemId = itemId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
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

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getTotalPrice() {
        return quantity * pricePerUnit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Vérifie si l'ordre est expiré
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Récupère le temps restant en secondes
     */
    public long getRemainingSeconds() {
        return Duration.between(Instant.now(), expiresAt).getSeconds();
    }

    /**
     * Formate le temps restant (jours, heures, minutes, secondes)
     */
    public String getFormattedTimeRemaining(String format) {
        if (isExpired()) {
            return "<red>Expiré";
        }

        long seconds = getRemainingSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return format
            .replace("%days%", String.valueOf(days))
            .replace("%hours%", String.valueOf(hours))
            .replace("%minutes%", String.valueOf(minutes))
            .replace("%seconds%", String.valueOf(secs));
    }
}
