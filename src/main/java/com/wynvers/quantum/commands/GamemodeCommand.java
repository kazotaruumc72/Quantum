package com.wynvers.quantum.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Gamemode shortcuts command handler
 * Handles all gamemode change commands: /gmc, /gms, /gmsp, /gma
 * Uses the command label to determine which gamemode to apply
 */
public class GamemodeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        // Determine gamemode from command label
        GameMode gameMode = switch (label.toLowerCase()) {
            case "gmc" -> GameMode.CREATIVE;
            case "gms" -> GameMode.SURVIVAL;
            case "gmsp" -> GameMode.SPECTATOR;
            case "gma" -> GameMode.ADVENTURE;
            default -> null;
        };

        if (gameMode == null) {
            player.sendMessage("§cUnknown gamemode command.");
            return true;
        }

        // Check permission
        String permission = "quantum.gamemode." + gameMode.name().toLowerCase();
        if (!player.hasPermission(permission) && !player.hasPermission("quantum.gamemode.*")) {
            player.sendMessage("§cYou don't have permission to change to " + gameMode.name() + " mode.");
            return true;
        }

        // Change gamemode
        player.setGameMode(gameMode);
        player.sendMessage("§aGamemode changed to §e" + gameMode.name() + "§a.");
        
        return true;
    }
}
