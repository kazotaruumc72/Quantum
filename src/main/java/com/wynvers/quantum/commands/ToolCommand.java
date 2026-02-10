package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.tools.ToolManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
        Player player = (Player) sender;
        
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
                    player.sendMessage("§cUtilisation: /tool give <type> <niveau>");
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
            player.sendMessage("§cVous devez tenir un outil dans votre main!");
            return true;
        }
        
        toolManager.upgradeTool(item, player);
        return true;
    }
    
    private boolean handleInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cVous devez tenir un outil dans votre main!");
            return true;
        }
        
        if (toolManager.getPickaxe().isQuantumTool(item)) {
            int level = toolManager.getPickaxe().getLevel(item);
            int multiplier = toolManager.getPickaxe().getLootMultiplier(level);
            player.sendMessage("§e=== Pioche Quantum ===");
            player.sendMessage("§7Niveau: §a" + level);
            player.sendMessage("§7Multiplicateur: §ax" + multiplier);
        } else if (toolManager.getAxe().isQuantumTool(item)) {
            int level = toolManager.getAxe().getLevel(item);
            player.sendMessage("§e=== Hache Quantum ===");
            player.sendMessage("§7Niveau: §a" + level);
            player.sendMessage("§7Compétence: §aOne-shot");
        } else if (toolManager.getHoe().isQuantumTool(item)) {
            int level = toolManager.getHoe().getLevel(item);
            int maxDrops = toolManager.getHoe().getMaxRareDrops(level);
            player.sendMessage("§e=== Houe Quantum ===");
            player.sendMessage("§7Niveau: §a" + level);
            player.sendMessage("§7Drops rares max: §a" + maxDrops);
        } else {
            player.sendMessage("§cCet item n'est pas un outil Quantum!");
        }
        
        return true;
    }
    
    private boolean handleGive(Player player, String type, String levelStr) {
        if (!player.hasPermission("quantum.tool.give")) {
            player.sendMessage("§cVous n'avez pas la permission!");
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
                player.sendMessage("§cType invalide! Utilisez: pickaxe, axe, ou hoe");
                return true;
        }
        
        if (tool != null) {
            player.getInventory().addItem(tool);
            player.sendMessage("§aVous avez reçu un outil Quantum!");
        } else {
            player.sendMessage("§cErreur lors de la création de l'outil!");
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§e=== Commandes Outils Quantum ===");
        player.sendMessage("§7/tool upgrade §f- Améliorer l'outil en main");
        player.sendMessage("§7/tool info §f- Afficher les infos de l'outil");
        player.sendMessage("§7/tool give <type> <niveau> §f- Obtenir un outil");
    }
}
