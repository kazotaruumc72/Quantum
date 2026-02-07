package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * Listener qui donne de l'expérience quand un joueur tue un mob dans la tour.
 */
public class TowerKillListener implements Listener {

    private final Quantum plugin;
    private final TowerManager towerManager;
    private final PlayerLevelManager levelManager;

    public TowerKillListener(Quantum plugin, TowerManager towerManager, PlayerLevelManager levelManager) {
        this.plugin = plugin;
        this.towerManager = towerManager;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // Vérifier que c'est un mob qui a été tué
        if (!(entity instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) entity;
        Player killer = mob.getKiller();
        
        // Vérifier qu'un joueur a tué le mob
        if (killer == null) {
            return;
        }

        UUID killerUuid = killer.getUniqueId();
        TowerProgress progress = towerManager.getProgress(killerUuid);
        
        // Vérifier que le joueur est dans une tour
        if (progress == null || progress.getCurrentTower() == null) {
            return;
        }

        String towerId = progress.getCurrentTower();
        int floor = progress.getCurrentFloor();
        
        // Récupérer l'EXP de base depuis les données vanilla
        int baseExp = event.getDroppedExp();
        
        // Multiplicateur basé sur l'étage (plus haut = plus d'exp)
        double floorMultiplier = 1.0 + (floor * 0.05); // +5% par étage
        
        // Bonus de type de mob (optionnel)
        double mobMultiplier = getMobMultiplier(mob);
        
        // Calcul de l'EXP finale
        int finalExp = (int) Math.round(baseExp * floorMultiplier * mobMultiplier);
        
        // Donner l'EXP au joueur
        levelManager.addExp(killerUuid, finalExp);
        
        // Message de débogue/détail (optionnel, peut être retiré en production)
        if (killer.hasPermission("quantum.debug")) {
            killer.sendMessage("§7[Debug] §f+" + finalExp + " XP §7(étage " + floor + ", multi: " + String.format("%.2f", floorMultiplier) + ")");
        }
        
        // Notifier le TowerManager du kill pour le tracking de progression
        towerManager.addKill(killer, mob.getType().name());
    }

    /**
     * Retourne un multiplicateur d'EXP basé sur le type de mob.
     */
    private double getMobMultiplier(Mob mob) {
        switch (mob.getType()) {
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case CREEPER:
                return 1.0;
            case WITCH:
            case ENDERMAN:
            case BLAZE:
                return 1.5;
            case WITHER_SKELETON:
            case GUARDIAN:
            case ELDER_GUARDIAN:
                return 2.0;
            case ENDER_DRAGON:
            case WITHER:
                return 5.0;
            default:
                return 1.0;
        }
    }
}