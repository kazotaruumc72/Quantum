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
    private final List<String> validNexoBlocks;
    private final List<String> validNexoFurniture;
    private final Map<String, Boolean> allowedActions;  // Action type -> enabled
    private final Map<Integer, List<JobReward>> levelRewards;
    
    public Job(String id, String displayName, List<String> description, String icon, 
               int maxLevel, List<String> validStructures, List<String> validNexoBlocks,
               List<String> validNexoFurniture, Map<String, Boolean> allowedActions) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.validStructures = validStructures;
        this.validNexoBlocks = validNexoBlocks != null ? validNexoBlocks : new ArrayList<>();
        this.validNexoFurniture = validNexoFurniture != null ? validNexoFurniture : new ArrayList<>();
        this.allowedActions = allowedActions != null ? allowedActions : new HashMap<>();
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
    
    public List<String> getValidNexoBlocks() {
        return validNexoBlocks;
    }
    
    public List<String> getValidNexoFurniture() {
        return validNexoFurniture;
    }
    
    public Map<String, Boolean> getAllowedActions() {
        return allowedActions;
    }
    
    public boolean isActionAllowed(String actionType) {
        return allowedActions.getOrDefault(actionType, false);
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
    
    public boolean isValidNexoBlock(String blockId) {
        return validNexoBlocks.contains(blockId);
    }
    
    public boolean isValidNexoFurniture(String furnitureId) {
        return validNexoFurniture.contains(furnitureId);
    }
}
