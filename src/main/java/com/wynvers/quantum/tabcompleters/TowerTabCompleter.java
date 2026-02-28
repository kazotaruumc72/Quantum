package com.wynvers.quantum.tabcompleters;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerConfig;
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

/**
 * Tab completer for /tower command
 * 
 * Provides auto-completion for:
 * - Subcommands (progress, reset, tp, reload)
 * - Player names
 * - Tower IDs
 * - Floor numbers
 */
public class TowerTabCompleter implements TabCompleter {

    private final Quantum plugin;

    public TowerTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // /tower <subcommand>
        if (args.length == 1) {
            completions.add("progress");
            completions.add("storage");

            if (sender.hasPermission("quantum.tower.reset")) {
                completions.add("reset");
            }

            if (sender.hasPermission("quantum.tower.teleport")) {
                completions.add("tp");
            }

            if (sender.hasPermission("quantum.tower.complete")) {
                completions.add("complete");
            }

            if (sender.hasPermission("quantum.tower.set")) {
                completions.add("set");
            }

            if (sender.hasPermission("quantum.tower.reload")) {
                completions.add("reload");
            }

            return filterCompletions(completions, args[0]);
        }

        // /tower progress <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("progress")) {
            if (sender.hasPermission("quantum.tower.progress.others")) {
                return filterCompletions(
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()),
                    args[1]
                );
            }
        }

        // /tower reset <player> [tower]
        if (args[0].equalsIgnoreCase("reset")) {
            // /tower reset <player>
            if (args.length == 2) {
                return filterCompletions(
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()),
                    args[1]
                );
            }

            // /tower reset <player> <tower>
            if (args.length == 3) {
                if (plugin.getTowerManager() != null) {
                    List<String> towerIds = plugin.getTowerManager().getTowerIds();
                    return filterCompletions(towerIds, args[2]);
                }
            }
        }

        // /tower tp <tower> <floor>
        if (args[0].equalsIgnoreCase("tp")) {
            // /tower tp <tower>
            if (args.length == 2) {
                if (plugin.getTowerManager() != null) {
                    List<String> towerIds = plugin.getTowerManager().getTowerIds();
                    return filterCompletions(towerIds, args[1]);
                }
            }

            // /tower tp <tower> <floor>
            if (args.length == 3) {
                if (plugin.getTowerManager() != null) {
                    String towerId = args[1];
                    TowerConfig tower = plugin.getTowerManager().getTower(towerId);
                    
                    if (tower != null) {
                        List<String> floors = new ArrayList<>();
                        
                        // Regular floors (1-25)
                        for (int i = 1; i <= tower.getFloors(); i++) {
                            floors.add(String.valueOf(i));
                        }
                        
                        // Final boss floor
                        if (tower.getFinalBossFloor() > 0) {
                            floors.add(String.valueOf(tower.getFinalBossFloor()));
                        }
                        
                        return filterCompletions(floors, args[2]);
                    }
                }
            }
        }

        // /tower complete <player> <tower> <floor>
        if (args[0].equalsIgnoreCase("complete")) {
            if (args.length == 2) {
                return filterCompletions(
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()),
                    args[1]
                );
            }
            if (args.length == 3 && plugin.getTowerManager() != null) {
                return filterCompletions(plugin.getTowerManager().getTowerIds(), args[2]);
            }
            if (args.length == 4 && plugin.getTowerManager() != null) {
                String towerId = args[2];
                TowerConfig tower = plugin.getTowerManager().getTower(towerId);
                if (tower != null) {
                    List<String> floors = new ArrayList<>();
                    for (int i = 1; i <= tower.getTotalFloors(); i++) {
                        floors.add(String.valueOf(i));
                    }
                    return filterCompletions(floors, args[3]);
                }
            }
        }

        // /tower storage add ...
        if (args[0].equalsIgnoreCase("storage")) {
            if (args.length == 2) {
                if (sender.hasPermission("quantum.tower.storage.add")) {
                    return filterCompletions(List.of("add"), args[1]);
                }
                return completions;
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
                List<String> items = new ArrayList<>(Arrays.asList(
                    "nexo:", "minecraft:",
                    "minecraft:diamond", "minecraft:emerald", "minecraft:gold_ingot",
                    "minecraft:iron_ingot", "minecraft:coal", "minecraft:redstone"
                ));
                // Nexo items
                try {
                    for (Object itemObj : NexoItems.items()) {
                        if (itemObj instanceof com.nexomc.nexo.items.ItemBuilder) {
                            var stack = ((com.nexomc.nexo.items.ItemBuilder) itemObj).build();
                            String id = NexoItems.idFromItem(stack);
                            if (id != null) items.add("nexo:" + id);
                        }
                    }
                } catch (Exception ignored) {}
                return filterCompletions(items, args[2]);
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
                return filterCompletions(Arrays.asList("1", "16", "32", "64", "128", "256"), args[3]);
            }
            if (args.length == 5 && args[1].equalsIgnoreCase("add")) {
                return filterCompletions(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    args[4]
                );
            }
        }

        // /tower set spawn <tower> <floor>
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 2) {
                return filterCompletions(List.of("spawn"), args[1]);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("spawn")) {
                if (plugin.getTowerManager() != null) {
                    return filterCompletions(plugin.getTowerManager().getTowerIds(), args[2]);
                }
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("spawn")) {
                if (plugin.getTowerManager() != null) {
                    String towerId = args[2];
                    TowerConfig tower = plugin.getTowerManager().getTower(towerId);
                    if (tower != null) {
                        List<String> floors = new ArrayList<>();
                        for (int i = 1; i <= tower.getTotalFloors(); i++) {
                            floors.add(String.valueOf(i));
                        }
                        return filterCompletions(floors, args[3]);
                    }
                }
            }
        }

        return completions;
    }

    /**
     * Filter completions based on what the player has typed
     * @param completions List of possible completions
     * @param input What the player has typed
     * @return Filtered list
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }
}
