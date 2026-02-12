package com.wynvers.quantum.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener to handle first join teleportation
 * Teleports new players to the first spawn location if set
 */
public class FirstJoinListener implements Listener {

    private final SpawnManager spawnManager;

    public FirstJoinListener(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if this is the player's first join
        if (!player.hasPlayedBefore()) {
            Location firstSpawn = spawnManager.getFirstSpawn();
            
            if (firstSpawn != null) {
                player.teleport(firstSpawn);
            }
        }
    }
}
