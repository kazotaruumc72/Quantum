package com.wynvers.quantum.menus;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.transactions.Transaction;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for statistics menu
 * Manages trading statistics display with different time periods
 */
public class StatisticsMenuHandler {
    
    private final Quantum plugin;
    private final Map<UUID, StatisticsSession> sessions;
    
    public StatisticsMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }
    
    /**
     * Open statistics menu for player
     */
    public void openStatisticsMenu(Player player, String period) {
        StatisticsSession session = getSession(player);
        session.setPeriod(period);
        
        // Open the menu using the menu manager
        // This assumes you have a menu configuration for statistics
        plugin.getLogger().info("[STATISTICS] Opening menu for " + player.getName() + " (period: " + period + ")");
    }
    
    /**
     * Get or create session for player
     */
    public StatisticsSession getSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), uuid -> new StatisticsSession());
    }
    
    /**
     * Get placeholders for statistics menu
     */
    public Map<String, String> getPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        StatisticsSession session = getSession(player);
        TransactionHistoryManager historyManager = plugin.getTransactionHistoryManager();
        
        // Get filtered transactions based on period
        List<Transaction> allTransactions = historyManager.getPlayerTransactions(player.getUniqueId());
        List<Transaction> filteredTransactions = filterByPeriod(allTransactions, session.getPeriod());
        
        // Buy/Sell transactions
        List<Transaction> buyTransactions = filteredTransactions.stream()
            .filter(Transaction::isBuyOrder)
            .collect(Collectors.toList());
        List<Transaction> sellTransactions = filteredTransactions.stream()
            .filter(Transaction::isSellOrder)
            .collect(Collectors.toList());
        
        // Calculate totals
        double buyTotal = buyTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
        double sellTotal = sellTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
        double netProfit = sellTotal - buyTotal;
        
        // Period info
        placeholders.put("%quantum_stats_period%", getPeriodDisplayName(session.getPeriod()));
        placeholders.put("%quantum_stats_period_key%", session.getPeriod());
        
        // Transaction counts
        placeholders.put("%quantum_stats_total_transactions%", String.valueOf(filteredTransactions.size()));
        placeholders.put("%quantum_stats_buy_count%", String.valueOf(buyTransactions.size()));
        placeholders.put("%quantum_stats_sell_count%", String.valueOf(sellTransactions.size()));
        
        // Money stats
        placeholders.put("%quantum_stats_buy_total%", String.format("%.2f", buyTotal));
        placeholders.put("%quantum_stats_sell_total%", String.format("%.2f", sellTotal));
        placeholders.put("%quantum_stats_net_profit%", String.format("%.2f", netProfit));
        placeholders.put("%quantum_stats_profit_color%", netProfit >= 0 ? "<green>" : "<red>");
        
        // Average prices
        double avgBuyPrice = buyTransactions.isEmpty() ? 0 : buyTotal / buyTransactions.size();
        double avgSellPrice = sellTransactions.isEmpty() ? 0 : sellTotal / sellTransactions.size();
        placeholders.put("%quantum_stats_avg_buy_price%", String.format("%.2f", avgBuyPrice));
        placeholders.put("%quantum_stats_avg_sell_price%", String.format("%.2f", avgSellPrice));
        
        // Most traded items
        placeholders.put("%quantum_stats_most_bought%", getMostTradedItem(buyTransactions));
        placeholders.put("%quantum_stats_most_sold%", getMostTradedItem(sellTransactions));
        
        // Period buttons status
        placeholders.put("%quantum_stats_global_status%", 
            session.getPeriod().equals("global") ? "<green>▶ Actif</green>" : "<gray>Cliquez</gray>");
        placeholders.put("%quantum_stats_month_status%", 
            session.getPeriod().equals("month") ? "<green>▶ Actif</green>" : "<gray>Cliquez</gray>");
        placeholders.put("%quantum_stats_week_status%", 
            session.getPeriod().equals("week") ? "<green>▶ Actif</green>" : "<gray>Cliquez</gray>");
        placeholders.put("%quantum_stats_today_status%", 
            session.getPeriod().equals("today") ? "<green>▶ Actif</green>" : "<gray>Cliquez</gray>");
        
        return placeholders;
    }
    
    /**
     * Filter transactions by time period
     */
    private List<Transaction> filterByPeriod(List<Transaction> transactions, String period) {
        if (period.equals("global")) {
            return transactions;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff;
        
        switch (period) {
            case "today":
                cutoff = now.truncatedTo(ChronoUnit.DAYS);
                break;
            case "week":
                cutoff = now.minusWeeks(1);
                break;
            case "month":
                cutoff = now.minusMonths(1);
                break;
            default:
                return transactions;
        }
        
        long cutoffTimestamp = cutoff.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        return transactions.stream()
            .filter(t -> t.getTimestamp() >= cutoffTimestamp)
            .collect(Collectors.toList());
    }
    
    /**
     * Get display name for period
     */
    private String getPeriodDisplayName(String period) {
        switch (period) {
            case "today":
                return "Aujourd'hui";
            case "week":
                return "Cette semaine";
            case "month":
                return "Ce mois";
            case "global":
            default:
                return "Toutes périodes";
        }
    }
    
    /**
     * Get most traded item from transaction list
     */
    private String getMostTradedItem(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "Aucun";
        }
        
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Transaction transaction : transactions) {
            String itemId = transaction.getItemId();
            itemCounts.put(itemId, itemCounts.getOrDefault(itemId, 0) + transaction.getQuantity());
        }
        
        return itemCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> formatItemName(e.getKey()))
            .orElse("Aucun");
    }
    
    /**
     * Format item name for display
     */
    private String formatItemName(String itemId) {
        String name = itemId;
        if (name.startsWith("nexo:")) {
            name = name.substring(5);
        } else if (name.startsWith("minecraft:")) {
            name = name.substring(10);
        }
        return name.replace("_", " ");
    }
    
    /**
     * Clear session for player
     */
    public void clearSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
    
    /**
     * Statistics session class
     */
    public static class StatisticsSession {
        private String period = "global";
        
        public String getPeriod() {
            return period;
        }
        
        public void setPeriod(String period) {
            this.period = period;
        }
    }
}
