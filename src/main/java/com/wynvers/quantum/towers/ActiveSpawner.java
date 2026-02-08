package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ActiveSpawner implements Listener {
    
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
        
        // Enregistrer le listener pour détecter la mort des mobs
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
                    LivingEntity mob = spawnOneMob();
                    if (mob != null) {
                        aliveMobs.add(mob.getUniqueId());
                        
                        // Démarrer les skills du mob
                        startMobSkills(mob);
                        
                        // Enregistrer les animations du mob
                        registerMobAnimations(mob);
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
        
        // Arrêter tous les skills des mobs vivants
        MobSkillExecutor executor = plugin.getMobSkillExecutor();
        if (executor != null) {
            aliveMobs.forEach(executor::stopSkills);
        }
        
        // Nettoyer les animations
        MobAnimationManager animManager = plugin.getMobAnimationManager();
        if (animManager != null) {
            aliveMobs.forEach(animManager::unregisterMob);
        }
        
        aliveMobs.clear();
    }

    private int getRunsForPlayer() {
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        return progress.getRuns(towerId);
    }

    private LivingEntity spawnOneMob() {
        World world = player.getWorld();
        if (world == null) return null;

        Location spawnLoc;
        if (config.getSpawnRegion() != null) {
            // Spawn dans la région configurée
            spawnLoc = config.getSpawnRegion().getRandomLocation(world);
        } else {
            // Fallback: ancien comportement, près du joueur
            Location base = player.getLocation();
            spawnLoc = base.clone().add(
                    ThreadLocalRandom.current().nextDouble(-3, 3),
                    0,
                    ThreadLocalRandom.current().nextDouble(-3, 3)
            );
        }

        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, config.getType());

        int runs = getRunsForPlayer();
        double health = config.getHealthForRuns(runs);
        entity.setMaxHealth(health);
        entity.setHealth(health);

        // Stocker les métadonnées
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "tower_spawner"), PersistentDataType.STRING, config.getId());
        pdc.set(new NamespacedKey(plugin, "tower_id"), PersistentDataType.STRING, towerId);
        
        // Métadatas pour TowerKillListener
        entity.setMetadata("tower_mob", new FixedMetadataValue(plugin, config.getMobId()));
        entity.setMetadata("spawner_id", new FixedMetadataValue(plugin, config.getId()));

        return entity;
    }
    
    // ... reste de la classe inchangé ...
}
