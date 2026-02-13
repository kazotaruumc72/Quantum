package com.wynvers.quantum.tabcompleters;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TabCompleter for /apartment command
 * Provides auto-completion for apartment subcommands and player names
 */
public class ApartmentTabCompleter implements TabCompleter {
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("quantum.apartment.use")) {
            return completions;
        }
        
        if (args.length == 1) {
            // Subcommands
            completions.addAll(Arrays.asList(
                "create",
                "upgrade",
                "invite",
                "remove",
                "lock",
                "unlock",
                "tp",
                "teleport"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("invite") || subCommand.equals("remove")) {
                // Add online player names
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            } else if (subCommand.equals("create")) {
                // Placeholder for apartment name
                completions.add("<name>");
            }
        }
        
        // Filter based on input
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
