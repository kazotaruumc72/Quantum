package com.wynvers.quantum.betterhud;

import kr.toxicity.hud.api.compass.Compass;
import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Optimized compass/waypoint manager for BetterHud integration.
 * Provides efficient waypoint tracking and management.
 */
public class QuantumCompassManager {
    
    private final QuantumBetterHudManager hudManager;
    private final Logger logger;
    
    // Cache for active waypoints per player
    private final Map<UUID, Map<String, CompassPoint>> playerWaypoints = new ConcurrentHashMap<>();
    
    public QuantumCompassManager(QuantumBetterHudManager hudManager, Logger logger) {
        this.hudManager = hudManager;
        this.logger = logger;
    }
    
    /**
     * Add a compass waypoint for a player.
     * 
     * @param player The target player
     * @param name Unique name for the waypoint
     * @param location The location to point to
     * @return true if successful
     */
    public boolean addWaypoint(Player player, String name, Location location) {
        return addWaypoint(player, name, location, null);
    }
    
    /**
     * Add a compass waypoint for a player with a custom icon.
     * 
     * @param player The target player
     * @param name Unique name for the waypoint
     * @param location The location to point to
     * @param icon Optional icon identifier
     * @return true if successful
     */
    public boolean addWaypoint(Player player, String name, Location location, String icon) {
        if (!hudManager.isAvailable()) return false;
        
        try {
            HudPlayer hudPlayer = hudManager.getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            // Create compass point
            Compass.Builder builder = Compass.builder()
                .name(name)
                .location(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            
            if (icon != null && !icon.isEmpty()) {
                builder.icon(icon);
            }
            
            Compass compass = builder.build();
            hudPlayer.compass().add(compass);
            
            // Cache the waypoint
            Map<String, CompassPoint> waypoints = playerWaypoints.computeIfAbsent(
                player.getUniqueId(), 
                k -> new ConcurrentHashMap<>()
            );
            waypoints.put(name, new CompassPoint(name, location, icon));
            
            return true;
        } catch (Exception e) {
            logger.warning("Failed to add waypoint '" + name + "' for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove a compass waypoint for a player.
     * 
     * @param player The target player
     * @param name The name of the waypoint to remove
     * @return true if successful
     */
    public boolean removeWaypoint(Player player, String name) {
        if (!hudManager.isAvailable()) return false;
        
        try {
            HudPlayer hudPlayer = hudManager.getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            hudPlayer.compass().remove(name);
            
            // Remove from cache
            Map<String, CompassPoint> waypoints = playerWaypoints.get(player.getUniqueId());
            if (waypoints != null) {
                waypoints.remove(name);
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("Failed to remove waypoint '" + name + "' for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove all waypoints for a player.
     * 
     * @param player The target player
     * @return true if successful
     */
    public boolean clearWaypoints(Player player) {
        if (!hudManager.isAvailable()) return false;
        
        try {
            Map<String, CompassPoint> waypoints = playerWaypoints.get(player.getUniqueId());
            if (waypoints != null) {
                // Create a copy of keys to avoid ConcurrentModificationException
                java.util.List<String> waypointNames = new java.util.ArrayList<>(waypoints.keySet());
                
                // Remove each waypoint
                for (String name : waypointNames) {
                    removeWaypoint(player, name);
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("Failed to clear waypoints for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all active waypoints for a player.
     * 
     * @param player The target player
     * @return Map of waypoint names to CompassPoint objects
     */
    public Map<String, CompassPoint> getWaypoints(Player player) {
        return playerWaypoints.getOrDefault(player.getUniqueId(), Map.of());
    }
    
    /**
     * Remove player from cache when they log out.
     * 
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        playerWaypoints.remove(player.getUniqueId());
    }
    
    /**
     * Represents a compass point/waypoint.
     */
    public static class CompassPoint {
        private final String name;
        private final Location location;
        private final String icon;
        
        public CompassPoint(String name, Location location, String icon) {
            this.name = name;
            this.location = location;
            this.icon = icon;
        }
        
        public String getName() {
            return name;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public String getIcon() {
            return icon;
        }
    }
}
