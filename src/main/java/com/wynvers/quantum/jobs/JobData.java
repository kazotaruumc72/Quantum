package com.wynvers.quantum.jobs;

import java.util.UUID;

/**
 * Stocke les données de progression d'un joueur pour un métier
 */
public class JobData {
    
    private final UUID playerUUID;
    private final String jobId;
    private int level;
    private int exp;
    
    public JobData(UUID playerUUID, String jobId, int level, int exp) {
        this.playerUUID = playerUUID;
        this.jobId = jobId;
        this.level = level;
        this.exp = exp;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getExp() {
        return exp;
    }
    
    public void setExp(int exp) {
        this.exp = exp;
    }
    
    /**
     * Alias for getExp() for PlaceholderAPI compatibility
     */
    public int getExperience() {
        return exp;
    }
}
