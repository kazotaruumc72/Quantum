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
 * TabCompleter pour la commande /storage
 * Fournit l'auto-complétion pour toutes les sous-commandes de storage
 */
public class StorageTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes de storage
            List<String> subcommands = new ArrayList<>();
            subcommands.add("open");
            
            // Ajouter les commandes admin si le sender a la permission
            if (sender.hasPermission("quantum.storage.admin")) {
                subcommands.addAll(Arrays.asList(
                    "view",
                    "clear",
                    "reload"
                ));
            }
            
            // Filtrer les suggestions
            String input = args[0].toLowerCase();
            for (String subcmd : subcommands) {
                if (subcmd.toLowerCase().startsWith(input)) {
                    completions.add(subcmd);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("clear"))) {
            // Auto-complétion des noms de joueurs pour view et clear
            if (sender.hasPermission("quantum.storage.admin")) {
                String input = args[1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}
