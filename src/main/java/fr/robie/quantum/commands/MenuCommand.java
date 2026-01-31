package fr.robie.quantum.commands;

import fr.robie.quantum.Quantum;
import fr.robie.quantum.menu.Menu;
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
        
        // Try to find menu by ID
        Menu menu = plugin.getMenuManager().getMenu(menuName);
        
        // Try to find by command
        if (menu == null) {
            menu = plugin.getMenuManager().getMenuByCommand(menuName);
        }
        
        if (menu == null) {
            player.sendMessage("§cMenu not found: §7" + menuName);
            return true;
        }
        
        // TODO: Open menu GUI
        player.sendMessage("§a§l✓ §aOpening menu: §e" + menu.getTitle());
        plugin.getQuantumLogger().debug("Opening menu '" + menu.getId() + "' for: " + player.getName());
        
        return true;
    }
}
