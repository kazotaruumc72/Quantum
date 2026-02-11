package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Ancienne commande /zoneexit.
 * Désactivée depuis la refonte du système de zones/tours.
 */
public class ZoneExitCommand implements CommandExecutor {

    private final Quantum plugin;

    public ZoneExitCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            plugin.getMessageManager().sendMessage((Player) sender, "commands.zone-exit-disabled");
        } else {
            sender.sendMessage("[Quantum] La commande /zoneexit est désactivée (ancien système de zones).");
        }
        return true;
    }
}
