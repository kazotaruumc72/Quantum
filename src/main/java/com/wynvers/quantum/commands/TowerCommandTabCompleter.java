package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.listeners.DoorSelectionListener;
import com.wynvers.quantum.towers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tab completer pour toutes les commandes du système de tours
 */
public class TowerCommandTabCompleter implements TabCompleter {
    
    private final Quantum plugin;
    private final TowerManager towerManager;
    private final TowerDoorManager doorManager;
    private final TowerNPCManager npcManager;
    
    // Animations communes pour Model Engine
    private static final List<String> COMMON_ANIMATIONS = Arrays.asList(
        "idle", "walk", "run", "attack", "death",
        "spawn", "hurt", "jump", "sit", "sleep",
        "fly", "swim", "eat", "drink", "roar",
        "cast", "defend", "victory", "taunt"
    );
    
    public TowerCommandTabCompleter(Quantum plugin, TowerManager towerManager, 
                                    TowerDoorManager doorManager, TowerNPCManager npcManager) {
        this.plugin = plugin;
        this.towerManager = towerManager;
        this.doorManager = doorManager;
        this.npcManager = npcManager;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum <subcommand>
        if (args.length == 1) {
            completions.add("tower");
            completions.add("door");
            completions.add("npc");
            completions.add("info");
            completions.add("progress");
            completions.add("reset");
            completions.add("reload");
            return filterCompletions(completions, args[0]);
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "tower":
                return handleTowerTab(sender, args);
            case "door":
                return handleDoorTab(sender, args);
            case "npc":
                return handleNPCTab(sender, args);
            case "info":
                return handleInfoTab(sender, args);
            case "progress":
                return handleProgressTab(sender, args);
            case "reset":
                return handleResetTab(sender, args);
            default:
                return completions;
        }
    }
    
    /**
     * Tab completion pour /quantum info
     */
    private List<String> handleInfoTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum info <type>
        if (args.length == 2) {
            completions.add("animation");
            completions.add("anim");
            return filterCompletions(completions, args[1]);
        }
        
        String infoType = args[1].toLowerCase();
        
        // /quantum info animation <model_id>
        if ((infoType.equals("animation") || infoType.equals("anim")) && args.length == 3) {
            // Suggérer les modèles depuis towers.yml
            completions.addAll(getModelIdsFromConfig());
            
            // Ajouter quelques exemples génériques
            if (completions.isEmpty()) {
                completions.add("slime_basic");
                completions.add("fire_knight_boss");
                completions.add("zombie_custom");
                completions.add("skeleton_warrior");
            }
            
            return filterCompletions(completions, args[2]);
        }
        
        // /quantum info animation <model_id> <animation_name>
        if ((infoType.equals("animation") || infoType.equals("anim")) && args.length == 4) {
            return filterCompletions(COMMON_ANIMATIONS, args[3]);
        }
        
        return completions;
    }
    
    /**
     * Récupère tous les model IDs depuis towers.yml
     */
    private List<String> getModelIdsFromConfig() {
        Set<String> modelIds = new HashSet<>();
        
        try {
            File towersFile = new File(plugin.getDataFolder(), "towers.yml");
            if (!towersFile.exists()) return new ArrayList<>(modelIds);
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
            ConfigurationSection towersSection = config.getConfigurationSection("towers");
            if (towersSection == null) return new ArrayList<>(modelIds);
            
            // Parcourir toutes les tours
            for (String towerId : towersSection.getKeys(false)) {
                ConfigurationSection floorsSection = towersSection.getConfigurationSection(towerId + ".floors");
                if (floorsSection == null) continue;
                
                // Parcourir tous les étages
                for (String floorKey : floorsSection.getKeys(false)) {
                    ConfigurationSection spawnersSection = floorsSection.getConfigurationSection(floorKey + ".spawners");
                    if (spawnersSection == null) continue;
                    
                    // Parcourir tous les spawners
                    for (String spawnerId : spawnersSection.getKeys(false)) {
                        String modelId = spawnersSection.getString(spawnerId + ".model");
                        if (modelId != null && !modelId.isEmpty()) {
                            modelIds.add(modelId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Error loading model IDs: " + e.getMessage());
        }
        
        return new ArrayList<>(modelIds);
    }
    
    /**
     * Tab completion pour /quantum tower
     */
    private List<String> handleTowerTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum tower <action>
        if (args.length == 2) {
            completions.add("étage");
            completions.add("etage");
            completions.add("tp");
            completions.add("info");
            return filterCompletions(completions, args[1]);
        }
        
        String action = args[1].toLowerCase();
        
        // /quantum tower etage <tower_id>
        if (action.equals("étage") || action.equals("etage") || action.equals("tp")) {
            if (args.length == 3) {
                // Liste des tower IDs
                return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[2]);
            }
            // /quantum tower etage <tower_id> <floor>
            else if (args.length == 4) {
                String towerId = args[2];
                TowerConfig tower = towerManager.getTower(towerId);
                if (tower != null) {
                    // Générer liste des étages
                    for (int i = 1; i <= tower.getTotalFloors(); i++) {
                        completions.add(String.valueOf(i));
                    }
                }
                return filterCompletions(completions, args[3]);
            }
        }
        
        // /quantum tower info <tower_id>
        if (action.equals("info") && args.length == 3) {
            return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[2]);
        }
        
        return completions;
    }
    
    /**
     * Tab completion pour /quantum door
     */
    private List<String> handleDoorTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum door <action>
        if (args.length == 2) {
            completions.add("wand");
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("info");
            return filterCompletions(completions, args[1]);
        }
        
        String action = args[1].toLowerCase();
        
        // /quantum door create <tower_id>
        if (action.equals("create")) {
            if (args.length == 3) {
                return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[2]);
            }
            // /quantum door create <tower_id> <floor>
            else if (args.length == 4) {
                String towerId = args[2];
                TowerConfig tower = towerManager.getTower(towerId);
                if (tower != null) {
                    for (int i = 1; i <= tower.getTotalFloors(); i++) {
                        completions.add(String.valueOf(i));
                    }
                }
                return filterCompletions(completions, args[3]);
            }
        }
        
        // /quantum door delete <tower_id>
        if (action.equals("delete") || action.equals("info")) {
            if (args.length == 3) {
                return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[2]);
            }
            // /quantum door delete <tower_id> <floor>
            else if (args.length == 4) {
                String towerId = args[2];
                TowerConfig tower = towerManager.getTower(towerId);
                if (tower != null) {
                    for (int i = 1; i <= tower.getTotalFloors(); i++) {
                        completions.add(String.valueOf(i));
                    }
                }
                return filterCompletions(completions, args[3]);
            }
        }
        
        return completions;
    }
    
    /**
     * Tab completion pour /quantum npc
     */
    private List<String> handleNPCTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum npc <action>
        if (args.length == 2) {
            completions.add("set");
            completions.add("remove");
            completions.add("list");
            completions.add("info");
            return filterCompletions(completions, args[1]);
        }
        
        String action = args[1].toLowerCase();
        
        // /quantum npc set <type>
        if (action.equals("set") && args.length == 3) {
            completions.add("goto");
            return filterCompletions(completions, args[2]);
        }
        
        // /quantum npc set goto <tower_id>
        if (action.equals("set") && args.length == 4 && args[2].equals("goto")) {
            return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[3]);
        }
        
        // /quantum npc set goto <tower_id> <floor>
        if (action.equals("set") && args.length == 5 && args[2].equals("goto")) {
            String towerId = args[3];
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower != null) {
                for (int i = 1; i <= tower.getTotalFloors(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
            return filterCompletions(completions, args[4]);
        }
        
        // /quantum npc set goto <tower_id> <floor> [model_id]
        if (action.equals("set") && args.length == 6 && args[2].equals("goto")) {
            // Suggérer les modèles depuis towers.yml
            completions.addAll(getModelIdsFromConfig());
            
            // Ajouter quelques exemples de NPC
            completions.add("guardian_npc");
            completions.add("tower_keeper");
            completions.add("portal_npc");
            
            return filterCompletions(completions, args[5]);
        }
        
        // /quantum npc remove <uuid>
        if (action.equals("remove") && args.length == 3) {
            // Liste des UUID des NPC
            for (UUID uuid : npcManager.getAllNPCs().keySet()) {
                completions.add(uuid.toString());
            }
            return filterCompletions(completions, args[2]);
        }
        
        return completions;
    }
    
    /**
     * Tab completion pour /quantum progress
     */
    private List<String> handleProgressTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum progress [player]
        if (args.length == 2) {
            return filterCompletions(getOnlinePlayerNames(), args[1]);
        }
        
        return completions;
    }
    
    /**
     * Tab completion pour /quantum reset
     */
    private List<String> handleResetTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /quantum reset <player>
        if (args.length == 2) {
            return filterCompletions(getOnlinePlayerNames(), args[1]);
        }
        
        // /quantum reset <player> [tower_id]
        if (args.length == 3) {
            return filterCompletions(new ArrayList<>(towerManager.getTowerIds()), args[2]);
        }
        
        return completions;
    }
    
    /**
     * Filtre les complétions selon l'input actuel
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        String lowerInput = input.toLowerCase();
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(lowerInput))
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Obtient la liste des noms des joueurs en ligne
     */
    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }
}
