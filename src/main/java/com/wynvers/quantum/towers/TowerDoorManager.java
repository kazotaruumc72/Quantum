package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * Gère les portes des tours avec système de sélection WorldEdit-style
 * Les portes disparaissent pendant 1m30s puis réapparaissent
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

    // Permissions temporaires pour les joueurs qui ont ouvert les portes
    private final Map<String, UUID> doorOpeners = new HashMap<>(); // doorId -> playerId

    // LuckPerms integration
    private LuckPerms luckPerms;
    private boolean luckPermsAvailable;
    
    public TowerDoorManager(Quantum plugin) {
        this.plugin = plugin;
        this.doorsFile = new File(plugin.getDataFolder(), "doors.yml");
        initLuckPerms();
        loadDoors();
    }

    /**
     * Initialise LuckPerms si disponible
     */
    private void initLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.luckPermsAvailable = true;
                plugin.getQuantumLogger().success("✓ LuckPerms integration enabled for door permissions");
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("⚠ LuckPerms found but failed to initialize for doors");
                this.luckPermsAvailable = false;
            }
        } else {
            plugin.getQuantumLogger().warning("⚠ LuckPerms not found - door permissions disabled");
            this.luckPermsAvailable = false;
        }
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
            if (config == null) {
                plugin.getQuantumLogger().warning("Failed to load door config: " + doorId + " (world not loaded?)");
                continue;
            }
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
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10);
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
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10);
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
     * Ouvre une porte (fait disparaître les blocks pendant 1m30s)
     */
    public void openDoor(String towerId, int floor, Player player) {
        String doorId = towerId + "_" + floor;
        DoorConfig config = doorConfigs.get(doorId);

        if (config == null) {
            plugin.getQuantumLogger().warning("No door config for " + doorId + " - use /tower door create to set it up");
            if (player != null) {
                player.sendMessage("§c§l[Tour] §cTous les monstres sont tués mais la porte n'est pas configurée. Contactez un administrateur.");
            }
            return;
        }

        // Annuler la fermeture précédente si elle existe
        BukkitTask existingTask = doorCloseTasks.remove(doorId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Révoquer l'ancienne permission si la porte était déjà ouverte
        UUID previousOpener = doorOpeners.get(doorId);
        if (previousOpener != null && luckPermsAvailable) {
            // Révoquer pour l'étage suivant (floor+1)
            revokeDoorPermission(previousOpener, towerId, floor + 1);
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

        // Enregistrer le joueur qui a ouvert la porte
        if (player != null) {
            doorOpeners.put(doorId, player.getUniqueId());

            // Accorder la permission temporaire pour accéder à l'étage suivant
            // Le joueur a complété l'étage actuel (floor) et peut maintenant accéder à l'étage suivant (floor+1)
            grantDoorPermission(player, towerId, floor + 1);
        }

        // Effets
        if (player != null) {
            player.sendTitle("§a§lPorte ouverte!", "§7Vous avez §e90 secondes §7pour passer.", 10, 60, 20);
            player.sendMessage("§a§l✓ §aLa porte s'ouvre!");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 0.8f);
        }

        // Programmer la fermeture dans 1m30s (90 secondes)
        BukkitTask closeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            closeDoor(doorId, true);
        }, 90 * 20L); // 1m30s

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

        // Révoquer la permission du joueur qui avait ouvert la porte
        UUID opener = doorOpeners.remove(doorId);
        if (opener != null && luckPermsAvailable) {
            // Extraire towerId et floor depuis doorId
            String[] parts = doorId.split("_");
            if (parts.length >= 2) {
                try {
                    int lastUnderscore = doorId.lastIndexOf('_');
                    String towerId = doorId.substring(0, lastUnderscore);
                    int floor = Integer.parseInt(doorId.substring(lastUnderscore + 1));
                    // Révoquer la permission pour l'étage suivant (floor+1)
                    revokeDoorPermission(opener, towerId, floor + 1);
                } catch (Exception e) {
                    plugin.getQuantumLogger().warning("Failed to parse doorId for permission revocation: " + doorId);
                }
            }
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

    /**
     * Accorde la permission temporaire au joueur pour accéder à l'étage suivant
     * Permission format: quantum.tower.door.<towerId>.<floor>
     * Durée: 90 secondes (1m30s)
     */
    private void grantDoorPermission(Player player, String towerId, int floor) {
        if (!luckPermsAvailable) {
            plugin.getQuantumLogger().debug("Cannot grant door permission - LuckPerms not available");
            return;
        }

        String permission = "quantum.tower.door." + towerId + "." + floor;

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                plugin.getQuantumLogger().warning("Cannot grant permission - user not found: " + player.getName());
                return;
            }

            // Créer un nœud de permission temporaire de 90 secondes
            Node node = Node.builder(permission)
                    .expiry(Duration.ofSeconds(90))
                    .build();

            // Ajouter la permission
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);

            plugin.getQuantumLogger().info("Granted temporary door permission to " + player.getName() +
                    ": " + permission + " (90s)");
            player.sendMessage("§e⏱ §7Vous avez §e90 secondes §7pour passer dans la salle suivante!");
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to grant door permission: " + e.getMessage());
        }
    }

    /**
     * Révoque la permission d'accès à la porte pour un joueur
     */
    private void revokeDoorPermission(UUID playerId, String towerId, int floor) {
        if (!luckPermsAvailable) {
            return;
        }

        String permission = "quantum.tower.door." + towerId + "." + floor;

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                plugin.getQuantumLogger().debug("Cannot revoke permission - user not loaded: " + playerId);
                return;
            }

            // Supprimer toutes les instances de cette permission
            user.data().clear(node -> node.getKey().equals(permission));
            luckPerms.getUserManager().saveUser(user);

            plugin.getQuantumLogger().info("Revoked door permission for player: " + permission);
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to revoke door permission: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un joueur a la permission temporaire d'accéder à un étage spécifique
     * Cette méthode est utilisée par ZoneManager pour contrôler l'accès
     */
    public boolean hasTemporaryAccess(UUID playerId, String towerId, int floor) {
        if (!luckPermsAvailable) {
            // Fallback sans LuckPerms : vérifier si ce joueur a ouvert la porte de l'étage précédent
            String doorId = towerId + "_" + (floor - 1);
            return playerId.equals(doorOpeners.get(doorId));
        }

        String permission = "quantum.tower.door." + towerId + "." + floor;

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                return false;
            }

            // Vérifier si l'utilisateur a la permission (temporaire ou permanente)
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to check door permission: " + e.getMessage());
            return false;
        }
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
        private final org.bukkit.block.data.BlockData blockData;
        
        public BlockSnapshot(Block block) {
            this.location = block.getLocation();
            this.blockData = block.getBlockData().clone();
        }
        
        public void restore() {
            Block block = location.getBlock();
            block.setBlockData(blockData, false);
        }
    }
}
