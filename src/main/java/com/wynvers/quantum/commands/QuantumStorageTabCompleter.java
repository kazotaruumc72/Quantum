package com.wynvers.quantum.commands;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuantumStorageTabCompleter implements TabCompleter {

    private final Quantum plugin;

    public QuantumStorageTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: subcommands
            completions.addAll(Arrays.asList("transfer", "remove"));
            return filterCompletions(completions, args[0]);
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("transfer")) {
            return handleTransferTabComplete(sender, args);
        }

        if (subCommand.equals("remove")) {
            return handleRemoveTabComplete(sender, args);
        }

        return completions;
    }

    private List<String> handleTransferTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            // Second argument: item type or special keywords
            if (sender instanceof Player) {
                completions.addAll(Arrays.asList("hand", "all"));
            }
            
            // Add nexo: prefix
            completions.add("nexo:");
            
            // Add minecraft: prefix
            completions.add("minecraft:");
            
            // Add all Nexo items with nexo: prefix
            completions.addAll(getNexoItemsWithPrefix());
            
            // Add all vanilla materials with minecraft: prefix
            completions.addAll(getVanillaMaterialsWithPrefix());
            
            return filterCompletions(completions, args[1]);
        }

        if (args.length == 3) {
            // Third argument: amount
            if (args[1].equalsIgnoreCase("hand") || args[1].equalsIgnoreCase("all")) {
                return completions; // No amount for 'all'
            }
            completions.addAll(Arrays.asList("1", "16", "32", "64"));
            return filterCompletions(completions, args[2]);
        }

        if (args.length == 4 && sender instanceof org.bukkit.command.ConsoleCommandSender) {
            // Fourth argument (console only): player name
            return getOnlinePlayerNames();
        }

        return completions;
    }

    private List<String> handleRemoveTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            // Second argument: item from storage
            
            // Add nexo: prefix
            completions.add("nexo:");
            
            // Add minecraft: prefix
            completions.add("minecraft:");
            
            if (sender instanceof Player) {
                Player player = (Player) sender;
                var storage = plugin.getStorageManager().getStorage(player);
                
                // Add Nexo items from player's storage with prefix
                for (String nexoId : storage.getNexoItems().keySet()) {
                    completions.add("nexo:" + nexoId);
                    completions.add(nexoId); // Also without prefix for convenience
                }
                
                // Add vanilla materials from player's storage with prefix
                for (Material material : storage.getItems().keySet()) {
                    completions.add("minecraft:" + material.name().toLowerCase());
                    completions.add(material.name().toLowerCase()); // Also without prefix
                }
            } else {
                // Console - show all possible items
                completions.addAll(getNexoItemsWithPrefix());
                completions.addAll(getVanillaMaterialsWithPrefix());
            }
            
            return filterCompletions(completions, args[1]);
        }

        if (args.length == 3) {
            // Third argument: amount
            if (sender instanceof Player) {
                Player player = (Player) sender;
                var storage = plugin.getStorageManager().getStorage(player);
                
                // Get max amount from storage
                String itemArg = args[1];
                int maxAmount = 0;
                
                if (itemArg.startsWith("nexo:")) {
                    String nexoId = itemArg.substring(5);
                    maxAmount = storage.getNexoAmount(nexoId);
                } else if (itemArg.startsWith("minecraft:")) {
                    String materialName = itemArg.substring(10).toUpperCase();
                    try {
                        Material material = Material.valueOf(materialName);
                        maxAmount = storage.getAmount(material);
                    } catch (IllegalArgumentException ignored) {}
                } else {
                    // Try without prefix
                    if (storage.getNexoItems().containsKey(itemArg)) {
                        maxAmount = storage.getNexoAmount(itemArg);
                    } else {
                        try {
                            Material material = Material.valueOf(itemArg.toUpperCase());
                            maxAmount = storage.getAmount(material);
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
                
                if (maxAmount > 0) {
                    completions.add(String.valueOf(Math.min(maxAmount, 64)));
                    if (maxAmount > 1) completions.add("1");
                    if (maxAmount >= 16) completions.add("16");
                    if (maxAmount >= 32) completions.add("32");
                    if (maxAmount >= 64) completions.add("64");
                }
            } else {
                completions.addAll(Arrays.asList("1", "16", "32", "64"));
            }
            return filterCompletions(completions, args[2]);
        }

        if (args.length == 4 && sender instanceof org.bukkit.command.ConsoleCommandSender) {
            // Fourth argument (console only): player name
            return getOnlinePlayerNames();
        }

        return completions;
    }

    private List<String> getNexoItemsWithPrefix() {
        List<String> items = new ArrayList<>();
        try {
            // Get all Nexo items
            for (String itemId : NexoItems.items()) {
                items.add("nexo:" + itemId);
            }
        } catch (Exception e) {
            // Nexo might not be available or no items
        }
        return items;
    }

    private List<String> getVanillaMaterialsWithPrefix() {
        List<String> materials = new ArrayList<>();
        // Only add commonly used materials to avoid huge list
        List<String> commonMaterials = Arrays.asList(
            "diamond", "emerald", "gold_ingot", "iron_ingot", "netherite_ingot",
            "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe",
            "coal", "redstone", "lapis_lazuli", "quartz", "glowstone_dust",
            "ender_pearl", "blaze_rod", "nether_star", "dragon_egg",
            "oak_log", "stone", "cobblestone", "dirt", "grass_block"
        );
        
        for (String material : commonMaterials) {
            materials.add("minecraft:" + material);
        }
        
        return materials;
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        String lowerInput = input.toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(lowerInput))
            .sorted()
            .collect(Collectors.toList());
    }
}
