package com.wynvers.quantum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter pour la commande /qexp
 */
public class QexpTabCompleter implements TabCompleter {

    private final List<String> actions = Arrays.asList("give", "take", "set", "reset");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("quantum.exp.admin")) {
            return null;
        }

        if (args.length == 1) {
            // Suggestions de noms de joueurs
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return filterList(playerNames, args[0]);
        }

        if (args.length == 2) {
            // Suggestions d'actions
            return filterList(actions, args[1]);
        }

        if (args.length == 3) {
            // Suggestions de montants
            String action = args[1].toLowerCase();
            if (action.equals("give") || action.equals("take") || action.equals("set")) {
                List<String> amounts = Arrays.asList("100", "500", "1000", "5000", "10000");
                return filterList(amounts, args[2]);
            }
        }

        return null;
    }

    /**
     * Filtre une liste selon le préfixe saisi
     */
    private List<String> filterList(List<String> list, String prefix) {
        if (prefix.isEmpty()) {
            return list;
        }
        
        List<String> filtered = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        
        for (String item : list) {
            if (item.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(item);
            }
        }
        
        return filtered.isEmpty() ? null : filtered;
    }
}