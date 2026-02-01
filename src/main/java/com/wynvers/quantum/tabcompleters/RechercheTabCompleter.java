package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.OrderManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RechercheTabCompleter implements TabCompleter {
    private final Quantum plugin;
    private final OrderManager orderManager;
    
    public RechercheTabCompleter(Quantum plugin) {
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
        }
        
        return completions;
    }
}
