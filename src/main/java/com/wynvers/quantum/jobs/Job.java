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
    private final Map<String, double[]> validOnHitGenerators;
    private final Map<String, double[]> validNexoBlocks;
    private final Map<String, double[]> validNexoFurniture;
    // Maps mob type -> [exp, money] for vanilla mobs (-1 means use global default)
    private final Map<String, double[]> mobRewards;
    private final Map<String, double[]> mmobsRewards;
    private final Map<String, Boolean> allowedActions;  // Action type -> enabled
    private final Map<Integer, List<JobReward>> levelRewards;

    public Job(String id, String displayName, List<String> description, String icon,
               int maxLevel, Map<String, double[]> validOnHitGenerators,
               Map<String, double[]> validNexoBlocks, Map<String, double[]> validNexoFurniture,
               Map<String, double[]> mobRewards, Map<String, double[]> mmobsRewards,
               Map<String, Boolean> allowedActions) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.validOnHitGenerators = validOnHitGenerators != null ? validOnHitGenerators : new HashMap<>();
        this.validNexoBlocks = validNexoBlocks != null ? validNexoBlocks : new HashMap<>();
        this.validNexoFurniture = validNexoFurniture != null ? validNexoFurniture : new HashMap<>();
        this.mobRewards = mobRewards != null ? mobRewards : new HashMap<>();
        this.mmobsRewards = mmobsRewards != null ? mmobsRewards : new HashMap<>();
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

    public Map<String, double[]> getValidOnHitGenerators() {
        return validOnHitGenerators;
    }

    public Map<String, double[]> getValidNexoBlocks() {
        return validNexoBlocks;
    }

    public Map<String, double[]> getValidNexoFurniture() {
        return validNexoFurniture;
    }

    /**
     * Returns per-mob rewards for vanilla mobs. Array: [exp, money] (-1 = use global default).
     */
    public Map<String, double[]> getMobRewards() {
        return mobRewards;
    }

    public Map<String, double[]> getMmobsRewards() {
        return mmobsRewards;
    }
    
    public Map<String, Boolean> getAllowedActions() {
        return allowedActions;
    }
    
    public boolean isActionAllowed(String actionType) {
        if (allowedActions.isEmpty()) return true;
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

    /**
     * Returns the per-mob rewards for a vanilla mob, or null if not defined.
     */
    public double[] getMobReward(String mobType) {
        return mobRewards.get(mobType);
    }
}
