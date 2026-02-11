package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerConfig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TowerScoreboardManager {
    
    private final Quantum plugin;
    private final ScoreboardManager scoreboardManager;
    
    public TowerScoreboardManager(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
    }
    
    public void showTowerScoreboard(Player player, TowerConfig tower) {
        List<String> lines = tower.getScoreboardLines();
        
        if (lines == null || lines.isEmpty()) {
            return;
        }
        
        // Pass raw lines - ScoreboardManager will handle PlaceholderAPI and MiniMessage
        String title = plugin.getConfig().getString("scoreboard.title", "&6&lTOURS");
        scoreboardManager.setScoreboard(player, title, lines);
        
        startAutoUpdate(player);
    }
    
    private void startAutoUpdate(Player player) {
        int updateInterval = plugin.getConfig().getInt("scoreboard.update-interval", 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!scoreboardManager.hasScoreboard(player) || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                TowerConfig tower = plugin.getTowerManager().getPlayerTower(player);
                if (tower != null) {
                    List<String> lines = tower.getScoreboardLines();
                    // Pass raw lines - ScoreboardManager will handle PlaceholderAPI and MiniMessage
                    scoreboardManager.updateAllLines(player, lines);
                }
            }
        }.runTaskTimer(plugin, updateInterval, updateInterval);
    }
    
    public void hideTowerScoreboard(Player player) {
        scoreboardManager.removeScoreboard(player);
    }
}
