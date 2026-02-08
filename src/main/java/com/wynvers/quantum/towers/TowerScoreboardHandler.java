package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.*;

/**
 * Gère le scoreboard personnalisé pour les tours avec support MiniMessage
 * 
 * Fonctionnalités:
 * - Scoreboard automatique quand joueur entre dans une tour
 * - Désactive Oreo Essentials avec /sb
 * - Mise à jour en temps réel (configurable)
 * - Restaure Oreo Essentials à la sortie
 * - Support complet de MiniMessage pour les couleurs et formats
 * - Pas de numéros rouges visibles (entries invisibles)
 */
public class TowerScoreboardHandler {
    
    private final Quantum plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, Scoreboard> originalScoreboards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    private final Set<UUID> playersInTower;
    private final MiniMessage miniMessage;
    
    private FileConfiguration towersScoreboardConfig;
    
    public TowerScoreboardHandler(Quantum plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.originalScoreboards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        this.playersInTower = new HashSet<>();
        this.miniMessage = MiniMessage.miniMessage();
        
        loadScoreboardConfig();
    }
    
    /**
     * Charge la configuration des scoreboards de tours
     */
    private void loadScoreboardConfig() {
        File configFile = new File(plugin.getDataFolder(), "towers_scoreboard.yml");
        if (!configFile.exists()) {
            plugin.saveResource("towers_scoreboard.yml", false);
        }
        towersScoreboardConfig = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Recharge la configuration des scoreboards
     */
    public void reload() {
        loadScoreboardConfig();
        // Rafraîchir les scoreboards actifs
        for (UUID uuid : new HashSet<>(playersInTower)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updateScoreboard(player);
            }
        }
    }
    
    /**
     * Active le scoreboard de tour pour un joueur
     * @param player Joueur
     * @param towerId ID de la tour
     */
    public void enableTowerScoreboard(Player player, String towerId) {
        // Sauvegarder le scoreboard d'origine
        originalScoreboards.put(player.getUniqueId(), player.getScoreboard());
        
        // Désactiver Oreo Essentials scoreboard
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sb " + player.getName());
        
        // Créer le scoreboard personnalisé
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getNewScoreboard();
        
        // Récupérer le titre depuis la config avec MiniMessage
        String titlePath = towerId + ".title";
        String titleMM = towersScoreboardConfig.getString(titlePath, 
                towersScoreboardConfig.getString("default.title", "<gold><bold>TOURS</bold></gold>"));
        
        Component titleComponent = miniMessage.deserialize(titleMM);
        String titleLegacy = LegacyComponentSerializer.legacySection().serialize(titleComponent);
        
        Objective objective = scoreboard.registerNewObjective("tower", "dummy", titleLegacy);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Appliquer le scoreboard
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        playersInTower.add(player.getUniqueId());
        
        // Démarrer la tâche de mise à jour
        startUpdateTask(player, towerId);
        
        plugin.getQuantumLogger().info("Tower scoreboard enabled for " + player.getName());
    }
    
    /**
     * Désactive le scoreboard de tour et restaure le scoreboard d'origine
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
        
        // Restaurer le scoreboard d'origine
        Scoreboard originalScoreboard = originalScoreboards.remove(uuid);
        if (originalScoreboard != null) {
            player.setScoreboard(originalScoreboard);
        } else {
            // Si pas de scoreboard sauvegardé, réactiver Oreo Essentials
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sb " + player.getName());
        }
        
        plugin.getQuantumLogger().info("Tower scoreboard disabled for " + player.getName());
    }
    
    /**
     * Démarre la tâche de mise à jour du scoreboard
     * @param player Joueur
     * @param towerId ID de la tour
     */
    private void startUpdateTask(Player player, String towerId) {
        // Récupérer l'interval depuis la config
        int interval = towersScoreboardConfig.getInt(towerId + ".update_interval",
                towersScoreboardConfig.getInt("default.update_interval", 20));
        
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
        
        task.runTaskTimer(plugin, 0L, interval);
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
        
        // Supprimer toutes les anciennes teams
        for (Team team : new HashSet<>(scoreboard.getTeams())) {
            team.unregister();
        }
        
        // Effacer les anciennes lignes
        for (String entry : new HashSet<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }
        
        // Récupérer les lignes
        List<String> lines = getScoreboardLines(player);
        
        // Ajouter les lignes avec Teams (de bas en haut)
        int lineNumber = lines.size();
        for (String line : lines) {
            // Remplacer les placeholders PlaceholderAPI
            line = PlaceholderAPI.setPlaceholders(player, line);
            
            // Convertir MiniMessage -> Legacy
            Component component = miniMessage.deserialize(line);
            String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
            
            // Créer une entrée INVISIBLE unique (pas de numéros rouges)
            String entry = generateInvisibleEntry(lineNumber);
            
            // Créer une Team pour cette ligne
            Team team = scoreboard.registerNewTeam("line_" + lineNumber);
            team.addEntry(entry);
            
            // Le texte est dans le prefix/suffix de la Team
            if (legacyText.length() <= 64) {
                team.setPrefix(legacyText);
            } else {
                // Si trop long, couper en prefix + suffix
                team.setPrefix(legacyText.substring(0, 64));
                if (legacyText.length() > 64) {
                    String suffix = legacyText.substring(64, Math.min(legacyText.length(), 128));
                    team.setSuffix(suffix);
                }
            }
            
            // Ajouter le score (ordre d'affichage)
            Score score = objective.getScore(entry);
            score.setScore(lineNumber);
            
            lineNumber--;
        }
    }
    
    /**
     * Génère une entrée invisible unique pour une ligne
     * Utilise des caractères de formatage invisibles
     */
    private String generateInvisibleEntry(int line) {
        // Utiliser des codes de couleur répétés + RESET pour créer des entrées uniques invisibles
        StringBuilder entry = new StringBuilder();
        String[] colors = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", 
                          "§a", "§b", "§c", "§d", "§e", "§f"};
        
        int index = line % colors.length;
        for (int i = 0; i < (line / colors.length) + 1; i++) {
            entry.append(colors[index]);
        }
        entry.append("§r"); // Reset pour rendre invisible
        
        return entry.toString();
    }
    
    /**
     * Obtient les lignes du scoreboard configurées depuis towers_scoreboard.yml
     * @param player Joueur
     * @return Liste des lignes
     */
    private List<String> getScoreboardLines(Player player) {
        TowerManager towerManager = plugin.getTowerManager();
        if (towerManager == null) {
            return Collections.singletonList("<red>Erreur: TowerManager non chargé</red>");
        }
        
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String towerId = progress.getCurrentTower();
        
        if (towerId == null) {
            towerId = "default";
        }
        
        // Chercher les lignes pour cette tour spécifique, sinon fallback sur default
        List<String> lines = towersScoreboardConfig.getStringList(towerId + ".lines");
        if (lines.isEmpty()) {
            lines = towersScoreboardConfig.getStringList("default.lines");
        }
        
        // Si toujours vide, retourner un message d'erreur
        if (lines.isEmpty()) {
            return Arrays.asList(
                "<red>Erreur: Aucune ligne configurée</red>",
                "<gray>Vérifiez towers_scoreboard.yml</gray>"
            );
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
        originalScoreboards.remove(uuid);
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
        originalScoreboards.clear();
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
