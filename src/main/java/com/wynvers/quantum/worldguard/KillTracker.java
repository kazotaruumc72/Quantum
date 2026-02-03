package com.wynvers.quantum.worldguard;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère le tracking des kills de mobs pour les zones
 * 
 * Fonctionnalités:
 * - Compte les kills de mobs spécifiques par joueur
 * - Vérifie si un joueur a atteint le quota requis
 * - Reset automatique après sortie de zone
 * - Sauvegarde dans kills.yml
 * 
 * Structure kills.yml:
 * kills:
 *   [player_uuid]:
 *     [mob_id]:
 *       [amount]: current_kills
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class KillTracker {
    
    private final Quantum plugin;
    private final File killsFile;
    private YamlConfiguration killsConfig;
    
    // Cache en mémoire: UUID -> (mobId_amount -> kills)
    private final Map<UUID, Map<String, Integer>> killsCache = new HashMap<>();
    
    public KillTracker(Quantum plugin) {
        this.plugin = plugin;
        this.killsFile = new File(plugin.getDataFolder(), "kills.yml");
        loadKillsConfig();
    }
    
    /**
     * Charge la configuration des kills
     */
    private void loadKillsConfig() {
        if (!killsFile.exists()) {
            try {
                killsFile.createNewFile();
                killsConfig = new YamlConfiguration();
                killsConfig.set("kills", new HashMap<>());
                killsConfig.save(killsFile);
                plugin.getLogger().info("[KILLS] Created kills.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("[KILLS] Failed to create kills.yml: " + e.getMessage());
            }
        } else {
            killsConfig = YamlConfiguration.loadConfiguration(killsFile);
            loadKillsToCache();
        }
    }
    
    /**
     * Charge les kills du fichier vers le cache
     */
    private void loadKillsToCache() {
        if (!killsConfig.contains("kills")) return;
        
        for (String uuidStr : killsConfig.getConfigurationSection("kills").getKeys(false)) {
            UUID playerUUID = UUID.fromString(uuidStr);
            Map<String, Integer> playerKills = new HashMap<>();
            
            for (String mobId : killsConfig.getConfigurationSection("kills." + uuidStr).getKeys(false)) {
                for (String amount : killsConfig.getConfigurationSection("kills." + uuidStr + "." + mobId).getKeys(false)) {
                    String key = mobId + "_" + amount;
                    int kills = killsConfig.getInt("kills." + uuidStr + "." + mobId + "." + amount);
                    playerKills.put(key, kills);
                }
            }
            
            killsCache.put(playerUUID, playerKills);
        }
        
        plugin.getLogger().info("[KILLS] Loaded kill data for " + killsCache.size() + " players");
    }
    
    /**
     * Ajoute un kill pour un joueur
     */
    public void addKill(Player player, String mobId, int requiredAmount) {
        UUID uuid = player.getUniqueId();
        String key = mobId + "_" + requiredAmount;
        
        Map<String, Integer> playerKills = killsCache.computeIfAbsent(uuid, k -> new HashMap<>());
        int currentKills = playerKills.getOrDefault(key, 0);
        
        // Incrémenter
        currentKills++;
        playerKills.put(key, currentKills);
        
        // Sauvegarder
        saveKill(uuid, mobId, requiredAmount, currentKills);
        
        plugin.getLogger().info("[KILLS] " + player.getName() + " killed " + mobId + ": " + currentKills + "/" + requiredAmount);
    }
    
    /**
     * Vérifie si un joueur a atteint le quota requis
     */
    public boolean hasReachedQuota(UUID playerUUID, String mobId, int requiredAmount) {
        String key = mobId + "_" + requiredAmount;
        Map<String, Integer> playerKills = killsCache.get(playerUUID);
        
        if (playerKills == null) return false;
        
        int currentKills = playerKills.getOrDefault(key, 0);
        return currentKills >= requiredAmount;
    }
    
    /**
     * Obtient le nombre de kills actuel
     */
    public int getKills(UUID playerUUID, String mobId, int requiredAmount) {
        String key = mobId + "_" + requiredAmount;
        Map<String, Integer> playerKills = killsCache.get(playerUUID);
        
        if (playerKills == null) return 0;
        
        return playerKills.getOrDefault(key, 0);
    }
    
    /**
     * Reset les kills d'un joueur pour un mob spécifique
     */
    public void resetKills(Player player, String mobId, int requiredAmount) {
        UUID uuid = player.getUniqueId();
        String key = mobId + "_" + requiredAmount;
        
        Map<String, Integer> playerKills = killsCache.get(uuid);
        if (playerKills != null) {
            playerKills.remove(key);
            if (playerKills.isEmpty()) {
                killsCache.remove(uuid);
            }
        }
        
        // Supprimer du fichier
        String path = "kills." + uuid.toString() + "." + mobId + "." + requiredAmount;
        killsConfig.set(path, null);
        
        try {
            killsConfig.save(killsFile);
            plugin.getLogger().info("[KILLS] Reset kills for " + player.getName() + ": " + mobId + "_" + requiredAmount);
        } catch (IOException e) {
            plugin.getLogger().severe("[KILLS] Failed to save kills.yml: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde un kill dans le fichier
     */
    private void saveKill(UUID playerUUID, String mobId, int requiredAmount, int kills) {
        String path = "kills." + playerUUID.toString() + "." + mobId + "." + requiredAmount;
        killsConfig.set(path, kills);
        
        try {
            killsConfig.save(killsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[KILLS] Failed to save kills.yml: " + e.getMessage());
        }
    }
    
    /**
     * Nettoie les données d'un joueur qui se déconnecte
     */
    public void onPlayerQuit(UUID playerUUID) {
        // Les données restent en cache pour la prochaine connexion
        // Elles sont déjà sauvegardées dans le fichier
    }
}
