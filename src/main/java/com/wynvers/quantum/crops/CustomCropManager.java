package com.wynvers.quantum.crops;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
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
 * Gestionnaire du système de cultures personnalisées
 */
public class CustomCropManager {
    
    private final Quantum plugin;
    private final Map<String, CustomCrop> cropsMap;
    private final Map<Location, PlantedCrop> plantedCrops;
    private YamlConfiguration config;
    
    public CustomCropManager(Quantum plugin) {
        this.plugin = plugin;
        this.cropsMap = new HashMap<>();
        this.plantedCrops = new HashMap<>();
        loadConfig();
        startGrowthTask();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "custom_crops.yml");
        if (!configFile.exists()) {
            plugin.saveResource("custom_crops.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadCrops();
        plugin.getLogger().info("✓ Custom Crops system loaded! (" + cropsMap.size() + " crop types)");
    }
    
    private void loadCrops() {
        cropsMap.clear();
        
        ConfigurationSection cropsSection = config.getConfigurationSection("crops");
        if (cropsSection == null) return;
        
        for (String cropId : cropsSection.getKeys(false)) {
            ConfigurationSection section = cropsSection.getConfigurationSection(cropId);
            if (section == null) continue;
            
            String displayName = section.getString("display_name", cropId);
            String nexoIdBase = section.getString("nexo_id_base");
            
            CustomCrop crop = new CustomCrop(cropId, displayName, nexoIdBase);
            
            // Charger les stades de croissance
            if (section.contains("growth_stages")) {
                List<?> stages = section.getList("growth_stages");
                if (stages != null) {
                    for (Object obj : stages) {
                        if (obj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> stageMap = (Map<String, Object>) obj;
                            int stage = ((Number) stageMap.get("stage")).intValue();
                            String nexoId = (String) stageMap.get("nexo_id");
                            int duration = ((Number) stageMap.get("duration")).intValue();
                            crop.addGrowthStage(stage, nexoId, duration);
                        }
                    }
                }
            }
            
            // Charger les commandes de récolte
            if (section.contains("harvest_commands")) {
                List<String> commands = section.getStringList("harvest_commands");
                for (String cmd : commands) {
                    crop.addHarvestCommand(cmd);
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
                            
                            String nexoId = (String) dropMap.get("nexo_id");
                            String minecraftType = (String) dropMap.get("minecraft");
                            int minAmount = ((Number) dropMap.getOrDefault("min_amount", 1)).intValue();
                            int maxAmount = ((Number) dropMap.getOrDefault("max_amount", 1)).intValue();
                            double chance = ((Number) dropMap.getOrDefault("chance", 100.0)).doubleValue();
                            
                            CropDrop drop = new CropDrop(nexoId, minecraftType, minAmount, maxAmount, chance);
                            crop.addDrop(drop);
                        }
                    }
                }
            }
            
            cropsMap.put(cropId, crop);
        }
    }
    
    public void reload() {
        loadConfig();
    }
    
    public CustomCrop getCrop(String cropId) {
        return cropsMap.get(cropId);
    }
    
    /**
     * Plante une culture à un emplacement
     */
    public boolean plantCrop(Location location, String cropId, Player player) {
        CustomCrop crop = cropsMap.get(cropId);
        if (crop == null) {
            String message = config.getString("messages.invalid_crop", "&cCette culture n'existe pas!");
            player.sendMessage(message.replace('&', '§'));
            return false;
        }
        
        // Vérifier si une culture est déjà plantée
        if (plantedCrops.containsKey(location)) {
            return false;
        }
        
        // Placer le premier stade de croissance
        CustomCrop.GrowthStage firstStage = crop.getStage(1);
        if (firstStage != null) {
            try {
                NexoFurniture.place(firstStage.getNexoId(), location, 0f, null);
                
                PlantedCrop planted = new PlantedCrop(cropId, location, System.currentTimeMillis());
                plantedCrops.put(location, planted);
                
                String message = config.getString("messages.crop_planted", "&aVous avez planté: &e{crop_name}");
                message = message.replace("{crop_name}", crop.getDisplayName());
                player.sendMessage(message.replace('&', '§'));
                
                return true;
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors de la plantation de " + cropId + ": " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Récolte une culture avec une houe
     */
    public void harvestCrop(Location location, Player player) {
        PlantedCrop planted = plantedCrops.get(location);
        if (planted == null) return;
        
        CustomCrop crop = cropsMap.get(planted.getCropId());
        if (crop == null) return;
        
        // Vérifier si la culture est mature
        if (!crop.isMature(planted.getCurrentStage())) {
            String message = config.getString("messages.crop_not_mature", "&cCette culture n'est pas encore mature!");
            message = message.replace("{stage}", String.valueOf(planted.getCurrentStage()));
            message = message.replace("{max_stage}", String.valueOf(crop.getMaxStage()));
            player.sendMessage(message.replace('&', '§'));
            return;
        }
        
        // Exécuter les commandes de récolte
        for (String command : crop.getHarvestCommands()) {
            String cmd = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
        
        // Donner les drops
        giveDrops(player, crop);
        
        // Retirer le furniture/bloc
        location.getBlock().setType(Material.AIR);
        
        // Retirer de la liste des cultures plantées
        plantedCrops.remove(location);
        
        String message = config.getString("messages.crop_harvested", "&aVous avez récolté: &e{crop_name}");
        message = message.replace("{crop_name}", crop.getDisplayName());
        player.sendMessage(message.replace('&', '§'));
    }
    
    /**
     * Donne les drops au joueur
     */
    private void giveDrops(Player player, CustomCrop crop) {
        for (CropDrop drop : crop.getDrops()) {
            if (!drop.shouldDrop()) continue;
            
            int amount = drop.calculateAmount();
            ItemStack item;
            
            if (drop.isNexoItem()) {
                var builder = NexoItems.itemFromId(drop.getNexoId());
                if (builder == null) continue;
                item = builder.build();
                if (item == null) continue;
                item.setAmount(amount);
            } else {
                Material material = Material.matchMaterial(drop.getMinecraftType());
                if (material == null) continue;
                item = new ItemStack(material, amount);
            }
            
            player.getInventory().addItem(item);
        }
    }
    
    /**
     * Démarre la tâche de croissance des cultures
     */
    private void startGrowthTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            for (Map.Entry<Location, PlantedCrop> entry : new HashMap<>(plantedCrops).entrySet()) {
                PlantedCrop planted = entry.getValue();
                CustomCrop crop = cropsMap.get(planted.getCropId());
                
                if (crop == null) continue;
                
                int currentStage = planted.getCurrentStage();
                if (currentStage >= crop.getMaxStage()) continue; // Déjà mature
                
                CustomCrop.GrowthStage stage = crop.getStage(currentStage);
                if (stage == null) continue;
                
                // Vérifier si assez de temps s'est écoulé
                long elapsedSeconds = (currentTime - planted.getLastGrowthTime()) / 1000;
                if (elapsedSeconds >= stage.getDuration()) {
                    // Passer au stade suivant
                    int nextStage = currentStage + 1;
                    CustomCrop.GrowthStage nextStageData = crop.getStage(nextStage);
                    
                    if (nextStageData != null) {
                        // Retirer l'ancien modèle
                        entry.getKey().getBlock().setType(Material.AIR);
                        
                        // Placer le nouveau modèle
                        try {
                            NexoFurniture.place(nextStageData.getNexoId(), entry.getKey(), 0f, null);
                            planted.grow();
                        } catch (Exception e) {
                            plugin.getLogger().warning("Erreur lors de la croissance de " + crop.getCropId() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }, 20L * 30, 20L * 30); // Vérifier toutes les 30 secondes
    }
    
    /**
     * Classe interne représentant une culture plantée
     */
    private static class PlantedCrop {
        private final String cropId;
        private final Location location;
        private long lastGrowthTime;
        private int currentStage;
        
        public PlantedCrop(String cropId, Location location, long plantTime) {
            this.cropId = cropId;
            this.location = location;
            this.lastGrowthTime = plantTime;
            this.currentStage = 1;
        }
        
        public String getCropId() {
            return cropId;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public long getLastGrowthTime() {
            return lastGrowthTime;
        }
        
        public int getCurrentStage() {
            return currentStage;
        }
        
        public void grow() {
            this.currentStage++;
            this.lastGrowthTime = System.currentTimeMillis();
        }
    }
}
