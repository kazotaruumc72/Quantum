package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class OrderManager {
    private final Quantum plugin;
    private final Map<String, ItemPrice> itemPrices;
    private final Map<UUID, List<Order>> playerOrders;
    
    public OrderManager(Quantum plugin) {
        this.plugin = plugin;
        this.itemPrices = new HashMap<>();
        this.playerOrders = new HashMap<>();
        loadPrices();
    }
    
    /**
     * Charge les prix depuis tous les fichiers de configuration
     */
    public void loadPrices() {
        itemPrices.clear();
        
        String[] categories = {"items", "cultures", "loots", "armures", "outils"};
        
        for (String category : categories) {
            File file = new File(plugin.getDataFolder(), "menus/orders_" + category + ".yml");
            if (!file.exists()) continue;
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection pricesSection = config.getConfigurationSection("prices");
            
            if (pricesSection != null) {
                for (String itemKey : pricesSection.getKeys(false)) {
                    double min = pricesSection.getDouble(itemKey + ".min", 0.0);
                    double max = pricesSection.getDouble(itemKey + ".max", 100.0);
                    String displayName = pricesSection.getString(itemKey + ".display_name", itemKey);
                    
                    itemPrices.put(itemKey.toLowerCase(), new ItemPrice(itemKey, displayName, min, max));
                }
            }
        }
        
        plugin.getQuantumLogger().info("Chargé " + itemPrices.size() + " prix d'items pour le système d'ordres");
    }
    
    /**
     * Vérifie si un item a une configuration de prix
     */
    public boolean hasItemPrice(String itemKey) {
        return itemPrices.containsKey(itemKey.toLowerCase());
    }
    
    /**
     * Récupère les informations de prix d'un item
     */
    public ItemPrice getItemPrice(String itemKey) {
        return itemPrices.get(itemKey.toLowerCase());
    }
    
    /**
     * Récupère tous les identifiants d'items configurés
     */
    public List<String> getAllItemKeys() {
        return new ArrayList<>(itemPrices.keySet());
    }
    
    /**
     * Vérifie si le prix est dans les limites acceptables
     */
    public PriceValidation validatePrice(String itemKey, double price) {
        ItemPrice itemPrice = getItemPrice(itemKey.toLowerCase());
        
        if (itemPrice == null) {
            return new PriceValidation(false, PriceStatus.ITEM_NOT_FOUND, 0, 0);
        }
        
        if (price < itemPrice.minPrice) {
            return new PriceValidation(false, PriceStatus.TOO_LOW, itemPrice.minPrice, itemPrice.maxPrice);
        }
        
        if (price > itemPrice.maxPrice) {
            return new PriceValidation(false, PriceStatus.TOO_HIGH, itemPrice.minPrice, itemPrice.maxPrice);
        }
        
        return new PriceValidation(true, PriceStatus.VALID, itemPrice.minPrice, itemPrice.maxPrice);
    }
    
    /**
     * Crée une nouvelle offre
     */
    public Order createOrder(Player player, String itemKey, int amount, double pricePerUnit) {
        UUID playerId = player.getUniqueId();
        
        Order order = new Order(
            UUID.randomUUID(),
            playerId,
            player.getName(),
            itemKey,
            amount,
            pricePerUnit,
            System.currentTimeMillis()
        );
        
        playerOrders.computeIfAbsent(playerId, k -> new ArrayList<>()).add(order);
        
        return order;
    }
    
    /**
     * Récupère toutes les offres actives pour un item
     */
    public List<Order> getOrdersForItem(String itemKey) {
        List<Order> orders = new ArrayList<>();
        
        for (List<Order> playerOrderList : playerOrders.values()) {
            for (Order order : playerOrderList) {
                if (order.itemKey.equalsIgnoreCase(itemKey) && order.isActive()) {
                    orders.add(order);
                }
            }
        }
        
        // Trier par prix (du plus élevé au plus bas)
        orders.sort((o1, o2) -> Double.compare(o2.pricePerUnit, o1.pricePerUnit));
        
        return orders;
    }
    
    /**
     * Récupère toutes les offres d'un joueur
     */
    public List<Order> getPlayerOrders(UUID playerId) {
        return playerOrders.getOrDefault(playerId, new ArrayList<>());
    }
    
    /**
     * Classe représentant un prix d'item
     */
    public static class ItemPrice {
        public final String key;
        public final String displayName;
        public final double minPrice;
        public final double maxPrice;
        
        public ItemPrice(String key, String displayName, double minPrice, double maxPrice) {
            this.key = key;
            this.displayName = displayName;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
    
    /**
     * Classe représentant une offre
     */
    public static class Order {
        public final UUID orderId;
        public final UUID playerId;
        public final String playerName;
        public final String itemKey;
        public int amount;
        public final double pricePerUnit;
        public final long createdAt;
        private boolean active;
        
        public Order(UUID orderId, UUID playerId, String playerName, String itemKey, 
                    int amount, double pricePerUnit, long createdAt) {
            this.orderId = orderId;
            this.playerId = playerId;
            this.playerName = playerName;
            this.itemKey = itemKey;
            this.amount = amount;
            this.pricePerUnit = pricePerUnit;
            this.createdAt = createdAt;
            this.active = true;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public double getTotalPrice() {
            return amount * pricePerUnit;
        }
    }
    
    /**
     * Résultat de validation de prix
     */
    public static class PriceValidation {
        public final boolean valid;
        public final PriceStatus status;
        public final double minPrice;
        public final double maxPrice;
        
        public PriceValidation(boolean valid, PriceStatus status, double minPrice, double maxPrice) {
            this.valid = valid;
            this.status = status;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
    
    /**
     * Statut de prix
     */
    public enum PriceStatus {
        VALID,
        TOO_LOW,
        TOO_HIGH,
        ITEM_NOT_FOUND
    }
}
