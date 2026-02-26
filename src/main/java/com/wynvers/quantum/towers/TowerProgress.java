package com.wynvers.quantum.towers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks a player's progress through towers
 */
public class TowerProgress {
    
    private final UUID playerUuid;
    private final Map<String, Integer> towerProgress; // towerId -> highest floor completed
    private String currentTower;
    private int currentFloor;
    private final Map<String, Integer> runsByTower = new HashMap<>();
    // towerId -> (floor -> (mobKey -> kills this session))
    private final Map<String, Map<Integer, Map<String, Integer>>> floorMobKills = new HashMap<>();
    // towerId -> (floor -> start time in millis) for clear-time tracking
    private final Map<String, Map<Integer, Long>> floorStartTime = new HashMap<>();

    public int getRuns(String towerId) {
        return runsByTower.getOrDefault(towerId, 0);
    }
    
    public void incrementRuns(String towerId) {
        runsByTower.put(towerId, getRuns(towerId) + 1);
    }

    /**
     * Get the number of kills for a specific mob key on a specific floor this session.
     *
     * @param towerId tower ID
     * @param floor   floor number
     * @param mobKey  key as returned by {@link FloorMobRequirement#getKey()}
     */
    public int getFloorMobKills(String towerId, int floor, String mobKey) {
        Map<Integer, Map<String, Integer>> byFloor = floorMobKills.get(towerId);
        if (byFloor == null) return 0;
        Map<String, Integer> byMob = byFloor.get(floor);
        if (byMob == null) return 0;
        return byMob.getOrDefault(mobKey, 0);
    }

    /**
     * Get the total number of kills across all mob types on a specific floor this session.
     * Used for backward-compatible PlaceholderAPI placeholder.
     */
    public int getFloorMobKills(String towerId, int floor) {
        Map<Integer, Map<String, Integer>> byFloor = floorMobKills.get(towerId);
        if (byFloor == null) return 0;
        Map<String, Integer> byMob = byFloor.get(floor);
        if (byMob == null) return 0;
        return byMob.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Increment the kill counter for a specific mob key on a specific floor.
     *
     * @param towerId tower ID
     * @param floor   floor number
     * @param mobKey  key as returned by {@link FloorMobRequirement#getKey()}
     */
    public void incrementFloorMobKills(String towerId, int floor, String mobKey) {
        floorMobKills.computeIfAbsent(towerId, k -> new HashMap<>())
                     .computeIfAbsent(floor, k -> new HashMap<>())
                     .merge(mobKey, 1, Integer::sum);
    }

    /**
     * Reset all mob kill counters for a specific floor.
     */
    public void resetFloorMobKills(String towerId, int floor) {
        Map<Integer, Map<String, Integer>> byFloor = floorMobKills.get(towerId);
        if (byFloor != null) byFloor.remove(floor);
    }

    /**
     * Start the floor clear timer (records current system time).
     */
    public void startFloorTimer(String towerId, int floor) {
        floorStartTime.computeIfAbsent(towerId, k -> new HashMap<>())
                       .put(floor, System.currentTimeMillis());
    }

    /**
     * Get the elapsed time since the floor timer was started, in milliseconds.
     * Returns -1 if no timer was started.
     */
    public long getFloorElapsedTime(String towerId, int floor) {
        Map<Integer, Long> byFloor = floorStartTime.get(towerId);
        if (byFloor == null) return -1;
        Long start = byFloor.get(floor);
        if (start == null) return -1;
        return System.currentTimeMillis() - start;
    }

    /**
     * Clear the floor timer.
     */
    public void clearFloorTimer(String towerId, int floor) {
        Map<Integer, Long> byFloor = floorStartTime.get(towerId);
        if (byFloor != null) byFloor.remove(floor);
    }

    public TowerProgress(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.towerProgress = new HashMap<>();
        this.currentTower = null;
        this.currentFloor = 0;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    /**
     * Get highest floor completed in a tower
     * @param towerId Tower ID
     * @return Highest floor completed (0 if never entered)
     */
    public int getFloorProgress(String towerId) {
        return towerProgress.getOrDefault(towerId, 0);
    }
    
    /**
     * Set floor progress for a tower
     * @param towerId Tower ID
     * @param floor Floor number
     */
    public void setFloorProgress(String towerId, int floor) {
        int current = getFloorProgress(towerId);
        if (floor > current) {
            towerProgress.put(towerId, floor);
        }
    }
    
    /**
     * Get all tower progress
     * @return Map of tower ID to highest floor
     */
    public Map<String, Integer> getAllProgress() {
        return new HashMap<>(towerProgress);
    }
    
    /**
     * Get current tower player is in
     * @return Tower ID or null
     */
    public String getCurrentTower() {
        return currentTower;
    }
    
    /**
     * Set current tower
     * @param towerId Tower ID
     */
    public void setCurrentTower(String towerId) {
        this.currentTower = towerId;
    }
    
    /**
     * Get current floor player is on
     * @return Floor number
     */
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    /**
     * Set current floor
     * @param floor Floor number
     */
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }
    
    /**
     * Check if player has completed a specific floor
     * @param towerId Tower ID
     * @param floor Floor number
     * @return True if completed
     */
    public boolean hasCompletedFloor(String towerId, int floor) {
        return getFloorProgress(towerId) >= floor;
    }
    
    /**
     * Get total number of towers completed
     * @param towers Map of tower configs
     * @return Number of completed towers
     */
    public int getCompletedTowersCount(Map<String, TowerConfig> towers) {
        int count = 0;
        for (Map.Entry<String, TowerConfig> entry : towers.entrySet()) {
            String towerId = entry.getKey();
            TowerConfig config = entry.getValue();
            if (getFloorProgress(towerId) >= config.getFinalBossFloor()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get total floors completed across all towers
     * @return Total floor count
     */
    public int getTotalFloorsCompleted() {
        return towerProgress.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Reset all progress
     */
    public void reset() {
        towerProgress.clear();
        currentTower = null;
        currentFloor = 0;
    }
    
    /**
     * Reset progress for a specific tower
     * @param towerId Tower ID
     */
    public void resetTower(String towerId) {
        towerProgress.remove(towerId);
        if (towerId.equals(currentTower)) {
            currentTower = null;
            currentFloor = 0;
        }
    }
}
