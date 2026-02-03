package com.wynvers.quantum.menus;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.transactions.TransactionHistoryManager.Transaction;
import com.wynvers.quantum.transactions.TransactionHistoryManager.TransactionRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère l'affichage du menu d'historique des transactions
 * 
 * Fonctionnalités:
 * - Affichage paginé des transactions (36 par page)
 * - Filtrage par type (achats/ventes)
 * - Navigation entre les pages
 * - Affichage détaillé de chaque transaction
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class HistoryMenuHandler {
    
    private final Quantum plugin;
    private final TransactionHistoryManager historyManager;
    
    // Cache des filtres actifs par joueur
    private final Map<Player, String> activeFilters = new HashMap<>();
    private final Map<Player, Integer> currentPages = new HashMap<>();
    
    private static final int TRANSACTIONS_PER_PAGE = 36;
    private static final int MENU_SIZE = 54;
    
    public HistoryMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.historyManager = plugin.getTransactionHistoryManager();
    }
    
    /**
     * Ouvre le menu d'historique pour un joueur
     * 
     * @param player Joueur
     * @param filter Filtre ("BUY", "SELL", ou null pour tout)
     * @param page Numéro de page (commence à 1)
     */
    public void openHistoryMenu(Player player, String filter, int page) {
        // Sauvegarder le filtre et la page
        activeFilters.put(player, filter);
        currentPages.put(player, page);
        
        // Récupérer les transactions
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, filter, 0);
        
        // Calculer la pagination
        int totalPages = (int) Math.ceil((double) allTransactions.size() / TRANSACTIONS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;
        
        // Récupérer les transactions pour cette page
        int startIndex = (page - 1) * TRANSACTIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + TRANSACTIONS_PER_PAGE, allTransactions.size());
        List<Transaction> pageTransactions = allTransactions.subList(startIndex, endIndex);
        
        // Créer le menu
        String title = "§8┃ §6§lHistorique des Transactions";
        Inventory menu = Bukkit.createInventory(null, MENU_SIZE, title);
        
        // Bordures
        addBorders(menu);
        
        // Boutons de filtre
        addFilterButtons(menu, filter);
        
        // Informations
        addInfoButton(menu, player);
        
        // Transactions (slots 9-44)
        addTransactions(menu, pageTransactions);
        
        // Pagination
        addPaginationButtons(menu, page, totalPages, allTransactions.size(), pageTransactions.size());
        
        // Ouvrir le menu
        player.openInventory(menu);
    }
    
    /**
     * Ajoute les bordures décoratives
     */
    private void addBorders(Inventory menu) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§7", null);
        
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 51, 52, 53};
        for (int slot : borderSlots) {
            menu.setItem(slot, border);
        }
    }
    
    /**
     * Ajoute les boutons de filtre
     */
    private void addFilterButtons(Inventory menu, String currentFilter) {
        // Toutes les transactions
        List<String> allLore = new ArrayList<>();
        allLore.add("§7");
        allLore.add("§7Affiche toutes vos transactions");
        allLore.add("§7(achats et ventes)");
        allLore.add("§7");
        if (currentFilter == null) {
            allLore.add("§a▶ Filtre actif");
        } else {
            allLore.add("§e▶ Cliquez pour filtrer");
        }
        menu.setItem(48, createItem(Material.BOOK, "§e§lToutes les Transactions", allLore));
        
        // Achats uniquement
        List<String> buyLore = new ArrayList<>();
        buyLore.add("§7");
        buyLore.add("§7Affiche uniquement vos achats");
        buyLore.add("§7");
        if ("BUY".equals(currentFilter)) {
            buyLore.add("§a▶ Filtre actif");
        } else {
            buyLore.add("§e▶ Cliquez pour filtrer");
        }
        menu.setItem(49, createItem(Material.EMERALD, "§a§lAchats Uniquement", buyLore));
        
        // Ventes uniquement
        List<String> sellLore = new ArrayList<>();
        sellLore.add("§7");
        sellLore.add("§7Affiche uniquement vos ventes");
        sellLore.add("§7");
        if ("SELL".equals(currentFilter)) {
            sellLore.add("§a▶ Filtre actif");
        } else {
            sellLore.add("§e▶ Cliquez pour filtrer");
        }
        menu.setItem(50, createItem(Material.GOLD_INGOT, "§6§lVentes Uniquement", sellLore));
    }
    
    /**
     * Ajoute le bouton d'informations
     */
    private void addInfoButton(Inventory menu, Player player) {
        double totalBuy = historyManager.getTotalBuyAmount(player);
        double totalSell = historyManager.getTotalSellAmount(player);
        double netProfit = historyManager.getNetProfit(player);
        int transactionCount = historyManager.getTotalTransactionCount(player);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7");
        lore.add("§7Total des achats: §a" + String.format("%.2f", totalBuy) + "€");
        lore.add("§7Total des ventes: §6" + String.format("%.2f", totalSell) + "€");
        lore.add("§7Profit net: §e" + String.format("%.2f", netProfit) + "€");
        lore.add("§7");
        lore.add("§7Transactions: §f" + transactionCount);
        lore.add("§7");
        lore.add("§8Votre historique complet de trading");
        
        menu.setItem(4, createItem(Material.BOOK, "§e§lInformations", lore));
    }
    
    /**
     * Ajoute les transactions à la page
     */
    private void addTransactions(Inventory menu, List<Transaction> transactions) {
        int slot = 9; // Commence au slot 9
        
        for (Transaction transaction : transactions) {
            if (slot > 44) break; // Ne pas dépasser le slot 44
            
            Material material = transaction.playerRole == TransactionRole.BUYER ? Material.EMERALD : Material.GOLD_INGOT;
            String displayName = transaction.playerRole == TransactionRole.BUYER 
                ? "§a§lACHAT" 
                : "§6§lVENTE";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7");
            lore.add("§7Date: §f" + transaction.date);
            
            if (transaction.playerRole == TransactionRole.BUYER) {
                lore.add("§7Vendeur: §f" + transaction.seller);
            } else {
                lore.add("§7Acheteur: §f" + transaction.buyer);
            }
            
            lore.add("§7Item: §f" + transaction.quantity + "x " + transaction.getFormattedItem());
            lore.add("§7Prix unitaire: §e" + String.format("%.2f", transaction.pricePerUnit) + "€");
            lore.add("§7Prix total: " + 
                (transaction.playerRole == TransactionRole.BUYER ? "§a" : "§6") + 
                String.format("%.2f", transaction.totalPrice) + "€");
            
            menu.setItem(slot, createItem(material, displayName, lore));
            slot++;
        }
    }
    
    /**
     * Ajoute les boutons de pagination
     */
    private void addPaginationButtons(Inventory menu, int currentPage, int totalPages, int totalTransactions, int shownTransactions) {
        // Page précédente (slot 45)
        if (currentPage > 1) {
            List<String> prevLore = new ArrayList<>();
            prevLore.add("§7");
            prevLore.add("§7Page actuelle: §f" + currentPage);
            prevLore.add("§7Total: §f" + totalPages + " pages");
            prevLore.add("§7");
            prevLore.add("§e▶ Cliquez pour page précédente");
            menu.setItem(45, createItem(Material.ARROW, "§e◀ Page Précédente", prevLore));
        }
        
        // Page suivante (slot 53)
        if (currentPage < totalPages) {
            List<String> nextLore = new ArrayList<>();
            nextLore.add("§7");
            nextLore.add("§7Page actuelle: §f" + currentPage);
            nextLore.add("§7Total: §f" + totalPages + " pages");
            nextLore.add("§7");
            nextLore.add("§e▶ Cliquez pour page suivante");
            menu.setItem(53, createItem(Material.ARROW, "§ePage Suivante ▶", nextLore));
        }
    }
    
    /**
     * Gère le clic sur un bouton du menu
     */
    public void handleClick(Player player, int slot) {
        String filter = activeFilters.getOrDefault(player, null);
        int page = currentPages.getOrDefault(player, 1);
        
        switch (slot) {
            case 45: // Page précédente
                if (page > 1) {
                    openHistoryMenu(player, filter, page - 1);
                }
                break;
                
            case 48: // Toutes les transactions
                openHistoryMenu(player, null, 1);
                break;
                
            case 49: // Achats uniquement
                openHistoryMenu(player, "BUY", 1);
                break;
                
            case 50: // Ventes uniquement
                openHistoryMenu(player, "SELL", 1);
                break;
                
            case 53: // Page suivante
                List<Transaction> allTransactions = historyManager.getPlayerHistory(player, filter, 0);
                int totalPages = (int) Math.ceil((double) allTransactions.size() / TRANSACTIONS_PER_PAGE);
                if (page < totalPages) {
                    openHistoryMenu(player, filter, page + 1);
                }
                break;
        }
    }
    
    /**
     * Nettoie le cache pour un joueur
     */
    public void clearCache(Player player) {
        activeFilters.remove(player);
        currentPages.remove(player);
    }
    
    /**
     * Crée un ItemStack avec display name et lore
     */
    private ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
