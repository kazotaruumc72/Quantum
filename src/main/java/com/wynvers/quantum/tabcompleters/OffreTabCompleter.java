package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.OrderManager;
import com.wynvers.quantum.managers.OrderManager.ItemPrice;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OffreTabCompleter implements TabCompleter {
    private final Quantum plugin;
    private final OrderManager orderManager;
    
    public OffreTabCompleter(Quantum plugin) {
        this.plugin = plugin;
        this.orderManager = plugin.getOrderManager();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggérer tous les items disponibles
            String partial = args[0].toLowerCase();
            
            completions = orderManager.getAllItemKeys().stream()
                .filter(key -> key.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
            
            // Si aucune correspondance, montrer tous les items
            if (completions.isEmpty() && partial.isEmpty()) {
                completions = orderManager.getAllItemKeys();
            }
        } else if (args.length == 2) {
            // Suggérer des quantités communes
            completions = Arrays.asList("1", "8", "16", "32", "64", "128");
        } else if (args.length == 3) {
            // Suggérer des prix basés sur l'item sélectionné
            String itemKey = args[0].toLowerCase();
            
            if (orderManager.hasItemPrice(itemKey)) {
                ItemPrice itemPrice = orderManager.getItemPrice(itemKey);
                
                double min = itemPrice.minPrice;
                double max = itemPrice.maxPrice;
                double mid = (min + max) / 2;
                
                completions = Arrays.asList(
                    String.format("%.2f", min),
                    String.format("%.2f", mid),
                    String.format("%.2f", max)
                );
            }
        }
        
        return completions;
    }
}
