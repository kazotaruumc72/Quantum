package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter pour la commande /quantum
 * Fournit l'auto-complétion pour toutes les sous-commandes
 */
public class QuantumTabCompleter implements TabCompleter {
    private final Quantum plugin;

    public QuantumTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes principales
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                "reload",
                "stats",
                "storagestats",
                "sstats",
                "help",
                "version"
            ));
            
            // Ajouter orders si admin
            if (sender.hasPermission("quantum.admin.orders")) {
                subcommands.add("orders");
            }
            
            // Filtrer les suggestions basées sur ce que l'utilisateur a tapé
            String input = args[0].toLowerCase();
            for (String subcmd : subcommands) {
                if (subcmd.toLowerCase().startsWith(input)) {
                    completions.add(subcmd);
                }
            }
        } 
        else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            // Sous-commandes pour /quantum reload
            List<String> reloadTypes = Arrays.asList(
                "all",
                "price",
                "config",
                "menus",
                "messages"
            );
            
            String input = args[1].toLowerCase();
            for (String type : reloadTypes) {
                if (type.toLowerCase().startsWith(input)) {
                    completions.add(type);
                }
            }
        }
        else if (args.length == 2 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("statistics"))) {
            // Sous-commandes pour /quantum stats
            List<String> statsOptions = new ArrayList<>(Arrays.asList(
                "all",
                "list",
                "reload",
                "recalculate",
                // Catégories par défaut
                "cultures",
                "loots",
                "items",
                "potions",
                "armures",
                "outils",
                "autre"
            ));
            
            // Ajouter les catégories du fichier orders_template.yml
            File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
            if (templateFile.exists()) {
                YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
                for (String key : template.getKeys(false)) {
                    if (!statsOptions.contains(key.toLowerCase())) {
                        statsOptions.add(key.toLowerCase());
                    }
                }
            }
            
            String input = args[1].toLowerCase();
            for (String option : statsOptions) {
                if (option.toLowerCase().startsWith(input)) {
                    completions.add(option);
                }
            }
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("orders")) {
            // /quantum orders ...
            completions.add("button");
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("orders") && args[1].equalsIgnoreCase("button")) {
            // /quantum orders button ...
            completions.add("createcategorie");
            completions.add("deletecategorie");
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("orders") && 
                 args[1].equalsIgnoreCase("button") && 
                 args[2].equalsIgnoreCase("deletecategorie")) {
            // /quantum orders button deletecategorie <categorie>
            File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
            if (templateFile.exists()) {
                YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
                completions.addAll(template.getKeys(false));
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("orders") && 
                 args[1].equalsIgnoreCase("button") && 
                 args[2].equalsIgnoreCase("createcategorie")) {
            // /quantum orders button createcategorie <nom>
            completions.add("<nom_categorie>");
        }
        
        // Filtrer selon l'input
        String input = args[args.length - 1].toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String c : completions) {
            if (c.toLowerCase().startsWith(input)) {
                filtered.add(c);
            }
        }
        
        return filtered;
    }
}
