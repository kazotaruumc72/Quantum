package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menus.StatisticsMenuHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande pour ouvrir le menu des statistiques de trading
 * 
 * Usage:
 * - /stats - Affiche les statistiques globales
 * - /stats today - Affiche les stats du jour
 * - /stats week - Affiche les stats de la semaine
 * - /stats month - Affiche les stats du mois
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class StatisticsCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final StatisticsMenuHandler menuHandler;
    
    public StatisticsCommand(Quantum plugin) {
        this.plugin = plugin;
        this.menuHandler = new StatisticsMenuHandler(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérifie que le sender est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Déterminer la période (optionnel)
        String period = "global";
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if (arg.equals("today") || arg.equals("aujourd'hui") || arg.equals("jour")) {
                period = "today";
            } else if (arg.equals("week") || arg.equals("semaine")) {
                period = "week";
            } else if (arg.equals("month") || arg.equals("mois")) {
                period = "month";
            } else if (!arg.equals("global")) {
                player.sendMessage("§cUsage: /stats [today|week|month|global]");
                return true;
            }
        }
        
        // Ouvrir le menu des statistiques
        menuHandler.openStatisticsMenu(player, period);
        
        return true;
    }
}
