package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.ScoreboardConfig;
import com.wynvers.quantum.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Listener qui gère l'affichage automatique du scoreboard
 * Utilise maintenant ScoreboardConfig pour charger depuis scoreboard.yml
 */
public class ScoreboardListener implements Listener {
    
    private final Quantum plugin;
    private final ScoreboardManager scoreboardManager;
    private final ScoreboardConfig scoreboardConfig;
    
    public ScoreboardListener(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
        this.scoreboardConfig = plugin.getScoreboardConfig();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Délai de 1 tick pour laisser le joueur charger complètement
        new BukkitRunnable() {
            @Override
            public void run() {
                // Vérifier si le scoreboard est activé globalement dans la config
                if (!scoreboardConfig.isEnabled()) {
                    return;
                }
                
                // Vérifier si le joueur a le scoreboard activé
                if (!scoreboardManager.isScoreboardEnabled(player)) {
                    return;
                }
                
                // Charger les lignes depuis scoreboard.yml
                List<String> lines = scoreboardConfig.getLines();
                String title = scoreboardConfig.getTitle();
                
                // Appliquer le scoreboard
                scoreboardManager.setScoreboard(player, title, lines);
                
                // Démarrer la mise à jour automatique
                startScoreboardUpdate(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Nettoyer les données du scoreboard
        scoreboardManager.cleanup(player);
    }
    
    /**
     * Démarre la mise à jour automatique du scoreboard
     * Utilise l'intervalle défini dans scoreboard.yml
     */
    private void startScoreboardUpdate(Player player) {
        long updateInterval = scoreboardConfig.getUpdateInterval();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                // Vérifier si le joueur est toujours connecté
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Vérifier si le scoreboard est toujours activé globalement
                if (!scoreboardConfig.isEnabled()) {
                    cancel();
                    return;
                }
                
                // Vérifier si le scoreboard est toujours activé pour ce joueur
                if (!scoreboardManager.isScoreboardEnabled(player)) {
                    cancel();
                    return;
                }
                
                // Vérifier si le joueur a toujours le scoreboard
                if (!scoreboardManager.hasScoreboard(player)) {
                    cancel();
                    return;
                }
                
                // Mettre à jour avec les lignes brutes - ScoreboardManager gère PlaceholderAPI et MiniMessage
                List<String> lines = scoreboardConfig.getLines();
                scoreboardManager.updateAllLines(player, lines);
            }
        }.runTaskTimer(plugin, updateInterval, updateInterval);
    }
}
