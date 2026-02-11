package com.wynvers.quantum.jobs;

import java.util.UUID;

/**
 * Représente un booster actif pour un joueur
 */
public class ActiveBooster {
    
    private final UUID playerUUID;
    private final String boosterType;  // "exp_booster" ou "money_booster"
    private final double multiplier;
    private final boolean dungeonOnly;
    private final long expirationTime;  // System.currentTimeMillis() + duration
    
    public ActiveBooster(UUID playerUUID, String boosterType, double multiplier, 
                        boolean dungeonOnly, long expirationTime) {
        this.playerUUID = playerUUID;
        this.boosterType = boosterType;
        this.multiplier = multiplier;
        this.dungeonOnly = dungeonOnly;
        this.expirationTime = expirationTime;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getBoosterType() {
        return boosterType;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public boolean isDungeonOnly() {
        return dungeonOnly;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
    
    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
}
