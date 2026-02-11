package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.mobs.MobSkillConfig;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spawner actif pour un joueur dans une tour.
 */
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
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        int periodTicks = config.getIntervalSeconds() * 20;
        
        // Cleanup task runs less frequently (every 5 seconds) to reduce overhead
        final int cleanupInterval = 100; // 5 seconds in ticks
        final int[] tickCounter = {0};

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Nettoyage des morts - only run every 5 seconds instead of every spawn interval
                tickCounter[0] += periodTicks;
                if (tickCounter[0] >= cleanupInterval) {
                    tickCounter[0] = 0;
                    cleanupDeadMobs();
                }

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
    
    /**
     * Nettoie les mobs morts de la liste aliveMobs
     * Optimisé pour réduire le nombre d'appels à Bukkit.getEntity()
     */
    private void cleanupDeadMobs() {
        // Use iterator for better performance than removeIf with Entity lookup
        Iterator<UUID> iterator = aliveMobs.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity e = Bukkit.getEntity(uuid);
            if (e == null || e.isDead()) {
                iterator.remove();
            }
        }
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
    
        // Vérifier que la région de spawn est configurée
        if (config.getSpawnRegion() == null) {
            // Région non configurée - ne pas spawner de mob
            plugin.getLogger().warning("[TowerSpawner] Aucune région de spawn configurée pour le spawner '" 
                + config.getFullId() + "'. Utilisez /quantum mobspawnzone create pour définir une zone.");
            return null;
        }
        
        // Spawn dans la région configurée uniquement
        Location spawnLoc = config.getSpawnRegion().getRandomLocation(world);
    
        // ⭐ IMPORTANT : on utilise maintenant ModelEngine via TowerSpawnerConfig
        int runs = getRunsForPlayer();
        LivingEntity entity = config.spawnWithModelEngine(spawnLoc, runs);
    
        // Stocker les métadonnées (inchangé)
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "tower_spawner"), PersistentDataType.STRING, config.getId());
        pdc.set(new NamespacedKey(plugin, "tower_id"), PersistentDataType.STRING, towerId);
    
        // Métadatas pour TowerKillListener
        entity.setMetadata("tower_mob", new FixedMetadataValue(plugin, config.getMobId()));
        entity.setMetadata("spawner_id", new FixedMetadataValue(plugin, config.getId()));
    
        return entity;
    }

    
    private void startMobSkills(LivingEntity mob) {
        MobSkillExecutor executor = plugin.getMobSkillExecutor();
        if (executor != null && config.getSkills() != null && !config.getSkills().isEmpty()) {
            // Convertir List<MobSkillConfig> en List<Map<String, Object>>
            List<Map<String, Object>> skillMaps = config.getSkills().stream()
                .map(MobSkillConfig::getAllParameters)
                .collect(Collectors.toList());
            
            executor.startSkills(mob, skillMaps);
        }
    }
    
    private void registerMobAnimations(LivingEntity mob) {
        MobAnimationManager animManager = plugin.getMobAnimationManager();
        if (animManager != null && config.getAnimations() != null && !config.getAnimations().isEmpty()) {
            // Extraire le modelId et créer AnimationConfig
            Map<String, String> animations = config.getAnimations();
            
            // Le mobId est utilisé comme modelId pour Model Engine
            String modelId = config.getMobId();
            
            // Créer une AnimationConfig à partir de la map
            MobAnimationManager.AnimationConfig animConfig = new MobAnimationManager.AnimationConfig(
                animations.getOrDefault("spawn", null),
                animations.getOrDefault("idle", null),
                animations.getOrDefault("walk", null),
                animations.getOrDefault("attack", null),
                animations.getOrDefault("death", null)
            );
            
            animManager.registerMob(mob, modelId, animConfig);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        if (aliveMobs.remove(uuid)) {
            // Arrêter les skills du mob
            MobSkillExecutor executor = plugin.getMobSkillExecutor();
            if (executor != null) {
                executor.stopSkills(uuid);
            }
            
            // Désenregistrer les animations
            MobAnimationManager animManager = plugin.getMobAnimationManager();
            if (animManager != null) {
                animManager.unregisterMob(uuid);
            }
        }
    }
    
    // ============ GETTERS ============
    
    public TowerSpawnerConfig getConfig() {
        return config;
    }
    
    public int getAliveMobCount() {
        return aliveMobs.size();
    }
    
    public Set<UUID> getAliveMobs() {
        return new HashSet<>(aliveMobs);
    }
}
