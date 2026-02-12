package com.wynvers.quantum.commands;

import com.wynvers.quantum.spawn.SpawnManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for /spawn
 * Teleports players to the spawn location
 */
public class SpawnCommand implements CommandExecutor {

    private final SpawnManager spawnManager;

    public SpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("quantum.spawn.use")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        Location spawnLocation = spawnManager.getSpawn();
        
        if (spawnLocation == null) {
            player.sendMessage("§cSpawn location is not set.");
            return true;
        }

        player.teleport(spawnLocation);
        player.sendMessage("§aTeleported to spawn.");
        return true;
    }
}
