package com.wynvers.quantum.worldguard.gui;

import org.bukkit.entity.EntityType;

import java.util.*;

/**
 * Configuration for a WorldGuard zone
 * Stores zone settings including mob spawning rules
 */
public class ZoneConfig {
    
    private final String regionName;
    private boolean pvpEnabled;
    private boolean mobSpawning;
    private Set<EntityType> allowedMobs;
    private Set<EntityType> deniedMobs;
    private Map<String, Boolean> flags;
    
    public ZoneConfig(String regionName) {
        this.regionName = regionName;
        this.pvpEnabled = false;
        this.mobSpawning = true;
        this.allowedMobs = new HashSet<>();
        this.deniedMobs = new HashSet<>();
        this.flags = new HashMap<>();
    }
    
    public String getRegionName() {
        return regionName;
    }
    
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
    
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
    
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }
    
    public Set<EntityType> getAllowedMobs() {
        return new HashSet<>(allowedMobs);
    }
    
    public Set<EntityType> getDeniedMobs() {
        return new HashSet<>(deniedMobs);
    }
    
    public void addAllowedMob(EntityType mob) {
        allowedMobs.add(mob);
        deniedMobs.remove(mob);
    }
    
    public void addDeniedMob(EntityType mob) {
        deniedMobs.add(mob);
        allowedMobs.remove(mob);
    }
    
    public void removeAllowedMob(EntityType mob) {
        allowedMobs.remove(mob);
    }
    
    public void removeDeniedMob(EntityType mob) {
        deniedMobs.remove(mob);
    }
    
    public boolean isMobAllowed(EntityType mob) {
        if (deniedMobs.contains(mob)) {
            return false;
        }
        // If allowedMobs is empty, all mobs are allowed (except denied)
        return allowedMobs.isEmpty() || allowedMobs.contains(mob);
    }
    
    public Map<String, Boolean> getFlags() {
        return new HashMap<>(flags);
    }
    
    public void setFlag(String flagName, boolean value) {
        flags.put(flagName, value);
    }
    
    public boolean getFlag(String flagName, boolean defaultValue) {
        return flags.getOrDefault(flagName, defaultValue);
    }
}
