package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Commande console pour forcer la sortie d'un joueur d'une zone
 * Usage: /zoneexit <player>
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ZoneExitCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public ZoneExitCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Cette commande ne peut être exécutée que par la console
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§c[Quantum] Cette commande ne peut être exécutée que par la console!");
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage("§c[Quantum] Usage: /zoneexit <player>");
            return true;
        }
        
        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage("§c[Quantum] Joueur introuvable: " + playerName);
            return true;
        }
        
        // Forcer la sortie de la zone
        plugin.getZoneManager().forcePlayerExitZone(target);
        sender.sendMessage("§a[Quantum] " + target.getName() + " a été autorisé à quitter la zone.");
        
        return true;
    }
}
