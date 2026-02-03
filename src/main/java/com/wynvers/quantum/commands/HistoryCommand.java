package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menus.HistoryMenuHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande pour ouvrir le menu d'historique des transactions
 * 
 * Usage:
 * - /history - Affiche toutes les transactions
 * - /history buy - Affiche uniquement les achats
 * - /history sell - Affiche uniquement les ventes
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class HistoryCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final HistoryMenuHandler menuHandler;
    
    public HistoryCommand(Quantum plugin) {
        this.plugin = plugin;
        this.menuHandler = new HistoryMenuHandler(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérifie que le sender est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Déterminer le filtre
        String filter = null;
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if (arg.equals("buy") || arg.equals("achat") || arg.equals("achats")) {
                filter = "BUY";
            } else if (arg.equals("sell") || arg.equals("vente") || arg.equals("ventes")) {
                filter = "SELL";
            } else {
                player.sendMessage("§cUsage: /history [buy|sell]");
                return true;
            }
        }
        
        // Ouvrir le menu avec le filtre
        menuHandler.openHistoryMenu(player, filter, 1);
        
        return true;
    }
}
