package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener pour les événements liés aux métiers
 */
public class JobListener implements Listener {
    
    private final Quantum plugin;
    private final JobManager jobManager;
    
    public JobListener(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Charger les données de métier en async
        new BukkitRunnable() {
            @Override
            public void run() {
                jobManager.loadPlayer(event.getPlayer().getUniqueId());
            }
        }.runTaskAsynchronously(plugin);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Sauvegarder les données de métier en async
        new BukkitRunnable() {
            @Override
            public void run() {
                jobManager.savePlayer(event.getPlayer().getUniqueId());
                jobManager.unloadPlayer(event.getPlayer().getUniqueId());
            }
        }.runTaskAsynchronously(plugin);
    }
}
