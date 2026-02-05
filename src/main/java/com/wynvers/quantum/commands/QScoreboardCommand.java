package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.ScoreboardConfig;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QScoreboardCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final ScoreboardConfig scoreboardConfig;
    
    public QScoreboardCommand(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardConfig = plugin.getScoreboardConfig();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Vérifier si c'est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage(scoreboardConfig.getMessage("errors.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Si pas d'arguments, toggle pour le joueur lui-même
        if (args.length == 0) {
            toggleScoreboard(player, player);
            return true;
        }
        
        // Si 1 argument
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            
            // Sous-commandes
            switch (arg) {
                case "on":
                case "enable":
                case "show":
                case "activer":
                    enableScoreboard(player, player);
                    return true;
                    
                case "off":
                case "disable":
                case "hide":
                case "desactiver":
                case "désactiver":
                    disableScoreboard(player, player);
                    return true;
                    
                case "status":
                case "statut":
                    showStatus(player, player);
                    return true;
                    
                default:
                    // Peut-être un nom de joueur (admin seulement)
                    if (player.hasPermission("quantum.admin")) {
                        Player target = Bukkit.getPlayer(arg);
                        if (target == null) {
                            String msg = scoreboardConfig.getMessage("errors.player_not_found")
                                .replace("{player}", arg);
                            player.sendMessage(ScoreboardUtils.color(msg));
                            return true;
                        }
                        toggleScoreboard(player, target);
                        return true;
                    } else {
                        player.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("errors.invalid_usage")));
                        return true;
                    }
            }
        }
        
        // Si 2 arguments (admin uniquement)
        if (args.length == 2) {
            if (!player.hasPermission("quantum.admin")) {
                player.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("errors.no_permission")));
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String msg = scoreboardConfig.getMessage("errors.player_not_found")
                    .replace("{player}", args[1]);
                player.sendMessage(ScoreboardUtils.color(msg));
                return true;
            }
            
            String action = args[0].toLowerCase();
            switch (action) {
                case "on":
                case "enable":
                case "show":
                case "activer":
                    enableScoreboard(player, target);
                    return true;
                    
                case "off":
                case "disable":
                case "hide":
                case "desactiver":
                case "désactiver":
                    disableScoreboard(player, target);
                    return true;
                    
                case "status":
                case "statut":
                    showStatus(player, target);
                    return true;
                    
                default:
                    player.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("errors.invalid_action")));
                    return true;
            }
        }
        
        player.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("errors.invalid_usage")));
        return true;
    }
    
    private void toggleScoreboard(Player sender, Player target) {
        if (plugin.getScoreboardManager().isScoreboardEnabled(target)) {
            disableScoreboard(sender, target);
        } else {
            enableScoreboard(sender, target);
        }
    }
    
    private void enableScoreboard(Player sender, Player target) {
        plugin.getScoreboardManager().enableScoreboard(target);
        
        if (sender.equals(target)) {
            sender.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("scoreboard_enabled")));
        } else {
            String msg = scoreboardConfig.getMessage("admin.scoreboard_enabled")
                .replace("{player}", target.getName());
            sender.sendMessage(ScoreboardUtils.color(msg));
            target.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("scoreboard_enabled")));
        }
        
        // Si le joueur est dans une tour, réafficher le scoreboard
        if (plugin.getScoreboardHandler() != null && plugin.getTowerManager() != null) {
            TowerConfig currentTower = plugin.getTowerManager().getPlayerTower(target);
            if (currentTower != null) {
                // Le joueur est dans une tour, activer le scoreboard de tour
                plugin.getScoreboardHandler().enableTowerScoreboard(target, currentTower.getId());
            }
        }
    }
    
    private void disableScoreboard(Player sender, Player target) {
        plugin.getScoreboardManager().disableScoreboard(target);
        
        if (sender.equals(target)) {
            sender.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("scoreboard_disabled")));
        } else {
            String msg = scoreboardConfig.getMessage("admin.scoreboard_disabled")
                .replace("{player}", target.getName());
            sender.sendMessage(ScoreboardUtils.color(msg));
            target.sendMessage(ScoreboardUtils.color(scoreboardConfig.getMessage("scoreboard_disabled")));
        }
        
        // Désactiver le scoreboard de tour si actif
        if (plugin.getScoreboardHandler() != null) {
            if (plugin.getScoreboardHandler().hasTowerScoreboard(target)) {
                plugin.getScoreboardHandler().disableTowerScoreboard(target);
            }
        }
        
        // Retirer le scoreboard actuel
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().removeScoreboard(target);
        }
    }
    
    private void showStatus(Player sender, Player target) {
        boolean enabled = plugin.getScoreboardManager().isScoreboardEnabled(target);
        
        String statusMsg = enabled ? 
            scoreboardConfig.getMessage("status.enabled") : 
            scoreboardConfig.getMessage("status.disabled");
        
        if (sender.equals(target)) {
            sender.sendMessage(ScoreboardUtils.color(statusMsg));
        } else {
            String msg = statusMsg + " §7pour " + target.getName();
            sender.sendMessage(ScoreboardUtils.color(msg));
        }
    }
}
