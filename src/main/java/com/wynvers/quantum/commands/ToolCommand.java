package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.tools.ToolManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Commande pour gérer les outils améliorables
 */
public class ToolCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final ToolManager toolManager;
    
    public ToolCommand(Quantum plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "upgrade":
                return handleUpgrade(player);
            case "info":
                return handleInfo(player);
            case "give":
                if (args.length < 3) {
                    plugin.getMessageManager().sendMessage(player, "tools.usage-give");
                    return true;
                }
                return handleGive(player, args[1], args[2]);
            default:
                sendHelp(player);
                return true;
        }
    }
    
    private boolean handleUpgrade(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(player, "tools.no-item-in-hand");
            return true;
        }
        
        toolManager.upgradeTool(item, player);
        return true;
    }
    
    private boolean handleInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(player, "tools.no-item-in-hand");
            return true;
        }
        
        if (toolManager.getPickaxe().isQuantumTool(item)) {
            int level = toolManager.getPickaxe().getLevel(item);
            int multiplier = toolManager.getPickaxe().getLootMultiplier(level);
            
            plugin.getMessageManager().sendMessage(player, "tools.info.pickaxe.header");
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(level));
            plugin.getMessageManager().sendMessage(player, "tools.info.pickaxe.level", placeholders);
            
            placeholders.clear();
            placeholders.put("multiplier", String.valueOf(multiplier));
            plugin.getMessageManager().sendMessage(player, "tools.info.pickaxe.multiplier", placeholders);
        } else if (toolManager.getAxe().isQuantumTool(item)) {
            int level = toolManager.getAxe().getLevel(item);
            
            plugin.getMessageManager().sendMessage(player, "tools.info.axe.header");
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(level));
            plugin.getMessageManager().sendMessage(player, "tools.info.axe.level", placeholders);
            plugin.getMessageManager().sendMessage(player, "tools.info.axe.skill");
        } else if (toolManager.getHoe().isQuantumTool(item)) {
            int level = toolManager.getHoe().getLevel(item);
            int maxDrops = toolManager.getHoe().getMaxRareDrops(level);
            
            plugin.getMessageManager().sendMessage(player, "tools.info.hoe.header");
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(level));
            plugin.getMessageManager().sendMessage(player, "tools.info.hoe.level", placeholders);
            
            placeholders.clear();
            placeholders.put("max_drops", String.valueOf(maxDrops));
            plugin.getMessageManager().sendMessage(player, "tools.info.hoe.max-drops", placeholders);
        } else {
            plugin.getMessageManager().sendMessage(player, "tools.not-quantum-tool");
        }
        
        return true;
    }
    
    private boolean handleGive(Player player, String type, String levelStr) {
        if (!player.hasPermission("quantum.tool.give")) {
            plugin.getMessageManager().sendMessage(player, "tools.no-permission");
            return true;
        }
        
        ItemStack tool = null;
        
        switch (type.toLowerCase()) {
            case "pickaxe":
            case "pioche":
                tool = toolManager.getPickaxe().createTool();
                break;
            case "axe":
            case "hache":
                tool = toolManager.getAxe().createTool();
                break;
            case "hoe":
            case "houe":
                tool = toolManager.getHoe().createTool();
                break;
            default:
                plugin.getMessageManager().sendMessage(player, "tools.invalid-type");
                return true;
        }
        
        if (tool != null) {
            player.getInventory().addItem(tool);
            plugin.getMessageManager().sendMessage(player, "tools.received");
        } else {
            plugin.getMessageManager().sendMessage(player, "tools.creation-error");
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        plugin.getMessageManager().sendMessage(player, "tools.help.header");
        for (String command : plugin.getMessageManager().getMessageList("tools.help.commands")) {
            player.sendMessage(plugin.getMessageManager().toComponent(command));
        }
    }
}
