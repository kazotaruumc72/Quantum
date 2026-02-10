package com.wynvers.quantum.crops;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une culture personnalisée avec ses stades de croissance
 */
public class CustomCrop {
    
    private final String cropId;
    private final String displayName;
    private final String nexoIdBase;
    private final List<GrowthStage> growthStages;
    private final List<String> harvestCommands;
    private final List<CropDrop> drops;
    
    public CustomCrop(String cropId, String displayName, String nexoIdBase) {
        this.cropId = cropId;
        this.displayName = displayName;
        this.nexoIdBase = nexoIdBase;
        this.growthStages = new ArrayList<>();
        this.harvestCommands = new ArrayList<>();
        this.drops = new ArrayList<>();
    }
    
    public void addGrowthStage(int stage, String nexoId, int duration) {
        this.growthStages.add(new GrowthStage(stage, nexoId, duration));
    }
    
    public void addHarvestCommand(String command) {
        this.harvestCommands.add(command);
    }
    
    public void addDrop(CropDrop drop) {
        this.drops.add(drop);
    }
    
    public String getCropId() {
        return cropId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getNexoIdBase() {
        return nexoIdBase;
    }
    
    public List<GrowthStage> getGrowthStages() {
        return growthStages;
    }
    
    public List<String> getHarvestCommands() {
        return harvestCommands;
    }
    
    public List<CropDrop> getDrops() {
        return drops;
    }
    
    public int getMaxStage() {
        return growthStages.size();
    }
    
    public GrowthStage getStage(int stage) {
        for (GrowthStage gs : growthStages) {
            if (gs.getStage() == stage) {
                return gs;
            }
        }
        return null;
    }
    
    public boolean isMature(int currentStage) {
        return currentStage >= getMaxStage();
    }
    
    /**
     * Classe interne représentant un stade de croissance
     */
    public static class GrowthStage {
        private final int stage;
        private final String nexoId;
        private final int duration; // en secondes
        
        public GrowthStage(int stage, String nexoId, int duration) {
            this.stage = stage;
            this.nexoId = nexoId;
            this.duration = duration;
        }
        
        public int getStage() {
            return stage;
        }
        
        public String getNexoId() {
            return nexoId;
        }
        
        public int getDuration() {
            return duration;
        }
    }
}
