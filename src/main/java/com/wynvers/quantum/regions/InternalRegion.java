package com.wynvers.quantum.regions;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a 3D cuboid region in a world
 * This is an internal implementation to replace WorldGuard dependency
 */
public class InternalRegion {
    
    private final String id;
    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    
    public InternalRegion(String id, String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.id = id;
        this.worldName = worldName;
        
        // Ensure min/max are correctly ordered
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }
    
    /**
     * Get the region ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the world name
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Check if a location is inside this region
     * Uses inclusive boundaries - blocks at exactly min or max coordinates are considered inside
     * This matches Minecraft's typical region behavior (e.g., WorldEdit, WorldGuard)
     * 
     * @param loc Location to check
     * @return true if location is inside region
     */
    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        
        // Check world
        if (!loc.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        // Check coordinates (inclusive boundaries)
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }
    
    /**
     * Get minimum X coordinate
     */
    public int getMinX() {
        return minX;
    }
    
    /**
     * Get minimum Y coordinate
     */
    public int getMinY() {
        return minY;
    }
    
    /**
     * Get minimum Z coordinate
     */
    public int getMinZ() {
        return minZ;
    }
    
    /**
     * Get maximum X coordinate
     */
    public int getMaxX() {
        return maxX;
    }
    
    /**
     * Get maximum Y coordinate
     */
    public int getMaxY() {
        return maxY;
    }
    
    /**
     * Get maximum Z coordinate
     */
    public int getMaxZ() {
        return maxZ;
    }
    
    @Override
    public String toString() {
        return "InternalRegion{" +
                "id='" + id + '\'' +
                ", world='" + worldName + '\'' +
                ", min=(" + minX + "," + minY + "," + minZ + ")" +
                ", max=(" + maxX + "," + maxY + "," + maxZ + ")" +
                '}';
    }
}
