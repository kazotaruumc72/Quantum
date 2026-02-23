package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.furniture.FurnitureData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Pioche améliorable avec compétence Double Extraction
 */
public class UpgradeablePickaxe extends UpgradeableTool {
    
    private final Quantum plugin;
    
    public UpgradeablePickaxe(Quantum plugin) {
        super(plugin, ToolType.PICKAXE);
        this.plugin = plugin;
    }
    
    @Override
    public ItemStack createTool() {
        String nexoId = "quantum_pickaxe_level1";
        var builder = NexoItems.itemFromId(nexoId);
        if (builder == null) {
            plugin.getQuantumLogger().error("Nexo item not found: " + nexoId + " - Please check that the item exists in your Nexo pack");
            return null;
        }
        
        ItemStack tool = builder.build();
        if (tool == null) {
            plugin.getQuantumLogger().error("Failed to build Nexo item: " + nexoId + " - ItemBuilder returned null");
            return null;
        }
        
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.PICKAXE.name());
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, 1);
            tool.setItemMeta(meta);
        }
        
        return tool;
    }
    
    @Override
    public void activateSkill(ItemStack tool, Object... args) {
        // La compétence Double Extraction est passive
        // Elle est appliquée lors du minage via le multiplicateur
    }
    
    /**
     * Récupère le multiplicateur de loot basé sur le niveau
     */
    public int getLootMultiplier(int level) {
        if (level >= 7) {
            return 4; // x4 loot
        } else if (level >= 4) {
            return 3; // x3 loot
        } else {
            return 2; // x2 loot
        }
    }
    
    @Override
    public boolean upgrade(ItemStack tool, int currentLevel) {
        if (currentLevel >= 10) return false; // Niveau max

        int nextLevel = currentLevel + 1;

        // Mettre à jour le Nexo ID
        String newNexoId = "quantum_pickaxe_level" + nextLevel;
        var builder = NexoItems.itemFromId(newNexoId);
        if (builder == null) {
            plugin.getQuantumLogger().error("Nexo item not found: " + newNexoId + " - Please check that the item exists in your Nexo pack");
            return false;
        }

        ItemStack newTool = builder.build();
        if (newTool == null) {
            plugin.getQuantumLogger().error("Failed to build Nexo item: " + newNexoId + " - ItemBuilder returned null");
            return false;
        }

        // Get the new meta from Nexo item (includes lore and enchantments)
        ItemMeta newMeta = newTool.getItemMeta();

        if (newMeta != null) {
            // Add our custom persistent data to the Nexo item's meta
            newMeta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.PICKAXE.name());
            newMeta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, nextLevel);

            // Apply the meta with both Nexo data (lore/enchants) and our custom data
            tool.setType(newTool.getType());
            tool.setItemMeta(newMeta);
        }

        return true;
    }
}
