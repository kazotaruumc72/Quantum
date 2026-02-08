package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gère les NPC de téléportation des tours avec Model Engine
 * Les NPC permettent d'accéder à des étages spécifiques
 */
public class TowerNPCManager implements Listener {
    
    private final Quantum plugin;
    private final File npcsFile;
    private FileConfiguration npcsConfig;
    
    // NPC enregistrés : UUID -> Config
    private final Map<UUID, NPCConfig> npcConfigs = new HashMap<>();
    
    // Clés pour le PersistentDataContainer
    private final NamespacedKey towerIdKey;
    private final NamespacedKey floorKey;
    private final NamespacedKey npcTypeKey;
    
    // Model Engine détecté
    private boolean modelEngineEnabled = false;
    
    public TowerNPCManager(Quantum plugin) {
        this.plugin = plugin;
        this.npcsFile = new File(plugin.getDataFolder(), "tower_npcs.yml");
        
        this.towerIdKey = new NamespacedKey(plugin, "tower_id");
        this.floorKey = new NamespacedKey(plugin, "floor");
        this.npcTypeKey = new NamespacedKey(plugin, "npc_type");
        
        checkModelEngine();
        loadNPCs();
        
        // Enregistrer le listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Vérifie si Model Engine est installé
     */
    private void checkModelEngine() {
        modelEngineEnabled = Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
        if (modelEngineEnabled) {
            plugin.getQuantumLogger().success("Model Engine detected - NPC models enabled");
        } else {
            plugin.getQuantumLogger().warning("Model Engine not found - NPCs will use vanilla entities");
        }
    }
    
    /**
     * Charge les NPC depuis tower_npcs.yml
     */
    public void loadNPCs() {
        if (!npcsFile.exists()) {
            try {
                npcsFile.createNewFile();
                plugin.getQuantumLogger().info("Created tower_npcs.yml");
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create tower_npcs.yml: " + e.getMessage());
                return;
            }
        }
        
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
        npcConfigs.clear();
        
        ConfigurationSection npcsSection = npcsConfig.getConfigurationSection("npcs");
        if (npcsSection == null) return;
        
        for (String uuidStr : npcsSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection npcSection = npcsSection.getConfigurationSection(uuidStr);
                
                NPCConfig config = NPCConfig.fromConfig(npcSection);
                if (config != null) {
                    npcConfigs.put(uuid, config);
                }
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid NPC UUID: " + uuidStr);
            }
        }
        
        plugin.getQuantumLogger().success("Loaded " + npcConfigs.size() + " tower NPCs");
    }
    
    /**
     * Sauvegarde les NPC dans tower_npcs.yml
     */
    public void saveNPCs() {
        try {
            npcsConfig.save(npcsFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save tower_npcs.yml: " + e.getMessage());
        }
    }
    
    /**
     * Crée un NPC à la position du joueur
     */
    public boolean createNPC(Player player, String towerId, int floor, String modelId) {
        Location loc = player.getLocation();
        
        // Spawn l'entité (Armor Stand pour Model Engine, Villager sinon)
        Entity entity;
        if (modelEngineEnabled && modelId != null) {
            entity = spawnModelEngineNPC(loc, modelId);
        } else {
            entity = spawnVanillaNPC(loc);
        }
        
        if (entity == null) {
            player.sendMessage("§c§l[NPC] §cErreur lors de la création du NPC!");
            return false;
        }
        
        // Configurer les metadatas
        entity.getPersistentDataContainer().set(towerIdKey, PersistentDataType.STRING, towerId);
        entity.getPersistentDataContainer().set(floorKey, PersistentDataType.INTEGER, floor);
        entity.getPersistentDataContainer().set(npcTypeKey, PersistentDataType.STRING, "tower_goto");
        
        // Empêcher le despawn
        entity.setCustomNameVisible(true);
        entity.setCustomName("§e§lTour: §f" + towerId + " §7Étage " + floor);
        entity.setPersistent(true);
        entity.setInvulnerable(true);
        
        // Sauvegarder la config
        NPCConfig config = new NPCConfig(entity.getUniqueId(), towerId, floor, loc, modelId);
        npcConfigs.put(entity.getUniqueId(), config);
        
        config.saveToConfig(npcsConfig, "npcs." + entity.getUniqueId().toString());
        saveNPCs();
        
        player.sendMessage("§a§l[NPC] §aNPC créé: §f" + towerId + " §7étage §f" + floor);
        player.sendMessage("§7UUID: §f" + entity.getUniqueId());
        
        return true;
    }
    
    /**
     * Spawn un NPC avec Model Engine
     */
    private Entity spawnModelEngineNPC(Location loc, String modelId) {
        try {
            // Utiliser la commande Model Engine
            String command = "meg spawn " + modelId + " " + 
                loc.getWorld().getName() + " " +
                loc.getX() + " " + loc.getY() + " " + loc.getZ();
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            // Récupérer l'entité spawnée (closest entity)
            return loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream()
                .filter(e -> e.getLocation().distanceSquared(loc) < 4)
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to spawn Model Engine NPC: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Spawn un NPC vanilla (Villager)
     */
    private Entity spawnVanillaNPC(Location loc) {
        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setSilent(true);
        return villager;
    }
    
    /**
     * Supprime un NPC par son UUID
     */
    public boolean deleteNPC(UUID npcUUID) {
        NPCConfig config = npcConfigs.remove(npcUUID);
        if (config == null) {
            return false;
        }
        
        // Supprimer l'entité du monde
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(npcUUID)) {
                    entity.remove();
                    break;
                }
            }
        }
        
        // Supprimer de la config
        npcsConfig.set("npcs." + npcUUID.toString(), null);
        saveNPCs();
        
        return true;
    }
    
    /**
     * Liste tous les NPC
     */
    public Map<UUID, NPCConfig> getAllNPCs() {
        return new HashMap<>(npcConfigs);
    }
    
    /**
     * Listener - Clic sur NPC
     */
    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        
        // Vérifier si c'est un NPC de tour
        if (!entity.getPersistentDataContainer().has(npcTypeKey, PersistentDataType.STRING)) {
            return;
        }
        
        String npcType = entity.getPersistentDataContainer().get(npcTypeKey, PersistentDataType.STRING);
        if (!"tower_goto".equals(npcType)) {
            return;
        }
        
        // Récupérer les infos
        String towerId = entity.getPersistentDataContainer().get(towerIdKey, PersistentDataType.STRING);
        Integer floor = entity.getPersistentDataContainer().get(floorKey, PersistentDataType.INTEGER);
        
        if (towerId == null || floor == null) {
            player.sendMessage("§c§l[NPC] §cCe NPC est mal configuré!");
            return;
        }
        
        // Annuler l'interaction normale
        event.setCancelled(true);
        
        // Exécuter la commande de téléportation
        player.performCommand("quantum tower etage " + towerId + " " + floor);
    }
    
    // ==================== CLASSES INTERNES ====================
    
    public static class NPCConfig {
        private final UUID uuid;
        private final String towerId;
        private final int floor;
        private final Location location;
        private final String modelId;
        
        public NPCConfig(UUID uuid, String towerId, int floor, Location location, String modelId) {
            this.uuid = uuid;
            this.towerId = towerId;
            this.floor = floor;
            this.location = location.clone();
            this.modelId = modelId;
        }
        
        public UUID getUuid() { return uuid; }
        public String getTowerId() { return towerId; }
        public int getFloor() { return floor; }
        public Location getLocation() { return location.clone(); }
        public String getModelId() { return modelId; }
        
        public void saveToConfig(FileConfiguration config, String path) {
            config.set(path + ".tower_id", towerId);
            config.set(path + ".floor", floor);
            config.set(path + ".world", location.getWorld().getName());
            config.set(path + ".x", location.getX());
            config.set(path + ".y", location.getY());
            config.set(path + ".z", location.getZ());
            config.set(path + ".yaw", location.getYaw());
            config.set(path + ".pitch", location.getPitch());
            if (modelId != null) {
                config.set(path + ".model_id", modelId);
            }
        }
        
        public static NPCConfig fromConfig(ConfigurationSection section) {
            String uuidStr = section.getName();
            UUID uuid = UUID.fromString(uuidStr);
            
            String towerId = section.getString("tower_id");
            int floor = section.getInt("floor");
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            
            if (world == null) return null;
            
            Location location = new Location(world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
            
            String modelId = section.getString("model_id");
            
            return new NPCConfig(uuid, towerId, floor, location, modelId);
        }
    }
}
