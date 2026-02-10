package com.wynvers.quantum.furniture;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.furniture.FurnitureMechanic;
import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

/**
 * Gestionnaire du système de furniture avec réapparition et drops
 */
public class FurnitureManager {
    
    private final Quantum plugin;
    private final Map<String, FurnitureData> furnitureMap;
    private final Map<Location, ScheduledRespawn> scheduledRespawns;
    private YamlConfiguration config;
    
    public FurnitureManager(Quantum plugin) {
        this.plugin = plugin;
        this.furnitureMap = new HashMap<>();
        this.scheduledRespawns = new HashMap<>();
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "furniture.yml");
        if (!configFile.exists()) {
            plugin.saveResource("furniture.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadFurniture();
        plugin.getLogger().info("✓ Furniture system loaded! (" + furnitureMap.size() + " furniture types)");
    }
    
    private void loadFurniture() {
        furnitureMap.clear();
        
        ConfigurationSection furnitureSection = config.getConfigurationSection("furniture");
        if (furnitureSection == null) return;
        
        for (String furnitureId : furnitureSection.getKeys(false)) {
            ConfigurationSection section = furnitureSection.getConfigurationSection(furnitureId);
            if (section == null) continue;
            
            String nexoId = section.getString("nexo_id");
            int respawnTime = section.getInt("respawn_time", 300);
            
            FurnitureData data = new FurnitureData(furnitureId, nexoId, respawnTime);
            
            // Charger les furnitures alternatifs
            if (section.contains("alternative_furniture")) {
                List<?> alternatives = section.getList("alternative_furniture");
                if (alternatives != null) {
                    for (Object obj : alternatives) {
                        if (obj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> altMap = (Map<String, Object>) obj;
                            String altId = (String) altMap.get("furniture_id");
                            double chance = ((Number) altMap.get("chance")).doubleValue();
                            data.addAlternativeFurniture(altId, chance);
                        }
                    }
                }
            }
            
            // Charger les drops
            if (section.contains("drops")) {
                List<?> drops = section.getList("drops");
                if (drops != null) {
                    for (Object obj : drops) {
                        if (obj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dropMap = (Map<String, Object>) obj;
                            
                            String nexoDropId = (String) dropMap.get("nexo_id");
                            String minecraftType = (String) dropMap.get("minecraft");
                            int minAmount = ((Number) dropMap.getOrDefault("min_amount", 1)).intValue();
                            int maxAmount = ((Number) dropMap.getOrDefault("max_amount", 1)).intValue();
                            double chance = ((Number) dropMap.getOrDefault("chance", 100.0)).doubleValue();
                            
                            FurnitureDrop drop = new FurnitureDrop(nexoDropId, minecraftType, minAmount, maxAmount, chance);
                            data.addDrop(drop);
                        }
                    }
                }
            }
            
            furnitureMap.put(furnitureId, data);
        }
    }
    
    public void reload() {
        loadConfig();
    }
    
    public FurnitureData getFurniture(String furnitureId) {
        return furnitureMap.get(furnitureId);
    }
    
    /**
     * Gère le break d'un furniture
     */
    public void handleFurnitureBreak(Location location, String furnitureId, Player player) {
        FurnitureData data = furnitureMap.get(furnitureId);
        if (data == null) return;
        
        // Donner les drops
        giveDrops(player, data);
        
        // Planifier la réapparition
        scheduleRespawn(location, data);
        
        // Message
        String message = config.getString("messages.furniture_broken", "&aVous avez cassé: &e{furniture_name}");
        message = message.replace("{furniture_name}", furnitureId);
        player.sendMessage(message.replace('&', '§'));
    }
    
    /**
     * Donne les drops au joueur avec multiplicateur de pioche
     */
    public void giveDrops(Player player, FurnitureData data) {
        giveDrops(player, data, 1);
    }
    
    /**
     * Donne les drops au joueur avec multiplicateur (pour pioche améliorée)
     */
    public void giveDrops(Player player, FurnitureData data, int multiplier) {
        for (FurnitureDrop drop : data.getDrops()) {
            if (!drop.shouldDrop()) continue;
            
            int amount = drop.calculateAmount() * multiplier;
            ItemStack item;
            
            if (drop.isNexoItem()) {
                // Item Nexo
                var builder = NexoItems.itemFromId(drop.getNexoId());
                if (builder == null) continue;
                item = builder.build();
                if (item == null) continue;
                item.setAmount(amount);
            } else {
                // Item Minecraft
                Material material = Material.matchMaterial(drop.getMinecraftType());
                if (material == null) continue;
                item = new ItemStack(material, amount);
            }
            
            // Donner l'item au joueur
            player.getInventory().addItem(item);
            
            // Message
            String message = config.getString("messages.drop_received", "&a+{amount}x &e{item_name}");
            message = message.replace("{amount}", String.valueOf(amount));
            message = message.replace("{item_name}", item.getType().name());
            player.sendMessage(message.replace('&', '§'));
        }
    }
    
    /**
     * Planifie la réapparition d'un furniture
     */
    private void scheduleRespawn(Location location, FurnitureData originalData) {
        // Sélectionner quel furniture va apparaître
        String furnitureToSpawn = originalData.selectFurnitureToSpawn();
        FurnitureData spawnData = furnitureMap.get(furnitureToSpawn);
        
        if (spawnData == null) {
            spawnData = originalData;
        }
        
        ScheduledRespawn respawn = new ScheduledRespawn(location, spawnData);
        scheduledRespawns.put(location, respawn);
        
        // Utiliser le temps de respawn de l'original
        long delay = originalData.getRespawnTime() * 20L; // Convertir en ticks
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            respawnFurniture(location, spawnData);
            scheduledRespawns.remove(location);
        }, delay);
    }
    
    /**
     * Fait réapparaître un furniture
     */
    private void respawnFurniture(Location location, FurnitureData data) {
        try {
            // Utiliser l'API Nexo pour placer le furniture
            NexoFurniture.place(data.getNexoId(), location, 0f, null);
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la réapparition du furniture " + data.getFurnitureId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Classe interne pour les réapparitions planifiées
     */
    private static class ScheduledRespawn {
        private final Location location;
        private final FurnitureData data;
        
        public ScheduledRespawn(Location location, FurnitureData data) {
            this.location = location;
            this.data = data;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public FurnitureData getData() {
            return data;
        }
    }
}
