package com.wynvers.quantum.commands;

import com.wynvers.quantum.home.Home;
import com.wynvers.quantum.home.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Home command handler
 * Supports: /home [name], /sethome [name], /delhome <name>
 */
public class HomeCommand implements CommandExecutor {

    private final HomeManager homeManager;

    public HomeCommand(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        String cmd = label.toLowerCase();

        switch (cmd) {
            case "home", "h" -> handleHome(player, args);
            case "sethome" -> handleSetHome(player, args);
            case "delhome", "removehome", "deletehome" -> handleDelHome(player, args);
        }

        return true;
    }

    private void handleHome(Player player, String[] args) {
        if (!player.hasPermission("quantum.home.use")) {
            player.sendMessage("§cYou don't have permission to use homes.");
            return;
        }

        List<Home> homes = homeManager.getHomes(player.getUniqueId());

        if (homes.isEmpty()) {
            player.sendMessage("§cYou don't have any homes set. Use §e/sethome [name]§c to create one.");
            return;
        }

        String homeName = args.length > 0 ? args[0] : "home";
        
        if (homeManager.teleportToHome(player, homeName)) {
            player.sendMessage("§aTeleported to home §e" + homeName + "§a.");
        } else {
            player.sendMessage("§cHome §e" + homeName + "§c not found.");
            player.sendMessage("§7Your homes: §f" + homes.stream()
                .map(Home::getName)
                .collect(Collectors.joining("§7, §f")));
        }
    }

    private void handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("quantum.home.set")) {
            player.sendMessage("§cYou don't have permission to set homes.");
            return;
        }

        String homeName = args.length > 0 ? args[0] : "home";

        // Validate name
        if (!homeName.matches("[a-zA-Z0-9_-]+")) {
            player.sendMessage("§cHome name can only contain letters, numbers, underscores, and hyphens.");
            return;
        }

        if (homeName.length() > 32) {
            player.sendMessage("§cHome name is too long (maximum 32 characters).");
            return;
        }

        // Check if home already exists
        Home existing = homeManager.getHome(player.getUniqueId(), homeName);
        
        if (homeManager.setHome(player, homeName, player.getLocation())) {
            if (existing != null) {
                player.sendMessage("§aHome §e" + homeName + "§a has been updated.");
            } else {
                int maxHomes = homeManager.getMaxHomes(player);
                int currentHomes = homeManager.getHomes(player.getUniqueId()).size();
                player.sendMessage("§aHome §e" + homeName + "§a has been set. §7(" + currentHomes + "/" + 
                    (maxHomes == Integer.MAX_VALUE ? "∞" : maxHomes) + ")");
            }
        } else {
            int maxHomes = homeManager.getMaxHomes(player);
            player.sendMessage("§cYou have reached your home limit of §e" + maxHomes + "§c.");
            player.sendMessage("§7Delete a home with §f/delhome <name>§7 to create a new one.");
        }
    }

    private void handleDelHome(Player player, String[] args) {
        if (!player.hasPermission("quantum.home.delete")) {
            player.sendMessage("§cYou don't have permission to delete homes.");
            return;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /delhome <name>");
            return;
        }

        String homeName = args[0];

        if (homeManager.deleteHome(player.getUniqueId(), homeName)) {
            player.sendMessage("§aHome §e" + homeName + "§a has been deleted.");
        } else {
            player.sendMessage("§cHome §e" + homeName + "§c not found.");
        }
    }
}
