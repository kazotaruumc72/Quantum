package com.wynvers.quantum.betterhud;

import kr.toxicity.hud.api.BetterHudAPI;
import kr.toxicity.hud.api.bukkit.BetterHudBukkitAPI;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.popup.PopupUpdater;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Optimized BetterHud manager for Quantum plugin.
 * Provides cached access to HUD players and efficient popup management.
 */
public class QuantumBetterHudManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private BetterHudBukkitAPI betterHudAPI;
    
    // Optimized cache for HUD players to avoid repeated lookups
    private final Map<UUID, HudPlayer> playerCache = new ConcurrentHashMap<>();
    
    // Cache for active popups to prevent duplicate displays
    private final Map<UUID, Map<String, Long>> activePopups = new ConcurrentHashMap<>();
    
    private static final long POPUP_COOLDOWN_MS = 100; // Minimum time between same popup shows
    
    public QuantumBetterHudManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Initialize the BetterHud integration.
     * Should be called after BetterHud plugin is loaded.
     */
    public void initialize() {
        try {
            betterHudAPI = BetterHudAPI.inst().bukkit();
            logger.info("BetterHud integration initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize BetterHud integration: " + e.getMessage());
        }
    }
    
    /**
     * Get HUD player with caching for better performance.
     * @param player The Bukkit player
     * @return HudPlayer instance or null if not available
     */
    public HudPlayer getHudPlayer(Player player) {
        if (betterHudAPI == null) return null;
        
        return playerCache.computeIfAbsent(player.getUniqueId(), uuid -> {
            try {
                return betterHudAPI.getHudPlayer(uuid);
            } catch (Exception e) {
                logger.warning("Failed to get HUD player for " + player.getName() + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Remove player from cache when they log out.
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        playerCache.remove(player.getUniqueId());
        activePopups.remove(player.getUniqueId());
    }
    
    /**
     * Show a popup to a player with cooldown optimization.
     * Prevents spam by enforcing a minimum cooldown between identical popups.
     * 
     * @param player The target player
     * @param popupName The name of the popup to show
     * @return true if popup was shown, false if on cooldown or failed
     */
    public boolean showPopup(Player player, String popupName) {
        return showPopup(player, popupName, Map.of());
    }
    
    /**
     * Show a popup to a player with variables and cooldown optimization.
     * 
     * @param player The target player
     * @param popupName The name of the popup to show
     * @param variables Variables to pass to the popup
     * @return true if popup was shown, false if on cooldown or failed
     */
    public boolean showPopup(Player player, String popupName, Map<String, String> variables) {
        if (betterHudAPI == null) return false;
        
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerPopups = activePopups.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        // Check cooldown
        long now = System.currentTimeMillis();
        Long lastShow = playerPopups.get(popupName);
        if (lastShow != null && (now - lastShow) < POPUP_COOLDOWN_MS) {
            return false; // On cooldown
        }
        
        try {
            HudPlayer hudPlayer = getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            PopupUpdater updater = hudPlayer.showPopup(popupName);
            if (updater == null) return false;
            
            // Apply variables if provided
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(updater::addVariable);
            }
            
            playerPopups.put(popupName, now);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to show popup '" + popupName + "' to " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove a popup from a player.
     * 
     * @param player The target player
     * @param popupName The name of the popup to remove
     * @return true if successful
     */
    public boolean removePopup(Player player, String popupName) {
        if (betterHudAPI == null) return false;
        
        try {
            HudPlayer hudPlayer = getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            hudPlayer.removePopup(popupName);
            
            // Clear from active popups cache
            Map<String, Long> playerPopups = activePopups.get(player.getUniqueId());
            if (playerPopups != null) {
                playerPopups.remove(popupName);
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("Failed to remove popup '" + popupName + "' from " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update a HUD for a player.
     * 
     * @param player The target player
     * @param hudName The name of the HUD to update
     * @param event The update event to trigger
     * @return true if successful
     */
    public boolean updateHud(Player player, String hudName, UpdateEvent event) {
        if (betterHudAPI == null) return false;
        
        try {
            HudPlayer hudPlayer = getHudPlayer(player);
            if (hudPlayer == null) return false;
            
            hudPlayer.update(event);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to update HUD for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if BetterHud is available and initialized.
     * 
     * @return true if BetterHud is available
     */
    public boolean isAvailable() {
        return betterHudAPI != null;
    }
    
    /**
     * Clear all caches. Useful for reload operations.
     */
    public void clearCaches() {
        playerCache.clear();
        activePopups.clear();
    }
    
    /**
     * Get the BetterHud API instance directly for advanced operations.
     * 
     * @return BetterHudBukkitAPI instance
     */
    public BetterHudBukkitAPI getAPI() {
        return betterHudAPI;
    }
}
