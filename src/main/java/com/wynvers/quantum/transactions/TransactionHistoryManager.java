package com.wynvers.quantum.transactions;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.transactions.Transaction.TransactionRole;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère l'historique des transactions (achats et ventes)
 * 
 * Fonctionnalités:
 * - Enregistrement de toutes les transactions dans transactions.yml
 * - Consultation de l'historique par joueur
 * - Filtrage par type (achat / vente)
 * - Statistiques sur les transactions
 * 
 * Structure YAML:
 * transactions:
 *   [transactionId]:
 *     type: "BUY" ou "SELL"
 *     buyer: "PlayerName"
 *     buyer_uuid: "UUID"
 *     seller: "PlayerName"
 *     seller_uuid: "UUID"
 *     item: "nexo:item_id"
 *     quantity: 64
 *     price_per_unit: 10.50
 *     total_price: 672.00
 *     timestamp: 1234567890
 *     date: "2026-02-03 10:22:00"
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class TransactionHistoryManager {
    
    private final Quantum plugin;
    private final File transactionsFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public TransactionHistoryManager(Quantum plugin) {
        this.plugin = plugin;
        this.transactionsFile = new File(plugin.getDataFolder(), "transactions.yml");
        
        // Créer le fichier s'il n'existe pas
        if (!transactionsFile.exists()) {
            try {
                transactionsFile.createNewFile();
                YamlConfiguration config = new YamlConfiguration();
                config.set("transactions", new HashMap<>());
                config.save(transactionsFile);
                plugin.getLogger().info("[TRANSACTIONS] Created transactions.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("[TRANSACTIONS] Failed to create transactions.yml: " + e.getMessage());
            }
        }
    }
    
    /**
     * Enregistre une transaction
     * 
     * @param buyer Acheteur
     * @param seller Vendeur
     * @param itemId ID de l'item
     * @param quantity Quantité
     * @param pricePerUnit Prix unitaire
     * @param totalPrice Prix total
     */
    public void recordTransaction(Player buyer, Player seller, String itemId, int quantity, double pricePerUnit, double totalPrice) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(transactionsFile);
        
        // Générer un ID unique pour la transaction
        String transactionId = UUID.randomUUID().toString();
        String path = "transactions." + transactionId;
        
        // Enregistrer les données
        long timestamp = System.currentTimeMillis();
        config.set(path + ".type", "TRADE");
        config.set(path + ".buyer", buyer.getName());
        config.set(path + ".buyer_uuid", buyer.getUniqueId().toString());
        config.set(path + ".seller", seller.getName());
        config.set(path + ".seller_uuid", seller.getUniqueId().toString());
        config.set(path + ".item", itemId);
        config.set(path + ".quantity", quantity);
        config.set(path + ".price_per_unit", pricePerUnit);
        config.set(path + ".total_price", totalPrice);
        config.set(path + ".timestamp", timestamp);
        config.set(path + ".date", dateFormat.format(new Date(timestamp)));
        
        try {
            config.save(transactionsFile);
            plugin.getLogger().info("[TRANSACTIONS] Recorded transaction: " + buyer.getName() + " <- " + seller.getName() + " (" + quantity + "x " + itemId + ")");
        } catch (IOException e) {
            plugin.getLogger().severe("[TRANSACTIONS] Failed to save transaction: " + e.getMessage());
        }
    }
    
    /**
     * Get all transactions for a player
     */
    public List<Transaction> getPlayerTransactions(UUID playerUUID) {
        return getPlayerHistory(playerUUID, null, 0);
    }
    
    /**
     * Get buy transactions for a player
     */
    public List<Transaction> getPlayerBuyTransactions(UUID playerUUID) {
        return getPlayerHistory(playerUUID, "BUY", 0);
    }
    
    /**
     * Get sell transactions for a player
     */
    public List<Transaction> getPlayerSellTransactions(UUID playerUUID) {
        return getPlayerHistory(playerUUID, "SELL", 0);
    }
    
    /**
     * Récupère l'historique des transactions pour un joueur
     * 
     * @param playerUUID UUID du joueur
     * @param type Type de transaction ("BUY", "SELL", ou null pour tout)
     * @param limit Nombre maximum de résultats (0 = illimité)
     * @return Liste des transactions triées par date (plus récent en premier)
     */
    private List<Transaction> getPlayerHistory(UUID playerUUID, String type, int limit) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(transactionsFile);
        
        if (!config.contains("transactions")) {
            return new ArrayList<>();
        }
        
        Set<String> transactionIds = config.getConfigurationSection("transactions").getKeys(false);
        List<Transaction> transactions = new ArrayList<>();
        
        for (String transactionId : transactionIds) {
            String path = "transactions." + transactionId;
            
            String buyerUUID = config.getString(path + ".buyer_uuid");
            String sellerUUID = config.getString(path + ".seller_uuid");
            
            boolean isBuyer = buyerUUID != null && buyerUUID.equals(playerUUID.toString());
            boolean isSeller = sellerUUID != null && sellerUUID.equals(playerUUID.toString());
            
            // Filtrer par type si spécifié
            if (type != null) {
                if (type.equalsIgnoreCase("BUY") && !isBuyer) continue;
                if (type.equalsIgnoreCase("SELL") && !isSeller) continue;
            }
            
            // Le joueur doit être impliqué dans la transaction
            if (!isBuyer && !isSeller) continue;
            
            Transaction transaction = new Transaction(
                transactionId,
                config.getString(path + ".buyer"),
                config.getString(path + ".seller"),
                config.getString(path + ".item"),
                config.getInt(path + ".quantity"),
                config.getDouble(path + ".price_per_unit"),
                config.getDouble(path + ".total_price"),
                config.getLong(path + ".timestamp"),
                config.getString(path + ".date"),
                isBuyer ? TransactionRole.BUYER : TransactionRole.SELLER
            );
            
            transactions.add(transaction);
        }
        
        // Trier par timestamp (plus récent en premier)
        transactions.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        // Limiter les résultats si nécessaire
        if (limit > 0 && transactions.size() > limit) {
            return transactions.subList(0, limit);
        }
        
        return transactions;
    }
    
    /**
     * Calcule le total des achats d'un joueur
     */
    public double getTotalBuyAmount(Player player) {
        List<Transaction> buyTransactions = getPlayerBuyTransactions(player.getUniqueId());
        return buyTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
    }
    
    /**
     * Calcule le total des ventes d'un joueur
     */
    public double getTotalSellAmount(Player player) {
        List<Transaction> sellTransactions = getPlayerSellTransactions(player.getUniqueId());
        return sellTransactions.stream().mapToDouble(Transaction::getTotalPrice).sum();
    }
    
    /**
     * Calcule le profit net d'un joueur (ventes - achats)
     */
    public double getNetProfit(Player player) {
        return getTotalSellAmount(player) - getTotalBuyAmount(player);
    }
    
    /**
     * Compte le nombre total de transactions pour un joueur
     */
    public int getTotalTransactionCount(Player player) {
        return getPlayerTransactions(player.getUniqueId()).size();
    }
    
    /**
     * Récupère l'item le plus vendu par un joueur
     */
    public String getMostSoldItem(Player player) {
        List<Transaction> sellTransactions = getPlayerSellTransactions(player.getUniqueId());
        
        if (sellTransactions.isEmpty()) {
            return "Aucun";
        }
        
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Transaction transaction : sellTransactions) {
            itemCounts.put(transaction.getItemId(), itemCounts.getOrDefault(transaction.getItemId(), 0) + transaction.getQuantity());
        }
        
        return itemCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucun");
    }
    
    /**
     * Récupère l'item le plus acheté par un joueur
     */
    public String getMostBoughtItem(Player player) {
        List<Transaction> buyTransactions = getPlayerBuyTransactions(player.getUniqueId());
        
        if (buyTransactions.isEmpty()) {
            return "Aucun";
        }
        
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Transaction transaction : buyTransactions) {
            itemCounts.put(transaction.getItemId(), itemCounts.getOrDefault(transaction.getItemId(), 0) + transaction.getQuantity());
        }
        
        return itemCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucun");
    }
}
