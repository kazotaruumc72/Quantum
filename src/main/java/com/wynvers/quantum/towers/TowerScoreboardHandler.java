package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Gère le scoreboard personnalisé pour les tours
 * 
 * Fonctionnalités:
 * - Scoreboard automatique quand joueur entre dans une tour
 * - Désactive Oreo Essentials avec /sb
 * - Mise à jour en temps réel (1 seconde)
 * - Restaure Oreo Essentials à la sortie
 */
public class TowerScoreboardHandler {
    
    private final Quantum plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    private final Set<UUID> playersInTower;
    
    public TowerScoreboardHandler(Quantum plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        this.playersInTower = new HashSet<>();
    }
    
    /**
     * Active le scoreboard de tour pour un joueur
     * @param player Joueur
     * @param towerId ID de la tour
     */
    public void enableTowerScoreboard(Player player, String towerId) {
        // Désactiver Oreo Essentials scoreboard
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sb " + player.getName());
        
        // Créer le scoreboard personnalisé
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("tower", "dummy", 
            ChatColor.translateAlternateColorCodes('&', "&6&lTOURS"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Appliquer le scoreboard
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        playersInTower.add(player.getUniqueId());
        
        // Démarrer la tâche de mise à jour
        startUpdateTask(player);
        
        plugin.getQuantumLogger().info("Tower scoreboard enabled for " + player.getName());
    }
    
    /**
     * Désactive le scoreboard de tour et restaure Oreo Essentials
     * @param player Joueur
     */
    public void disableTowerScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Arrêter la tâche de mise à jour
        BukkitRunnable task = updateTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        
        // Supprimer le scoreboard
        playerScoreboards.remove(uuid);
        playersInTower.remove(uuid);
        
        // Réactiver Oreo Essentials scoreboard
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sb " + player.getName());
        
        plugin.getQuantumLogger().info("Tower scoreboard disabled for " + player.getName());
    }
    
    /**
     * Démarre la tâche de mise à jour du scoreboard
     * @param player Joueur
     */
    private void startUpdateTask(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !playersInTower.contains(player.getUniqueId())) {
                    cancel();
                    return;
                }
                
                updateScoreboard(player);
            }
        };
        
        task.runTaskTimer(plugin, 0L, 20L); // Mise à jour toutes les secondes
        updateTasks.put(player.getUniqueId(), task);
    }
    
    /**
     * Met à jour le scoreboard d'un joueur
     * @param player Joueur
     */
    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;
        
        Objective objective = scoreboard.getObjective("tower");
        if (objective == null) return;
        
        // Effacer les anciennes lignes
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        // Récupérer les placeholders
        List<String> lines = getScoreboardLines(player);
        
        // Ajouter les lignes (de bas en haut)
        int score = lines.size();
        for (String line : lines) {
            // Remplacer les placeholders
            line = PlaceholderAPI.setPlaceholders(player, line);
            line = ChatColor.translateAlternateColorCodes('&', line);
            
            // Gérer les lignes dupliquées en ajoutant des espaces invisibles
            String entry = line;
            int duplicateCount = 0;
            while (scoreboard.getEntries().contains(entry)) {
                entry = line + " ".repeat(duplicateCount + 1);
                duplicateCount++;
            }
            
            objective.getScore(entry).setScore(score--);
        }
    }
    
    /**
     * Obtient les lignes du scoreboard configurées
     * @param player Joueur
     * @return Liste des lignes
     */
    private List<String> getScoreboardLines(Player player) {
        // Configuration par défaut (peut être modifiée via zones.yml)
        List<String> lines = new ArrayList<>();
        
        lines.add("&7&m                    ");
        lines.add("&6&lTour Actuelle:");
        lines.add("  &f%quantum_tower_current%");
        lines.add("");
        lines.add("&e&lÉtage: &f%quantum_tower_floor%");
        lines.add("&7Progression: &a%quantum_tower_progress%");
        lines.add("");
        lines.add("&c&lMonstres:");
        lines.add("&7Tués: &f%quantum_tower_kills_progress%");
        lines.add("&7%quantum_tower_percentage% &acomplété");
        lines.add("");
        lines.add("&d&lProchain Boss:");
        lines.add("&7%quantum_tower_next_boss%");
        lines.add("");
        lines.add("&b&lTours Complétées:");
        lines.add("&7%quantum_towers_completed%");
        lines.add("&7&m                    ");
        
        // Charger depuis la config si disponible
        TowerManager towerManager = plugin.getTowerManager();
        if (towerManager != null) {
            TowerProgress progress = towerManager.getProgress(player.getUniqueId());
            String towerId = progress.getCurrentTower();
            
            if (towerId != null) {
                List<String> customLines = plugin.getConfig().getStringList("towers." + towerId + ".scoreboard.lines");
                if (!customLines.isEmpty()) {
                    return customLines;
                }
            }
        }
        
        return lines;
    }
    
    /**
     * Nettoie les données d'un joueur qui se déconnecte
     * @param player Joueur
     */
    public void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Arrêter la tâche de mise à jour
        BukkitRunnable task = updateTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        
        playerScoreboards.remove(uuid);
        playersInTower.remove(uuid);
    }
    
    /**
     * Arrête toutes les tâches de mise à jour
     */
    public void shutdown() {
        for (BukkitRunnable task : updateTasks.values()) {
            task.cancel();
        }
        updateTasks.clear();
        playerScoreboards.clear();
        playersInTower.clear();
    }
    
    /**
     * Vérifie si un joueur a le scoreboard de tour actif
     * @param player Joueur
     * @return true si actif
     */
    public boolean hasTowerScoreboard(Player player) {
        return playersInTower.contains(player.getUniqueId());
    }
}
