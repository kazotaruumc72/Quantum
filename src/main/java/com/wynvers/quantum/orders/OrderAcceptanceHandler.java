package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

/**
 * Gère l'acceptation d'un ordre par un vendeur
 * 
 * Workflow:
 * 1. Vendeur clique sur un ordre en mode VENTE
 * 2. Vérification du stock disponible (DOUBLE CHECK)
 * 3. Retrait des items du storage
 * 4. Récupération de l'argent depuis l'escrow
 * 5. Transfert de l'argent au vendeur
 * 6. Ajout des items au storage de l'acheteur
 * 7. Notification à l'acheteur
 * 8. Mise à jour de l'ordre (status = COMPLETED)
 * 
 * PATCH: Utilise UUID complet comme clé d'ordre (orderId = UUID complet)
 * PATCH: Double vérification du stock avant transaction
 * 
 * @author Kazotaruu_
 * @version 1.2
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
     * @param orderId UUID COMPLET de l'ordre (clé dans orders.yml)
     * @param category Catégorie de l'ordre
     * @return true si l'ordre a été accepté avec succès
     */
    public boolean acceptOrder(Player seller, String orderId, String category) {
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Processing order acceptance:");
        plugin.getLogger().info("  - Seller: " + seller.getName());
        plugin.getLogger().info("  - OrderID: " + orderId);
        plugin.getLogger().info("  - Category: " + category);
        
        // Charger l'ordre depuis orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            seller.sendMessage("§c⚠ Fichier orders.yml introuvable!");
            return false;
        }
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        String path = category + "." + orderId;
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Looking for order at path: " + path);
        
        // Vérifier que l'ordre existe
        if (!ordersConfig.contains(path)) {
            seller.sendMessage("§c⚠ Ordre introuvable!");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] Order not found at path: " + path);
            return false;
        }
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Order found! Validating order data...");
        
        // Récupérer les infos de l'ordre
        String buyerName = ordersConfig.getString(path + ".orderer");
        String buyerUUIDStr = ordersConfig.getString(path + ".orderer_uuid");
        String itemId = ordersConfig.getString(path + ".item");
        int quantity = ordersConfig.getInt(path + ".quantity");
        double pricePerUnit = ordersConfig.getDouble(path + ".price_per_unit");
        double totalPrice = ordersConfig.getDouble(path + ".total_price");
        String status = ordersConfig.getString(path + ".status", "ACTIVE");
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Order data:");
        plugin.getLogger().info("  - Buyer: " + buyerName);
        plugin.getLogger().info("  - Item: " + itemId);
        plugin.getLogger().info("  - Quantity: " + quantity);
        plugin.getLogger().info("  - Total Price: " + totalPrice);
        plugin.getLogger().info("  - Status: " + status);
        
        // Vérifier que l'ordre est actif
        if (!"ACTIVE".equals(status)) {
            seller.sendMessage("§c⚠ Cet ordre n'est plus actif!");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] Order status is not ACTIVE: " + status);
            return false;
        }
        
        // Convertir l'orderId (UUID complet) en UUID
        UUID orderUUID;
        try {
            orderUUID = UUID.fromString(orderId);
            plugin.getLogger().info("[ORDER_ACCEPTANCE] UUID parsed successfully: " + orderUUID);
        } catch (IllegalArgumentException e) {
            seller.sendMessage("§c⚠ UUID d'ordre invalide!");
            plugin.getLogger().severe("[ORDER_ACCEPTANCE] Invalid UUID: " + orderId);
            return false;
        }
        
        // Vérifier que le vendeur n'est pas l'acheteur
        if (seller.getName().equalsIgnoreCase(buyerName)) {
            seller.sendMessage("§c⚠ Vous ne pouvez pas accepter votre propre ordre!");
            return false;
        }
        
        // === ÉTAPE 1: VÉRIFICATION DU STOCK (PREMIÈRE VÉRIFICATION) ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Checking seller stock (initial check)...");
        PlayerStorage storage = plugin.getStorageManager().getStorage(seller);
        int availableStock = storage.getAmountByItemId(itemId);
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Seller stock: " + availableStock + "x (required: " + quantity + "x)");
        
        if (availableStock < quantity) {
            seller.sendMessage("§c⚠ Stock insuffisant!");
            seller.sendMessage("§7Requis: §e" + quantity + "x");
            seller.sendMessage("§7Disponible: §e" + availableStock + "x");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] Insufficient stock: " + availableStock + "x < " + quantity + "x");
            return false;
        }
        
        // === ÉTAPE 2: VÉRIFIER L'ESCROW ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Validating escrow...");
        if (!plugin.getEscrowManager().hasDeposit(orderUUID)) {
            seller.sendMessage("§c⚠ Aucun dépôt escrow trouvé pour cet ordre!");
            seller.sendMessage("§7L'ordre est invalide ou déjà traité.");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] No escrow deposit found for UUID: " + orderUUID);
            
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
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Escrow amount: " + escrowAmount + "€ (expected: " + totalPrice + "€)");
        
        if (Math.abs(escrowAmount - totalPrice) > 0.01) {
            seller.sendMessage("§c⚠ Montant escrow incorrect!");
            seller.sendMessage("§7Attendu: " + totalPrice + "€, Trouvé: " + escrowAmount + "€");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] Escrow amount mismatch!");
            return false;
        }
        
        // === ÉTAPE 3: VÉRIFICATION DU STOCK (DOUBLE CHECK AVANT TRANSACTION) ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Double-checking seller stock before transaction...");
        int currentStock = storage.getAmountByItemId(itemId);
        
        if (currentStock < quantity) {
            seller.sendMessage("§c⚠ Erreur: Stock insuffisant lors de la vérification finale!");
            seller.sendMessage("§7Requis: §e" + quantity + "x");
            seller.sendMessage("§7Disponible maintenant: §e" + currentStock + "x");
            seller.sendMessage("§7§oVotre stock a peut-être changé entre-temps.");
            plugin.getLogger().warning("[ORDER_ACCEPTANCE] Stock check failed at transaction time: " + currentStock + "x < " + quantity + "x");
            return false;
        }
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Stock verification passed: " + currentStock + "x >= " + quantity + "x");
        
        // === ÉTAPE 4: RETIRER LES ITEMS DU STORAGE ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Removing items from seller storage...");
        boolean removed = storage.removeItemById(itemId, quantity);
        if (!removed) {
            seller.sendMessage("§c⚠ Erreur lors du retrait des items!");
            plugin.getLogger().severe("[ORDER_ACCEPTANCE] Failed to remove items from seller storage!");
            return false;
        }
        
        // Sauvegarder le storage
        storage.save(plugin);
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Items removed successfully from seller");
        
        // === ÉTAPE 5: RÉCUPÉRER L'ARGENT DE L'ESCROW ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Retrieving money from escrow...");
        double withdrawnAmount = plugin.getEscrowManager().withdraw(orderUUID);
        if (withdrawnAmount <= 0) {
            // Erreur critique: les items ont été retirés mais pas d'argent
            seller.sendMessage("§c⚠ ERREUR CRITIQUE: Impossible de récupérer l'argent de l'escrow!");
            seller.sendMessage("§7Contactez un administrateur immédiatement.");
            plugin.getQuantumLogger().error("CRITICAL: Failed to withdraw escrow for order " + orderUUID);
            plugin.getQuantumLogger().error("Seller: " + seller.getName() + ", Items removed: " + quantity + "x " + itemId);
            
            // Tenter de restaurer les items
            storage.addItemById(itemId, quantity);
            storage.save(plugin);
            plugin.getLogger().info("[ORDER_ACCEPTANCE] Items restored to seller storage (rollback)");
            return false;
        }
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Escrow withdrawn: " + withdrawnAmount + "€");
        
        // === ÉTAPE 6: DONNER L'ARGENT AU VENDEUR ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Depositing money to seller...");
        if (plugin.getVaultManager().isEnabled()) {
            if (!plugin.getVaultManager().deposit(seller, withdrawnAmount)) {
                // Erreur: argent retiré de l'escrow mais pas donné au vendeur
                seller.sendMessage("§c⚠ ERREUR: Dépôt bancaire échoué!");
                plugin.getQuantumLogger().error("CRITICAL: Failed to deposit money to seller " + seller.getName());
                plugin.getQuantumLogger().error("Amount: " + withdrawnAmount + "€, Order: " + orderUUID);
                
                // Tenter de remettre l'argent en escrow
                plugin.getEscrowManager().deposit(orderUUID, withdrawnAmount);
                
                // Restaurer les items
                storage.addItemById(itemId, quantity);
                storage.save(plugin);
                plugin.getLogger().info("[ORDER_ACCEPTANCE] Full rollback completed");
                return false;
            }
        } else {
            seller.sendMessage("§c⚠ Système économique indisponible!");
            
            // Restaurer l'escrow et les items
            plugin.getEscrowManager().deposit(orderUUID, withdrawnAmount);
            storage.addItemById(itemId, quantity);
            storage.save(plugin);
            plugin.getLogger().info("[ORDER_ACCEPTANCE] Full rollback completed (Vault unavailable)");
            return false;
        }
        
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Money deposited to seller successfully");
        
        // === ÉTAPE 7: AJOUTER LES ITEMS AU STORAGE DE L'ACHETEUR ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Adding items to buyer storage...");
        if (buyerUUIDStr != null) {
            try {
                UUID buyerUUID = UUID.fromString(buyerUUIDStr);
                PlayerStorage buyerStorage = plugin.getStorageManager().getStorage(buyerUUID);
                buyerStorage.addItemById(itemId, quantity);
                buyerStorage.save(plugin);
                plugin.getLogger().info("[ORDER_ACCEPTANCE] Items added to buyer storage successfully");
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid buyer UUID: " + buyerUUIDStr);
            }
        }
        
        // === ÉTAPE 8: METTRE À JOUR L'ORDRE ===
        plugin.getLogger().info("[ORDER_ACCEPTANCE] Updating order status...");
        ordersConfig.set(path + ".status", "COMPLETED");
        ordersConfig.set(path + ".seller", seller.getName());
        ordersConfig.set(path + ".seller_uuid", seller.getUniqueId().toString());
        ordersConfig.set(path + ".completed_at", System.currentTimeMillis());
        
        try {
            ordersConfig.save(ordersFile);
            plugin.getLogger().info("[ORDER_ACCEPTANCE] Order updated successfully");
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to save completed order " + orderId);
            e.printStackTrace();
        }
        
        // === ÉTAPE 9: NOTIFICATIONS ===
        
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
                    buyer.playSound(buyer.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid buyer UUID: " + buyerUUIDStr);
            }
        }
        
        // Log de la transaction
        plugin.getQuantumLogger().info("[ORDER_ACCEPTANCE] Order accepted successfully!");
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
