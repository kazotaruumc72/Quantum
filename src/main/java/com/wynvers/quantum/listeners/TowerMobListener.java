package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Écoute les morts d'entités pour mettre à jour la progression dans les tours
 * Empêche également les spawns de renfort des zombies de tour
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class TowerMobListener implements Listener {
    
    private final Quantum plugin;
    private final TowerManager towerManager;
    
    public TowerMobListener(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        // Vérifier que c'est un joueur qui a tué
        if (killer == null) {
            return;
        }
        
        // Vérifier que le joueur est dans une tour
        if (towerManager == null) {
            return;
        }
        
        TowerProgress progress = towerManager.getProgress(killer.getUniqueId());
        if (progress == null || progress.getCurrentTower() == null) {
            return;
        }
        
        // Récupérer le type de mob
        String mobType = entity.getType().name().toLowerCase();
        
        // Ajouter le kill à la progression
        progress.addKill(mobType);
        
        // Log pour debug
        plugin.getQuantumLogger().debug(
            killer.getName() + " a tué un " + mobType + 
            " dans la tour " + progress.getCurrentTower() + 
            " (étage " + progress.getCurrentFloor() + ")"
        );
    }
    
    /**
     * Empêche les spawns de zombies de renfort (reinforcements) pour les mobs de tour
     * Les zombies vanilla peuvent spawner des renforts quand ils sont attaqués,
     * mais nous ne voulons pas cela pour les mobs de tour.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Ne gérer que les spawns de type REINFORCEMENTS (zombies qui appellent des renforts)
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS) {
            return;
        }
        
        // Si c'est un zombie qui essaie de spawner un renfort
        if (event.getEntity() instanceof Zombie) {
            Location spawnLoc = event.getLocation();
            
            // Vérifier si le spawn est lié à un mob de tour
            // Les renforts spawent toujours très proche du zombie parent (< 10 blocs)
            boolean nearTowerMob = spawnLoc.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .anyMatch(e -> e.hasMetadata("tower_mob"));
            
            if (nearTowerMob) {
                // On a trouvé un mob de tour à proximité, annuler le spawn de renfort
                event.setCancelled(true);
                plugin.getQuantumLogger().debug(
                    "Prevented reinforcement zombie spawn near tower mob"
                );
            }
        }
    }
}
