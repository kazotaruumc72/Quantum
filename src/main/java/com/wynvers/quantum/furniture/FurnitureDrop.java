package com.wynvers.quantum.furniture;

/**
 * Représente un drop possible d'un furniture
 */
public class FurnitureDrop {
    
    private final String nexoId;      // ID Nexo de l'item (null si Minecraft)
    private final String minecraftType; // Type Minecraft (null si Nexo)
    private final int minAmount;
    private final int maxAmount;
    private final double chance;      // % de chance (0-100)
    
    public FurnitureDrop(String nexoId, String minecraftType, int minAmount, int maxAmount, double chance) {
        this.nexoId = nexoId;
        this.minecraftType = minecraftType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
    }
    
    public boolean isNexoItem() {
        return nexoId != null;
    }
    
    public String getNexoId() {
        return nexoId;
    }
    
    public String getMinecraftType() {
        return minecraftType;
    }
    
    public int getMinAmount() {
        return minAmount;
    }
    
    public int getMaxAmount() {
        return maxAmount;
    }
    
    public double getChance() {
        return chance;
    }
    
    /**
     * Calcule la quantité à dropper (entre min et max)
     */
    public int calculateAmount() {
        if (minAmount == maxAmount) {
            return minAmount;
        }
        return minAmount + (int) (Math.random() * (maxAmount - minAmount + 1));
    }
    
    /**
     * Vérifie si ce drop doit être donné (basé sur le % de chance)
     */
    public boolean shouldDrop() {
        return Math.random() * 100 < chance;
    }
}
