package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gère les portes des tours avec système de sélection WorldEdit-style
 * Les portes disparaissent pendant 30s puis réapparaissent
 */
public class TowerDoorManager {
    
    private final Quantum plugin;
    private final File doorsFile;
    private FileConfiguration doorsConfig;
    
    // Selection en cours pour chaque joueur
    private final Map<UUID, Location> pos1Selection = new HashMap<>();
    private final Map<UUID, Location> pos2Selection = new HashMap<>();
    
    // Configuration des portes sauvegardées
    private final Map<String, DoorConfig> doorConfigs = new HashMap<>();
    
    // Portes actuellement ouvertes (dépop)
    private final Map<String, Set<BlockSnapshot>> openedDoors = new HashMap<>();
    private final Map<String, BukkitTask> doorCloseTasks = new HashMap<>();
    
    public TowerDoorManager(Quantum plugin) {
        this.plugin = plugin;
        this.doorsFile = new File(plugin.getDataFolder(), "doors.yml");
        loadDoors();
    }
    
    /**
     * Charge les portes depuis doors.yml
     */
    public void loadDoors() {
        if (!doorsFile.exists()) {
            try {
                doorsFile.createNewFile();
                plugin.getQuantumLogger().info("Created doors.yml");
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create doors.yml: " + e.getMessage());
                return;
            }
        }
        
        doorsConfig = YamlConfiguration.loadConfiguration(doorsFile);
        doorConfigs.clear();
        
        ConfigurationSection doorsSection = doorsConfig.getConfigurationSection("doors");
        if (doorsSection == null) return;
        
        for (String doorId : doorsSection.getKeys(false)) {
            ConfigurationSection doorSection = doorsSection.getConfigurationSection(doorId);
            if (doorSection == null) continue;
            
            DoorConfig config = DoorConfig.fromConfig(doorSection);
            doorConfigs.put(doorId, config);
        }
        
        plugin.getQuantumLogger().success("Loaded " + doorConfigs.size() + " door configs");
    }
    
    /**
     * Sauvegarde les portes dans doors.yml
     */
    public void saveDoors() {
        try {
            doorsConfig.save(doorsFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save doors.yml: " + e.getMessage());
        }
    }
    
    /**
     * Définit le premier point de sélection
     */
    public void setPos1(Player player, Location loc) {
        pos1Selection.put(player.getUniqueId(), loc.clone());
        player.sendMessage("§e§l[Doors] §7Position 1 définie: §f" + 
            loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        
        // Effet visuel
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 0.5, 0.5), 10);
    }
    
    /**
     * Définit le second point de sélection
     */
    public void setPos2(Player player, Location loc) {
        pos2Selection.put(player.getUniqueId(), loc.clone());
        player.sendMessage("§e§l[Doors] §7Position 2 définie: §f" + 
            loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        
        // Effet visuel
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 0.5, 0.5), 10);
    }
    
    /**
     * Enregistre une nouvelle porte avec les positions sélectionnées
     */
    public boolean createDoor(Player player, String towerId, int floor) {
        UUID uuid = player.getUniqueId();
        
        if (!pos1Selection.containsKey(uuid) || !pos2Selection.containsKey(uuid)) {
            player.sendMessage("§c§l[Doors] §cVous devez d'abord sélectionner deux positions!");
            return false;
        }
        
        Location pos1 = pos1Selection.get(uuid);
        Location pos2 = pos2Selection.get(uuid);
        
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c§l[Doors] §cLes deux positions doivent être dans le même monde!");
            return false;
        }
        
        String doorId = towerId + "_" + floor;
        DoorConfig config = new DoorConfig(doorId, towerId, floor, pos1, pos2);
        doorConfigs.put(doorId, config);
        
        // Sauvegarder dans doors.yml
        config.saveToConfig(doorsConfig, "doors." + doorId);
        saveDoors();
        
        // Nettoyer la sélection
        pos1Selection.remove(uuid);
        pos2Selection.remove(uuid);
        
        player.sendMessage("§a§l[Doors] §aPorte créée: §f" + doorId);
        player.sendMessage("§7Taille: §f" + config.getBlockCount() + " blocks");
        return true;
    }
    
    /**
     * Ouvre une porte (fait disparaître les blocks pendant 30s)
     */
    public void openDoor(String towerId, int floor, Player player) {
        String doorId = towerId + "_" + floor;
        DoorConfig config = doorConfigs.get(doorId);
        
        if (config == null) {
            plugin.getQuantumLogger().debug("No door config for " + doorId);
            return;
        }
        
        // Annuler la fermeture précédente si elle existe
        BukkitTask existingTask = doorCloseTasks.remove(doorId);
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        // Sauvegarder et retirer les blocks
        Set<BlockSnapshot> snapshots = new HashSet<>();
        World world = config.getPos1().getWorld();
        
        for (int x = config.getMinX(); x <= config.getMaxX(); x++) {
            for (int y = config.getMinY(); y <= config.getMaxY(); y++) {
                for (int z = config.getMinZ(); z <= config.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    // Ignorer l'air
                    if (block.getType() == Material.AIR) continue;
                    
                    BlockSnapshot snapshot = new BlockSnapshot(block);
                    snapshots.add(snapshot);
                    
                    // Retirer le block (remplacer par air)
                    block.setType(Material.AIR, false);
                }
            }
        }
        
        openedDoors.put(doorId, snapshots);
        
        // Effets
        if (player != null) {
            player.sendMessage("§a§l✓ §aLa porte s'ouvre!");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 0.8f);
        }
        
        // Programmer la fermeture dans 30 secondes
        BukkitTask closeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            closeDoor(doorId, true);
        }, 30 * 20L); // 30 secondes
        
        doorCloseTasks.put(doorId, closeTask);
        
        plugin.getQuantumLogger().info("Door opened: " + doorId + " (" + snapshots.size() + " blocks)");
    }
    
    /**
     * Ferme une porte (remet les blocks)
     */
    private void closeDoor(String doorId, boolean timeout) {
        Set<BlockSnapshot> snapshots = openedDoors.remove(doorId);
        if (snapshots == null) return;
        
        // Remettre tous les blocks
        for (BlockSnapshot snapshot : snapshots) {
            snapshot.restore();
        }
        
        // Annuler la tâche de fermeture
        BukkitTask task = doorCloseTasks.remove(doorId);
        if (task != null) {
            task.cancel();
        }
        
        if (timeout) {
            plugin.getQuantumLogger().info("Door closed (timeout): " + doorId);
        } else {
            plugin.getQuantumLogger().info("Door closed: " + doorId);
        }
    }
    
    /**
     * Vérifie si une porte est ouverte
     */
    public boolean isDoorOpen(String towerId, int floor) {
        String doorId = towerId + "_" + floor;
        return openedDoors.containsKey(doorId);
    }
    
    /**
     * Obtient la configuration d'une porte
     */
    public DoorConfig getDoorConfig(String towerId, int floor) {
        String doorId = towerId + "_" + floor;
        return doorConfigs.get(doorId);
    }
    
    /**
     * Supprime une porte
     */
    public boolean deleteDoor(String towerId, int floor) {
        String doorId = towerId + "_" + floor;
        
        if (!doorConfigs.containsKey(doorId)) {
            return false;
        }
        
        // Fermer la porte si elle est ouverte
        if (openedDoors.containsKey(doorId)) {
            closeDoor(doorId, false);
        }
        
        doorConfigs.remove(doorId);
        doorsConfig.set("doors." + doorId, null);
        saveDoors();
        
        return true;
    }
    
    /**
     * Liste toutes les portes
     */
    public Set<String> getAllDoorIds() {
        return new HashSet<>(doorConfigs.keySet());
    }
    
    // ==================== CLASSES INTERNES ====================
    
    public static class DoorConfig {
        private final String id;
        private final String towerId;
        private final int floor;
        private final Location pos1;
        private final Location pos2;
        
        public DoorConfig(String id, String towerId, int floor, Location pos1, Location pos2) {
            this.id = id;
            this.towerId = towerId;
            this.floor = floor;
            this.pos1 = pos1.clone();
            this.pos2 = pos2.clone();
        }
        
        public int getMinX() { return Math.min(pos1.getBlockX(), pos2.getBlockX()); }
        public int getMaxX() { return Math.max(pos1.getBlockX(), pos2.getBlockX()); }
        public int getMinY() { return Math.min(pos1.getBlockY(), pos2.getBlockY()); }
        public int getMaxY() { return Math.max(pos1.getBlockY(), pos2.getBlockY()); }
        public int getMinZ() { return Math.min(pos1.getBlockZ(), pos2.getBlockZ()); }
        public int getMaxZ() { return Math.max(pos1.getBlockZ(), pos2.getBlockZ()); }
        
        public int getBlockCount() {
            return (getMaxX() - getMinX() + 1) * 
                   (getMaxY() - getMinY() + 1) * 
                   (getMaxZ() - getMinZ() + 1);
        }
        
        public String getId() { return id; }
        public String getTowerId() { return towerId; }
        public int getFloor() { return floor; }
        public Location getPos1() { return pos1.clone(); }
        public Location getPos2() { return pos2.clone(); }
        
        public void saveToConfig(FileConfiguration config, String path) {
            config.set(path + ".tower_id", towerId);
            config.set(path + ".floor", floor);
            config.set(path + ".world", pos1.getWorld().getName());
            config.set(path + ".pos1.x", pos1.getBlockX());
            config.set(path + ".pos1.y", pos1.getBlockY());
            config.set(path + ".pos1.z", pos1.getBlockZ());
            config.set(path + ".pos2.x", pos2.getBlockX());
            config.set(path + ".pos2.y", pos2.getBlockY());
            config.set(path + ".pos2.z", pos2.getBlockZ());
        }
        
        public static DoorConfig fromConfig(ConfigurationSection section) {
            String id = section.getName();
            String towerId = section.getString("tower_id");
            int floor = section.getInt("floor");
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            
            if (world == null) return null;
            
            Location pos1 = new Location(world,
                section.getInt("pos1.x"),
                section.getInt("pos1.y"),
                section.getInt("pos1.z"));
            
            Location pos2 = new Location(world,
                section.getInt("pos2.x"),
                section.getInt("pos2.y"),
                section.getInt("pos2.z"));
            
            return new DoorConfig(id, towerId, floor, pos1, pos2);
        }
    }
    
    private static class BlockSnapshot {
        private final Location location;
        private final Material type;
        private final byte data;
        
        public BlockSnapshot(Block block) {
            this.location = block.getLocation();
            this.type = block.getType();
            this.data = block.getData();
        }
        
        public void restore() {
            Block block = location.getBlock();
            block.setType(type, false);
        }
    }
}
