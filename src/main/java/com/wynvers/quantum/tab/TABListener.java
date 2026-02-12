package com.wynvers.quantum.tab;

import com.wynvers.quantum.Quantum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for TAB-related events
 * Handles player join/quit to update tab displays
 */
public class TABListener implements Listener {
    
    private final Quantum plugin;
    
    public TABListener(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay slightly to ensure all data is loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online before updating
                if (event.getPlayer().isOnline() && plugin.getTabManager() != null && plugin.getTabManager().isEnabled()) {
                    plugin.getTabManager().updatePlayer(event.getPlayer());
                }
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup if needed (currently not needed as TAB handles it)
    }
}
