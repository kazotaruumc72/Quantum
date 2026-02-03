package com.wynvers.quantum.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gère les zones WorldGuard avec restrictions de sortie basées sur les kills de mobs
 * 
 * Fonctionnalités:
 * - Empêche les joueurs de quitter une zone tant qu'ils n'ont pas tué assez de mobs
 * - Support de plusieurs types de mobs par zone
 * - Commande console pour forcer la sortie d'une zone
 * - Permission de bypass
 * - Reset automatique du placeholder après sortie
 * 
 * Structure zones.yml:
 * zones:
 *   zone_name:
 *     world: "world"
 *     region: "region_name"
 *     requirements:
 *       - mob: "zombie_boss"
 *         amount: 10
 *       - mob: "skeleton_king"
 *         amount: 5
 *     messages:
 *       enter: "§cVous entrez dans la zone dangereuse!"
 *       exit_denied: "§cVous devez tuer %requirements% avant de sortir!"
 *       exit_allowed: "§aVous avez complété les objectifs!"
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ZoneManager {
    
    private final Quantum plugin;
    private final File zonesFile;
    private YamlConfiguration zonesConfig;
    private final Map<UUID, Set<String>> playerInZones = new HashMap<>();
    private final String BYPASS_PERMISSION = "quantum.zone.bypass";
    
    public ZoneManager(Quantum plugin) {
        this.plugin = plugin;
        this.zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        loadZonesConfig();
    }
    
    /**
     * Charge la configuration des zones
     */
    private void loadZonesConfig() {
        if (!zonesFile.exists()) {
            plugin.saveResource("zones.yml", false);
        }
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        plugin.getLogger().info("[ZONES] Loaded " + getZoneCount() + " zones");
    }
    
    /**
     * Recharge la configuration
     */
    public void reloadConfig() {
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        plugin.getLogger().info("[ZONES] Reloaded " + getZoneCount() + " zones");
    }
    
    /**
     * Sauvegarde la configuration
     */
    public void saveConfig() {
        try {
            zonesConfig.save(zonesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[ZONES] Failed to save zones.yml: " + e.getMessage());
        }
    }
    
    /**
     * Compte le nombre de zones configurées
     */
    public int getZoneCount() {
        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        return zonesSection != null ? zonesSection.getKeys(false).size() : 0;
    }
    
    /**
     * Vérifie si un joueur est dans une zone restreinte
     */
    public boolean isPlayerInRestrictedZone(Player player, Location location) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return false;
        }
        
        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        if (zonesSection == null) return false;
        
        for (String zoneName : zonesSection.getKeys(false)) {
            String worldName = zonesConfig.getString("zones." + zoneName + ".world");
            String regionName = zonesConfig.getString("zones." + zoneName + ".region");
            
            if (worldName == null || regionName == null) continue;
            
            // Vérifier si le joueur est dans cette région
            if (isInRegion(location, worldName, regionName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie si une location est dans une région WorldGuard
     */
    private boolean isInRegion(Location location, String worldName, String regionName) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        
        if (regions == null) return false;
        
        ProtectedRegion region = regions.getRegion(regionName);
        if (region == null) return false;
        
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Gère l'entrée d'un joueur dans une zone
     */
    public void onPlayerEnterZone(Player player, String zoneName) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return;
        }
        
        // Ajouter le joueur dans la zone
        playerInZones.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(zoneName);
        
        // Message d'entrée
        String enterMessage = zonesConfig.getString("zones." + zoneName + ".messages.enter");
        if (enterMessage != null && !enterMessage.isEmpty()) {
            player.sendMessage(enterMessage);
        }
        
        plugin.getLogger().info("[ZONES] " + player.getName() + " entered zone: " + zoneName);
    }
    
    /**
     * Gère la sortie d'un joueur d'une zone
     */
    public void onPlayerExitZone(Player player, String zoneName) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return;
        }
        
        Set<String> zones = playerInZones.get(player.getUniqueId());
        if (zones != null) {
            zones.remove(zoneName);
            if (zones.isEmpty()) {
                playerInZones.remove(player.getUniqueId());
            }
        }
        
        // Reset les placeholders de cette zone
        resetZonePlaceholders(player, zoneName);
        
        plugin.getLogger().info("[ZONES] " + player.getName() + " exited zone: " + zoneName);
    }
    
    /**
     * Vérifie si un joueur peut quitter une zone
     */
    public boolean canPlayerLeaveZone(Player player, String zoneName) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }
        
        // Vérifier si toutes les requirements sont remplies
        List<Map<?, ?>> requirements = zonesConfig.getMapList("zones." + zoneName + ".requirements");
        
        for (Map<?, ?> requirement : requirements) {
            String mobId = (String) requirement.get("mob");
            int requiredAmount = (int) requirement.get("amount");
            
            // Vérifier le placeholder
            String placeholder = "%quantum_killed_" + mobId + "_" + requiredAmount + "%";
            String result = PlaceholderAPI.setPlaceholders(player, placeholder);
            
            // Si le placeholder ne retourne pas "true", le joueur ne peut pas sortir
            if (!"true".equalsIgnoreCase(result)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Obtient le nom de la zone dans laquelle se trouve un joueur
     */
    public String getPlayerZone(Player player, Location location) {
        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        if (zonesSection == null) return null;
        
        for (String zoneName : zonesSection.getKeys(false)) {
            String worldName = zonesConfig.getString("zones." + zoneName + ".world");
            String regionName = zonesConfig.getString("zones." + zoneName + ".region");
            
            if (worldName == null || regionName == null) continue;
            
            if (isInRegion(location, worldName, regionName)) {
                return zoneName;
            }
        }
        
        return null;
    }
    
    /**
     * Force la sortie d'un joueur d'une zone (via commande console)
     */
    public void forcePlayerExitZone(Player player) {
        Set<String> zones = playerInZones.get(player.getUniqueId());
        if (zones != null) {
            for (String zoneName : new HashSet<>(zones)) {
                resetZonePlaceholders(player, zoneName);
                zones.remove(zoneName);
            }
            playerInZones.remove(player.getUniqueId());
            
            String exitMessage = "§aVous avez été autorisé à quitter la zone.";
            player.sendMessage(exitMessage);
            
            plugin.getLogger().info("[ZONES] Forced exit for " + player.getName());
        }
    }
    
    /**
     * Reset tous les placeholders d'une zone pour un joueur
     */
    private void resetZonePlaceholders(Player player, String zoneName) {
        List<Map<?, ?>> requirements = zonesConfig.getMapList("zones." + zoneName + ".requirements");
        
        for (Map<?, ?> requirement : requirements) {
            String mobId = (String) requirement.get("mob");
            int amount = (int) requirement.get("amount");
            
            // Reset le compteur de kills pour ce mob
            plugin.getKillTracker().resetKills(player, mobId, amount);
        }
        
        plugin.getLogger().info("[ZONES] Reset placeholders for " + player.getName() + " in zone " + zoneName);
    }
    
    /**
     * Obtient le message d'erreur pour une zone
     */
    public String getDeniedMessage(Player player, String zoneName) {
        String message = zonesConfig.getString("zones." + zoneName + ".messages.exit_denied", 
            "§cVous devez compléter les objectifs avant de sortir!");
        
        // Remplacer %requirements% par la liste des requirements
        StringBuilder requirements = new StringBuilder();
        List<Map<?, ?>> requirementList = zonesConfig.getMapList("zones." + zoneName + ".requirements");
        
        for (int i = 0; i < requirementList.size(); i++) {
            Map<?, ?> requirement = requirementList.get(i);
            String mobId = (String) requirement.get("mob");
            int amount = (int) requirement.get("amount");
            
            requirements.append(amount).append("x ").append(mobId);
            if (i < requirementList.size() - 1) {
                requirements.append(", ");
            }
        }
        
        return message.replace("%requirements%", requirements.toString());
    }
    
    /**
     * Nettoie les données d'un joueur qui se déconnecte
     */
    public void onPlayerQuit(Player player) {
        playerInZones.remove(player.getUniqueId());
    }
    
    /**
     * Obtient les zones dans lesquelles se trouve un joueur
     */
    public Set<String> getPlayerZones(UUID playerUUID) {
        return playerInZones.getOrDefault(playerUUID, Collections.emptySet());
    }
}
