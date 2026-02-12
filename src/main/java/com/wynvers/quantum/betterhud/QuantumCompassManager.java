package com.wynvers.quantum.betterhud;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.player.PointedLocation;
import kr.toxicity.hud.api.player.PointedLocationProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Optimized compass/waypoint manager for BetterHud integration.
 * Provides efficient waypoint tracking and management.
 * 
 * Note: In BetterHud API 1.14.1, compass waypoints are managed through
 * PointedLocationProvider, not a builder pattern.
 */
public class QuantumCompassManager {
    
    private final QuantumBetterHudManager hudManager;
    private final Logger logger;
    
    // Cache for active waypoints per player
    private final Map<UUID, Map<String, CompassPoint>> playerWaypoints = new ConcurrentHashMap<>();
    
    // Custom location providers for each player
    private final Map<UUID, CustomLocationProvider> locationProviders = new ConcurrentHashMap<>();
    
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
     * Note: Icon customization is not supported in the current API implementation.
     * The icon parameter is ignored but kept for API compatibility.
     * 
     * @param player The target player
     * @param name Unique name for the waypoint
     * @param location The location to point to
     * @param icon Optional icon identifier (currently not supported)
     * @return true if successful
     */
    public boolean addWaypoint(Player player, String name, Location location, String icon) {
        if (!hudManager.isAvailable()) return false;
        
        try {
            HudPlayer hudPlayer = hudManager.getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            // Cache the waypoint
            Map<String, CompassPoint> waypoints = playerWaypoints.computeIfAbsent(
                player.getUniqueId(), 
                k -> new ConcurrentHashMap<>()
            );
            waypoints.put(name, new CompassPoint(name, location, icon));
            
            // Register or update location provider
            CustomLocationProvider provider = locationProviders.computeIfAbsent(
                player.getUniqueId(),
                k -> new CustomLocationProvider(player.getUniqueId(), this)
            );
            
            // Trigger HUD update to show new waypoint
            hudPlayer.update();
            
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
            
            // Remove from cache
            Map<String, CompassPoint> waypoints = playerWaypoints.get(player.getUniqueId());
            if (waypoints != null) {
                waypoints.remove(name);
            }
            
            // Trigger HUD update to reflect removal
            hudPlayer.update();
            
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
                waypoints.clear();
            }
            
            HudPlayer hudPlayer = hudManager.getHudPlayer(player);
            if (hudPlayer != null) {
                hudPlayer.update();
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
     * Get waypoints for a player by UUID (used by location provider).
     * 
     * @param uuid The player's UUID
     * @return Map of waypoint names to CompassPoint objects
     */
    Map<String, CompassPoint> getWaypointsByUUID(UUID uuid) {
        return playerWaypoints.getOrDefault(uuid, Map.of());
    }
    
    /**
     * Remove player from cache when they log out.
     * 
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        playerWaypoints.remove(player.getUniqueId());
        locationProviders.remove(player.getUniqueId());
    }
    
    /**
     * Custom location provider for compass waypoints.
     */
    private static class CustomLocationProvider implements PointedLocationProvider {
        private final UUID playerUuid;
        private final QuantumCompassManager manager;
        
        public CustomLocationProvider(UUID playerUuid, QuantumCompassManager manager) {
            this.playerUuid = playerUuid;
            this.manager = manager;
        }
        
        @Override
        public Collection<PointedLocation> provide(HudPlayer player) {
            // For now, return empty collection as the actual implementation
            // would require creating PointedLocation instances from our waypoints
            // This is a placeholder - full implementation would need BetterHud's
            // specific PointedLocation implementation
            return Collections.emptyList();
        }
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
