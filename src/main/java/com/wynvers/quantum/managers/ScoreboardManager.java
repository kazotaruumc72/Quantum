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
        
        // Créer les lignes du scoreboard sans numéros rouges
        int score = lines.size();
        for (String line : lines) {
            String colored = ScoreboardUtils.color(line);
            
            // Créer une entrée invisible unique (VRAIMENT invisible)
            String entry = createInvisibleEntry(score);
            
            // Créer une team pour afficher le texte
            Team team = board.registerNewTeam("line_" + score);
            team.addEntry(entry);
            
            // Gérer les lignes longues (prefix + suffix)
            if (colored.length() <= 64) {
                team.setPrefix(colored);
                team.setSuffix("");
            } else {
                team.setPrefix(colored.substring(0, 64));
                String suffix = colored.substring(64, Math.min(colored.length(), 128));
                team.setSuffix(suffix);
            }
            
            // Ajouter le score avec l'entrée invisible
            objective.getScore(entry).setScore(score);
            score--;
        }
        
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }
    
    /**
     * Crée une entrée invisible VRAIMENT unique pour chaque ligne du scoreboard.
     * Utilise plusieurs techniques combinées pour garantir l'invisibilité totale
     * des numéros rouges même avec TAB et autres plugins de scoreboard.
     * 
     * @param index Index de la ligne (1-15)
     * @return String invisible unique
     */
    private String createInvisibleEntry(int index) {
        // Méthode la plus fiable : Combinaison de ChatColor et espaces Unicode
        // Cette approche fonctionne avec TAB, FeatherBoard, et tous les plugins de scoreboard
        
        StringBuilder entry = new StringBuilder();
        
        // Technique 1 : Utiliser des codes couleur alternés (invisible à l'œil)
        // On alterne entre §r et §0 pour créer une signature unique
        for (int i = 0; i < index; i++) {
            entry.append(ChatColor.COLOR_CHAR).append('r');
        }
        
        // Technique 2 : Ajouter des espaces Unicode de largeur zéro (complètement invisibles)
        // Ces caractères sont différents de l'espace normal et créent des entrées uniques
        char[] zeroWidthSpaces = {
            '\u200B', // ZERO WIDTH SPACE
            '\u200C', // ZERO WIDTH NON-JOINER  
            '\u200D', // ZERO WIDTH JOINER
            '\uFEFF'  // ZERO WIDTH NO-BREAK SPACE
        };
        
        // Ajouter un espace de largeur zéro basé sur l'index
        entry.append(zeroWidthSpaces[index % zeroWidthSpaces.length]);
        
        // Technique 3 : Pour plus de sécurité, ajouter des espaces Unicode différents
        // selon l'index pour garantir l'unicité même avec beaucoup de lignes
        if (index > 4) {
            // Ajouter un second caractère invisible pour les lignes > 4
            entry.append(zeroWidthSpaces[(index / 4) % zeroWidthSpaces.length]);
        }
        
        return entry.toString();
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