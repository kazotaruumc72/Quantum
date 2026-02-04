package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

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
        
        // Utiliser des Teams pour afficher le texte sans les numéros rouges
        int lineNumber = lines.size();
        for (String line : lines) {
            // Créer une entrée invisible unique pour chaque ligne
            String entry = getInvisibleString(lineNumber);
            
            // Créer une Team pour cette ligne
            Team team = board.registerNewTeam("line_" + lineNumber);
            team.addEntry(entry);
            
            // Le texte est dans le prefix/suffix de la Team (max 64 chars chacun)
            String colored = ScoreboardUtils.color(line);
            if (colored.length() <= 64) {
                team.setPrefix(colored);
            } else {
                // Si trop long, couper en prefix + suffix
                team.setPrefix(colored.substring(0, 64));
                if (colored.length() > 64) {
                    String suffix = colored.substring(64, Math.min(colored.length(), 128));
                    team.setSuffix(suffix);
                }
            }
            
            // Ajouter le score (l'entrée invisible)
            Score score = objective.getScore(entry);
            score.setScore(lineNumber);
            
            lineNumber--;
        }
        
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }
    
    /**
     * Génère une chaîne invisible unique pour chaque ligne
     * Utilise des codes couleur répétés qui sont invisibles
     */
    private String getInvisibleString(int index) {
        // Utiliser des codes couleur ChatColor.RESET répétés
        // Chaque ligne aura un nombre différent de reset pour être unique
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index; i++) {
            sb.append(ChatColor.RESET);
        }
        return sb.toString();
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
                if (colored.length() > 64) {
                    String suffix = colored.substring(64, Math.min(colored.length(), 128));
                    team.setSuffix(suffix);
                }
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
                    if (colored.length() > 64) {
                        String suffix = colored.substring(64, Math.min(colored.length(), 128));
                        team.setSuffix(suffix);
                    }
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
