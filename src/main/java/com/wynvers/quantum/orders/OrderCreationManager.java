package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère la création d'offres d'achat (recherche)
 * Workflow:
 * 1. Player clique sur item en mode RECHERCHE
 * 2. Menu quantité s'ouvre
 * 3. Player sélectionne quantité
 * 4. Menu prix s'ouvre
 * 5. Player sélectionne prix
 * 6. Offre créée et placée dans la bonne catégorie
 */
public class OrderCreationManager {
    
    private final Quantum plugin;
    
    // Stockage temporaire des offres en cours de création
    private final Map<UUID, PendingOrder> pendingOrders = new HashMap<>();
    
    public OrderCreationManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Étape 1: Démarre la création d'une offre
     * Vérifie que l'item est stockable et qu'au moins 1 est en stock
     */
    public boolean startOrderCreation(Player player, Material material, int stockQuantity) {
        if (stockQuantity <= 0) {
            player.sendMessage("§c⚠ Vous devez avoir au moins 1 item en stock!");
            return false;
        }
        
        // Créer l'offre en attente
        PendingOrder order = new PendingOrder(player.getUniqueId(), material, stockQuantity);
        pendingOrders.put(player.getUniqueId(), order);
        
        return true;
    }
    
    /**
     * Étape 2: Définit la quantité de l'offre
     */
    public boolean setOrderQuantity(Player player, int quantity) {
        PendingOrder order = pendingOrders.get(player.getUniqueId());
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours de création!");
            return false;
        }
        
        // Vérifier que la quantité est valide (> 0)
        if (quantity <= 0) {
            player.sendMessage("§cLa quantité doit être supérieure à 0!");
            return false;
        }
        
        order.setQuantity(quantity);
        return true;
    }
    
    /**
     * Étape 3: Définit le prix unitaire et finalise l'offre
     */
    public boolean setOrderPriceAndFinalize(Player player, double pricePerUnit) {
        PendingOrder order = pendingOrders.get(player.getUniqueId());
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours de création!");
            return false;
        }
        
        if (order.getQuantity() <= 0) {
            player.sendMessage("§cErreur: Quantité non définie!");
            return false;
        }
        
        // Vérifier la fourchette de prix
        PriceRange range = getPriceRange(order.getMaterial());
        if (range != null) {
            if (pricePerUnit < range.getMinPrice() || pricePerUnit > range.getMaxPrice()) {
                player.sendMessage("§cLe prix doit être entre " + range.getMinPrice() + "$ et " + range.getMaxPrice() + "$!");
                return false;
            }
        }
        
        order.setPricePerUnit(pricePerUnit);
        
        // Finaliser et sauvegarder l'offre
        return finalizeOrder(player, order);
    }
    
    /**
     * Finalise l'offre: la sauvegarde dans orders.yml et la place dans la bonne catégorie
     */
    private boolean finalizeOrder(Player player, PendingOrder order) {
        // Trouver la catégorie de l'item
        String category = findItemCategory(order.getMaterial());
        if (category == null) {
            player.sendMessage("§c⚠ Cet item n'appartient à aucune catégorie d'offres!");
            player.sendMessage("§7Contact un administrateur pour l'ajouter.");
            cancelOrder(player);
            return false;
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
        ordersConfig.set(path + ".item", order.getMaterial().name());
        ordersConfig.set(path + ".quantity", order.getQuantity());
        ordersConfig.set(path + ".price_per_unit", order.getPricePerUnit());
        ordersConfig.set(path + ".total_price", order.getQuantity() * order.getPricePerUnit());
        ordersConfig.set(path + ".created_at", System.currentTimeMillis());
        
        try {
            ordersConfig.save(ordersFile);
            
            // Message de succès
            player.sendMessage("§a§l✓ Offre créée avec succès!");
            player.sendMessage("§7Item: §f" + order.getMaterial().name());
            player.sendMessage("§7Quantité: §a" + order.getQuantity());
            player.sendMessage("§7Prix unitaire: §6" + order.getPricePerUnit() + "$");
            player.sendMessage("§7Prix total: §e" + (order.getQuantity() * order.getPricePerUnit()) + "$");
            player.sendMessage("§7Catégorie: §b" + category);
            player.sendMessage("");
            player.sendMessage("§eLes joueurs peuvent maintenant vous vendre cet item!");
            
            // Nettoyer l'offre en attente
            pendingOrders.remove(player.getUniqueId());
            
            return true;
            
        } catch (Exception e) {
            player.sendMessage("§c⚠ Erreur lors de la sauvegarde de l'offre!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trouve la catégorie d'un item dans orders_template.yml
     */
    private String findItemCategory(Material material) {
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            return null;
        }
        
        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        
        // Parcourir toutes les catégories
        for (String category : template.getKeys(false)) {
            if (template.contains(category + ".items." + material.name())) {
                return category;
            }
        }
        
        return null;
    }
    
    /**
     * Récupère la fourchette de prix d'un item depuis orders_template.yml
     */
    public PriceRange getPriceRange(Material material) {
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            return null;
        }
        
        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        
        // Chercher l'item dans toutes les catégories
        for (String category : template.getKeys(false)) {
            String itemPath = category + ".items." + material.name();
            if (template.contains(itemPath)) {
                double minPrice = template.getDouble(itemPath + ".min_price", 1.0);
                double maxPrice = template.getDouble(itemPath + ".max_price", 100.0);
                return new PriceRange(minPrice, maxPrice);
            }
        }
        
        return null;
    }
    
    /**
     * Récupère l'offre en cours de création d'un joueur
     */
    public PendingOrder getPendingOrder(Player player) {
        return pendingOrders.get(player.getUniqueId());
    }
    
    /**
     * Annule l'offre en cours de création
     */
    public void cancelOrder(Player player) {
        pendingOrders.remove(player.getUniqueId());
        player.sendMessage("§eOffre annulée.");
    }
    
    /**
     * Classe interne: Offre en cours de création
     */
    public static class PendingOrder {
        private final UUID playerUUID;
        private final Material material;
        private final int maxQuantity;
        private int quantity = 0;
        private double pricePerUnit = 0.0;
        
        public PendingOrder(UUID playerUUID, Material material, int maxQuantity) {
            this.playerUUID = playerUUID;
            this.material = material;
            this.maxQuantity = maxQuantity;
        }
        
        public UUID getPlayerUUID() { return playerUUID; }
        public Material getMaterial() { return material; }
        public int getMaxQuantity() { return maxQuantity; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
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
