package com.wynvers.quantum.weapon;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener pour les événements de l'arme de donjon
 */
public class DungeonWeaponListener implements Listener {
    
    private final Quantum plugin;
    private final DungeonWeapon dungeonWeapon;
    
    public DungeonWeaponListener(Quantum plugin, DungeonWeapon dungeonWeapon) {
        this.plugin = plugin;
        this.dungeonWeapon = dungeonWeapon;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Vérifier si le damager est un joueur
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        
        // Vérifier si c'est une arme de donjon
        if (!dungeonWeapon.isDungeonWeapon(weapon)) return;
        
        // Vérifier si le joueur est dans un donjon
        if (!dungeonWeapon.isInDungeon(player)) {
            // Annuler l'attaque
            event.setCancelled(true);
            
            // Afficher le message d'interdiction
            dungeonWeapon.showDungeonOnlyMessage(player);
        }
    }
}
