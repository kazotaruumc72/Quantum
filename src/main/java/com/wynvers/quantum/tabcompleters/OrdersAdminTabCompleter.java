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
import java.util.List;
import java.util.stream.Collectors;

/**
 * TabCompleter pour les commandes admin d'ordres
 */
public class OrdersAdminTabCompleter implements TabCompleter {
    private final Quantum plugin;

    public OrdersAdminTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // /quantum orders ...
        if (args.length == 1) {
            completions.add("orders");
        }
        // /quantum orders button ...
        else if (args.length == 2 && args[0].equalsIgnoreCase("orders")) {
            completions.add("button");
        }
        // /quantum orders button <action>
        else if (args.length == 3 && args[0].equalsIgnoreCase("orders") && args[1].equalsIgnoreCase("button")) {
            completions.add("createcategorie");
            completions.add("deletecategorie");
        }
        // /quantum orders button deletecategorie <categorie>
        else if (args.length == 4 && args[0].equalsIgnoreCase("orders") && 
                 args[1].equalsIgnoreCase("button") && 
                 args[2].equalsIgnoreCase("deletecategorie")) {
            // Liste des catégories existantes
            File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
            if (templateFile.exists()) {
                YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
                completions.addAll(template.getKeys(false));
            }
        }
        // /quantum orders button createcategorie <nouveau nom>
        else if (args.length == 4 && args[0].equalsIgnoreCase("orders") && 
                 args[1].equalsIgnoreCase("button") && 
                 args[2].equalsIgnoreCase("createcategorie")) {
            completions.add("<nom_categorie>");
        }

        // Filtrer selon ce que l'utilisateur a déjà tapé
        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
