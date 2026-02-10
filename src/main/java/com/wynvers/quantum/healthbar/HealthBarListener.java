package com.wynvers.quantum.healthbar;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener pour mettre à jour l'affichage de vie des mobs
 */
public class HealthBarListener implements Listener {
    
    private final Quantum plugin;
    private final HealthBarManager healthBarManager;
    
    public HealthBarListener(Quantum plugin, HealthBarManager healthBarManager) {
        this.plugin = plugin;
        this.healthBarManager = healthBarManager;
    }
    
    /**
     * Quand un mob spawn, initialiser sa barre de vie
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof Player) return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Appliquer le modèle ModelEngine si configuré
        // Délai de 1 tick nécessaire car:
        // - L'entité doit être complètement initialisée dans le monde avant l'application du modèle
        // - ModelEngine nécessite que l'entité soit dans la liste des entités du monde
        // - Sans ce délai, ModelEngineAPI peut recevoir une entité en état "loading" et échouer silencieusement
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                healthBarManager.applyModelEngineModel(entity);
            }
        }, 1L);
        
        // Délai de 2 ticks pour la mise à jour de la healthbar
        // Nécessaire car:
        // - Pour les mobs spawned via TowerSpawnerConfig.spawnWithModelEngine(), le custom name
        //   et le modèle ModelEngine sont appliqués APRÈS l'EntitySpawnEvent
        // - Le délai permet à la configuration complète du mob (nom + modèle) d'être appliquée
        //   avant que la healthbar ne soit générée
        // - Sans ce délai, la healthbar serait calculée avec un nom vide ou incorrect
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                // Mettre à jour pour tous les joueurs dans un rayon de 50 blocs
                entity.getWorld().getNearbyEntities(entity.getLocation(), 50, 50, 50).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .forEach(player -> healthBarManager.updateMobHealthDisplay(entity, player));
            }
        }, 2L);
    }
    
    /**
     * Quand un mob prend des dégâts, mettre à jour sa barre de vie
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof Player) return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Mettre à jour après les dégâts
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                // Mettre à jour pour tous les joueurs dans un rayon de 50 blocs
                entity.getWorld().getNearbyEntities(entity.getLocation(), 50, 50, 50).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .forEach(player -> healthBarManager.updateMobHealthDisplay(entity, player));
            }
        }, 1L);
    }
    
    /**
     * Quand un mob régénère de la vie
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof Player) return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Mettre à jour après la régénération
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) {
                entity.getWorld().getNearbyEntities(entity.getLocation(), 50, 50, 50).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .forEach(player -> healthBarManager.updateMobHealthDisplay(entity, player));
            }
        }, 1L);
    }
    
    /**
     * Quand un joueur rejoint, charger sa préférence
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Mettre à jour tous les mobs visibles avec la préférence du joueur
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.getWorld().getLivingEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> entity.getLocation().distance(player.getLocation()) <= 50)
                .forEach(entity -> healthBarManager.updateMobHealthDisplay(entity, player));
        }, 20L);
    }
}
