package com.wynvers.quantum.crops;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;

/**
 * Listener pour les interactions avec les cultures
 */
public class CustomCropListener implements Listener {
    
    private final Quantum plugin;
    private final CustomCropManager cropManager;
    
    public CustomCropListener(Quantum plugin, CustomCropManager cropManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();
        
        // Vérifier si le joueur a une houe (n'importe quel type)
        if (!isHoe(itemInHand)) return;
        
        // Essayer de récolter la culture
        cropManager.harvestCrop(event.getClickedBlock().getLocation(), player);
    }
    
    /**
     * Vérifie si l'item est une houe
     */
    private boolean isHoe(Material material) {
        return material == Material.WOODEN_HOE ||
               material == Material.STONE_HOE ||
               material == Material.IRON_HOE ||
               material == Material.GOLDEN_HOE ||
               material == Material.DIAMOND_HOE ||
               material == Material.NETHERITE_HOE;
    }
}
