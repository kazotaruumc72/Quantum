package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire du fichier de configuration scoreboard.yml
 * Charge et gère la configuration du scoreboard personnalisé
 */
public class ScoreboardConfig {
    
    private final Quantum plugin;
    private File configFile;
    private FileConfiguration config;
    
    public ScoreboardConfig(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Charge ou recharge la configuration depuis scoreboard.yml
     */
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        
        // Si le fichier n'existe pas, le créer depuis les ressources
        if (!configFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Recharge la configuration
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * Sauvegarde la configuration
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Impossible de sauvegarder scoreboard.yml");
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si le scoreboard est activé dans la config
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * Récupère le titre du scoreboard
     */
    public String getTitle() {
        return config.getString("title", "&6&lQUANTUM");
    }
    
    /**
     * Récupère les lignes du scoreboard depuis la config
     */
    public List<String> getLines() {
        List<String> configLines = config.getStringList("lines");
        
        // Si pas de config, utiliser les lignes par défaut
        if (configLines.isEmpty()) {
            List<String> defaultLines = new ArrayList<>();
            defaultLines.add("&7&m                    ");
            defaultLines.add("&6&lQUANTUM");
            defaultLines.add("");
            defaultLines.add("&7Joueurs: &f%server_online%/%server_max_players%");
            defaultLines.add("&7Rang: %vault_rank%");
            defaultLines.add("");
            defaultLines.add("&e&lStatistiques:");
            defaultLines.add("&7Items stockés: &a%quantum_stats_total_items%");
            defaultLines.add("&7Trades: &a%quantum_stats_trades%");
            defaultLines.add("");
            defaultLines.add("&b&lTours:");
            defaultLines.add("&7Tours complétées: &a%quantum_towers_completed%");
            defaultLines.add("&7&m                    ");
            return defaultLines;
        }
        
        return configLines;
    }
    
    /**
     * Récupère l'intervalle de mise à jour en ticks (20 ticks = 1 seconde)
     */
    public long getUpdateInterval() {
        return config.getLong("update-interval", 20L);
    }
    
    /**
     * Récupère un message depuis la config
     */
    public String getMessage(String path) {
        return config.getString("messages." + path, "§cMessage manquant: " + path);
    }
    
    /**
     * Récupère la configuration brute
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
