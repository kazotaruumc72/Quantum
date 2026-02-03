package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

/**
 * Gère l'acceptation d'un ordre par un vendeur
 * 
 * Workflow:
 * 1. Vendeur clique sur un ordre en mode VENTE
 * 2. Vérification du stock disponible
 * 3. Retrait des items du storage
 * 4. Récupération de l'argent depuis l'escrow
 * 5. Transfert de l'argent au vendeur
 * 6. Notification à l'acheteur
 * 7. Mise à jour de l'ordre (status = COMPLETED)
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class OrderAcceptanceHandler {
    
    private final Quantum plugin;
    
    public OrderAcceptanceHandler(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Traite l'acceptation d'un ordre par un vendeur
     * 
     * @param seller Vendeur qui accepte l'ordre
     * @param orderId ID court de l'ordre (ex: "a1b2c3d4")
     * @param category Catégorie de l'ordre
     * @return true si l'ordre a été accepté avec succès
     */
    public boolean acceptOrder(Player seller, String orderId, String category) {
        // Charger l'ordre depuis orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            seller.sendMessage("§c⚠ Fichier orders.yml introuvable!");
            return false;
        }
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        String path = category + "." + orderId;
        
        // Vérifier que l'ordre existe
        if (!ordersConfig.contains(path)) {
            seller.sendMessage("§c⚠ Ordre introuvable!");
            return false;
        }
        
        // Récupérer les infos de l'ordre
        String orderUUIDStr = ordersConfig.getString(path + ".order_uuid");
        String buyerName = ordersConfig.getString(path + ".orderer");
        String buyerUUIDStr = ordersConfig.getString(path + ".orderer_uuid");
        String itemId = ordersConfig.getString(path + ".item");
        int quantity = ordersConfig.getInt(path + ".quantity");
        double pricePerUnit = ordersConfig.getDouble(path + ".price_per_unit");
        double totalPrice = ordersConfig.getDouble(path + ".total_price");
        String status = ordersConfig.getString(path + ".status", "ACTIVE");
        
        // Vérifier que l'ordre est actif
        if (!"ACTIVE".equals(status)) {
            seller.sendMessage("§c⚠ Cet ordre n'est plus actif!");
            return false;
        }
        
        // Vérifier que l'UUID de l'ordre existe
        if (orderUUIDStr == null) {
            seller.sendMessage("§c⚠ Ordre invalide (UUID manquant)!");
            return false;
        }
        
        UUID orderUUID;
        try {
            orderUUID = UUID.fromString(orderUUIDStr);
        } catch (IllegalArgumentException e) {
            seller.sendMessage("§c⚠ UUID d'ordre invalide!");
            return false;
        }
        
        // Vérifier que le vendeur n'est pas l'acheteur
        if (seller.getName().equals(buyerName)) {
            seller.sendMessage("§c⚠ Vous ne pouvez pas accepter votre propre ordre!");
            return false;
        }
        
        // === ÉTAPE 1: VÉRIFIER LE STOCK ===
        StorageData storage = plugin.getStorageManager().getStorage(seller.getUniqueId());
        int availableStock = storage.getAmount(itemId);
        
        if (availableStock < quantity) {
            seller.sendMessage("§c⚠ Stock insuffisant!");
            seller.sendMessage("§7Requis: §e" + quantity + "x");
            seller.sendMessage("§7Disponible: §e" + availableStock + "x");
            return false;
        }
        
        // === ÉTAPE 2: VÉRIFIER L'ESCROW ===
        if (!plugin.getEscrowManager().hasDeposit(orderUUID)) {
            seller.sendMessage("§c⚠ Aucun dépôt escrow trouvé pour cet ordre!");
            seller.sendMessage("§7L'ordre est invalide ou déjà traité.");
            // Marquer l'ordre comme invalide
            ordersConfig.set(path + ".status", "INVALID");
            try {
                ordersConfig.save(ordersFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        
        double escrowAmount = plugin.getEscrowManager().getAmount(orderUUID);
        if (Math.abs(escrowAmount - totalPrice) > 0.01) {
            seller.sendMessage("§c⚠ Montant escrow incorrect!");
            seller.sendMessage("§7Attendu: " + totalPrice + "€, Trouvé: " + escrowAmount + "€");
            return false;
        }
        
        // === ÉTAPE 3: RETIRER LES ITEMS DU STORAGE ===
        boolean removed = storage.removeItem(itemId, quantity);
        if (!removed) {
            seller.sendMessage("§c⚠ Erreur lors du retrait des items!");
            return false;
        }
        
        // Sauvegarder le storage
        plugin.getStorageManager().saveStorage(seller.getUniqueId());
        
        // === ÉTAPE 4: RÉCUPÉRER L'ARGENT DE L'ESCROW ===
        double withdrawnAmount = plugin.getEscrowManager().withdraw(orderUUID);
        if (withdrawnAmount <= 0) {
            // Erreur critique: les items ont été retirés mais pas d'argent
            seller.sendMessage("§c⚠ ERREUR CRITIQUE: Impossible de récupérer l'argent de l'escrow!");
            seller.sendMessage("§7Contactez un administrateur immédiatement.");
            plugin.getQuantumLogger().error("CRITICAL: Failed to withdraw escrow for order " + orderUUID);
            plugin.getQuantumLogger().error("Seller: " + seller.getName() + ", Items removed: " + quantity + "x " + itemId);
            
            // Tenter de restaurer les items
            storage.addItem(itemId, quantity);
            plugin.getStorageManager().saveStorage(seller.getUniqueId());
            return false;
        }
        
        // === ÉTAPE 5: DONNER L'ARGENT AU VENDEUR ===
        if (plugin.getVaultManager().isEnabled()) {
            if (!plugin.getVaultManager().deposit(seller, withdrawnAmount)) {
                // Erreur: argent retiré de l'escrow mais pas donné au vendeur
                seller.sendMessage("§c⚠ ERREUR: Dépôt bancaire échoué!");
                plugin.getQuantumLogger().error("CRITICAL: Failed to deposit money to seller " + seller.getName());
                plugin.getQuantumLogger().error("Amount: " + withdrawnAmount + "€, Order: " + orderUUID);
                
                // Tenter de remettre l'argent en escrow
                plugin.getEscrowManager().deposit(orderUUID, withdrawnAmount);
                
                // Restaurer les items
                storage.addItem(itemId, quantity);
                plugin.getStorageManager().saveStorage(seller.getUniqueId());
                return false;
            }
        } else {
            seller.sendMessage("§c⚠ Système économique indisponible!");
            
            // Restaurer l'escrow et les items
            plugin.getEscrowManager().deposit(orderUUID, withdrawnAmount);
            storage.addItem(itemId, quantity);
            plugin.getStorageManager().saveStorage(seller.getUniqueId());
            return false;
        }
        
        // === ÉTAPE 6: METTRE À JOUR L'ORDRE ===
        ordersConfig.set(path + ".status", "COMPLETED");
        ordersConfig.set(path + ".seller", seller.getName());
        ordersConfig.set(path + ".seller_uuid", seller.getUniqueId().toString());
        ordersConfig.set(path + ".completed_at", System.currentTimeMillis());
        
        try {
            ordersConfig.save(ordersFile);
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to save completed order " + orderId);
            e.printStackTrace();
        }
        
        // === ÉTAPE 7: NOTIFICATIONS ===
        
        // Message au vendeur
        seller.sendMessage("§a§l✓ Ordre accepté avec succès!");
        seller.sendMessage("§7Vous avez vendu §e" + quantity + "x §f" + formatItemName(itemId));
        seller.sendMessage("§7à §b" + buyerName);
        seller.sendMessage("§8[§6Quantum§8] §a+" + String.format("%.2f", withdrawnAmount) + "$");
        
        // Notification à l'acheteur (si en ligne)
        if (buyerUUIDStr != null) {
            try {
                UUID buyerUUID = UUID.fromString(buyerUUIDStr);
                Player buyer = Bukkit.getPlayer(buyerUUID);
                
                if (buyer != null && buyer.isOnline()) {
                    buyer.sendMessage("§a§l✓ Votre ordre a été rempli!");
                    buyer.sendMessage("§7§b" + seller.getName() + " §7vous a vendu §e" + quantity + "x §f" + formatItemName(itemId));
                    buyer.sendMessage("§7Coût total: §6" + String.format("%.2f", totalPrice) + "$");
                    buyer.sendMessage("§7Les items sont disponibles dans votre storage!");
                    
                    // Ajouter les items au storage de l'acheteur
                    StorageData buyerStorage = plugin.getStorageManager().getStorage(buyerUUID);
                    buyerStorage.addItem(itemId, quantity);
                    plugin.getStorageManager().saveStorage(buyerUUID);
                }
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid buyer UUID: " + buyerUUIDStr);
            }
        }
        
        // Log de la transaction
        plugin.getQuantumLogger().info("Order completed: " + orderId);
        plugin.getQuantumLogger().info("  Buyer: " + buyerName + ", Seller: " + seller.getName());
        plugin.getQuantumLogger().info("  Item: " + quantity + "x " + itemId);
        plugin.getQuantumLogger().info("  Price: " + totalPrice + "€");
        
        return true;
    }
    
    /**
     * Formate joliment un itemId pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return "[Nexo] " + itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }
}
