package com.wynvers.quantum.tools;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

/**
 * Gestionnaire des outils améliorables
 */
public class ToolManager {
    
    private final Quantum plugin;
    private final UpgradeablePickaxe pickaxe;
    private final UpgradeableAxe axe;
    private final UpgradeableHoe hoe;
    private YamlConfiguration config;
    
    public ToolManager(Quantum plugin) {
        this.plugin = plugin;
        this.pickaxe = new UpgradeablePickaxe(plugin);
        this.axe = new UpgradeableAxe(plugin);
        this.hoe = new UpgradeableHoe(plugin);
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "tools.yml");
        if (!configFile.exists()) {
            plugin.saveResource("tools.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("✓ Tools system loaded!");
    }
    
    public void reload() {
        loadConfig();
    }
    
    /**
     * Améliore un outil
     */
    public boolean upgradeTool(ItemStack item, Player player) {
        UpgradeableTool tool = getToolFromItem(item);
        if (tool == null) {
            String message = config.getString("messages.invalid_tool", "&cCet outil n'est pas un outil Quantum!");
            player.sendMessage(message.replace('&', '§'));
            return false;
        }
        
        int currentLevel = tool.getLevel(item);
        if (currentLevel >= 10) {
            String message = config.getString("messages.tool_max_level", "&cCet outil est déjà au niveau maximum!");
            player.sendMessage(message.replace('&', '§'));
            return false;
        }
        
        // Vérifier le coût
        int cost = getUpgradeCost(tool.getToolType(), currentLevel);
        
        // TODO: Vérifier l'argent du joueur avec Vault
        // Pour l'instant, on suppose que le joueur a assez d'argent
        
        if (tool.upgrade(item, currentLevel)) {
            String message = config.getString("messages.tool_upgraded", "&a✓ Outil amélioré au niveau {level}!");
            message = message.replace("{level}", String.valueOf(currentLevel + 1));
            player.sendMessage(message.replace('&', '§'));
            return true;
        }
        
        return false;
    }
    
    /**
     * Récupère le coût d'amélioration
     */
    public int getUpgradeCost(ToolType toolType, int currentLevel) {
        String path = toolType.name().toLowerCase() + ".upgrade_costs." + currentLevel + "_to_" + (currentLevel + 1);
        return config.getInt(path, 1000);
    }
    
    /**
     * Identifie quel type d'outil est l'item
     */
    private UpgradeableTool getToolFromItem(ItemStack item) {
        if (pickaxe.isQuantumTool(item)) return pickaxe;
        if (axe.isQuantumTool(item)) return axe;
        if (hoe.isQuantumTool(item)) return hoe;
        return null;
    }
    
    public UpgradeablePickaxe getPickaxe() {
        return pickaxe;
    }
    
    public UpgradeableAxe getAxe() {
        return axe;
    }
    
    public UpgradeableHoe getHoe() {
        return hoe;
    }
    
    public YamlConfiguration getConfig() {
        return config;
    }
}
