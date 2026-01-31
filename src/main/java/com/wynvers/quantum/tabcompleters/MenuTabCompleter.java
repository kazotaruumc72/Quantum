package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter pour la commande /menu
 * Fournit l'auto-complétion pour les sous-commandes et les noms de menus
 */
public class MenuTabCompleter implements TabCompleter {

    private final Quantum plugin;

    public MenuTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes de menu
            List<String> subcommands = Arrays.asList(
                "open",
                "list",
                "reload"
            );
            
            // Filtrer les suggestions
            String input = args[0].toLowerCase();
            for (String subcmd : subcommands) {
                if (subcmd.toLowerCase().startsWith(input)) {
                    completions.add(subcmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            // Auto-complétion des noms de menus disponibles
            // Cette partie peut être étendue pour récupérer dynamiquement les noms de menus
            // depuis le MenuManager
            String input = args[1].toLowerCase();
            // Exemple de menus (peut être remplacé par une récupération dynamique)
            List<String> menus = Arrays.asList(
                "main",
                "shop",
                "settings"
            );
            
            for (String menu : menus) {
                if (menu.toLowerCase().startsWith(input)) {
                    completions.add(menu);
                }
            }
        }
        
        return completions;
    }
}
