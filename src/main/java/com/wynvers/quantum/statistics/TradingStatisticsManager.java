package com.wynvers.quantum.statistics;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.transactions.TransactionHistoryManager.Transaction;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère les statistiques de trading pour les joueurs
 * 
 * Fonctionnalités:
 * - Statistiques globales (total achats/ventes, profit, etc.)
 * - Statistiques par période (aujourd'hui, cette semaine, ce mois)
 * - Items les plus échangés
 * - Partenaires commerciaux les plus fréquents
 * - Graphiques de tendances
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class TradingStatisticsManager {
    
    private final Quantum plugin;
    private final TransactionHistoryManager historyManager;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    
    public TradingStatisticsManager(Quantum plugin) {
        this.plugin = plugin;
        this.historyManager = plugin.getTransactionHistoryManager();
    }
    
    /**
     * Récupère les statistiques globales d'un joueur
     */
    public PlayerStatistics getGlobalStatistics(Player player) {
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, null, 0);
        
        double totalBuy = 0;
        double totalSell = 0;
        int buyCount = 0;
        int sellCount = 0;
        
        for (Transaction transaction : allTransactions) {
            if (transaction.playerRole == TransactionHistoryManager.TransactionRole.BUYER) {
                totalBuy += transaction.totalPrice;
                buyCount++;
            } else {
                totalSell += transaction.totalPrice;
                sellCount++;
            }
        }
        
        double netProfit = totalSell - totalBuy;
        int totalTransactions = allTransactions.size();
        
        return new PlayerStatistics(
            totalBuy,
            totalSell,
            netProfit,
            buyCount,
            sellCount,
            totalTransactions
        );
    }
    
    /**
     * Récupère les statistiques pour une période donnée
     * 
     * @param period Période (TODAY, WEEK, MONTH)
     */
    public PlayerStatistics getPeriodStatistics(Player player, TimePeriod period) {
        long periodStart = getPeriodStartTimestamp(period);
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, null, 0);
        
        // Filtrer les transactions de la période
        List<Transaction> periodTransactions = allTransactions.stream()
            .filter(t -> t.timestamp >= periodStart)
            .collect(Collectors.toList());
        
        double totalBuy = 0;
        double totalSell = 0;
        int buyCount = 0;
        int sellCount = 0;
        
        for (Transaction transaction : periodTransactions) {
            if (transaction.playerRole == TransactionHistoryManager.TransactionRole.BUYER) {
                totalBuy += transaction.totalPrice;
                buyCount++;
            } else {
                totalSell += transaction.totalPrice;
                sellCount++;
            }
        }
        
        double netProfit = totalSell - totalBuy;
        int totalTransactions = periodTransactions.size();
        
        return new PlayerStatistics(
            totalBuy,
            totalSell,
            netProfit,
            buyCount,
            sellCount,
            totalTransactions
        );
    }
    
    /**
     * Récupère le timestamp de début d'une période
     */
    private long getPeriodStartTimestamp(TimePeriod period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        switch (period) {
            case TODAY:
                return calendar.getTimeInMillis();
            case WEEK:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                return calendar.getTimeInMillis();
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return calendar.getTimeInMillis();
            default:
                return 0;
        }
    }
    
    /**
     * Récupère les items les plus échangés (achats + ventes)
     * 
     * @param limit Nombre maximum d'items à retourner
     */
    public List<ItemStatistic> getMostTradedItems(Player player, int limit) {
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, null, 0);
        
        Map<String, ItemStatistic> itemStats = new HashMap<>();
        
        for (Transaction transaction : allTransactions) {
            ItemStatistic stat = itemStats.computeIfAbsent(
                transaction.itemId,
                k -> new ItemStatistic(transaction.itemId)
            );
            
            if (transaction.playerRole == TransactionHistoryManager.TransactionRole.BUYER) {
                stat.addBuy(transaction.quantity, transaction.totalPrice);
            } else {
                stat.addSell(transaction.quantity, transaction.totalPrice);
            }
        }
        
        return itemStats.values().stream()
            .sorted((a, b) -> Integer.compare(b.totalQuantity, a.totalQuantity))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les partenaires commerciaux les plus fréquents
     * 
     * @param limit Nombre maximum de partenaires à retourner
     */
    public List<PartnerStatistic> getTopTradingPartners(Player player, int limit) {
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, null, 0);
        
        Map<String, PartnerStatistic> partnerStats = new HashMap<>();
        
        for (Transaction transaction : allTransactions) {
            String partnerName = transaction.playerRole == TransactionHistoryManager.TransactionRole.BUYER
                ? transaction.seller
                : transaction.buyer;
            
            PartnerStatistic stat = partnerStats.computeIfAbsent(
                partnerName,
                k -> new PartnerStatistic(partnerName)
            );
            
            stat.addTransaction(transaction.totalPrice);
        }
        
        return partnerStats.values().stream()
            .sorted((a, b) -> Integer.compare(b.transactionCount, a.transactionCount))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Calcule le prix moyen d'achat pour un item
     */
    public double getAverageBuyPrice(Player player, String itemId) {
        List<Transaction> buyTransactions = historyManager.getPlayerHistory(player, "BUY", 0).stream()
            .filter(t -> t.itemId.equals(itemId))
            .collect(Collectors.toList());
        
        if (buyTransactions.isEmpty()) return 0.0;
        
        double totalPrice = buyTransactions.stream().mapToDouble(t -> t.totalPrice).sum();
        int totalQuantity = buyTransactions.stream().mapToInt(t -> t.quantity).sum();
        
        return totalQuantity > 0 ? totalPrice / totalQuantity : 0.0;
    }
    
    /**
     * Calcule le prix moyen de vente pour un item
     */
    public double getAverageSellPrice(Player player, String itemId) {
        List<Transaction> sellTransactions = historyManager.getPlayerHistory(player, "SELL", 0).stream()
            .filter(t -> t.itemId.equals(itemId))
            .collect(Collectors.toList());
        
        if (sellTransactions.isEmpty()) return 0.0;
        
        double totalPrice = sellTransactions.stream().mapToDouble(t -> t.totalPrice).sum();
        int totalQuantity = sellTransactions.stream().mapToInt(t -> t.quantity).sum();
        
        return totalQuantity > 0 ? totalPrice / totalQuantity : 0.0;
    }
    
    /**
     * Génère un résumé textuel des statistiques
     */
    public List<String> generateStatisticsSummary(Player player) {
        PlayerStatistics globalStats = getGlobalStatistics(player);
        PlayerStatistics todayStats = getPeriodStatistics(player, TimePeriod.TODAY);
        List<ItemStatistic> topItems = getMostTradedItems(player, 3);
        List<PartnerStatistic> topPartners = getTopTradingPartners(player, 3);
        
        List<String> summary = new ArrayList<>();
        summary.add("§8§m──────────────────────────────");
        summary.add("§6§lStatistiques de Trading");
        summary.add("§8§m──────────────────────────────");
        summary.add("");
        summary.add("§e§lGlobal:");
        summary.add("§7  Achats: §c" + moneyFormat.format(globalStats.totalBuy) + "$ §7(" + globalStats.buyCount + " transactions)");
        summary.add("§7  Ventes: §a" + moneyFormat.format(globalStats.totalSell) + "$ §7(" + globalStats.sellCount + " transactions)");
        summary.add("§7  Profit net: " + (globalStats.netProfit >= 0 ? "§a+" : "§c") + moneyFormat.format(globalStats.netProfit) + "$");
        summary.add("");
        summary.add("§b§lAujourd'hui:");
        summary.add("§7  Transactions: §e" + todayStats.totalTransactions);
        summary.add("§7  Profit net: " + (todayStats.netProfit >= 0 ? "§a+" : "§c") + moneyFormat.format(todayStats.netProfit) + "$");
        summary.add("");
        
        if (!topItems.isEmpty()) {
            summary.add("§d§lTop Items:");
            for (int i = 0; i < topItems.size(); i++) {
                ItemStatistic item = topItems.get(i);
                summary.add("§7  " + (i + 1) + ". §f" + item.getFormattedName() + " §7(" + item.totalQuantity + "x)");
            }
            summary.add("");
        }
        
        if (!topPartners.isEmpty()) {
            summary.add("§a§lTop Partenaires:");
            for (int i = 0; i < topPartners.size(); i++) {
                PartnerStatistic partner = topPartners.get(i);
                summary.add("§7  " + (i + 1) + ". §b" + partner.partnerName + " §7(" + partner.transactionCount + " transactions)");
            }
        }
        
        summary.add("§8§m──────────────────────────────");
        
        return summary;
    }
    
    // ============================================================
    // CLASSES INTERNES
    // ============================================================
    
    /**
     * Statistiques globales d'un joueur
     */
    public static class PlayerStatistics {
        public final double totalBuy;
        public final double totalSell;
        public final double netProfit;
        public final int buyCount;
        public final int sellCount;
        public final int totalTransactions;
        
        public PlayerStatistics(double totalBuy, double totalSell, double netProfit,
                              int buyCount, int sellCount, int totalTransactions) {
            this.totalBuy = totalBuy;
            this.totalSell = totalSell;
            this.netProfit = netProfit;
            this.buyCount = buyCount;
            this.sellCount = sellCount;
            this.totalTransactions = totalTransactions;
        }
    }
    
    /**
     * Statistiques pour un item spécifique
     */
    public static class ItemStatistic {
        public final String itemId;
        public int buyQuantity = 0;
        public int sellQuantity = 0;
        public int totalQuantity = 0;
        public double buyTotal = 0;
        public double sellTotal = 0;
        
        public ItemStatistic(String itemId) {
            this.itemId = itemId;
        }
        
        public void addBuy(int quantity, double price) {
            this.buyQuantity += quantity;
            this.totalQuantity += quantity;
            this.buyTotal += price;
        }
        
        public void addSell(int quantity, double price) {
            this.sellQuantity += quantity;
            this.totalQuantity += quantity;
            this.sellTotal += price;
        }
        
        public String getFormattedName() {
            if (itemId.startsWith("nexo:")) {
                return itemId.substring(5).replace("_", " ");
            } else if (itemId.startsWith("minecraft:")) {
                return itemId.substring(10).replace("_", " ");
            }
            return itemId.replace("_", " ");
        }
    }
    
    /**
     * Statistiques pour un partenaire commercial
     */
    public static class PartnerStatistic {
        public final String partnerName;
        public int transactionCount = 0;
        public double totalAmount = 0;
        
        public PartnerStatistic(String partnerName) {
            this.partnerName = partnerName;
        }
        
        public void addTransaction(double amount) {
            this.transactionCount++;
            this.totalAmount += amount;
        }
    }
    
    /**
     * Périodes temporelles pour les statistiques
     */
    public enum TimePeriod {
        TODAY,
        WEEK,
        MONTH
    }
}
