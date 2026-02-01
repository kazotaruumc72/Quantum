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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TabCompleter pour /offre
 * Arg 1: Items autorisés depuis orders_template.yml
 * Arg 2: Quantités communes
 * Arg 3: Prix suggérés
 */
public class OffreTabCompleter implements TabCompleter {
    private final Quantum plugin;

    public OffreTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Arg 1: Items autorisés avec leurs IDs
            Set<String> itemIds = plugin.getOrderManager().getAllItemIds();
            completions.addAll(itemIds);
            
            // Filtrer selon l'input
            String input = args[0].toLowerCase();
            return completions.stream()
                .filter(item -> item.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
            
        } else if (args.length == 2) {
            // Arg 2: Quantités communes
            completions.addAll(Arrays.asList("1", "8", "16", "32", "64", "128", "256"));
            
        } else if (args.length == 3) {
            // Arg 3: Prix suggérés
            completions.addAll(Arrays.asList("10", "50", "100", "500", "1000"));
        }

        return completions;
    }
}
