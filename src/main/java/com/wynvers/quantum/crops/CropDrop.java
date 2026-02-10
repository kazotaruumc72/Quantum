package com.wynvers.quantum.crops;

/**
 * Représente un drop possible d'une culture
 */
public class CropDrop {
    
    private final String nexoId;
    private final String minecraftType;
    private final int minAmount;
    private final int maxAmount;
    private final double chance;
    
    public CropDrop(String nexoId, String minecraftType, int minAmount, int maxAmount, double chance) {
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
    
    public int calculateAmount() {
        if (minAmount == maxAmount) {
            return minAmount;
        }
        return minAmount + (int) (Math.random() * (maxAmount - minAmount + 1));
    }
    
    public boolean shouldDrop() {
        return Math.random() * 100 < chance;
    }
}
