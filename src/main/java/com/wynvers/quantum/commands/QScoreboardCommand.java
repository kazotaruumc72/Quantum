package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QScoreboardCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private static final Set<UUID> disabledPlayers = new HashSet<>();
    
    public QScoreboardCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Vérifier si c'est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c· Cette commande est réservée aux joueurs.");
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
                            player.sendMessage("§c· Joueur introuvable: " + arg);
                            return true;
                        }
                        toggleScoreboard(player, target);
                        return true;
                    } else {
                        player.sendMessage("§c· Utilisation: /qscoreboard [on|off|status]");
                        return true;
                    }
            }
        }
        
        // Si 2 arguments (admin uniquement)
        if (args.length == 2) {
            if (!player.hasPermission("quantum.admin")) {
                player.sendMessage("§c· Vous n'avez pas la permission d'utiliser cette commande pour d'autres joueurs.");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§c· Joueur introuvable: " + args[1]);
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
                    player.sendMessage("§c· Action invalide. Utilisez: on, off, status");
                    return true;
            }
        }
        
        player.sendMessage("§c· Utilisation: /qscoreboard [on|off|status] [joueur]");
        return true;
    }
    
    private void toggleScoreboard(Player sender, Player target) {
        UUID uuid = target.getUniqueId();
        
        if (disabledPlayers.contains(uuid)) {
            enableScoreboard(sender, target);
        } else {
            disableScoreboard(sender, target);
        }
    }
    
    private void enableScoreboard(Player sender, Player target) {
        UUID uuid = target.getUniqueId();
        disabledPlayers.remove(uuid);
        
        if (sender.equals(target)) {
            sender.sendMessage("§a✓ Scoreboard activé !");
        } else {
            sender.sendMessage("§a✓ Scoreboard activé pour " + target.getName());
            target.sendMessage("§a✓ Votre scoreboard a été activé par un administrateur.");
        }
        
        // Si le joueur est dans une tour, réafficher le scoreboard
        if (plugin.getScoreboardHandler() != null && plugin.getTowerManager() != null) {
            if (plugin.getTowerManager().isInTower(target)) {
                plugin.getScoreboardHandler().updateScoreboard(target);
            }
        }
    }
    
    private void disableScoreboard(Player sender, Player target) {
        UUID uuid = target.getUniqueId();
        disabledPlayers.add(uuid);
        
        if (sender.equals(target)) {
            sender.sendMessage("§c✗ Scoreboard désactivé !");
        } else {
            sender.sendMessage("§c✗ Scoreboard désactivé pour " + target.getName());
            target.sendMessage("§c✗ Votre scoreboard a été désactivé par un administrateur.");
        }
        
        // Retirer le scoreboard actuel
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().removeScoreboard(target);
        }
    }
    
    private void showStatus(Player sender, Player target) {
        UUID uuid = target.getUniqueId();
        boolean enabled = !disabledPlayers.contains(uuid);
        
        if (sender.equals(target)) {
            sender.sendMessage("§6· Statut du scoreboard: " + (enabled ? "§a✓ Activé" : "§c✗ Désactivé"));
        } else {
            sender.sendMessage("§6· Scoreboard de " + target.getName() + ": " + (enabled ? "§a✓ Activé" : "§c✗ Désactivé"));
        }
    }
    
    /**
     * Vérifie si le scoreboard est désactivé pour un joueur
     */
    public static boolean isDisabled(Player player) {
        return disabledPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Nettoie les données d'un joueur qui se déconnecte
     */
    public static void cleanup(Player player) {
        disabledPlayers.remove(player.getUniqueId());
    }
}
