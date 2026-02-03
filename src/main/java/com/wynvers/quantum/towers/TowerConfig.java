package com.wynvers.quantum.towers;

import java.util.List;

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
    
    public TowerConfig(String id, String name, String world, int totalFloors, 
                       List<Integer> bossFloors, int finalBossFloor) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.totalFloors = totalFloors;
        this.bossFloors = bossFloors;
        this.finalBossFloor = finalBossFloor;
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
}
