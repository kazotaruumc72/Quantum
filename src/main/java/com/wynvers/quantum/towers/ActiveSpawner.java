package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ActiveSpawner {
    
    private final Quantum plugin;
    private final TowerSpawnerConfig config;
    private final Player player;
    private final String towerId;
    private final TowerManager towerManager;
    private final Set<UUID> aliveMobs = new HashSet<>();
    
    private BukkitTask task;

    public ActiveSpawner(Quantum plugin, TowerSpawnerConfig config, Player player, 
                         String towerId, TowerManager towerManager) {
        this.plugin = plugin;
        this.config = config;
        this.player = player;
        this.towerId = towerId;
        this.towerManager = towerManager;
    }

    public void start() {
        int periodTicks = config.getIntervalSeconds() * 20;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Nettoyage des morts
                aliveMobs.removeIf(uuid -> {
                    Entity e = Bukkit.getEntity(uuid);
                    return e == null || e.isDead();
                });

                if (aliveMobs.size() >= config.getMaxAlive()) {
                    return;
                }

                int runs = getRunsForPlayer();
                int targetAmount = config.getAmountForRuns(runs);
                int spaceLeft = config.getMaxAlive() - aliveMobs.size();
                int toSpawn = Math.min(targetAmount, spaceLeft);
                if (toSpawn <= 0) return;

                for (int i = 0; i < toSpawn; i++) {
                    LivingEntity mob = spawnOneMobNearPlayer(player.getLocation());
                    if (mob != null) {
                        aliveMobs.add(mob.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, periodTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        task = null;
    }

    private int getRunsForPlayer() {
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        return progress.getRuns(towerId);
    }

    private LivingEntity spawnOneMobNearPlayer(Location base) {
        World world = base.getWorld();
        if (world == null) return null;

        Location spawnLoc = base.clone().add(
                ThreadLocalRandom.current().nextDouble(-3, 3),
                0,
                ThreadLocalRandom.current().nextDouble(-3, 3)
        );

        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, config.getType());

        int runs = getRunsForPlayer();
        double health = config.getHealthForRuns(runs);
        entity.setMaxHealth(health);
        entity.setHealth(health);

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "tower_spawner"), PersistentDataType.STRING, config.getId());
        pdc.set(new NamespacedKey(plugin, "tower_id"), PersistentDataType.STRING, towerId);

        return entity;
    }
}
