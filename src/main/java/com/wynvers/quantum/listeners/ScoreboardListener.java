package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.ScoreboardConfig;
import com.wynvers.quantum.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener qui gère l'affichage automatique du scoreboard
 * Utilise maintenant ScoreboardConfig pour charger depuis scoreboard.yml
 * Optimisé pour utiliser une seule tâche partagée pour tous les joueurs
 */
public class ScoreboardListener implements Listener {
    
    private final Quantum plugin;
    private final ScoreboardManager scoreboardManager;
    private final ScoreboardConfig scoreboardConfig;
    private BukkitTask sharedUpdateTask;
    
    public ScoreboardListener(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
        this.scoreboardConfig = plugin.getScoreboardConfig();
        startSharedUpdateTask();
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
     * Démarre une tâche partagée unique qui met à jour tous les scoreboards
     * Plus efficace que d'avoir une tâche par joueur
     * 
     * Note: While we could track active scoreboard players in a Set to avoid
     * iterating all online players, the iteration overhead is minimal compared
     * to the benefits of simplicity and consistency. The checks are very fast
     * (HashMap lookups) and this avoids synchronization issues.
     */
    private void startSharedUpdateTask() {
        long updateInterval = scoreboardConfig.getUpdateInterval();
        
        sharedUpdateTask = new BukkitRunnable() {
        new BukkitRunnable() {
            // Instance-level cache to track last lines for this specific player's task
            private final List<String> lastLines = new ArrayList<>();
            
            @Override
            public void run() {
                // Vérifier si le scoreboard est toujours activé globalement
                if (!scoreboardConfig.isEnabled()) {
                    return;
                }
                
                // Charger les lignes une seule fois pour tous les joueurs
                List<String> lines = scoreboardConfig.getLines();
                
                // Mettre à jour tous les joueurs en ligne avec un scoreboard
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Vérifier si le scoreboard est activé pour ce joueur
                    if (!scoreboardManager.isScoreboardEnabled(player)) {
                        continue;
                    }
                    
                    // Vérifier si le joueur a un scoreboard
                    if (!scoreboardManager.hasScoreboard(player)) {
                        continue;
                    }
                    
                    // Mettre à jour avec les lignes brutes - ScoreboardManager gère PlaceholderAPI et MiniMessage
                    scoreboardManager.updateAllLines(player, lines);
                }
                
                // Get current lines
                List<String> lines = scoreboardConfig.getLines();
                
                // Only update if lines have changed (reduces unnecessary updates)
                if (!lines.equals(lastLines)) {
                    lastLines.clear();
                    lastLines.addAll(lines);
                    // Mettre à jour avec les lignes brutes - ScoreboardManager gère PlaceholderAPI et MiniMessage
                    scoreboardManager.updateAllLines(player, lines);
                }
            }
        }.runTaskTimer(plugin, updateInterval, updateInterval);
    }
    
    /**
     * Arrête la tâche partagée de mise à jour (appelé lors du unload du plugin)
     */
    public void shutdown() {
        if (sharedUpdateTask != null && !sharedUpdateTask.isCancelled()) {
            sharedUpdateTask.cancel();
        }
    }
}
