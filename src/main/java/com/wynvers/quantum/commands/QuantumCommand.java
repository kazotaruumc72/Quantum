package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class QuantumCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public QuantumCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("quantum.admin")) {
                    sender.sendMessage("§cYou don't have permission!");
                    return true;
                }
                
                plugin.reloadPlugin();
                sender.sendMessage("§a§l✓ §aQuantum reloaded successfully!");
                break;
                
            case "version":
            case "ver":
                sender.sendMessage("§8───────────────────────────");
                sender.sendMessage("§6§lQUANTUM §f- Advanced Storage");
                sender.sendMessage("§7Version: §e1.0.0");
                sender.sendMessage("§7Author: §eRobie");
                sender.sendMessage("§7Menus: §e" + plugin.getMenuManager().getMenuCount());
                sender.sendMessage("§8───────────────────────────");
                break;
                
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8───────────────────────────");
        sender.sendMessage("§6§lQUANTUM COMMANDS");
        sender.sendMessage("");
        sender.sendMessage("§e/quantum reload §7- Reload configuration");
        sender.sendMessage("§e/quantum version §7- Show version info");
        sender.sendMessage("§e/quantum help §7- Show this help");
        sender.sendMessage("§e/storage §7- Open virtual storage");
        sender.sendMessage("§e/menu <name> §7- Open custom menu");
        sender.sendMessage("§8───────────────────────────");
    }
}
