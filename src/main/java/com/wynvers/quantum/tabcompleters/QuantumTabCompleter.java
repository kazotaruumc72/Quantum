package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerConfig;
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
import java.util.Map;

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
                "version",
                "tower",
                "door",
                "npc",
                "progress",
                "reset",
                "info",
                "mobspawnzone"
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
        // Tab completion pour /quantum mobspawnzone
        else if (args[0].equalsIgnoreCase("mobspawnzone")) {
            if (args.length == 2) {
                // /quantum mobspawnzone <create>
                completions.add("create");
            }
            else if (args.length == 3 && args[1].equalsIgnoreCase("create")) {
                // /quantum mobspawnzone create <tower_id>
                completions.addAll(getTowerIds());
            }
            else if (args.length == 4 && args[1].equalsIgnoreCase("create")) {
                // /quantum mobspawnzone create <tower_id> <floor>
                String towerId = args[2];
                completions.addAll(getFloorsForTower(towerId));
            }
        }
        // Tab completion pour /quantum tower
        else if (args[0].equalsIgnoreCase("tower")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList("etage", "étage", "tp", "info"));
            }
            else if (args.length == 3 && (args[1].equalsIgnoreCase("etage") || args[1].equalsIgnoreCase("étage") || args[1].equalsIgnoreCase("tp"))) {
                // /quantum tower etage <tower_id>
                completions.addAll(getTowerIds());
            }
            else if (args.length == 4 && (args[1].equalsIgnoreCase("etage") || args[1].equalsIgnoreCase("étage") || args[1].equalsIgnoreCase("tp"))) {
                // /quantum tower etage <tower_id> <floor>
                String towerId = args[2];
                completions.addAll(getFloorsForTower(towerId));
            }
            else if (args.length == 3 && args[1].equalsIgnoreCase("info")) {
                // /quantum tower info <tower_id>
                completions.addAll(getTowerIds());
            }
        }
        // Tab completion pour /quantum door
        else if (args[0].equalsIgnoreCase("door")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList("wand", "create", "delete", "list"));
            }
            else if (args.length == 3 && (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("delete"))) {
                // /quantum door create/delete <tower_id>
                completions.addAll(getTowerIds());
            }
            else if (args.length == 4 && (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("delete"))) {
                // /quantum door create/delete <tower_id> <floor>
                String towerId = args[2];
                completions.addAll(getFloorsForTower(towerId));
            }
        }
        // Tab completion pour /quantum npc
        else if (args[0].equalsIgnoreCase("npc")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList("set", "remove", "list"));
            }
            else if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
                completions.add("goto");
            }
            else if (args.length == 4 && args[1].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("goto")) {
                // /quantum npc set goto <tower_id>
                completions.addAll(getTowerIds());
            }
            else if (args.length == 5 && args[1].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("goto")) {
                // /quantum npc set goto <tower_id> <floor>
                String towerId = args[3];
                completions.addAll(getFloorsForTower(towerId));
            }
            else if (args.length == 6 && args[1].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("goto")) {
                // /quantum npc set goto <tower_id> <floor> [model_id]
                completions.add("<model_id>");
            }
        }
        // Tab completion pour /quantum info
        else if (args[0].equalsIgnoreCase("info")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList("animation", "anim"));
            }
            else if (args.length == 3 && (args[1].equalsIgnoreCase("animation") || args[1].equalsIgnoreCase("anim"))) {
                // /quantum info animation <model_id>
                completions.add("<model_id>");
            }
            else if (args.length == 4 && (args[1].equalsIgnoreCase("animation") || args[1].equalsIgnoreCase("anim"))) {
                // /quantum info animation <model_id> <animation>
                completions.addAll(Arrays.asList(
                    "idle", "walk", "run", "attack", "death",
                    "spawn", "hurt", "jump", "sit", "sleep"
                ));
            }
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            // Sous-commandes pour /quantum reload avec TOUS les fichiers
            List<String> reloadTypes = Arrays.asList(
                "all",
                "config",
                "config.yml",
                "price",
                "price.yml",
                "prices",
                "messages",
                "messages.yml",
                "messages_gui",
                "messages_gui.yml",
                "gui",
                "menus",
                "menu",
                "zones",
                "zones.yml",
                "towers",
                "tower",
                "escrow",
                "escrow.yml",
                "orders",
                "orders_template.yml",
                "stats",
                "statistics",
                "statistics.yml",
                "storage",
                "storage.yml",
                "animations",
                "animation",
                "animations.yml",
                "healthbar",
                "healthbars",
                "mob_healthbar",
                "mob_healthbar.yml"
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
    
    /**
     * Récupère tous les IDs de tours disponibles
     */
    private List<String> getTowerIds() {
        List<String> towerIds = new ArrayList<>();
        
        try {
            Map<String, TowerConfig> towers = plugin.getTowerManager().getAllTowers();
            if (towers != null) {
                towerIds.addAll(towers.keySet());
            }
        } catch (Exception e) {
            // Ignorer les erreurs silencieusement
        }
        
        return towerIds;
    }
    
    /**
     * Récupère tous les étages disponibles pour une tour
     */
    private List<String> getFloorsForTower(String towerId) {
        List<String> floors = new ArrayList<>();
        
        try {
            TowerConfig tower = plugin.getTowerManager().getTower(towerId);
            if (tower != null) {
                int totalFloors = tower.getTotalFloors();
                for (int i = 1; i <= totalFloors; i++) {
                    floors.add(String.valueOf(i));
                }
            }
        } catch (Exception e) {
            // Ignorer les erreurs silencieusement
        }
        
        return floors;
    }
}
