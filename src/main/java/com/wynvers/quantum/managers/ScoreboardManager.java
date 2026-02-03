package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {
    
    private final Quantum plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, Scoreboard> previousBoards = new HashMap<>();
    
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
            "quantum_tower",
            "dummy",
            ScoreboardUtils.color(title)
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        int lineNumber = lines.size();
        for (String line : lines) {
            String invisibleChar = ScoreboardUtils.getInvisibleChar(lineNumber);
            
            Team team = board.registerNewTeam("line_" + lineNumber);
            team.addEntry(invisibleChar);
            team.setPrefix(ScoreboardUtils.color(line));
            
            Score score = objective.getScore(invisibleChar);
            score.setScore(lineNumber);
            
            lineNumber--;
        }
        
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }
    
    public void updateLine(Player player, int lineIndex, String newText) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        Team team = board.getTeam("line_" + lineIndex);
        if (team != null) {
            team.setPrefix(ScoreboardUtils.color(newText));
        }
    }
    
    public void updateAllLines(Player player, List<String> lines) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        int lineNumber = lines.size();
        for (String line : lines) {
            Team team = board.getTeam("line_" + lineNumber);
            if (team != null) {
                team.setPrefix(ScoreboardUtils.color(line));
            }
            lineNumber--;
        }
    }
    
    public void removeScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (plugin.getConfig().getBoolean("scoreboard.restore-on-leave", true)) {
            Scoreboard previous = previousBoards.remove(uuid);
            if (previous != null) {
                player.setScoreboard(previous);
            } else {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
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
    }
}
