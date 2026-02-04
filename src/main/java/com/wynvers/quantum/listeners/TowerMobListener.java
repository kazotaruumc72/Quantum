package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Écoute les morts d'entités pour mettre à jour la progression dans les tours
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
        Entity entity = event.getEntity();
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
}
