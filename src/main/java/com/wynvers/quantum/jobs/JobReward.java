package com.wynvers.quantum.jobs;

import java.util.List;

/**
 * Représente une récompense de niveau pour un métier
 */
public class JobReward {
    
    private final String type;
    private final String value;
    private final int amount;
    private final int duration;
    private final boolean dungeonOnly;
    
    public JobReward(String type, String value, int amount, int duration, boolean dungeonOnly) {
        this.type = type;
        this.value = value;
        this.amount = amount;
        this.duration = duration;
        this.dungeonOnly = dungeonOnly;
    }
    
    public String getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public boolean isDungeonOnly() {
        return dungeonOnly;
    }
}
