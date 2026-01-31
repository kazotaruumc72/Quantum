package fr.robie.quantum.commands;

import fr.robie.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StorageCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public StorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("quantum.storage")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }
        
        // TODO: Open storage GUI
        player.sendMessage("§a§l✓ §aOpening virtual storage...");
        plugin.getQuantumLogger().debug("Opening storage for: " + player.getName());
        
        return true;
    }
}
