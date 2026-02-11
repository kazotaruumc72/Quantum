package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.furniture.FurnitureData;
import com.wynvers.quantum.jobs.JobManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Listener pour les compétences des outils
 */
public class ToolListener implements Listener {
    
    private final Quantum plugin;
    private final ToolManager toolManager;
    private final StructureManager structureManager;  // Can be null if not initialized
    
    public ToolListener(Quantum plugin, ToolManager toolManager) {
        this.plugin = plugin;
        this.toolManager = toolManager;
        this.structureManager = plugin.getStructureManager();  // May be null
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Vérifier si c'est une pioche Quantum
        if (toolManager.getPickaxe().isQuantumTool(tool)) {
            handlePickaxe(event, player, tool);
        }
        // Vérifier si c'est une hache Quantum
        else if (toolManager.getAxe().isQuantumTool(tool)) {
            handleAxe(event, player, tool);
        }
    }
    
    /**
     * Gère la compétence de la pioche (Double Extraction)
     */
    private void handlePickaxe(BlockBreakEvent event, Player player, ItemStack tool) {
        // Vérifier si c'est un furniture Nexo
        try {
            FurnitureMechanic mechanic = NexoFurniture.furnitureMechanic(event.getBlock());
            if (mechanic == null) return;
            
            String furnitureId = mechanic.getItemID();
            
            int level = toolManager.getPickaxe().getLevel(tool);
            int multiplier = toolManager.getPickaxe().getLootMultiplier(level);
            
            // Le multiplicateur sera appliqué dans FurnitureManager
            // On pourrait stocker le multiplicateur dans les metadata du joueur
            // ou passer directement au FurnitureManager
            
            String message = toolManager.getConfig().getString("messages.double_extraction", "&a✓ Extraction doublée! x{multiplier}");
            message = message.replace("{multiplier}", String.valueOf(multiplier));
            player.sendMessage(message.replace('&', '§'));
            
        } catch (Exception e) {
            // Pas un furniture Nexo
        }
    }
    
    /**
     * Gère la compétence de la hache (One-shot)
     */
    private void handleAxe(BlockBreakEvent event, Player player, ItemStack tool) {
        int level = toolManager.getAxe().getLevel(tool);
        
        // Vérifier si la compétence s'active
        if (toolManager.getAxe().shouldActivateOneShot(level)) {
            // TODO: Détecter et casser la structure complète
            // Nécessite l'intégration avec StructureManager
            
            String message = toolManager.getConfig().getString("messages.oneshot_activated", "&e⚡ One-shot activé! Structure complète coupée!");
            player.sendMessage(message.replace('&', '§'));
            
            String costMessage = toolManager.getConfig().getString("messages.oneshot_cost", "&7Coût: {cost}$");
            costMessage = costMessage.replace("{cost}", "5000");
            player.sendMessage(costMessage.replace('&', '§'));
        }
    }
    
    /**
     * Gère les interactions avec les structures pour le système de jobs
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        Location blockLocation = event.getClickedBlock().getLocation();
        
        // Vérifier si le joueur a un job manager disponible
        JobManager jobManager = plugin.getJobManager();
        if (jobManager == null) return;
        
        // Essayer de détecter une structure à cette position
        if (structureManager != null) {
            String[] structureInfo = structureManager.detectStructure(blockLocation);
            
            if (structureInfo != null && structureInfo.length == 2) {
                String structureId = structureInfo[0];
                String stateName = structureInfo[1];
                
                // Trigger le job manager
                jobManager.handleStructureTap(player, structureId, stateName);
                
                // Dégrader la structure
                StructureManager.StructureState currentState = StructureManager.StructureState.valueOf(stateName);
                StructureManager.StructureState newState = structureManager.degradeStructure(blockLocation, structureId, currentState);
                
                if (newState != null) {
                    // Structure dégradée avec succès
                    event.setCancelled(true);  // Annuler l'event de clic pour éviter la destruction normale du bloc
                }
            }
        }
    }
}
