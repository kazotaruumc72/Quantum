package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.statistics.StatisticsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Commande pour afficher les statistiques de Quantum
 * Usage: /quantum stats [category]
 */
public class StatsCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final NumberFormat numberFormat;
    
    public StatsCommand(Quantum plugin) {
        this.plugin = plugin;
        this.numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StatisticsManager statsManager = plugin.getStatisticsManager();
        if (statsManager == null) {
            sender.sendMessage("§c⚠ StatisticsManager non initialisé!");
            return true;
        }
        
        // Si aucune catégorie spécifiée, afficher les stats globales
        if (args.length == 0) {
            displayGlobalStats(sender, statsManager);
            return true;
        }
        
        // Si une catégorie est spécifiée
        String category = args[0].toLowerCase();
        
        // Commandes spéciales
        if (category.equals("all") || category.equals("list")) {
            displayAllCategories(sender, statsManager);
            return true;
        }
        
        if (category.equals("reload") || category.equals("recalculate")) {
            if (!sender.hasPermission("quantum.admin")) {
                sender.sendMessage("§c⚠ Vous n'avez pas la permission!");
                return true;
            }
            
            sender.sendMessage("§e⏳ Recalcul des statistiques en cours...");
            statsManager.recalculateAll();
            sender.sendMessage("§a✓ Statistiques recalculées avec succès!");
            return true;
        }
        
        // Afficher les stats d'une catégorie spécifique
        displayCategoryStats(sender, statsManager, category);
        return true;
    }
    
    /**
     * Affiche les statistiques globales
     */
    private void displayGlobalStats(CommandSender sender, StatisticsManager statsManager) {
        StatisticsManager.GlobalStats global = statsManager.getGlobalStats();
        
        sender.sendMessage("");
        sender.sendMessage("§6§l┌──────────────────────────────────────┐");
        sender.sendMessage("§6§l│    §f§lSTATISTIQUES GLOBALES     §6§l│");
        sender.sendMessage("§6§l├──────────────────────────────────────┤");
        sender.sendMessage("§6│");
        sender.sendMessage("§6│ §e📦 Items Stockés:");
        sender.sendMessage("§6│   §f" + formatNumber(global.totalItemsStored) + " items");
        sender.sendMessage("§6│");
        sender.sendMessage("§6│ §b📊 Trades Créés:");
        sender.sendMessage("§6│   §f" + formatNumber(global.totalTradesCreated) + " offres");
        sender.sendMessage("§6│");
        sender.sendMessage("§6│ §a✓ Trades Complétés:");
        sender.sendMessage("§6│   §f" + formatNumber(global.totalTradesCompleted) + " trades");
        sender.sendMessage("§6│");
        sender.sendMessage("§6│ §d🔁 Volume Total Échangé:");
        sender.sendMessage("§6│   §f" + formatNumber(global.totalVolumeTraded) + " items");
        sender.sendMessage("§6│");
        sender.sendMessage("§6§l└──────────────────────────────────────┘");
        sender.sendMessage("§7§oUtilise: /quantum stats list pour voir par catégorie");
        sender.sendMessage("");
    }
    
    /**
     * Affiche toutes les catégories et leurs stats
     */
    private void displayAllCategories(CommandSender sender, StatisticsManager statsManager) {
        Map<String, StatisticsManager.CategoryStats> allStats = statsManager.getAllCategoryStats();
        
        if (allStats.isEmpty()) {
            sender.sendMessage("§e§l⚠ Aucune statistique disponible");
            sender.sendMessage("§7Ajoutez des items au storage pour commencer à tracker!");
            return;
        }
        
        sender.sendMessage("");
        sender.sendMessage("§6§l┌──────────────────────────────────────────────┐");
        sender.sendMessage("§6§l│  §f§lSTATISTIQUES PAR CATÉGORIE  §6§l│");
        sender.sendMessage("§6§l├──────────────────────────────────────────────┤");
        
        for (Map.Entry<String, StatisticsManager.CategoryStats> entry : allStats.entrySet()) {
            String category = entry.getKey();
            StatisticsManager.CategoryStats stats = entry.getValue();
            
            sender.sendMessage("§6│");
            sender.sendMessage("§6│ §e§l➤ " + formatCategoryName(category));
            sender.sendMessage("§6│   §7Stockés: §f" + formatNumber(stats.itemsStored));
            sender.sendMessage("§6│   §7Créés: §f" + formatNumber(stats.tradesCreated));
            sender.sendMessage("§6│   §7Complétés: §f" + formatNumber(stats.tradesCompleted));
            sender.sendMessage("§6│   §7Volume: §f" + formatNumber(stats.volumeTraded));
        }
        
        sender.sendMessage("§6│");
        sender.sendMessage("§6§l└──────────────────────────────────────────────┘");
        sender.sendMessage("§7§oTotal: §f" + allStats.size() + " §7catégories");
        sender.sendMessage("");
    }
    
    /**
     * Affiche les stats d'une catégorie spécifique
     */
    private void displayCategoryStats(CommandSender sender, StatisticsManager statsManager, String category) {
        StatisticsManager.CategoryStats stats = statsManager.getCategoryStats(category);
        
        sender.sendMessage("");
        sender.sendMessage("§6§l┌──────────────────────────────────────┐");
        sender.sendMessage("§6§l│   §f§l" + formatCategoryName(category).toUpperCase() + "   §6§l│");
        sender.sendMessage("§6§l├──────────────────────────────────────┤");
        sender.sendMessage("§6│");
        sender.sendMessage("§6│ §e📦 Items Stockés: §f" + formatNumber(stats.itemsStored));
        sender.sendMessage("§6│ §b📊 Trades Créés: §f" + formatNumber(stats.tradesCreated));
        sender.sendMessage("§6│ §a✓ Trades Complétés: §f" + formatNumber(stats.tradesCompleted));
        sender.sendMessage("§6│ §d🔁 Volume Échangé: §f" + formatNumber(stats.volumeTraded));
        sender.sendMessage("§6│");
        sender.sendMessage("§6§l└──────────────────────────────────────┘");
        sender.sendMessage("");
    }
    
    /**
     * Formate un nom de catégorie pour l'affichage
     */
    private String formatCategoryName(String category) {
        return category.substring(0, 1).toUpperCase() + category.substring(1);
    }
    
    /**
     * Formate un nombre avec séparateurs de milliers
     */
    private String formatNumber(long number) {
        return numberFormat.format(number);
    }
}
