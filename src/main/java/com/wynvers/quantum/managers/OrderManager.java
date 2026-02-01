package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire du système d'ordres refactorisé
 * - Chargement dynamique des items depuis orders_template.yml
 * - Catégorisation automatique par type
 * - Intégration LuckPerms pour les durées
 * - Support des timers avec placeholders
 */
public class OrderManager {
    private final Quantum plugin;
    private final Map<String, OrderItem> items;
    private final Map<UUID, Order> activeOrders;
    private LuckPerms luckPerms;
    private boolean luckPermsAvailable;

    public OrderManager(Quantum plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        this.activeOrders = new HashMap<>();
        
        // Initialiser LuckPerms si disponible
        initLuckPerms();
        
        // Charger les items
        loadItems();
        
        plugin.getQuantumLogger().success("✓ OrderManager initialized with " + items.size() + " items");
    }

    /**
     * Initialise LuckPerms si disponible
     */
    private void initLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.luckPermsAvailable = true;
                plugin.getQuantumLogger().success("✓ LuckPerms integration enabled");
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("⚠ LuckPerms found but failed to initialize");
                this.luckPermsAvailable = false;
            }
        } else {
            plugin.getQuantumLogger().warning("⚠ LuckPerms not found - using default order durations");
            this.luckPermsAvailable = false;
        }
    }

    /**
     * Charge tous les items depuis orders_template.yml
     */
    public void loadItems() {
        items.clear();
        
        File file = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!file.exists()) {
            plugin.getQuantumLogger().warning("⚠ orders_template.yml not found! Creating default...");
            plugin.saveResource("orders_template.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                try {
                    ConfigurationSection section = config.getConfigurationSection(key);
                    OrderItem item = new OrderItem(key, section);
                    items.put(key.toLowerCase(), item);
                    
                    plugin.getQuantumLogger().info("  ✓ Loaded: " + key + " (" + item.getType().getDisplayName() + ")");
                } catch (Exception e) {
                    plugin.getQuantumLogger().error("✗ Failed to load item: " + key);
                    e.printStackTrace();
                }
            }
        }
        
        plugin.getQuantumLogger().success("✓ Loaded " + items.size() + " items from orders_template.yml");
    }

    /**
     * Récupère un item par son ID
     */
    public OrderItem getItem(String itemId) {
        return items.get(itemId.toLowerCase());
    }

    /**
     * Récupère tous les items d'une catégorie
     */
    public List<OrderItem> getItemsByType(OrderType type) {
        return items.values().stream()
            .filter(item -> item.getType() == type)
            .collect(Collectors.toList());
    }

    /**
     * Récupère tous les IDs d'items
     */
    public Set<String> getAllItemIds() {
        return new HashSet<>(items.keySet());
    }

    /**
     * Vérifie si un item existe
     */
    public boolean hasItem(String itemId) {
        return items.containsKey(itemId.toLowerCase());
    }

    /**
     * Valide un prix pour un item
     */
    public boolean validatePrice(String itemId, double price) {
        OrderItem item = getItem(itemId);
        if (item == null) return false;
        return item.isValidPrice(price);
    }

    /**
     * Récupère la durée d'ordre pour un joueur (en jours)
     * Basé sur les permissions LuckPerms
     */
    public long getOrderDurationForPlayer(Player player) {
        if (!luckPermsAvailable) {
            return plugin.getConfig().getLong("orders.durations.default", 3);
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return plugin.getConfig().getLong("orders.durations.default", 3);
            }

            // Vérifie chaque permission dans l'ordre (du plus élevé au plus bas)
            ConfigurationSection durationsSection = plugin.getConfig().getConfigurationSection("orders.durations");
            if (durationsSection == null) {
                return 3; // Fallback
            }

            // Trier les permissions par durée (desc)
            List<Map.Entry<String, Long>> sortedPerms = durationsSection.getKeys(false).stream()
                .filter(key -> !key.equals("default"))
                .map(key -> new AbstractMap.SimpleEntry<>(key, durationsSection.getLong(key)))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

            // Vérifie quelle permission le joueur a
            for (Map.Entry<String, Long> entry : sortedPerms) {
                if (user.getCachedData().getPermissionData().checkPermission(entry.getKey()).asBoolean()) {
                    return entry.getValue();
                }
            }

            return durationsSection.getLong("default", 3);
            
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Error getting order duration for " + player.getName());
            e.printStackTrace();
            return plugin.getConfig().getLong("orders.durations.default", 3);
        }
    }

    /**
     * Récupère la limite d'ordres actifs pour un joueur
     */
    public int getMaxActiveOrdersForPlayer(Player player) {
        if (!luckPermsAvailable) {
            return plugin.getConfig().getInt("orders.max-active-orders.default", 3);
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return plugin.getConfig().getInt("orders.max-active-orders.default", 3);
            }

            ConfigurationSection maxSection = plugin.getConfig().getConfigurationSection("orders.max-active-orders");
            if (maxSection == null) {
                return 3;
            }

            // Trier par limite (desc)
            List<Map.Entry<String, Integer>> sortedPerms = maxSection.getKeys(false).stream()
                .filter(key -> !key.equals("default"))
                .map(key -> new AbstractMap.SimpleEntry<>(key, maxSection.getInt(key)))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

            for (Map.Entry<String, Integer> entry : sortedPerms) {
                if (user.getCachedData().getPermissionData().checkPermission(entry.getKey()).asBoolean()) {
                    return entry.getValue();
                }
            }

            return maxSection.getInt("default", 3);
            
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Error getting max orders for " + player.getName());
            e.printStackTrace();
            return plugin.getConfig().getInt("orders.max-active-orders.default", 3);
        }
    }

    /**
     * Crée un nouvel ordre
     */
    public Order createOrder(Player player, String itemId, int quantity, double pricePerUnit) {
        long duration = getOrderDurationForPlayer(player);
        Order order = new Order(
            player.getUniqueId(),
            player.getName(),
            itemId,
            quantity,
            pricePerUnit,
            duration
        );
        
        activeOrders.put(order.getOrderId(), order);
        return order;
    }

    /**
     * Récupère tous les ordres actifs pour un item
     */
    public List<Order> getActiveOrdersForItem(String itemId) {
        return activeOrders.values().stream()
            .filter(order -> order.getItemId().equalsIgnoreCase(itemId))
            .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
            .filter(order -> !order.isExpired())
            .sorted(Comparator.comparingDouble(Order::getPricePerUnit).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Récupère tous les ordres actifs d'un joueur
     */
    public List<Order> getActiveOrdersForPlayer(UUID playerId) {
        return activeOrders.values().stream()
            .filter(order -> order.getPlayerId().equals(playerId))
            .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
            .filter(order -> !order.isExpired())
            .collect(Collectors.toList());
    }

    /**
     * Compte les ordres actifs d'un joueur
     */
    public int countActiveOrdersForPlayer(UUID playerId) {
        return getActiveOrdersForPlayer(playerId).size();
    }

    /**
     * Annule un ordre
     */
    public boolean cancelOrder(UUID orderId) {
        Order order = activeOrders.get(orderId);
        if (order != null && order.getStatus() == OrderStatus.ACTIVE) {
            order.setStatus(OrderStatus.CANCELLED);
            activeOrders.remove(orderId);
            return true;
        }
        return false;
    }

    /**
     * Nettoie les ordres expirés
     */
    public void cleanExpiredOrders() {
        activeOrders.values().removeIf(order -> {
            if (order.isExpired()) {
                order.setStatus(OrderStatus.EXPIRED);
                return true;
            }
            return false;
        });
    }

    /**
     * Formate le temps restant d'un ordre
     */
    public String formatTimeRemaining(Order order) {
        String format = plugin.getConfig().getString("orders.timer.format", "<#32b8c6>%days%j %hours%h %minutes%m %seconds%s");
        String expiredFormat = plugin.getConfig().getString("orders.timer.format-expired", "<red>Expiré");
        
        if (order.isExpired()) {
            return expiredFormat;
        }
        
        return order.getFormattedTimeRemaining(format);
    }
}
