package com.wynvers.quantum.towers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration data for a tower
 */
public class TowerConfig {
    
    private final String id;
    private final String name;
    private final String world;
    private final int totalFloors;
    private final List<Integer> bossFloors;
    private final int finalBossFloor;
    private final List<String> scoreboardLines;
    private final int minLevel;
    private final int maxLevel;
    
    // Map floor number -> WorldGuard region name
    private final Map<Integer, String> floorRegions;
    
    public TowerConfig(String id, String name, String world, int totalFloors, List<Integer> bossFloors, int finalBossFloor, int minLevel, int maxLevel) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.totalFloors = totalFloors;
        this.bossFloors = bossFloors;
        this.finalBossFloor = finalBossFloor;
        this.scoreboardLines = new java.util.ArrayList<>();
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.floorRegions = new HashMap<>();
    }
    
    public int getMinLevel() { 
        return minLevel; 
    }
    
    public int getMaxLevel() { 
        return maxLevel; 
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String world() {
        return world;
    }
    
    public int getTotalFloors() {
        return totalFloors;
    }
    
    /**
     * Alias for getTotalFloors() for compatibility
     * @return Total number of regular floors
     */
    public int getFloors() {
        return totalFloors;
    }
    
    public List<Integer> getBossFloors() {
        return bossFloors;
    }
    
    public int getFinalBossFloor() {
        return finalBossFloor;
    }
    
    public boolean isBossFloor(int floor) {
        return bossFloors.contains(floor);
    }
    
    public boolean isFinalBoss(int floor) {
        return floor == finalBossFloor;
    }

    public String getWorldGuardRegion() {
        return worldGuardRegion;  // ou le nom du champ que tu utilises
    }
    
    /**
     * Get next boss floor after current floor
     * @param currentFloor Current floor number
     * @return Next boss floor or -1 if none
     */
    public int getNextBossFloor(int currentFloor) {
        for (int bossFloor : bossFloors) {
            if (bossFloor > currentFloor) {
                return bossFloor;
            }
        }
        // Check final boss
        if (finalBossFloor > currentFloor) {
            return finalBossFloor;
        }
        return -1;
    }

    public List<String> getScoreboardLines() {
        return scoreboardLines;
    }
    
    public void setScoreboardLines(List<String> lines) {
        this.scoreboardLines.clear();
        this.scoreboardLines.addAll(lines);
    }
    
    /**
     * Set the WorldGuard region name for a specific floor
     */
    public void setFloorRegion(int floor, String regionName) {
        floorRegions.put(floor, regionName);
    }
    
    /**
     * Get the WorldGuard region name for a specific floor
     * @return region name or null if not configured
     */
    public String getFloorRegion(int floor) {
        return floorRegions.get(floor);
    }
    
    /**
     * Get all floor regions
     */
    public Map<Integer, String> getAllFloorRegions() {
        return new HashMap<>(floorRegions);
    }
    
    /**
     * Check if a WorldGuard region belongs to this tower
     * @param regionName WorldGuard region name
     * @return floor number, or -1 if not found
     */
    public int getFloorByRegion(String regionName) {
        for (Map.Entry<Integer, String> entry : floorRegions.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(regionName)) {
                return entry.getKey();
            }
        }
        return -1;
    }
}
