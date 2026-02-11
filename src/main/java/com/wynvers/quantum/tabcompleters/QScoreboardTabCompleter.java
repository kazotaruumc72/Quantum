package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QScoreboardTabCompleter implements TabCompleter {
    
    private final Quantum plugin;
    
    public QScoreboardTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Premier argument: actions
        if (args.length == 1) {
            List<String> actions = new ArrayList<>(Arrays.asList(
                "on", "off", "status",
                "enable", "disable",
                "show", "hide",
                "activer", "desactiver", "désactiver",
                "statut"
            ));
            
            // Ajouter les joueurs en ligne si admin
            if (sender.hasPermission("quantum.admin")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    actions.add(player.getName());
                }
            }
            
            // Filtrer selon ce que l'utilisateur a tapé
            String input = args[0].toLowerCase();
            completions = actions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .sorted()
                .collect(Collectors.toList());
        }
        
        // Deuxième argument: nom du joueur (admin seulement)
        else if (args.length == 2) {
            if (sender.hasPermission("quantum.admin")) {
                String input = args[1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}
