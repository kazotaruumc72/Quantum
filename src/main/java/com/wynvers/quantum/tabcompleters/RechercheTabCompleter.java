package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * TabCompleter pour /recherche
 * Suggère tous les items configurés
 */
public class RechercheTabCompleter implements TabCompleter {
    private final Quantum plugin;

    public RechercheTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggérer tous les items
            completions.addAll(plugin.getOrderManager().getAllItemIds());
            
            // Filtrer selon ce que le joueur a tapé
            String input = args[0].toLowerCase();
            completions.removeIf(item -> !item.toLowerCase().startsWith(input));
        }

        return completions;
    }
}
