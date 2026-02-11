package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.utils.ScoreboardUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScoreboardManager - Gestion des scoreboards personnalisés
 * Utilise NumberFormat.blank() de Paper 1.20.3+ pour cacher les nombres rouges
 * Compatible avec Minecraft 1.21.1+
 */
public class ScoreboardManager {
    
    private final Quantum plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, Scoreboard> previousBoards = new HashMap<>();
    private final Map<UUID, Boolean> scoreboardEnabled = new HashMap<>();
    
    // Cache pour les lignes parsées et colorées par joueur - Thread-safe
    // Key: UUID du joueur, Value: Map<lineNumber, parsedAndColoredText>
    private final Map<UUID, Map<Integer, String>> lineCache = new ConcurrentHashMap<>();
    
    public ScoreboardManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    public void setScoreboard(Player player, String title, List<String> lines) {
        if (!playerBoards.containsKey(player.getUniqueId())) {
            previousBoards.put(player.getUniqueId(), player.getScoreboard());
        }
        
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard board = manager.getNewScoreboard();
        
        Objective objective = board.registerNewObjective(
            "quantum_board",
            "dummy",
            ScoreboardUtils.color(title)
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // MÉTHODE MODERNE PAPER 1.20.3+ / 1.21+
        // Utiliser NumberFormat.blank() pour supprimer complètement les nombres
        int score = lines.size();
        for (String line : lines) {
            // Parse les placeholders PUIS applique les couleurs
            String parsedLine = PlaceholderAPI.setPlaceholders(player, line);
            String colored = ScoreboardUtils.color(parsedLine);
            
            // Créer une entrée unique (utilise des espaces invisibles)
            String entry = getUniqueEntry(score);
            
            // Créer une team pour afficher le texte
            Team team = board.registerNewTeam("line_" + score);
            team.addEntry(entry);
            
            // Diviser le texte si trop long
            if (colored.length() <= 64) {
                team.setPrefix(colored);
                team.setSuffix("");
            } else {
                team.setPrefix(colored.substring(0, 64));
                String suffix = colored.substring(64, Math.min(colored.length(), 128));
                team.setSuffix(suffix);
            }
            
            // Assigner le score avec NumberFormat.blank() pour masquer le nombre rouge
            Score scoreEntry = objective.getScore(entry);
            scoreEntry.setScore(score);
            
            // ✨ LA CLÉ : NumberFormat.blank() de Paper API 1.20.3+
            // Cela cache complètement le nombre rouge sur le scoreboard
            scoreEntry.numberFormat(NumberFormat.blank());
            
            score--;
        }
        
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }
    
    /**
     * Crée une entrée unique pour chaque ligne du scoreboard
     * Utilise des espaces (invisibles) pour rendre chaque ligne unique
     * 
     * @param lineNumber Numéro de ligne (1-15)
     * @return String unique pour cette ligne
     */
    private String getUniqueEntry(int lineNumber) {
        // Utiliser des espaces pour créer des entrées uniques
        // Chaque ligne aura un nombre différent d'espaces
        return " ".repeat(lineNumber);
    }
    
    public void updateLine(Player player, int lineIndex, String newText) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        Team team = board.getTeam("line_" + lineIndex);
        if (team != null) {
            // Parse les placeholders PUIS applique les couleurs
            String parsedText = PlaceholderAPI.setPlaceholders(player, newText);
            String colored = ScoreboardUtils.color(parsedText);
            if (colored.length() <= 64) {
                team.setPrefix(colored);
                team.setSuffix("");
            } else {
                team.setPrefix(colored.substring(0, 64));
                String suffix = colored.substring(64, Math.min(colored.length(), 128));
                team.setSuffix(suffix);
            }
        }
    }
    
    public void updateAllLines(Player player, List<String> lines) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        UUID uuid = player.getUniqueId();
        Map<Integer, String> cachedLines = lineCache.computeIfAbsent(uuid, k -> new HashMap<>());
        
        // Batch parse all placeholders at once to reduce PlaceholderAPI overhead
        StringBuilder batchText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) batchText.append("\n");
            batchText.append(lines.get(i));
        }
        
        // Single PlaceholderAPI call for all lines
        String parsedBatch = PlaceholderAPI.setPlaceholders(player, batchText.toString());
        // Normalize line endings and split - handle both \r\n and \n
        String[] parsedLines = parsedBatch.replace("\r\n", "\n").split("\n", -1);
        
        // Ensure we got the expected number of lines
        if (parsedLines.length != lines.size()) {
            // Fallback to per-line parsing if batch parsing fails
            parsedLines = new String[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                parsedLines[i] = PlaceholderAPI.setPlaceholders(player, lines.get(i));
            }
        }
        
        int lineNumber = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            Team team = board.getTeam("line_" + lineNumber);
            if (team != null) {
                // Apply colors to the already parsed line
                String colored = ScoreboardUtils.color(parsedLines[i]);
                
                // Vérifier si la ligne a changé depuis la dernière mise à jour
                String cachedLine = cachedLines.get(lineNumber);
                if (cachedLine == null || !cachedLine.equals(colored)) {
                    // La ligne a changé, mettre à jour
                    if (colored.length() <= 64) {
                        team.setPrefix(colored);
                        team.setSuffix("");
                    } else {
                        team.setPrefix(colored.substring(0, 64));
                        String suffix = colored.substring(64, Math.min(colored.length(), 128));
                        team.setSuffix(suffix);
                    }
                    
                    // Mettre à jour le cache
                    cachedLines.put(lineNumber, colored);
                }
            }
            lineNumber--;
        }
    }
    
    public void removeScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        
        Scoreboard previous = previousBoards.remove(uuid);
        if (previous != null) {
            player.setScoreboard(previous);
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        
        playerBoards.remove(uuid);
    }
    
    public boolean hasScoreboard(Player player) {
        return playerBoards.containsKey(player.getUniqueId());
    }
    
    public void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        playerBoards.remove(uuid);
        previousBoards.remove(uuid);
        scoreboardEnabled.remove(uuid);
        lineCache.remove(uuid); // Nettoyer aussi le cache des lignes
    }
    
    /**
     * Enable scoreboard for a player
     */
    public void enableScoreboard(Player player) {
        scoreboardEnabled.put(player.getUniqueId(), true);
    }
    
    /**
     * Disable scoreboard for a player
     */
    public void disableScoreboard(Player player) {
        scoreboardEnabled.put(player.getUniqueId(), false);
        removeScoreboard(player);
    }
    
    /**
     * Check if scoreboard is enabled for a player
     */
    public boolean isScoreboardEnabled(Player player) {
        return scoreboardEnabled.getOrDefault(player.getUniqueId(), true);
    }
}
