package com.wynvers.quantum.menus;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.transactions.Transaction;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for history menu
 * Manages transaction history display with pagination and filters
 */
public class HistoryMenuHandler {
    
    private final Quantum plugin;
    private final Map<UUID, HistoryViewSession> sessions;
    private static final int ITEMS_PER_PAGE = 21;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public HistoryMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }
    
    /**
     * Get or create session for player
     */
    public HistoryViewSession getSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), uuid -> new HistoryViewSession());
    }
    
    /**
     * Set filter for player
     */
    public void setFilter(Player player, FilterType filter) {
        HistoryViewSession session = getSession(player);
        session.setFilter(filter);
        session.setCurrentPage(1);
    }
    
    /**
     * Set page for player
     */
    public void setPage(Player player, int page) {
        HistoryViewSession session = getSession(player);
        List<Transaction> filtered = getFilteredTransactions(player);
        int totalPages = getTotalPages(filtered.size());
        
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        session.setCurrentPage(page);
    }
    
    /**
     * Get filtered transactions for player
     */
    private List<Transaction> getFilteredTransactions(Player player) {
        TransactionHistoryManager historyManager = plugin.getTransactionHistoryManager();
        HistoryViewSession session = getSession(player);
        
        switch (session.getFilter()) {
            case BUY:
                return historyManager.getPlayerBuyTransactions(player.getUniqueId());
            case SELL:
                return historyManager.getPlayerSellTransactions(player.getUniqueId());
            case ALL:
            default:
                return historyManager.getPlayerTransactions(player.getUniqueId());
        }
    }
    
    /**
     * Get total number of pages
     */
    private int getTotalPages(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
    }
    
    /**
     * Get placeholders for history menu
     */
    public Map<String, String> getPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        HistoryViewSession session = getSession(player);
        List<Transaction> filtered = getFilteredTransactions(player);
        
        int currentPage = session.getCurrentPage();
        int totalPages = getTotalPages(filtered.size());
        int totalTransactions = filtered.size();
        
        // Page info
        placeholders.put("%quantum_history_current_page%", String.valueOf(currentPage));
        placeholders.put("%quantum_history_total_pages%", String.valueOf(totalPages));
        placeholders.put("%quantum_history_total%", String.valueOf(totalTransactions));
        
        // Page navigation
        placeholders.put("%quantum_history_has_previous%", String.valueOf(currentPage > 1));
        placeholders.put("%quantum_history_has_next%", String.valueOf(currentPage < totalPages));
        placeholders.put("%quantum_history_previous_page%", String.valueOf(Math.max(1, currentPage - 1)));
        placeholders.put("%quantum_history_next_page%", String.valueOf(Math.min(totalPages, currentPage + 1)));
        
        // Status messages
        placeholders.put("%quantum_history_previous_status%", 
            currentPage > 1 ? "<green>Cliquez pour voir</green>" : "<gray>Première page</gray>");
        placeholders.put("%quantum_history_next_status%", 
            currentPage < totalPages ? "<green>Cliquez pour voir</green>" : "<gray>Dernière page</gray>");
        
        // Range display
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalTransactions);
        placeholders.put("%quantum_history_showing_from%", String.valueOf(startIndex + 1));
        placeholders.put("%quantum_history_showing_to%", String.valueOf(endIndex));
        
        // Filter status
        FilterType filter = session.getFilter();
        placeholders.put("%quantum_history_filter_all_status%", 
            filter == FilterType.ALL ? "<green>▶ Actif</green>" : "<gray>Cliquez pour activer</gray>");
        placeholders.put("%quantum_history_filter_buy_status%", 
            filter == FilterType.BUY ? "<green>▶ Actif</green>" : "<gray>Cliquez pour activer</gray>");
        placeholders.put("%quantum_history_filter_sell_status%", 
            filter == FilterType.SELL ? "<green>▶ Actif</green>" : "<gray>Cliquez pour activer</gray>");
        
        // Statistics by type
        TransactionHistoryManager historyManager = plugin.getTransactionHistoryManager();
        List<Transaction> buyTransactions = historyManager.getPlayerBuyTransactions(player.getUniqueId());
        List<Transaction> sellTransactions = historyManager.getPlayerSellTransactions(player.getUniqueId());
        
        double buyTotal = buyTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
        double sellTotal = sellTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
        
        placeholders.put("%quantum_history_buy_count%", String.valueOf(buyTransactions.size()));
        placeholders.put("%quantum_history_buy_total%", String.format("%.2f", buyTotal));
        placeholders.put("%quantum_history_sell_count%", String.valueOf(sellTransactions.size()));
        placeholders.put("%quantum_history_sell_total%", String.format("%.2f", sellTotal));
        
        // Transaction items (dynamic)
        List<Transaction> pageTransactions = filtered.subList(
            startIndex, 
            Math.min(startIndex + ITEMS_PER_PAGE, totalTransactions)
        );
        
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            String slotPrefix = "%quantum_history_" + i + "_";
            
            if (i < pageTransactions.size()) {
                Transaction transaction = pageTransactions.get(i);
                
                placeholders.put(slotPrefix + "material%", transaction.getItemMaterial());
                placeholders.put(slotPrefix + "item_name%", transaction.getItemName());
                placeholders.put(slotPrefix + "type_display%", 
                    transaction.isBuyOrder() ? "<green>Achat</green>" : "<gold>Vente</gold>");
                placeholders.put(slotPrefix + "quantity%", String.valueOf(transaction.getQuantity()));
                placeholders.put(slotPrefix + "unit_price%", String.format("%.2f", transaction.getUnitPrice()));
                placeholders.put(slotPrefix + "total_price%", String.format("%.2f", transaction.getTotalPrice()));
                placeholders.put(slotPrefix + "partner_label%", 
                    transaction.isBuyOrder() ? "Vendeur" : "Acheteur");
                placeholders.put(slotPrefix + "partner%", transaction.getPartnerName());
                placeholders.put(slotPrefix + "date%", transaction.getDate().format(DATE_FORMAT));
                placeholders.put(slotPrefix + "id%", transaction.getId());
            } else {
                // Empty slot
                placeholders.put(slotPrefix + "material%", "LIGHT_GRAY_STAINED_GLASS_PANE");
                placeholders.put(slotPrefix + "item_name%", "<gray>Aucune transaction</gray>");
                placeholders.put(slotPrefix + "type_display%", "");
                placeholders.put(slotPrefix + "quantity%", "0");
                placeholders.put(slotPrefix + "unit_price%", "0.00");
                placeholders.put(slotPrefix + "total_price%", "0.00");
                placeholders.put(slotPrefix + "partner_label%", "");
                placeholders.put(slotPrefix + "partner%", "");
                placeholders.put(slotPrefix + "date%", "");
                placeholders.put(slotPrefix + "id%", "");
            }
        }
        
        return placeholders;
    }
    
    /**
     * Clear session for player
     */
    public void clearSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
    
    /**
     * History view session
     */
    public static class HistoryViewSession {
        private FilterType filter = FilterType.ALL;
        private int currentPage = 1;
        
        public FilterType getFilter() {
            return filter;
        }
        
        public void setFilter(FilterType filter) {
            this.filter = filter;
        }
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
    }
    
    /**
     * Filter type enum
     */
    public enum FilterType {
        ALL,
        BUY,
        SELL
    }
}
