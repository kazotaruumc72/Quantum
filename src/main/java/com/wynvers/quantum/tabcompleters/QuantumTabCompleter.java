package com.wynvers.quantum.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter pour la commande /quantum
 * Fournit l'auto-complétion pour toutes les sous-commandes
 */
public class QuantumTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes principales
            List<String> subcommands = Arrays.asList(
                "reload",
                "help",
                "info",
                "version",
                "storage"
            );
            
            // Filtrer les suggestions basées sur ce que l'utilisateur a tapé
            String input = args[0].toLowerCase();
            for (String subcmd : subcommands) {
                if (subcmd.toLowerCase().startsWith(input)) {
                    completions.add(subcmd);
                }
            }
        }
        
        return completions;
    }
}
