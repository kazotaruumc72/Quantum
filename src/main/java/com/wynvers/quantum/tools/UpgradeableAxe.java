package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

/**
 * Hache améliorable avec compétence One-shot
 */
public class UpgradeableAxe extends UpgradeableTool {
    
    private final Quantum plugin;
    private final Random random;
    
    public UpgradeableAxe(Quantum plugin) {
        super(plugin, ToolType.AXE);
        this.plugin = plugin;
        this.random = new Random();
    }
    
    @Override
    public ItemStack createTool() {
        String nexoId = "quantum_axe_level1";
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
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.AXE.name());
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, 1);
            tool.setItemMeta(meta);
        }
        
        return tool;
    }
    
    @Override
    public void activateSkill(ItemStack tool, Object... args) {
        // La compétence One-shot est activée lors de la casse d'un arbre
        // Voir StructureManager pour l'implémentation
    }
    
    /**
     * Vérifie si la compétence One-shot s'active (basé sur le taux d'activation)
     */
    public boolean shouldActivateOneShot(int level) {
        int activationRate;
        
        if (level >= 7) {
            activationRate = 300; // 1/300
        } else if (level >= 4) {
            activationRate = 400; // 1/400
        } else {
            activationRate = 500; // 1/500
        }
        
        return random.nextInt(activationRate) == 0;
    }
    
    @Override
    public boolean upgrade(ItemStack tool, int currentLevel) {
        if (currentLevel >= 10) return false;

        int nextLevel = currentLevel + 1;

        String newNexoId = "quantum_axe_level" + nextLevel;
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
            newMeta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.AXE.name());
            newMeta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, nextLevel);

            // Apply the meta with both Nexo data (lore/enchants) and our custom data
            tool.setType(newTool.getType());
            tool.setItemMeta(newMeta);
        }

        return true;
    }
}
