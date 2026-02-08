package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Gère l'ouverture et la fermeture des portes des tours
 * Supporte : portes vanilla, commandes console, blocks redstone
 */
public class TowerDoorManager {
    
    private final Quantum plugin;
    private final Map<String, DoorConfig> doorConfigs = new HashMap<>();
    
    public TowerDoorManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Charge la configuration des portes depuis towers.yml
     */
    public void loadFromConfig(FileConfiguration config) {
        doorConfigs.clear();
        
        ConfigurationSection towersSection = config.getConfigurationSection("towers");
        if (towersSection == null) return;
        
        for (String towerId : towersSection.getKeys(false)) {
            ConfigurationSection towerSection = towersSection.getConfigurationSection(towerId);
            if (towerSection == null) continue;
            
            ConfigurationSection floorsSection = towerSection.getConfigurationSection("floors");
            if (floorsSection == null) continue;
            
            for (String floorKey : floorsSection.getKeys(false)) {
                ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
                if (floorSection == null) continue;
                
                ConfigurationSection doorSection = floorSection.getConfigurationSection("door");
                if (doorSection == null) continue;
                
                String doorId = towerId + "_" + floorKey;
                DoorConfig doorConfig = new DoorConfig(doorId, doorSection);
                doorConfigs.put(doorId, doorConfig);
                
                plugin.getQuantumLogger().info("Loaded door config: " + doorId);
            }
        }
    }
    
    /**
     * Ouvre les portes d'un étage quand tous les mobs sont tués
     */
    public void openDoors(String towerId, int floor, Player player) {
        String doorId = towerId + "_" + floor;
        DoorConfig config = doorConfigs.get(doorId);
        
        if (config == null) {
            plugin.getQuantumLogger().debug("No door config found for " + doorId);
            return;
        }
        
        // Exécuter les commandes d'ouverture
        for (String command : config.getOpenCommands()) {
            executeCommand(command, player);
        }
        
        // Ouvrir les portes vanilla
        for (DoorLocation loc : config.getDoorLocations()) {
            openDoorBlock(loc, player);
        }
        
        // Actions redstone
        for (RedstoneAction action : config.getRedstoneActions()) {
            triggerRedstone(action, player);
        }
        
        // Message de confirmation
        if (player != null && config.getOpenMessage() != null) {
            player.sendMessage(config.getOpenMessage().replace("&", "§"));
        }
        
        plugin.getQuantumLogger().info("Doors opened for " + doorId + " (player: " + 
            (player != null ? player.getName() : "null") + ")");
    }
    
    /**
     * Ferme les portes quand le joueur entre dans un nouvel étage
     */
    public void closeDoors(String towerId, int floor, Player player) {
        String doorId = towerId + "_" + floor;
        DoorConfig config = doorConfigs.get(doorId);
        
        if (config == null) return;
        
        // Exécuter les commandes de fermeture
        for (String command : config.getCloseCommands()) {
            executeCommand(command, player);
        }
        
        // Fermer les portes vanilla
        for (DoorLocation loc : config.getDoorLocations()) {
            closeDoorBlock(loc, player);
        }
        
        plugin.getQuantumLogger().info("Doors closed for " + doorId);
    }
    
    /**
     * Exécute une commande console avec remplacement de placeholders
     */
    private void executeCommand(String command, Player player) {
        if (command == null || command.isEmpty()) return;
        
        String processedCommand = command;
        if (player != null) {
            processedCommand = processedCommand
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
        }
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    }
    
    /**
     * Ouvre un block porte vanilla
     */
    private void openDoorBlock(DoorLocation loc, Player player) {
        try {
            World world = Bukkit.getWorld(loc.getWorld());
            if (world == null) return;
            
            Location location = new Location(world, loc.getX(), loc.getY(), loc.getZ());
            Block block = location.getBlock();
            
            if (block.getBlockData() instanceof Openable) {
                Openable openable = (Openable) block.getBlockData();
                openable.setOpen(true);
                block.setBlockData(openable);
                
                // Mettre à jour la porte double si présente
                updateDoubleDoor(block, true);
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to open door at " + loc + ": " + e.getMessage());
        }
    }
    
    /**
     * Ferme un block porte vanilla
     */
    private void closeDoorBlock(DoorLocation loc, Player player) {
        try {
            World world = Bukkit.getWorld(loc.getWorld());
            if (world == null) return;
            
            Location location = new Location(world, loc.getX(), loc.getY(), loc.getZ());
            Block block = location.getBlock();
            
            if (block.getBlockData() instanceof Openable) {
                Openable openable = (Openable) block.getBlockData();
                openable.setOpen(false);
                block.setBlockData(openable);
                
                // Mettre à jour la porte double si présente
                updateDoubleDoor(block, false);
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to close door at " + loc + ": " + e.getMessage());
        }
    }
    
    /**
     * Met à jour une porte double (la partie haute ou basse)
     */
    private void updateDoubleDoor(Block block, boolean open) {
        Material type = block.getType();
        
        // Vérifier si c'est une porte
        if (!type.name().contains("DOOR") || type.name().contains("TRAPDOOR")) return;
        
        // Essayer de trouver et mettre à jour l'autre moitié de la porte
        Block otherHalf = null;
        Block above = block.getRelative(0, 1, 0);
        Block below = block.getRelative(0, -1, 0);
        
        if (above.getType() == type) {
            otherHalf = above;
        } else if (below.getType() == type) {
            otherHalf = below;
        }
        
        if (otherHalf != null && otherHalf.getBlockData() instanceof Openable) {
            Openable openable = (Openable) otherHalf.getBlockData();
            openable.setOpen(open);
            otherHalf.setBlockData(openable);
        }
    }
    
    /**
     * Déclenche une action redstone (changement de block temporaire)
     */
    private void triggerRedstone(RedstoneAction action, Player player) {
        try {
            World world = Bukkit.getWorld(action.getWorld());
            if (world == null) return;
            
            Location location = new Location(world, action.getX(), action.getY(), action.getZ());
            Block block = location.getBlock();
            Material originalType = block.getType();
            
            // Placer le block activé (ex: REDSTONE_BLOCK)
            block.setType(action.getActivateMaterial());
            
            // Programmer le retour à la normale
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(originalType);
            }, action.getDurationTicks());
            
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to trigger redstone action: " + e.getMessage());
        }
    }
    
    // ==================== CLASSES INTERNES ====================
    
    private static class DoorConfig {
        private final String id;
        private final List<DoorLocation> doorLocations = new ArrayList<>();
        private final List<String> openCommands = new ArrayList<>();
        private final List<String> closeCommands = new ArrayList<>();
        private final List<RedstoneAction> redstoneActions = new ArrayList<>();
        private String openMessage;
        private String closeMessage;
        
        public DoorConfig(String id, ConfigurationSection section) {
            this.id = id;
            
            // Charger les locations de portes
            if (section.contains("locations")) {
                List<Map<?, ?>> locations = section.getMapList("locations");
                for (Map<?, ?> map : locations) {
                    String world = (String) map.get("world");
                    int x = (Integer) map.get("x");
                    int y = (Integer) map.get("y");
                    int z = (Integer) map.get("z");
                    doorLocations.add(new DoorLocation(world, x, y, z));
                }
            }
            
            // Charger les commandes
            openCommands.addAll(section.getStringList("open_commands"));
            closeCommands.addAll(section.getStringList("close_commands"));
            
            // Charger les actions redstone
            if (section.contains("redstone")) {
                List<Map<?, ?>> redstoneList = section.getMapList("redstone");
                for (Map<?, ?> map : redstoneList) {
                    String world = (String) map.get("world");
                    int x = (Integer) map.get("x");
                    int y = (Integer) map.get("y");
                    int z = (Integer) map.get("z");
                    String activate = (String) map.get("activate_material");
                    int duration = map.containsKey("duration") ? (Integer) map.get("duration") : 40;
                    
                    Material activateMaterial = Material.valueOf(activate.toUpperCase());
                    redstoneActions.add(new RedstoneAction(world, x, y, z, activateMaterial, duration));
                }
            }
            
            // Charger les messages
            openMessage = section.getString("open_message");
            closeMessage = section.getString("close_message");
        }
        
        public String getId() { return id; }
        public List<DoorLocation> getDoorLocations() { return doorLocations; }
        public List<String> getOpenCommands() { return openCommands; }
        public List<String> getCloseCommands() { return closeCommands; }
        public List<RedstoneAction> getRedstoneActions() { return redstoneActions; }
        public String getOpenMessage() { return openMessage; }
        public String getCloseMessage() { return closeMessage; }
    }
    
    private static class DoorLocation {
        private final String world;
        private final int x, y, z;
        
        public DoorLocation(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public String getWorld() { return world; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        
        @Override
        public String toString() {
            return world + "," + x + "," + y + "," + z;
        }
    }
    
    private static class RedstoneAction {
        private final String world;
        private final int x, y, z;
        private final Material activateMaterial;
        private final int durationTicks;
        
        public RedstoneAction(String world, int x, int y, int z, Material activateMaterial, int durationTicks) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.activateMaterial = activateMaterial;
            this.durationTicks = durationTicks;
        }
        
        public String getWorld() { return world; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public Material getActivateMaterial() { return activateMaterial; }
        public int getDurationTicks() { return durationTicks; }
    }
}
