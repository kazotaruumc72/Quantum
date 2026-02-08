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
 * TabCompleter pour la commande /healthbar
 */
public class HealthBarTabCompleter implements TabCompleter {
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Modes disponibles
            List<String> modes = Arrays.asList(
                "pourcentage",
                "coeurs",
                "info"
            );
            
            String input = args[0].toLowerCase();
            for (String mode : modes) {
                if (mode.toLowerCase().startsWith(input)) {
                    completions.add(mode);
                }
            }
        }
        
        return completions;
    }
}
