package com.wynvers.quantum.regions;

import com.wynvers.quantum.Quantum;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages internal regions (replaces WorldGuard dependency)
 * Loads and queries regions from configuration
 */
public class InternalRegionManager {
    
    private final Quantum plugin;
    private final Map<String, InternalRegion> regions;
    
    public InternalRegionManager(Quantum plugin) {
        this.plugin = plugin;
        this.regions = new HashMap<>();
    }
    
    /**
     * Register a region
     * @param region Region to register
     */
    public void registerRegion(InternalRegion region) {
        regions.put(region.getId().toLowerCase(), region);
        plugin.getQuantumLogger().info("Registered region: " + region.getId());
    }
    
    /**
     * Get a region by ID
     * @param id Region ID
     * @return InternalRegion or null if not found
     */
    public InternalRegion getRegion(String id) {
        return regions.get(id.toLowerCase());
    }
    
    /**
     * Get the first region that contains the given location
     * @param loc Location to check
     * @return Region ID or null if no region contains the location
     */
    public String getRegionAt(Location loc) {
        if (loc == null) {
            return null;
        }
        
        // Check all regions and return the first one that contains the location
        for (InternalRegion region : regions.values()) {
            if (region.contains(loc)) {
                return region.getId();
            }
        }
        
        return null;
    }
    
    /**
     * Load a region from configuration section
     * Expected format:
     * region_name:
     *   world: world_name
     *   min: x,y,z
     *   max: x,y,z
     * 
     * @param regionId Region ID
     * @param config Configuration section containing region data
     * @return true if loaded successfully
     */
    public boolean loadRegionFromConfig(String regionId, ConfigurationSection config) {
        try {
            String world = config.getString("world");
            if (world == null || world.isEmpty()) {
                plugin.getQuantumLogger().warning("Region " + regionId + " has no world specified");
                return false;
            }
            
            String minStr = config.getString("min");
            String maxStr = config.getString("max");
            
            if (minStr == null || maxStr == null) {
                plugin.getQuantumLogger().warning("Region " + regionId + " has no min/max coordinates");
                return false;
            }
            
            // Parse coordinates
            String[] minParts = minStr.split(",");
            String[] maxParts = maxStr.split(",");
            
            if (minParts.length != 3 || maxParts.length != 3) {
                plugin.getQuantumLogger().warning("Region " + regionId + " has invalid coordinate format");
                return false;
            }
            
            int minX = Integer.parseInt(minParts[0].trim());
            int minY = Integer.parseInt(minParts[1].trim());
            int minZ = Integer.parseInt(minParts[2].trim());
            
            int maxX = Integer.parseInt(maxParts[0].trim());
            int maxY = Integer.parseInt(maxParts[1].trim());
            int maxZ = Integer.parseInt(maxParts[2].trim());
            
            InternalRegion region = new InternalRegion(regionId, world, minX, minY, minZ, maxX, maxY, maxZ);
            registerRegion(region);
            
            return true;
            
        } catch (NumberFormatException e) {
            plugin.getQuantumLogger().error("Failed to parse coordinates for region " + regionId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clear all regions
     */
    public void clear() {
        regions.clear();
    }
    
    /**
     * Get the number of registered regions
     */
    public int getRegionCount() {
        return regions.size();
    }
    
    /**
     * Check if a region exists
     */
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }
}
