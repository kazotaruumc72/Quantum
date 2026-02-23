package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

/**
 * Houe améliorable avec compétence Rare Loot
 */
public class UpgradeableHoe extends UpgradeableTool {
    
    private final Quantum plugin;
    private final Random random;
    
    public UpgradeableHoe(Quantum plugin) {
        super(plugin, ToolType.HOE);
        this.plugin = plugin;
        this.random = new Random();
    }
    
    @Override
    public ItemStack createTool() {
        String nexoId = "quantum_hoe_level1";
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
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.HOE.name());
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, 1);
            tool.setItemMeta(meta);
        }

        // Update lore with config
        if (plugin.getToolManager() != null) {
            updateLore(tool, 1, plugin.getToolManager().getConfig());
        }

        return tool;
    }
    
    @Override
    public void activateSkill(ItemStack tool, Object... args) {
        // La compétence Rare Loot est activée lors de la récolte
        // Voir CustomCropManager pour l'implémentation
    }
    
    /**
     * Vérifie si un loot rare devrait être donné
     */
    public boolean shouldGiveRareLoot(int level) {
        double chance;
        
        if (level >= 7) {
            chance = 10.0; // 10%
        } else if (level >= 4) {
            chance = 7.5; // 7.5%
        } else {
            chance = 5.0; // 5%
        }
        
        return random.nextDouble() * 100 < chance;
    }
    
    /**
     * Récupère le nombre maximum de drops rares
     */
    public int getMaxRareDrops(int level) {
        if (level >= 7) {
            return 3;
        } else if (level >= 4) {
            return 2;
        } else {
            return 1;
        }
    }
    
    @Override
    public boolean upgrade(ItemStack tool, int currentLevel) {
        if (currentLevel >= 10) return false;

        int nextLevel = currentLevel + 1;

        String newNexoId = "quantum_hoe_level" + nextLevel;
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
            newMeta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, ToolType.HOE.name());
            newMeta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, nextLevel);

            // Apply the meta with both Nexo data (lore/enchants) and our custom data
            tool.setType(newTool.getType());
            tool.setItemMeta(newMeta);
        }

        // Update lore with config
        if (plugin.getToolManager() != null) {
            updateLore(tool, nextLevel, plugin.getToolManager().getConfig());
        }

        return true;
    }
}
