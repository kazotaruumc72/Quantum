package com.wynvers.quantum.healthbar;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for automatic health bar management
 */
public class HealthBarListener implements Listener {

    private final Quantum plugin;
    private final HealthBarManager manager;

    public HealthBarListener(Quantum plugin, HealthBarManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        Player player = event.getPlayer();

        // Apply health bar to player if enabled
        if (manager.getConfig().isPlayersEnabled() && manager.getConfig().isShowOnPlayers()) {
            // Delay to ensure player is fully loaded
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                manager.applyHealthBar(player);
            }, 20L); // 1 second delay
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        manager.removeHealthBar(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // Apply health bar to mob if enabled
        if (manager.getConfig().isMobsEnabled() && manager.getConfig().isShowOnMobs()) {
            // Delay to ensure entity is fully loaded
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (entity.isValid() && !entity.isDead()) {
                    manager.applyHealthBar(entity);
                }
            }, 5L); // 0.25 second delay
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // Update health bar when entity takes damage
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                manager.updateHealthBar(entity);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // Update health bar when entity regains health
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                manager.updateHealthBar(entity);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!manager.isAvailable()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        manager.removeHealthBar(entity);
    }
}
