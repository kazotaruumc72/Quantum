package com.wynvers.quantum.worldguard;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Écoute les mouvements des joueurs pour gérer les zones restreintes et la progression des tours
 * 
 * @author Kazotaruu_
 * @version 2.0
 */
public class ZoneListener implements Listener {
    
    private final Quantum plugin;
    private final ZoneManager zoneManager;
    private final TowerManager towerManager;
    private final Map<UUID, String> lastZone = new HashMap<>();
    
    public ZoneListener(Quantum plugin) {
        this.plugin = plugin;
        this.zoneManager = plugin.getZoneManager();
        this.towerManager = plugin.getTowerManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Ne vérifier que si le joueur change de bloc
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        String fromZone = zoneManager.getPlayerZone(player, from);
        String toZone = zoneManager.getPlayerZone(player, to);
        
        // Le joueur change de zone
        if (!equals(fromZone, toZone)) {
            handleZoneChange(player, fromZone, toZone, event);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        String fromZone = zoneManager.getPlayerZone(player, from);
        String toZone = zoneManager.getPlayerZone(player, to);
        
        // Le joueur change de zone via téléportation
        if (!equals(fromZone, toZone)) {
            handleZoneChange(player, fromZone, toZone, event);
        }
    }
    
    /**
     * Gère le changement de zone d'un joueur
     */
    private void handleZoneChange(Player player, String fromZone, String toZone, PlayerMoveEvent event) {
        // Sortie d'une zone
        if (fromZone != null && toZone == null) {
            if (!zoneManager.canPlayerLeaveZone(player, fromZone)) {
                // Annuler le mouvement
                event.setCancelled(true);
                
                // Envoyer le message d'erreur
                String message = zoneManager.getDeniedMessage(player, fromZone);
                player.sendMessage(message);
                return;
            }
            
            // Le joueur peut sortir - compléter l'étage si dans une tour
            handleFloorCompletion(player, fromZone);
            
            zoneManager.onPlayerExitZone(player, fromZone);
            lastZone.remove(player.getUniqueId());
            
            // Clear tower location
            if (towerManager != null) {
                towerManager.clearCurrentLocation(player);
            }
        }
        // Entrée dans une zone
        else if (fromZone == null && toZone != null) {
            zoneManager.onPlayerEnterZone(player, toZone);
            lastZone.put(player.getUniqueId(), toZone);
            
            // Update tower location
            if (towerManager != null) {
                String towerId = towerManager.getTowerByRegion(toZone);
                int floor = towerManager.getFloorByRegion(toZone);
                if (towerId != null && floor != -1) {
                    towerManager.updateCurrentLocation(player, towerId, floor);
                }
            }
        }
        // Changement de zone à zone
        else if (fromZone != null && toZone != null && !fromZone.equals(toZone)) {
            // Vérifier si le joueur peut quitter l'ancienne zone
            if (!zoneManager.canPlayerLeaveZone(player, fromZone)) {
                event.setCancelled(true);
                String message = zoneManager.getDeniedMessage(player, fromZone);
                player.sendMessage(message);
                return;
            }
            
            // Compléter l'étage précédent
            handleFloorCompletion(player, fromZone);
            
            zoneManager.onPlayerExitZone(player, fromZone);
            zoneManager.onPlayerEnterZone(player, toZone);
            lastZone.put(player.getUniqueId(), toZone);
            
            // Update tower location
            if (towerManager != null) {
                String towerId = towerManager.getTowerByRegion(toZone);
                int floor = towerManager.getFloorByRegion(toZone);
                if (towerId != null && floor != -1) {
                    towerManager.updateCurrentLocation(player, towerId, floor);
                }
            }
        }
    }
    
    /**
     * Gère le changement de zone d'un joueur (téléportation)
     */
    private void handleZoneChange(Player player, String fromZone, String toZone, PlayerTeleportEvent event) {
        // Pour les téléportations, on vérifie aussi la sortie de zone
        if (fromZone != null && toZone == null) {
            if (!zoneManager.canPlayerLeaveZone(player, fromZone)) {
                event.setCancelled(true);
                String message = zoneManager.getDeniedMessage(player, fromZone);
                player.sendMessage(message);
                return;
            }
            
            handleFloorCompletion(player, fromZone);
            zoneManager.onPlayerExitZone(player, fromZone);
            lastZone.remove(player.getUniqueId());
            
            if (towerManager != null) {
                towerManager.clearCurrentLocation(player);
            }
        }
        else if (fromZone == null && toZone != null) {
            zoneManager.onPlayerEnterZone(player, toZone);
            lastZone.put(player.getUniqueId(), toZone);
            
            if (towerManager != null) {
                String towerId = towerManager.getTowerByRegion(toZone);
                int floor = towerManager.getFloorByRegion(toZone);
                if (towerId != null && floor != -1) {
                    towerManager.updateCurrentLocation(player, towerId, floor);
                }
            }
        }
        else if (fromZone != null && toZone != null && !fromZone.equals(toZone)) {
            if (!zoneManager.canPlayerLeaveZone(player, fromZone)) {
                event.setCancelled(true);
                String message = zoneManager.getDeniedMessage(player, fromZone);
                player.sendMessage(message);
                return;
            }
            
            handleFloorCompletion(player, fromZone);
            zoneManager.onPlayerExitZone(player, fromZone);
            zoneManager.onPlayerEnterZone(player, toZone);
            lastZone.put(player.getUniqueId(), toZone);
            
            if (towerManager != null) {
                String towerId = towerManager.getTowerByRegion(toZone);
                int floor = towerManager.getFloorByRegion(toZone);
                if (towerId != null && floor != -1) {
                    towerManager.updateCurrentLocation(player, towerId, floor);
                }
            }
        }
    }
    
    /**
     * Handle floor completion when player exits a tower floor
     */
    private void handleFloorCompletion(Player player, String zoneName) {
        if (towerManager == null) return;
        
        String towerId = towerManager.getTowerByRegion(zoneName);
        int floor = towerManager.getFloorByRegion(zoneName);
        
        if (towerId != null && floor != -1) {
            // Mark floor as completed
            towerManager.completeFloor(player, towerId, floor);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastZone.remove(player.getUniqueId());
        zoneManager.onPlayerQuit(player);
        
        // Clear tower location
        if (towerManager != null) {
            towerManager.clearCurrentLocation(player);
        }
    }
    
    /**
     * Compare deux zones (gère les nulls)
     */
    private boolean equals(String zone1, String zone2) {
        if (zone1 == null && zone2 == null) return true;
        if (zone1 == null || zone2 == null) return false;
        return zone1.equals(zone2);
    }
}
