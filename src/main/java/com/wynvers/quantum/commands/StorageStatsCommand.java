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
 * Affiche : total items stockés, items vendus, items actuellement en stock
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
        
        // Items stockés historiquement
        sender.sendMessage("§6│ §e📦 Total Items Stockés:");
        sender.sendMessage("§6│   §7(Historique depuis toujours)");
        sender.sendMessage("§6│   §f" + formatNumber(stats.totalItemsStored) + " items");
        sender.sendMessage("§6│");
        
        // Items actuellement en stock
        sender.sendMessage("§6│ §b📋 Items Actuellement en Stock:");
        sender.sendMessage("§6│   §7(Total de tous les joueurs)");
        sender.sendMessage("§6│   §f" + formatNumber(stats.currentStoredItems) + " items");
        sender.sendMessage("§6│");
        
        // Items vendus
        sender.sendMessage("§6│ §a💰 Total Items Vendus:");
        sender.sendMessage("§6│   §7(Vendus via /sell)");
        sender.sendMessage("§6│   §f" + formatNumber(stats.totalItemsSold) + " items");
        sender.sendMessage("§6│");
        
        // Taux de vente (si applicable)
        if (stats.totalItemsStored > 0) {
            double sellRate = ((double) stats.totalItemsSold / stats.totalItemsStored) * 100;
            sender.sendMessage("§6│ §d📊 Taux de Vente:");
            sender.sendMessage("§6│   §f" + String.format("%.2f", sellRate) + "%");
            sender.sendMessage("§6│");
        }
        
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
