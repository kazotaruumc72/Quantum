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
            // Détecter et casser la structure complète si le bloc fait partie d'une structure
            if (structureManager != null) {
                Location blockLocation = event.getBlock().getLocation();
                String[] structureInfo = structureManager.detectStructure(blockLocation);
                
                if (structureInfo != null && structureInfo.length == 2) {
                    String structureId = structureInfo[0];
                    String stateName = structureInfo[1];
                    try {
                        StructureManager.StructureState currentState = StructureManager.StructureState.valueOf(stateName);
                        structureManager.breakStructure(blockLocation, structureId, currentState);
                    } catch (IllegalArgumentException ignored) {
                        // État de structure invalide, ignorer
                    }
                }
            }
        }
    }
    
    /**
     * Gère les interactions avec les structures pour le système de jobs
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Vérifier que c'est un clic sur un bloc
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
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
                
                // Right-click: Afficher la preview (sans annuler le placement de blocs)
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    // Ne pas annuler si le joueur tient un bloc plaçable (pour ne pas empêcher le placement)
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (heldItem != null && heldItem.getType().isBlock() && heldItem.getType() != org.bukkit.Material.AIR) {
                        return;
                    }
                    jobManager.showStructureTapPreview(player, structureId, stateName);
                    event.setCancelled(true);
                    return;
                }
                
                // Left-click: Exécuter l'action (récompenses job uniquement, pas de multi-break)
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    // Trigger le job manager pour les récompenses
                    jobManager.handleStructureTap(player, structureId, stateName);
                    
                    // Ne pas dégrader la structure automatiquement
                    // La dégradation ne se fait que via la compétence One-shot de la hache Quantum
                    // Le joueur casse uniquement le bloc qu'il a cliqué (comportement normal)
                }
            }
        }
    }
}
