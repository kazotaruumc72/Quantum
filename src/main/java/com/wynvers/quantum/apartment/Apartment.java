package com.wynvers.quantum.apartment;

import java.util.UUID;

/**
 * Represents an apartment that can be owned by a player
 * PREPARATION PHASE - This is a skeleton structure to be expanded later
 */
public class Apartment {
    
    private final int apartmentId;
    private UUID ownerId;
    private String apartmentName;
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;
    private int tier;
    private boolean isLocked;
    
    public Apartment(int apartmentId, UUID ownerId, String apartmentName) {
        this.apartmentId = apartmentId;
        this.ownerId = ownerId;
        this.apartmentName = apartmentName;
        this.tier = 1;
        this.isLocked = false;
    }
    
    public int getApartmentId() {
        return apartmentId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getApartmentName() {
        return apartmentName;
    }
    
    public void setApartmentName(String apartmentName) {
        this.apartmentName = apartmentName;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public void setLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public int getTier() {
        return tier;
    }
    
    public void setTier(int tier) {
        this.tier = tier;
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
}
