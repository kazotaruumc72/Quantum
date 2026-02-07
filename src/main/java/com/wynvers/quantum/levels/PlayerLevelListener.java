package com.wynvers.quantum.levels;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerLevelListener implements Listener {

    private final Quantum plugin;
    private final PlayerLevelManager levelManager;

    public PlayerLevelListener(Quantum plugin, PlayerLevelManager levelManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
    
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            levelManager.loadPlayer(uuid);
    
            // Une fois chargé, on applique la barre en sync
            Bukkit.getScheduler().runTask(plugin, () -> {
                levelManager.applyToBar(player);
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            levelManager.savePlayer(uuid);
            levelManager.unloadPlayer(uuid);
        });
    }
}
