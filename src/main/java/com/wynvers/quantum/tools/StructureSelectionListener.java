package com.wynvers.quantum.tools;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener pour la sélection de structures avec la baguette (hache en fer)
 */
public class StructureSelectionListener implements Listener {
    
    private final Quantum plugin;
    private final StructureSelectionManager selectionManager;
    
    public StructureSelectionListener(Quantum plugin, StructureSelectionManager selectionManager) {
        this.plugin = plugin;
        this.selectionManager = selectionManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Vérifier si c'est la baguette de structure
        if (!isStructureWand(item)) {
            return;
        }
        
        // Vérifier les permissions
        if (!player.hasPermission("quantum.structure.wand")) {
            event.setCancelled(true);
            player.sendMessage("§c§l✗ §cVous n'avez pas la permission d'utiliser la baguette de structure!");
            return;
        }
        
        Action action = event.getAction();
        
        // Clic gauche = Position 1
        if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            selectionManager.setPos1(player, event.getClickedBlock().getLocation());
        }
        // Clic droit = Position 2
        else if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            selectionManager.setPos2(player, event.getClickedBlock().getLocation());
        }
        // Clics en l'air - informer le joueur
        else if (action == Action.LEFT_CLICK_AIR) {
            event.setCancelled(true);
            player.sendMessage("§e§l⚠ §eClic gauche sur un bloc pour définir la position 1!");
        }
        else if (action == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            player.sendMessage("§e§l⚠ §eClic droit sur un bloc pour définir la position 2!");
        }
    }
    
    /**
     * Vérifie si un item est la baguette de structure
     */
    private boolean isStructureWand(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_AXE) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        // Vérifier le nom de la baguette
        return meta.getDisplayName().equals("§e§lBaguette de Structure");
    }
}
