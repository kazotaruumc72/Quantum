package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MenuCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public MenuCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (!player.hasPermission("quantum.menu")) {
            plugin.getMessageManager().sendMessage(player, "system.no-permission");
            return true;
        }
        
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(player, "commands.menu-usage");
            return true;
        }
        
        String menuName = args[0].toLowerCase();
        
        Menu menu = plugin.getMenuManager().getMenu(menuName);
        
        if (menu == null) {
            menu = plugin.getMenuManager().getMenuByCommand(menuName);
        }
        
        if (menu == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("menu_name", menuName);
            plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open", placeholders);
            return true;
        }
        
        menu.open(player, plugin);
        
        return true;
    }
}
