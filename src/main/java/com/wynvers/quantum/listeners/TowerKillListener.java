package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.towers.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.UUID;

/**
 * Listener qui gère les kills dans les tours:
 * - Donne de l'expérience
 * - Track les kills pour la progression
 * - Gère les loots par mob
 * - Déclenche les récompenses et l'ouverture des portes quand l'étage est complété
 */
public class TowerKillListener implements Listener {

    private final Quantum plugin;
    private final TowerManager towerManager;
    private final PlayerLevelManager levelManager;
    private final TowerDoorManager doorManager;
    private final TowerLootManager lootManager;

    public TowerKillListener(Quantum plugin, TowerManager towerManager, 
                            PlayerLevelManager levelManager,
                            TowerDoorManager doorManager,
                            TowerLootManager lootManager) {
        this.plugin = plugin;
        this.towerManager = towerManager;
        this.levelManager = levelManager;
        this.doorManager = doorManager;
        this.lootManager = lootManager;
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
        
        // Vérifier si c'est un mob de spawner de tour
        if (!mob.hasMetadata("tower_mob")) {
            return;
        }
        
        String mobId = mob.getMetadata("tower_mob").get(0).asString();
        String spawnerId = mob.hasMetadata("spawner_id") ? 
            mob.getMetadata("spawner_id").get(0).asString() : null;
        
        // ========== 1. EXPÉRIENCE ==========
        giveExpForKill(killer, floor, mob, event.getDroppedExp());
        
        // ========== 2. LOOTS PAR MOB ==========
        lootManager.onMobKilled(towerId, mobId, killer, mob.getLocation());
        
        // ========== 3. TRACKING DES KILLS ==========
        towerManager.addKill(killer, mobId);
        
        // ========== 4. VÉRIFIER SI L'ÉTAGE EST COMPLÉTÉ ==========
        checkFloorCompletion(killer, towerId, floor, spawnerId, progress);
    }
    
    /**
     * Donne de l'expérience au joueur pour le kill
     */
    private void giveExpForKill(Player killer, int floor, Mob mob, int baseExp) {
        // Multiplicateur basé sur l'étage (plus haut = plus d'exp)
        double floorMultiplier = 1.0 + (floor * 0.05); // +5% par étage
        
        // Bonus de type de mob
        double mobMultiplier = getMobMultiplier(mob);
        
        // Calcul de l'EXP finale
        int finalExp = (int) Math.round(baseExp * floorMultiplier * mobMultiplier);
        
        // Donner l'EXP au joueur
        levelManager.addExp(killer.getUniqueId(), finalExp);
        
        // Message de débogue
        if (killer.hasPermission("quantum.debug")) {
            killer.sendMessage("§7[Debug] §f+" + finalExp + " XP §7(étage " + floor + 
                ", multi: " + String.format("%.2f", floorMultiplier) + ")");
        }
    }
    
    /**
     * Vérifie si tous les mobs requis de l'étage sont tués et déclenche la complétion
     */
    private void checkFloorCompletion(Player player, String towerId, int floor, String spawnerId, TowerProgress progress) {
        if (spawnerId == null) return;
        
        TowerSpawnerManager spawnerManager = towerManager.getSpawnerManager();
        if (spawnerManager == null) return;

        // 1) Vérification basée sur les kills (ce qui alimente ton scoreboard)
        int currentKills = progress.getCurrentKills().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int requiredKills = getRequiredKills(towerId, floor);

        if (requiredKills > 0 && currentKills >= requiredKills) {
            onFloorCompleted(player, towerId, floor);
            return;
        }
        
        // 2) Fallback: tous les spawners de cet étage sont vides
        boolean allSpawnersEmpty = spawnerManager.areAllSpawnersEmpty(player, towerId, floor);
        
        if (allSpawnersEmpty) {
            // Tous les mobs sont morts!
            onFloorCompleted(player, towerId, floor);
        }
    }
    
    /**
     * Appelé quand un étage est complété
     */
    private void onFloorCompleted(Player player, String towerId, int floor) {
        plugin.getQuantumLogger().info("Floor completed: " + towerId + " floor " + floor + 
            " by " + player.getName());
        
        // ========== 1. DONNER LES RÉCOMPENSES D'ÉTAGE ==========
        lootManager.onFloorCompleted(towerId, floor, player, player.getLocation());
        
        // ========== 2. OUVRIR LES PORTES (30 secondes) ==========
        doorManager.openDoor(towerId, floor, player);
        
        // ========== 3. COMPLÉTER L'ÉTAGE DANS LA PROGRESSION ==========
        towerManager.completeFloor(player, towerId, floor);
        
        // ========== 4. EFFETS ET MESSAGES ==========
        TowerConfig tower = towerManager.getTower(towerId);
        if (tower != null) {
            // Messages selon le type d'étage
            if (tower.isFinalBoss(floor)) {
                // Boss final
                player.sendMessage("§6§l✦═══════════════════════✦");
                player.sendMessage("§6§l      TOUR COMPLÉTÉE!");
                player.sendMessage("§7Vous avez terminé: §f" + tower.getName());
                player.sendMessage("§6§l✦═══════════════════════✦");
                
                Bukkit.broadcastMessage("§6§l[TOURS] §f" + player.getName() + 
                    " §7a complété §f" + tower.getName() + "§7!");
                    
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                
            } else if (tower.isBossFloor(floor)) {
                // Boss intermédiaire
                player.sendMessage("§a§l✓ BOSS VAINCU!");
                player.sendMessage("§7Étage §f" + floor + " §7terminé!");
                player.sendMessage("§e⚠ Vous avez 30 secondes pour passer la porte!");
                
                player.playSound(player.getLocation(), "entity.ender_dragon.death", 1.0f, 1.5f);
                
            } else {
                // Étage normal
                player.sendMessage("§a§l✓ Étage " + floor + " terminé!");
                player.sendMessage("§e⚠ Vous avez 30 secondes pour passer la porte!");
                
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.5f);
            }
        }
        
        // ========== 5. ARRÊTER LES SPAWNERS DE CET ÉTAGE ==========
        TowerSpawnerManager spawnerManager = towerManager.getSpawnerManager();
        if (spawnerManager != null) {
            spawnerManager.stopFloorSpawners(player, towerId, floor);
        }
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

    /**
     * Récupère le nombre total de kills requis pour un étage depuis towers.yml
     * (même logique que dans QuantumExpansion#getRequiredKills)
     */
    private int getRequiredKills(String towerId, int floor) {
        try {
            File towersFile = new File(plugin.getDataFolder(), "towers.yml");
            if (!towersFile.exists()) return 0;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
            String path = "towers." + towerId + ".floors." + floor + ".spawners";
            ConfigurationSection spawnersSection = config.getConfigurationSection(path);

            if (spawnersSection == null) return 0;

            int total = 0;
            for (String spawnerKey : spawnersSection.getKeys(false)) {
                ConfigurationSection spawner = spawnersSection.getConfigurationSection(spawnerKey);
                if (spawner != null) {
                    int amount = spawner.getInt("amount", 0);
                    total += amount;
                }
            }

            return total;
        } catch (Exception e) {
            return 0;
        }
    }
}
