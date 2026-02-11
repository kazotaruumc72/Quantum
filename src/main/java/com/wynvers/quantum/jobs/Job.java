package com.wynvers.quantum.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition d'un métier
 */
public class Job {
    
    private final String id;
    private final String displayName;
    private final List<String> description;
    private final String icon;
    private final int maxLevel;
    private final List<String> validStructures;
    private final Map<Integer, List<JobReward>> levelRewards;
    
    public Job(String id, String displayName, List<String> description, String icon, 
               int maxLevel, List<String> validStructures) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.validStructures = validStructures;
        this.levelRewards = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    public List<String> getValidStructures() {
        return validStructures;
    }
    
    public void addLevelReward(int level, JobReward reward) {
        levelRewards.computeIfAbsent(level, k -> new ArrayList<>()).add(reward);
    }
    
    public List<JobReward> getLevelRewards(int level) {
        return levelRewards.getOrDefault(level, new ArrayList<>());
    }
    
    public Map<Integer, List<JobReward>> getAllLevelRewards() {
        return levelRewards;
    }
    
    public boolean isValidStructure(String structureId) {
        return validStructures.contains(structureId);
    }
}
