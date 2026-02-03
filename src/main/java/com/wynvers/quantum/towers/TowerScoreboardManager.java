package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages scoreboard switching when entering/leaving towers
 * 
 * Works with ANY scoreboard plugin (Oreo Essentials, TAB, SimpleScore, etc.)
 * Uses command execution to switch scoreboards
 * 
 * Configuration in zones.yml:
 * towers:
 *   fire_tower:
 *     scoreboard:
 *       command: "sb switch {player} tower_scoreboard"  # Command to run
 *       restore_command: "sb switch {player} default"   # Command to restore
 */
public class TowerScoreboardManager {
    
    private final Quantum plugin;
    private final Map<UUID, String> previousScoreboard;
    
    public TowerScoreboardManager(Quantum plugin) {
        this.plugin = plugin;
        this.previousScoreboard = new HashMap<>();
    }
    
    /**
     * Switch to tower scoreboard when player enters
     * @param player Player entering tower
     * @param towerId Tower ID
     */
    public void onTowerEnter(Player player, String towerId) {
        TowerConfig tower = plugin.getTowerManager().getTower(towerId);
        if (tower == null) return;
        
        // Get scoreboard command from config (si configuré)
        String command = getScoreboardCommand(towerId);
        if (command == null || command.isEmpty()) {
            // Par défaut, utilise le système de PlaceholderAPI
            // Les scoreboards peuvent utiliser les placeholders %quantum_tower_*%
            plugin.getQuantumLogger().info("Tower " + towerId + " has no scoreboard command configured");
            return;
        }
        
        // Remplace les placeholders
        command = command.replace("{player}", player.getName());
        command = command.replace("{tower}", towerId);
        
        // Exécute la commande depuis la console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        
        plugin.getQuantumLogger().info("Switched scoreboard for " + player.getName() + " (tower: " + towerId + ")");
    }
    
    /**
     * Restore previous scoreboard when player exits
     * @param player Player exiting tower
     * @param towerId Tower ID
     */
    public void onTowerExit(Player player, String towerId) {
        TowerConfig tower = plugin.getTowerManager().getTower(towerId);
        if (tower == null) return;
        
        // Get restore command from config
        String command = getRestoreCommand(towerId);
        if (command == null || command.isEmpty()) {
            plugin.getQuantumLogger().info("Tower " + towerId + " has no restore command configured");
            return;
        }
        
        // Remplace les placeholders
        command = command.replace("{player}", player.getName());
        command = command.replace("{tower}", towerId);
        
        // Exécute la commande depuis la console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        
        plugin.getQuantumLogger().info("Restored scoreboard for " + player.getName());
    }
    
    /**
     * Get scoreboard switch command from zones.yml
     * @param towerId Tower ID
     * @return Command or null
     */
    private String getScoreboardCommand(String towerId) {
        return plugin.getConfig().getString("towers." + towerId + ".scoreboard.command");
    }
    
    /**
     * Get scoreboard restore command from zones.yml
     * @param towerId Tower ID
     * @return Command or null
     */
    private String getRestoreCommand(String towerId) {
        return plugin.getConfig().getString("towers." + towerId + ".scoreboard.restore_command");
    }
}
