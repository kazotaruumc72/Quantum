package com.wynvers.quantum.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TabCompleter pour la commande /tool
 */
public class ToolTabCompleter implements TabCompleter {
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes disponibles
            List<String> subCommands = Arrays.asList("upgrade", "info", "give");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                // Types d'outils disponibles
                List<String> toolTypes = Arrays.asList(
                    "pickaxe", "pioche",
                    "axe", "hache",
                    "hoe", "houe"
                );
                StringUtil.copyPartialMatches(args[1], toolTypes, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                // Niveau de l'outil (note: actuellement seul le niveau 1 est utilisé)
                List<String> levels = Arrays.asList("1", "2", "3", "4", "5");
                StringUtil.copyPartialMatches(args[2], levels, completions);
                return completions;
            }
        }
        
        return completions;
    }
}
