package com.wynvers.quantum.betterhud;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for BetterHud integration.
 * Handles cleanup and cache management.
 */
public class BetterHudListener implements Listener {
    
    private final QuantumBetterHudManager hudManager;
    private final QuantumCompassManager compassManager;
    
    public BetterHudListener(QuantumBetterHudManager hudManager, QuantumCompassManager compassManager) {
        this.hudManager = hudManager;
        this.compassManager = compassManager;
    }
    
    /**
     * Clean up player data when they quit.
     * Optimizes memory usage by removing cached data.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove from HUD manager cache
        hudManager.removePlayer(event.getPlayer());
        
        // Remove from compass manager cache
        compassManager.removePlayer(event.getPlayer());
    }
}
