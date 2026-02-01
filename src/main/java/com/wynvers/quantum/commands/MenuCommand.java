package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public MenuCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("quantum.menu")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cUsage: /menu <menu_name>");
            return true;
        }
        
        String menuName = args[0].toLowerCase();
        
        Menu menu = plugin.getMenuManager().getMenu(menuName);
        
        if (menu == null) {
            menu = plugin.getMenuManager().getMenuByCommand(menuName);
        }
        
        if (menu == null) {
            player.sendMessage("§cMenu not found: §7" + menuName);
            return true;
        }
        
        menu.open(player, plugin);
        
        return true;
    }
}
