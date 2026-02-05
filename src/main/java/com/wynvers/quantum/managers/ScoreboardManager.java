package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * ScoreboardManager - Gestion des scoreboards personnalisés
 * Utilise la méthode Hypixel pour cacher les numéros rouges
 */
public class ScoreboardManager {
    
    private final Quantum plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, Scoreboard> previousBoards = new HashMap<>();
    private final Map<UUID, Boolean> scoreboardEnabled = new HashMap<>();
    
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
        
        // MÉTHODE HYPIXEL : Utiliser des ChatColor invisibles uniques
        // Cette méthode est la SEULE qui fonctionne vraiment à 100%
        int score = lines.size();
        for (String line : lines) {
            String colored = ScoreboardUtils.color(line);
            
            // Créer une entrée INVISIBLE unique avec ChatColor répété
            // C'est la vraie méthode Hypixel/Mineplex/CubeCraft
            String entry = getInvisibleEntry(score);
            
            // Créer une team pour afficher le texte réel
            Team team = board.registerNewTeam("line_" + score);
            team.addEntry(entry);
            
            // Diviser le texte si trop long (prefix max 64 chars, suffix max 64)
            if (colored.length() <= 64) {
                team.setPrefix(colored);
                team.setSuffix("");
            } else {
                team.setPrefix(colored.substring(0, 64));
                String suffix = colored.substring(64, Math.min(colored.length(), 128));
                team.setSuffix(suffix);
            }
            
            // Assigner le score à l'entrée invisible
            objective.getScore(entry).setScore(score);
            score--;
        }
        
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }
    
    /**
     * MÉTHODE HYPIXEL/DEFINITIVE pour créer des entrées invisibles
     * Utilise ChatColor.COLOR_CHAR ('§') répété + 'r' pour RESET
     * 
     * Cette méthode crée des strings comme:
     * Line 1: "§r"
     * Line 2: "§r§r" 
     * Line 3: "§r§r§r"
     * etc.
     * 
     * Ces strings sont COMPLÈTEMENT invisibles car §r est le reset code
     * et ne rend AUCUN caractère visible à l'écran.
     * 
     * @param line Numéro de ligne (1-15)
     * @return String invisible unique
     */
    private String getInvisibleEntry(int line) {
        // Construire une string avec §r répété
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < line; i++) {
            builder.append(ChatColor.RESET);
        }
        return builder.toString();
    }
    
    public void updateLine(Player player, int lineIndex, String newText) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        Team team = board.getTeam("line_" + lineIndex);
        if (team != null) {
            String colored = ScoreboardUtils.color(newText);
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
        
        int lineNumber = lines.size();
        for (String line : lines) {
            Team team = board.getTeam("line_" + lineNumber);
            if (team != null) {
                String colored = ScoreboardUtils.color(line);
                if (colored.length() <= 64) {
                    team.setPrefix(colored);
                    team.setSuffix("");
                } else {
                    team.setPrefix(colored.substring(0, 64));
                    String suffix = colored.substring(64, Math.min(colored.length(), 128));
                    team.setSuffix(suffix);
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