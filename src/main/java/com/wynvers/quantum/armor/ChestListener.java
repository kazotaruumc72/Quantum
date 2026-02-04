package com.wynvers.quantum.armor;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Écoute l'ouverture des coffres de donjon pour vérifier les prérequis
 * - Minimum de kills requis (configurable via dungeon.yml)
 * - Armure complète de donjon nécessaire
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChestListener implements Listener {
    
    private final Quantum plugin;
    private final DungeonArmor dungeonArmor;
    private final TowerManager towerManager;
    private final int minKillsToOpen;
    
    public ChestListener(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonArmor = plugin.getDungeonArmor();
        this.towerManager = plugin.getTowerManager();
        
        // Charger le minimum de kills requis depuis la config
        this.minKillsToOpen = plugin.getConfig().getInt("dungeon.min_kills_to_open", 5);
    }
    
    /**
     * Vérifie l'ouverture des coffres dans les tours
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Vérifier que c'est un coffre
        if (!(block.getState() instanceof Chest)) return;
        
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans une tour
        if (towerManager == null) return;
        
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        if (progress == null || progress.getCurrentTower() == null) {
            return; // Pas dans une tour, autoriser l'ouverture
        }
        
        // Vérifier si le joueur a l'armure complète
        if (!dungeonArmor.hasCompleteArmor(player)) {
            event.setCancelled(true);
            player.sendMessage("§c§l⚠ §cVous devez porter l'armure de donjon complète pour ouvrir ce coffre !");
            return;
        }
        
        // Vérifier le nombre de kills
        int totalKills = progress.getTotalKills();
        if (totalKills < minKillsToOpen) {
            event.setCancelled(true);
            player.sendMessage("§c§l⚠ §cVous devez tuer au moins §6" + minKillsToOpen + " §cmonstres avant d'ouvrir ce coffre !");
            player.sendMessage("§7Kills actuels: §e" + totalKills + "§7/§6" + minKillsToOpen);
            return;
        }
        
        // Coffre peut être ouvert
        player.sendMessage("§a§l✓ §aCoffre de donjon ouvert ! (§6" + totalKills + " §akills)");
    }
}
