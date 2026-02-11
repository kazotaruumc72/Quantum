package com.wynvers.quantum.tools;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire de sélection de structures pour les joueurs
 * Permet de stocker les deux positions sélectionnées par chaque joueur
 */
public class StructureSelectionManager {
    
    private final Map<UUID, Location> pos1Selection = new HashMap<>();
    private final Map<UUID, Location> pos2Selection = new HashMap<>();
    
    /**
     * Définit la première position pour un joueur
     */
    public void setPos1(Player player, Location location) {
        pos1Selection.put(player.getUniqueId(), location.clone());
        player.sendMessage("§a§l✓ §aPosition 1 définie: §e" + formatLocation(location));
    }
    
    /**
     * Définit la deuxième position pour un joueur
     */
    public void setPos2(Player player, Location location) {
        pos2Selection.put(player.getUniqueId(), location.clone());
        player.sendMessage("§a§l✓ §aPosition 2 définie: §e" + formatLocation(location));
    }
    
    /**
     * Récupère la première position d'un joueur
     */
    public Location getPos1(Player player) {
        return pos1Selection.get(player.getUniqueId());
    }
    
    /**
     * Récupère la deuxième position d'un joueur
     */
    public Location getPos2(Player player) {
        return pos2Selection.get(player.getUniqueId());
    }
    
    /**
     * Vérifie si un joueur a défini les deux positions
     */
    public boolean hasSelection(Player player) {
        UUID uuid = player.getUniqueId();
        return pos1Selection.containsKey(uuid) && pos2Selection.containsKey(uuid);
    }
    
    /**
     * Efface la sélection d'un joueur
     */
    public void clearSelection(Player player) {
        UUID uuid = player.getUniqueId();
        pos1Selection.remove(uuid);
        pos2Selection.remove(uuid);
    }
    
    /**
     * Formate une location pour l'affichage
     */
    private String formatLocation(Location loc) {
        return String.format("(%d, %d, %d)", 
            loc.getBlockX(), 
            loc.getBlockY(), 
            loc.getBlockZ()
        );
    }
}
