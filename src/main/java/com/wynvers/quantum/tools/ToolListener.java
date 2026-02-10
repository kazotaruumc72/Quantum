package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoFurniture;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.furniture.FurnitureData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener pour les compétences des outils
 */
public class ToolListener implements Listener {
    
    private final Quantum plugin;
    private final ToolManager toolManager;
    
    public ToolListener(Quantum plugin, ToolManager toolManager) {
        this.plugin = plugin;
        this.toolManager = toolManager;
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
            String furnitureId = NexoFurniture.furnitureIdFromBlock(event.getBlock());
            if (furnitureId == null) return;
            
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
}
