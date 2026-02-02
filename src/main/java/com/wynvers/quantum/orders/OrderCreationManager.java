package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère la création d'offres d'achat (recherche)
 * Supporte les items Minecraft (minecraft:stone) et Nexo (nexo:custom_item)
 * 
 * Workflow:
 * 1. Player clique sur item en mode RECHERCHE
 * 2. Menu quantité s'ouvre
 * 3. Player sélectionne quantité
 * 4. Menu prix s'ouvre
 * 5. Player sélectionne prix
 * 6. Offre créée et placée dans la bonne catégorie
 * 7. ARGENT RETIRÉ IMMÉDIATEMENT
 */
public class OrderCreationManager {
    
    private final Quantum plugin;
    
    // Stockage des sessions de création d'ordres
    private final Map<UUID, OrderCreationSession> sessions = new HashMap<>();
    
    public OrderCreationManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Étape 1: Démarre la création d'une offre
     * Vérifie que l'item est stockable et qu'au moins 1 est en stock
     */
    public boolean startOrderCreation(Player player, String itemId, int stockQuantity) {
        // Récupérer la fourchette de prix depuis orders_template.yml
        PriceRange priceRange = getPriceRange(itemId);
        if (priceRange == null) {
            player.sendMessage("§c⚠ Cet item n'a pas de fourchette de prix définie!");
            return false;
        }
        
        // Formater le nom de l'item
        String itemName = formatItemName(itemId);
        
        // Créer la session
        OrderCreationSession session = new OrderCreationSession(
            itemId,
            itemName,
            stockQuantity,
            priceRange.getMinPrice(),
            priceRange.getMaxPrice()
        );
        
        sessions.put(player.getUniqueId(), session);
        
        return true;
    }
    
    /**
     * Récupère la session de création d'ordre d'un joueur
     */
    public OrderCreationSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }
    
    /**
     * Finalise l'ordre avec la session actuelle
     * NOUVEAU: Retire l'argent du joueur immédiatement après la création
     */
    public boolean finalizeOrder(Player player) {
        OrderCreationSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§c⚠ Aucune session de création d'ordre active!");
            return false;
        }
        
        // Trouver la catégorie de l'item
        String category = findItemCategory(session.getItemId());
        if (category == null) {
            player.sendMessage("§c⚠ Cet item n'appartient à aucune catégorie d'offres!");
            player.sendMessage("§7Contact un administrateur pour l'ajouter.");
            cancelOrder(player);
            return false;
        }
        
        // Vérifier que le joueur a assez d'argent (double-check)
        double totalPrice = session.getTotalPrice();
        
        if (plugin.getVaultManager().isEnabled()) {
            if (!plugin.getVaultManager().has(player, totalPrice)) {
                player.sendMessage("§c⚠ Vous n'avez pas assez d'argent!");
                player.sendMessage("§7Requis: §6" + String.format("%.2f", totalPrice) + "$");
                player.sendMessage("§7Solde: §6" + String.format("%.2f", plugin.getVaultManager().getBalance(player)) + "$");
                cancelOrder(player);
                return false;
            }
        }
        
        // Sauvegarder l'offre dans orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        YamlConfiguration ordersConfig;
        
        if (ordersFile.exists()) {
            ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        } else {
            ordersConfig = new YamlConfiguration();
        }
        
        // Générer un ID unique pour l'offre
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        String path = category + "." + orderId;
        
        ordersConfig.set(path + ".orderer", player.getName());
        ordersConfig.set(path + ".orderer_uuid", player.getUniqueId().toString());
        ordersConfig.set(path + ".item", session.getItemId()); // Format: minecraft:stone ou nexo:custom_item
        ordersConfig.set(path + ".quantity", session.getQuantity());
        ordersConfig.set(path + ".price_per_unit", session.getPrice());
        ordersConfig.set(path + ".total_price", totalPrice);
        ordersConfig.set(path + ".created_at", System.currentTimeMillis());
        
        try {
            ordersConfig.save(ordersFile);
            
            // === RETRAIT D'ARGENT ===
            if (plugin.getVaultManager().isEnabled()) {
                if (plugin.getVaultManager().withdraw(player, totalPrice)) {
                    player.sendMessage("§8[§6Quantum§8] §c-" + String.format("%.2f", totalPrice) + "$");
                    player.sendMessage("§7Argent bloqué jusqu'à ce que l'ordre soit rempli.");
                } else {
                    // Le retrait a échoué, supprimer l'ordre créé
                    player.sendMessage("§c⚠ Erreur lors du retrait de l'argent!");
                    ordersConfig.set(path, null);
                    ordersConfig.save(ordersFile);
                    cancelOrder(player);
                    return false;
                }
            } else {
                // Vault non disponible, supprimer l'ordre
                player.sendMessage("§c⚠ Système économique indisponible!");
                ordersConfig.set(path, null);
                ordersConfig.save(ordersFile);
                cancelOrder(player);
                return false;
            }
            
            // Nettoyer la session
            sessions.remove(player.getUniqueId());
            
            return true;
            
        } catch (Exception e) {
            player.sendMessage("§c⚠ Erreur lors de la sauvegarde de l'offre!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trouve la catégorie d'un item dans orders_template.yml
     * Supporte minecraft:stone et nexo:custom_item
     */
    private String findItemCategory(String itemId) {
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            return null;
        }
        
        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        
        // Parcourir toutes les catégories
        for (String category : template.getKeys(false)) {
            if (template.contains(category + ".items." + itemId)) {
                return category;
            }
        }
        
        return null;
    }
    
    /**
     * Récupère la fourchette de prix d'un item depuis orders_template.yml
     */
    public PriceRange getPriceRange(String itemId) {
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            return null;
        }
        
        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        
        // Chercher l'item dans toutes les catégories
        for (String category : template.getKeys(false)) {
            String itemPath = category + ".items." + itemId;
            if (template.contains(itemPath)) {
                double minPrice = template.getDouble(itemPath + ".min_price", 1.0);
                double maxPrice = template.getDouble(itemPath + ".max_price", 100.0);
                return new PriceRange(minPrice, maxPrice);
            }
        }
        
        return null;
    }
    
    /**
     * Convertit un ItemStack en itemId (minecraft:xxx ou nexo:xxx)
     */
    public static String getItemId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        // Vérifier si c'est un item Nexo
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            // TODO: Intégrer avec NexoItemsManager pour obtenir le vrai ID Nexo
            // Pour l'instant, on utilise le custom model data
            String nexoId = "nexo:item_" + item.getItemMeta().getCustomModelData();
            return nexoId;
        }
        
        // Item Minecraft vanilla
        return "minecraft:" + item.getType().name().toLowerCase();
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
    
    /**
     * Annule l'offre en cours de création
     */
    public void cancelOrder(Player player) {
        sessions.remove(player.getUniqueId());
    }
    
    /**
     * Classe interne: Fourchette de prix
     */
    public static class PriceRange {
        private final double minPrice;
        private final double maxPrice;
        
        public PriceRange(double minPrice, double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
        
        public double getMinPrice() { return minPrice; }
        public double getMaxPrice() { return maxPrice; }
        
        public double getPrice25() {
            return minPrice + (maxPrice - minPrice) * 0.25;
        }
        
        public double getPrice50() {
            return minPrice + (maxPrice - minPrice) * 0.5;
        }
        
        public double getPrice75() {
            return minPrice + (maxPrice - minPrice) * 0.75;
        }
    }
}
