package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère les transactions d'ordres entre acheteurs et vendeurs
 * 
 * Workflow:
 * 1. Un vendeur clique sur un ordre dans orders_*
 * 2. Validation des conditions (argent, items, ordre valide)
 * 3. Exécution de la transaction (transfert argent + items)
 * 4. Suppression de l'ordre de orders.yml
 * 5. Messages de confirmation
 */
public class OrderTransaction {
    
    private final Quantum plugin;
    
    public OrderTransaction(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Exécute une transaction d'ordre
     * 
     * @param seller Le joueur qui vend (a les items en storage)
     * @param category La catégorie de l'ordre
     * @param orderId L'ID de l'ordre dans orders.yml
     * @return true si la transaction a réussi, false sinon
     */
    public boolean executeTransaction(Player seller, String category, String orderId) {
        // 1. Charger l'ordre depuis orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            seller.sendMessage("§c⚠ Fichier orders.yml introuvable!");
            return false;
        }
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        String orderPath = category + "." + orderId;
        
        if (!ordersConfig.contains(orderPath)) {
            seller.sendMessage("§c⚠ Cet ordre n'existe plus!");
            return false;
        }
        
        // 2. Récupérer les infos de l'ordre
        String buyerName = ordersConfig.getString(orderPath + ".orderer");
        String buyerUuidStr = ordersConfig.getString(orderPath + ".orderer_uuid");
        String itemId = ordersConfig.getString(orderPath + ".item");
        int quantity = ordersConfig.getInt(orderPath + ".quantity");
        double pricePerUnit = ordersConfig.getDouble(orderPath + ".price_per_unit");
        double totalPrice = ordersConfig.getDouble(orderPath + ".total_price");
        
        if (buyerName == null || buyerUuidStr == null || itemId == null) {
            seller.sendMessage("§c⚠ Ordre corrompu!");
            return false;
        }
        
        UUID buyerUuid;
        try {
            buyerUuid = UUID.fromString(buyerUuidStr);
        } catch (IllegalArgumentException e) {
            seller.sendMessage("§c⚠ UUID de l'acheteur invalide!");
            return false;
        }
        
        // 3. Vérifier que le vendeur n'est pas l'acheteur
        if (seller.getUniqueId().equals(buyerUuid)) {
            seller.sendMessage("§c⚠ Vous ne pouvez pas vendre à vous-même!");
            return false;
        }
        
        // 4. Vérifier que le vendeur a les items en stock
        PlayerStorage sellerStorage = plugin.getStorageManager().getStorage(seller);
        int sellerStock = sellerStorage.getAmountByItemId(itemId);
        
        if (sellerStock < quantity) {
            seller.sendMessage("§c⚠ Vous n'avez pas assez d'items en stock!");
            seller.sendMessage("§7Requis: §e" + quantity + " §7| Stock: §c" + sellerStock);
            return false;
        }
        
        // 5. Vérifier que l'acheteur a assez d'argent
        OfflinePlayer buyer = Bukkit.getOfflinePlayer(buyerUuid);
        if (!plugin.getVaultManager().has(buyer, totalPrice)) {
            seller.sendMessage("§c⚠ L'acheteur n'a pas assez d'argent!");
            seller.sendMessage("§7Transaction annulée.");
            return false;
        }
        
        // 6. EXÉCUTER LA TRANSACTION (ATOMIQUE)
        try {
            // 6a. Retirer l'argent de l'acheteur
            if (!plugin.getVaultManager().withdraw(buyer, totalPrice)) {
                seller.sendMessage("§c⚠ Erreur lors du retrait d'argent de l'acheteur!");
                return false;
            }
            
            // 6b. Ajouter l'argent au vendeur
            if (!plugin.getVaultManager().deposit(seller, totalPrice)) {
                // ROLLBACK: Rembourser l'acheteur
                plugin.getVaultManager().deposit(buyer, totalPrice);
                seller.sendMessage("§c⚠ Erreur lors de l'ajout d'argent au vendeur!");
                return false;
            }
            
            // 6c. Retirer les items du storage du vendeur
            sellerStorage.removeItemById(itemId, quantity);
            sellerStorage.save(plugin);
            
            // 6d. Donner les items à l'acheteur (si connecté, sinon en attente)
            if (buyer.isOnline()) {
                Player buyerPlayer = buyer.getPlayer();
                if (buyerPlayer != null) {
                    // Donner les items directement
                    giveItemsToPlayer(buyerPlayer, itemId, quantity);
                    
                    // Notification acheteur
                    buyerPlayer.sendMessage("§8[§6Quantum§8] §a✓ Votre ordre a été rempli!");
                    buyerPlayer.sendMessage("§7Item: §f" + formatItemName(itemId));
                    buyerPlayer.sendMessage("§7Quantité: §a" + quantity);
                    buyerPlayer.sendMessage("§7Prix payé: §6" + String.format("%.2f", totalPrice) + "$");
                    buyerPlayer.sendMessage("§7Vendeur: §f" + seller.getName());
                    buyerPlayer.playSound(buyerPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            } else {
                // TODO: Système de stockage temporaire pour items en attente
                plugin.getQuantumLogger().info("Buyer " + buyerName + " is offline. Items need to be stored temporarily.");
            }
            
            // 7. Supprimer l'ordre de orders.yml
            ordersConfig.set(orderPath, null);
            ordersConfig.save(ordersFile);
            
            // 8. Notification vendeur
            seller.sendMessage("§8[§6Quantum§8] §a✓ Vente réussie!");
            seller.sendMessage("§7Item: §f" + formatItemName(itemId));
            seller.sendMessage("§7Quantité: §a" + quantity);
            seller.sendMessage("§7Argent reçu: §6+" + String.format("%.2f", totalPrice) + "$");
            seller.sendMessage("§7Acheteur: §f" + buyerName);
            seller.playSound(seller.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            
            return true;
            
        } catch (Exception e) {
            seller.sendMessage("§c⚠ Erreur critique lors de la transaction!");
            plugin.getQuantumLogger().error("Transaction failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Donne des items à un joueur (Nexo ou vanilla)
     */
    private void giveItemsToPlayer(Player player, String itemId, int amount) {
        if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            giveNexoItems(player, nexoId, amount);
        } else if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            try {
                org.bukkit.Material material = org.bukkit.Material.valueOf(materialName);
                giveVanillaItems(player, material, amount);
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid material: " + materialName);
            }
        }
    }
    
    /**
     * Donne des items Nexo au joueur
     */
    private void giveNexoItems(Player player, String nexoId, int amount) {
        int remaining = amount;
        com.nexomc.nexo.items.ItemBuilder itemBuilder = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId);
        if (itemBuilder == null) {
            plugin.getQuantumLogger().warning("Cannot give Nexo item " + nexoId + " - ItemBuilder is null");
            return;
        }
        
        ItemStack nexoItem = itemBuilder.build();
        int maxStackSize = nexoItem.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId).build();
            stack.setAmount(stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }
    
    /**
     * Donne des items vanilla au joueur
     */
    private void giveVanillaItems(Player player, org.bukkit.Material material, int amount) {
        int remaining = amount;
        int maxStackSize = material.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(material, stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }
    
    /**
     * Formate joliment un itemId pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }
}
