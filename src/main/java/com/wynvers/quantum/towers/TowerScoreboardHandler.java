package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.ScoreboardManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

/**
 * Gère le scoreboard personnalisé pour les tours avec support MiniMessage
 * Utilise ScoreboardManager pour éviter les numéros rouges
 * 
 * Fonctionnalités:
 * - Scoreboard automatique quand joueur entre dans une tour
 * - Mise à jour en temps réel (configurable)
 * - Support complet de MiniMessage pour les couleurs et formats
 * - Pas de numéros rouges visibles (utilise le système ScoreboardManager)
 */
public class TowerScoreboardHandler {

    private final Quantum plugin;
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, BukkitRunnable> updateTasks;
    private final Set<UUID> playersInTower;

    private FileConfiguration towersScoreboardConfig;
    private int updateInterval = 20; // Ticks (1 seconde par défaut)

    public TowerScoreboardHandler(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
        this.updateTasks = new HashMap<>();
        this.playersInTower = new HashSet<>();

        loadScoreboardConfig();
    }

    /**
     * Charge la configuration towers_scoreboard.yml
     */
    private void loadScoreboardConfig() {
        File configFile = new File(plugin.getDataFolder(), "towers_scoreboard.yml");
        if (!configFile.exists()) {
            plugin.saveResource("towers_scoreboard.yml", false);
        }
        towersScoreboardConfig = YamlConfiguration.loadConfiguration(configFile);
        
        updateInterval = towersScoreboardConfig.getInt("update_interval", 20);
        
        plugin.getQuantumLogger().success("✓ Tower scoreboard config loaded!");
    }

    /**
     * Active le scoreboard de tour pour un joueur
     */
    public void enableTowerScoreboard(Player player, String towerId) {
        UUID uuid = player.getUniqueId();

        // Marquer le joueur comme étant dans une tour
        playersInTower.add(uuid);

        // Charger le titre MiniMessage depuis towers_scoreboard.yml
        String titlePath = towerId + ".title";
        String title = towersScoreboardConfig.getString(
                titlePath,
                towersScoreboardConfig.getString("default.title", "<gold><bold>TOURS</bold></gold>")
        );

        // Charger les lignes (ScoreboardManager gérera les placeholders et MiniMessage)
        List<String> lines = getScoreboardLines(player);

        // Appliquer le scoreboard via ScoreboardManager (même système que QUANTUM)
        // ScoreboardManager va gérer PlaceholderAPI + MiniMessage via ScoreboardUtils.color()
        scoreboardManager.setScoreboard(player, title, lines);

        // Démarrer la tâche de mise à jour
        startUpdateTask(player, towerId);

        plugin.getQuantumLogger().info("Tower scoreboard enabled for " + player.getName());
    }

    /**
     * Désactive le scoreboard de tour pour un joueur
     */
    public void disableTowerScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        // Arrêter la tâche de mise à jour
        BukkitRunnable task = updateTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        playersInTower.remove(uuid);

        plugin.getQuantumLogger().info("Tower scoreboard disabled for " + player.getName());
    }

    /**
     * Démarre la tâche de mise à jour du scoreboard
     */
    private void startUpdateTask(Player player, String towerId) {
        UUID uuid = player.getUniqueId();

        // Annuler l'ancienne tâche si elle existe
        BukkitRunnable oldTask = updateTasks.get(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }

        // Créer une nouvelle tâche
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !playersInTower.contains(uuid)) {
                    this.cancel();
                    updateTasks.remove(uuid);
                    return;
                }

                updateScoreboard(player);
            }
        };

        task.runTaskTimer(plugin, 0L, updateInterval);
        updateTasks.put(uuid, task);
    }

    /**
     * Met à jour le scoreboard du joueur
     */
    private void updateScoreboard(Player player) {
        if (!scoreboardManager.hasScoreboard(player)) {
            return;
        }

        // Récupérer les lignes brutes (MiniMessage)
        // ScoreboardManager va gérer PlaceholderAPI + MiniMessage via ScoreboardUtils.color()
        List<String> lines = getScoreboardLines(player);

        // Met à jour toutes les lignes via ScoreboardManager
        scoreboardManager.updateAllLines(player, lines);
    }

    /**
     * Récupère les lignes du scoreboard depuis la config
     */
    private List<String> getScoreboardLines(Player player) {
        TowerProgress progress = plugin.getTowerManager().getProgress(player.getUniqueId());
        if (progress == null || progress.getCurrentTower() == null) {
            return towersScoreboardConfig.getStringList("default.lines");
        }

        String towerId = progress.getCurrentTower();
        String linesPath = towerId + ".lines";
        
        List<String> lines = towersScoreboardConfig.getStringList(linesPath);
        if (lines.isEmpty()) {
            lines = towersScoreboardConfig.getStringList("default.lines");
        }

        return lines;
    }

    /**
     * Recharge la configuration
     */
    public void reload() {
        loadScoreboardConfig();
    }

    /**
     * Arrête toutes les tâches de mise à jour
     */
    public void shutdown() {
        for (BukkitRunnable task : updateTasks.values()) {
            task.cancel();
        }
        updateTasks.clear();
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
