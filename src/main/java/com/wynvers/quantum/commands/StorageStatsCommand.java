package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.statistics.StorageStatsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Commande pour afficher les statistiques globales du storage
 * Affiche : items en stock, items vendus, nombre de joueurs
 */
public class StorageStatsCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final NumberFormat numberFormat;
    
    public StorageStatsCommand(Quantum plugin) {
        this.plugin = plugin;
        this.numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StorageStatsManager statsManager = plugin.getStorageStatsManager();
        if (statsManager == null) {
            sender.sendMessage("§c⚠ StorageStatsManager non initialisé!");
            return true;
        }
        
        // Récupérer les statistiques
        StorageStatsManager.StorageStats stats = statsManager.getStorageStats();
        
        // Afficher les statistiques
        sender.sendMessage("");
        sender.sendMessage("§6§l┌──────────────────────────────────────────────┐");
        sender.sendMessage("§6§l│  §f§lSTATISTIQUES DU STORAGE  §6§l│");
        sender.sendMessage("§6§l├──────────────────────────────────────────────┤");
        sender.sendMessage("§6│");
        
        // Items actuellement en stock avec nombre de joueurs en vert lime
        sender.sendMessage("§6│ §b📋 Items Actuellement en Stock §a[§a" + formatNumber(stats.totalPlayers) + " joueurs§a]§:");
        sender.sendMessage("§6│   §7(Total de tous les joueurs)");
        sender.sendMessage("§6│   §f" + formatNumber(stats.currentStoredItems) + " items");
        sender.sendMessage("§6│");
        
        // Items vendus
        sender.sendMessage("§6│ §a💰 Total Items Vendus:");
        sender.sendMessage("§6│   §7(Vendus via /sell)");
        sender.sendMessage("§6│   §f" + formatNumber(stats.totalItemsSold) + " items");
        sender.sendMessage("§6│");
        
        sender.sendMessage("§6§l└──────────────────────────────────────────────┘");
        sender.sendMessage("§7§oUtilise: /quantum stats pour les stats par catégorie");
        sender.sendMessage("");
        
        return true;
    }
    
    /**
     * Formate un nombre avec séparateurs de milliers
     */
    private String formatNumber(long number) {
        return numberFormat.format(number);
    }
}
