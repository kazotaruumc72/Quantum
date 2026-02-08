package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Gère les drops d'items (Nexo, MythicMobs, vanilla) et les commandes
 * par mob tué et par complétion d'étage
 */
public class TowerLootManager {
    
    private final Quantum plugin;
    private final Map<String, MobLootConfig> mobLootConfigs = new HashMap<>();
    private final Map<String, FloorRewardConfig> floorRewardConfigs = new HashMap<>();
    
    // APIs externes
    private boolean nexoEnabled = false;
    private boolean mythicMobsEnabled = false;
    
    public TowerLootManager(Quantum plugin) {
        this.plugin = plugin;
        checkExternalPlugins();
    }
    
    /**
     * Vérifie quels plugins externes sont disponibles
     */
    private void checkExternalPlugins() {
        nexoEnabled = Bukkit.getPluginManager().getPlugin("Nexo") != null;
        mythicMobsEnabled = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
        
        plugin.getQuantumLogger().info("TowerLootManager - Nexo: " + nexoEnabled + ", MythicMobs: " + mythicMobsEnabled);
    }
    
    /**
     * Charge la configuration des loots depuis towers.yml
     */
    public void loadFromConfig(FileConfiguration config) {
        mobLootConfigs.clear();
        floorRewardConfigs.clear();
        
        ConfigurationSection towersSection = config.getConfigurationSection("towers");
        if (towersSection == null) return;
        
        for (String towerId : towersSection.getKeys(false)) {
            ConfigurationSection towerSection = towersSection.getConfigurationSection(towerId);
            if (towerSection == null) continue;
            
            // Charger les loots par mob
            ConfigurationSection lootsSection = towerSection.getConfigurationSection("mob_loots");
            if (lootsSection != null) {
                for (String mobId : lootsSection.getKeys(false)) {
                    ConfigurationSection mobSection = lootsSection.getConfigurationSection(mobId);
                    if (mobSection == null) continue;
                    
                    String fullId = towerId + ":" + mobId;
                    MobLootConfig lootConfig = new MobLootConfig(mobSection);
                    mobLootConfigs.put(fullId, lootConfig);
                }
            }
            
            // Charger les récompenses d'étage
            ConfigurationSection floorsSection = towerSection.getConfigurationSection("floors");
            if (floorsSection != null) {
                for (String floorKey : floorsSection.getKeys(false)) {
                    ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
                    if (floorSection == null) continue;
                    
                    ConfigurationSection rewardsSection = floorSection.getConfigurationSection("rewards");
                    if (rewardsSection != null) {
                        String rewardId = towerId + "_" + floorKey;
                        FloorRewardConfig rewardConfig = new FloorRewardConfig(rewardsSection);
                        floorRewardConfigs.put(rewardId, rewardConfig);
                    }
                }
            }
        }
        
        plugin.getQuantumLogger().info("Loaded " + mobLootConfigs.size() + " mob loot configs and " + 
            floorRewardConfigs.size() + " floor reward configs");
    }
    
    /**
     * Donne les loots quand un mob est tué
     */
    public void onMobKilled(String towerId, String mobId, Player player, Location deathLocation) {
        String fullId = towerId + ":" + mobId;
        MobLootConfig config = mobLootConfigs.get(fullId);
        
        if (config == null) {
            // Essayer sans le towerId (loot global)
            config = mobLootConfigs.get(mobId);
        }
        
        if (config == null) return;
        
        // Donner les items Nexo
        for (NexoItemDrop drop : config.getNexoDrops()) {
            giveNexoItem(player, drop, deathLocation);
        }
        
        // Donner les items MythicMobs
        for (MythicItemDrop drop : config.getMythicDrops()) {
            giveMythicItem(player, drop, deathLocation);
        }
        
        // Exécuter les commandes
        for (String command : config.getCommands()) {
            executeCommand(command, player, deathLocation);
        }
    }
    
    /**
     * Donne les récompenses quand un étage est complété
     */
    public void onFloorCompleted(String towerId, int floor, Player player, Location location) {
        String rewardId = towerId + "_" + floor;
        FloorRewardConfig config = floorRewardConfigs.get(rewardId);
        
        if (config == null) return;
        
        // Donner les items Nexo
        for (NexoItemDrop drop : config.getNexoRewards()) {
            giveNexoItem(player, drop, location);
        }
        
        // Donner les items MythicMobs
        for (MythicItemDrop drop : config.getMythicRewards()) {
            giveMythicItem(player, drop, location);
        }
        
        // Exécuter les commandes console
        for (String command : config.getCommands()) {
            executeCommand(command, player, location);
        }
        
        // Message de récompense
        if (config.getMessage() != null && player != null) {
            player.sendMessage(config.getMessage().replace("&", "§"));
        }
        
        plugin.getQuantumLogger().info("Floor rewards given for " + rewardId + " to " + player.getName());
    }
    
    /**
     * Donne un item Nexo au joueur
     */
    private void giveNexoItem(Player player, NexoItemDrop drop, Location location) {
        if (!nexoEnabled) {
            plugin.getQuantumLogger().warning("Nexo not found, cannot give item: " + drop.getId());
            return;
        }
        
        try {
            // Utiliser la commande Nexo pour donner l'item
            String command = "nexo give " + player.getName() + " " + drop.getId() + " " + drop.getAmount();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (drop.getMessage() != null) {
                player.sendMessage(drop.getMessage().replace("&", "§")
                    .replace("%item%", drop.getId())
                    .replace("%amount%", String.valueOf(drop.getAmount())));
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to give Nexo item " + drop.getId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Donne un item MythicMobs au joueur
     */
    private void giveMythicItem(Player player, MythicItemDrop drop, Location location) {
        if (!mythicMobsEnabled) {
            plugin.getQuantumLogger().warning("MythicMobs not found, cannot give item: " + drop.getId());
            return;
        }
        
        try {
            // Utiliser la commande MythicMobs pour donner l'item
            String command = "mm items give " + player.getName() + " " + drop.getId() + " " + drop.getAmount();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (drop.getMessage() != null) {
                player.sendMessage(drop.getMessage().replace("&", "§")
                    .replace("%item%", drop.getId())
                    .replace("%amount%", String.valueOf(drop.getAmount())));
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to give Mythic item " + drop.getId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Exécute une commande avec remplacement de placeholders
     */
    private void executeCommand(String command, Player player, Location location) {
        if (command == null || command.isEmpty()) return;
        
        String processedCommand = command;
        if (player != null) {
            processedCommand = processedCommand
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%x%", String.valueOf(location != null ? location.getBlockX() : player.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(location != null ? location.getBlockY() : player.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(location != null ? location.getBlockZ() : player.getLocation().getBlockZ()));
        }
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    }
    
    // ==================== CLASSES INTERNES ====================
    
    private static class MobLootConfig {
        private final List<NexoItemDrop> nexoDrops = new ArrayList<>();
        private final List<MythicItemDrop> mythicDrops = new ArrayList<>();
        private final List<String> commands = new ArrayList<>();
        
        public MobLootConfig(ConfigurationSection section) {
            // Charger les drops Nexo
            if (section.contains("nexo")) {
                List<Map<?, ?>> nexoList = section.getMapList("nexo");
                for (Map<?, ?> map : nexoList) {
                    String id = (String) map.get("id");
                    int amount = map.containsKey("amount") ? (Integer) map.get("amount") : 1;
                    double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 100.0;
                    String message = (String) map.get("message");
                    nexoDrops.add(new NexoItemDrop(id, amount, chance, message));
                }
            }
            
            // Charger les drops MythicMobs
            if (section.contains("mythic")) {
                List<Map<?, ?>> mythicList = section.getMapList("mythic");
                for (Map<?, ?> map : mythicList) {
                    String id = (String) map.get("id");
                    int amount = map.containsKey("amount") ? (Integer) map.get("amount") : 1;
                    double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 100.0;
                    String message = (String) map.get("message");
                    mythicDrops.add(new MythicItemDrop(id, amount, chance, message));
                }
            }
            
            // Charger les commandes
            commands.addAll(section.getStringList("commands"));
        }
        
        public List<NexoItemDrop> getNexoDrops() { return nexoDrops; }
        public List<MythicItemDrop> getMythicDrops() { return mythicDrops; }
        public List<String> getCommands() { return commands; }
    }
    
    private static class FloorRewardConfig {
        private final List<NexoItemDrop> nexoRewards = new ArrayList<>();
        private final List<MythicItemDrop> mythicRewards = new ArrayList<>();
        private final List<String> commands = new ArrayList<>();
        private String message;
        
        public FloorRewardConfig(ConfigurationSection section) {
            // Charger les récompenses Nexo
            if (section.contains("nexo")) {
                List<Map<?, ?>> nexoList = section.getMapList("nexo");
                for (Map<?, ?> map : nexoList) {
                    String id = (String) map.get("id");
                    int amount = map.containsKey("amount") ? (Integer) map.get("amount") : 1;
                    double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 100.0;
                    String msg = (String) map.get("message");
                    nexoRewards.add(new NexoItemDrop(id, amount, chance, msg));
                }
            }
            
            // Charger les récompenses MythicMobs
            if (section.contains("mythic")) {
                List<Map<?, ?>> mythicList = section.getMapList("mythic");
                for (Map<?, ?> map : mythicList) {
                    String id = (String) map.get("id");
                    int amount = map.containsKey("amount") ? (Integer) map.get("amount") : 1;
                    double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 100.0;
                    String msg = (String) map.get("message");
                    mythicRewards.add(new MythicItemDrop(id, amount, chance, msg));
                }
            }
            
            // Charger les commandes
            commands.addAll(section.getStringList("commands"));
            
            // Charger le message
            message = section.getString("message");
        }
        
        public List<NexoItemDrop> getNexoRewards() { return nexoRewards; }
        public List<MythicItemDrop> getMythicRewards() { return mythicRewards; }
        public List<String> getCommands() { return commands; }
        public String getMessage() { return message; }
    }
    
    private static class NexoItemDrop {
        private final String id;
        private final int amount;
        private final double chance;
        private final String message;
        
        public NexoItemDrop(String id, int amount, double chance, String message) {
            this.id = id;
            this.amount = amount;
            this.chance = chance;
            this.message = message;
        }
        
        public String getId() { return id; }
        public int getAmount() { return amount; }
        public double getChance() { return chance; }
        public String getMessage() { return message; }
    }
    
    private static class MythicItemDrop {
        private final String id;
        private final int amount;
        private final double chance;
        private final String message;
        
        public MythicItemDrop(String id, int amount, double chance, String message) {
            this.id = id;
            this.amount = amount;
            this.chance = chance;
            this.message = message;
        }
        
        public String getId() { return id; }
        public int getAmount() { return amount; }
        public double getChance() { return chance; }
        public String getMessage() { return message; }
    }
}
