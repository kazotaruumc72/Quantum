package com.wynvers.quantum.transactions;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * Récupère l'historique des transactions pour un joueur
     * 
     * @param player Joueur
     * @param type Type de transaction ("BUY", "SELL", ou null pour tout)
     * @param limit Nombre maximum de résultats (0 = illimité)
     * @return Liste des transactions triées par date (plus récent en premier)
     */
    public List<Transaction> getPlayerHistory(Player player, String type, int limit) {
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
            
            boolean isBuyer = buyerUUID != null && buyerUUID.equals(player.getUniqueId().toString());
            boolean isSeller = sellerUUID != null && sellerUUID.equals(player.getUniqueId().toString());
            
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
        transactions.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        
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
        List<Transaction> buyTransactions = getPlayerHistory(player, "BUY", 0);
        return buyTransactions.stream().mapToDouble(t -> t.totalPrice).sum();
    }
    
    /**
     * Calcule le total des ventes d'un joueur
     */
    public double getTotalSellAmount(Player player) {
        List<Transaction> sellTransactions = getPlayerHistory(player, "SELL", 0);
        return sellTransactions.stream().mapToDouble(t -> t.totalPrice).sum();
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
        return getPlayerHistory(player, null, 0).size();
    }
    
    /**
     * Récupère l'item le plus vendu par un joueur
     */
    public String getMostSoldItem(Player player) {
        List<Transaction> sellTransactions = getPlayerHistory(player, "SELL", 0);
        
        if (sellTransactions.isEmpty()) {
            return "Aucun";
        }
        
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Transaction transaction : sellTransactions) {
            itemCounts.put(transaction.itemId, itemCounts.getOrDefault(transaction.itemId, 0) + transaction.quantity);
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
        List<Transaction> buyTransactions = getPlayerHistory(player, "BUY", 0);
        
        if (buyTransactions.isEmpty()) {
            return "Aucun";
        }
        
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Transaction transaction : buyTransactions) {
            itemCounts.put(transaction.itemId, itemCounts.getOrDefault(transaction.itemId, 0) + transaction.quantity);
        }
        
        return itemCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucun");
    }
    
    // ============================================================
    // CLASSES INTERNES
    // ============================================================
    
    /**
     * Représente une transaction
     */
    public static class Transaction {
        public final String transactionId;
        public final String buyer;
        public final String seller;
        public final String itemId;
        public final int quantity;
        public final double pricePerUnit;
        public final double totalPrice;
        public final long timestamp;
        public final String date;
        public final TransactionRole playerRole;
        
        public Transaction(String transactionId, String buyer, String seller, String itemId, int quantity,
                         double pricePerUnit, double totalPrice, long timestamp, String date, TransactionRole playerRole) {
            this.transactionId = transactionId;
            this.buyer = buyer;
            this.seller = seller;
            this.itemId = itemId;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.totalPrice = totalPrice;
            this.timestamp = timestamp;
            this.date = date;
            this.playerRole = playerRole;
        }
        
        /**
         * Formate l'item pour l'affichage
         */
        public String getFormattedItem() {
            if (itemId.startsWith("nexo:")) {
                return itemId.substring(5).replace("_", " ");
            } else if (itemId.startsWith("minecraft:")) {
                return itemId.substring(10).replace("_", " ");
            }
            return itemId.replace("_", " ");
        }
    }
    
    /**
     * Rôle du joueur dans une transaction
     */
    public enum TransactionRole {
        BUYER,
        SELLER
    }
}
