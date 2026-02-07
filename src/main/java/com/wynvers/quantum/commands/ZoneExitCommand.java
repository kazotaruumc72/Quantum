package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        sender.sendMessage("§c[Quantum] La commande /zoneexit est désactivée (ancien système de zones).");
        return true;
    }
}
