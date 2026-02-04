package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.ScoreboardManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener qui gère l'affichage automatique du scoreboard
 */
public class ScoreboardListener implements Listener {
    
    private final Quantum plugin;
    private final ScoreboardManager scoreboardManager;
    
    public ScoreboardListener(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Délai de 1 tick pour laisser le joueur charger complètement
        new BukkitRunnable() {
            @Override
            public void run() {
                // Vérifier si le joueur a le scoreboard activé
                if (!scoreboardManager.isScoreboardEnabled(player)) {
                    return;
                }
                
                // Charger les lignes depuis scoreboard.yml
                List<String> lines = getScoreboardLines();
                String title = plugin.getConfig().getString("scoreboard.title", "&6&lQUANTUM");
                
                // Appliquer le scoreboard
                scoreboardManager.setScoreboard(player, title, lines);
                
                // Démarrer la mise à jour automatique toutes les secondes
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
     * Récupère les lignes du scoreboard depuis la config
     */
    private List<String> getScoreboardLines() {
        List<String> configLines = plugin.getConfig().getStringList("scoreboard.lines");
        
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
     * Démarre la mise à jour automatique du scoreboard
     */
    private void startScoreboardUpdate(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Vérifier si le joueur est toujours connecté
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Vérifier si le scoreboard est toujours activé
                if (!scoreboardManager.isScoreboardEnabled(player)) {
                    cancel();
                    return;
                }
                
                // Vérifier si le joueur a toujours le scoreboard
                if (!scoreboardManager.hasScoreboard(player)) {
                    cancel();
                    return;
                }
                
                // Mettre à jour les lignes avec les placeholders
                List<String> lines = getScoreboardLines();
                List<String> processedLines = new ArrayList<>();
                
                for (String line : lines) {
                    // Remplacer les placeholders si PlaceholderAPI est disponible
                    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        line = PlaceholderAPI.setPlaceholders(player, line);
                    }
                    processedLines.add(line);
                }
                
                // Mettre à jour le scoreboard
                scoreboardManager.updateAllLines(player, processedLines);
            }
        }.runTaskTimer(plugin, 20L, 20L); // Toutes les secondes
    }
}
