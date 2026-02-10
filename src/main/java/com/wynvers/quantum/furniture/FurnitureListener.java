package com.wynvers.quantum.furniture;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.wynvers.quantum.Quantum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;

/**
 * Listener pour les events de furniture
 */
public class FurnitureListener implements Listener {
    
    private final Quantum plugin;
    private final FurnitureManager furnitureManager;
    
    public FurnitureListener(Quantum plugin, FurnitureManager furnitureManager) {
        this.plugin = plugin;
        this.furnitureManager = furnitureManager;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si c'est un furniture Nexo
        try {
            FurnitureMechanic mechanic = NexoFurniture.furnitureMechanic(event.getBlock());
            if (mechanic == null) return;
            
            String furnitureId = mechanic.getItemID();
            
            // Gérer le break du furniture
            furnitureManager.handleFurnitureBreak(event.getBlock().getLocation(), furnitureId, player);
            
        } catch (Exception e) {
            // Pas un furniture Nexo, ignorer
        }
    }
}
