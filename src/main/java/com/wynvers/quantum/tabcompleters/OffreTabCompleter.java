package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter pour /offre
 * Arg 1: Items
 * Arg 2: Quantités communes
 * Arg 3: Prix suggérés (min, moyen, max)
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
            // Arg 1: Items
            completions.addAll(plugin.getOrderManager().getAllItemIds());
            
            String input = args[0].toLowerCase();
            completions.removeIf(item -> !item.toLowerCase().startsWith(input));
            
        } else if (args.length == 2) {
            // Arg 2: Quantités communes
            completions.addAll(Arrays.asList("1", "8", "16", "32", "64", "128"));
            
        } else if (args.length == 3) {
            // Arg 3: Prix suggérés basés sur l'item
            String itemId = args[0].toLowerCase();
            if (plugin.getOrderManager().hasItem(itemId)) {
                OrderItem item = plugin.getOrderManager().getItem(itemId);
                double min = item.getMinPrice();
                double max = item.getMaxPrice();
                double avg = (min + max) / 2.0;
                
                completions.add(String.format("%.2f", min));
                completions.add(String.format("%.2f", avg));
                completions.add(String.format("%.2f", max));
            } else {
                completions.add("<prix>");
            }
        }

        return completions;
    }
}
