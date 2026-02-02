package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.*;
import com.wynvers.quantum.utils.ItemUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    private final Map<String, String> itemToCategory; // itemId -> category
    private final Map<String, List<String>> categoryItems; // category -> list of items
    private final Map<UUID, Order> activeOrders;
    private LuckPerms luckPerms;
    private boolean luckPermsAvailable;

    public OrderManager(Quantum plugin) {
        this.plugin = plugin;
        this.itemToCategory = new HashMap<>();
        this.categoryItems = new HashMap<>();
        this.activeOrders = new HashMap<>();
        
        // Initialiser LuckPerms si disponible
        initLuckPerms();
        
        // Charger les items
        loadItems();
        
        plugin.getQuantumLogger().success("✓ OrderManager initialized with " + itemToCategory.size() + " items");
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
     * FIX: Supporte maintenant la structure imbriquée avec min_price/max_price
     */
    public void loadItems() {
        itemToCategory.clear();
        categoryItems.clear();
        
        File file = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!file.exists()) {
            plugin.getQuantumLogger().warning("⚠ orders_template.yml not found!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Parcourir chaque catégorie
        for (String category : config.getKeys(false)) {
            if (!config.isConfigurationSection(category)) continue;
            
            ConfigurationSection categorySection = config.getConfigurationSection(category);
            
            // FIX: Vérifier que la section 'items' existe
            if (!categorySection.isConfigurationSection("items")) {
                plugin.getQuantumLogger().warning("⚠ Category " + category + " has no 'items' section");
                continue;
            }
            
            ConfigurationSection itemsSection = categorySection.getConfigurationSection("items");
            List<String> items = new ArrayList<>();
            
            // FIX: Parcourir les clés de la section items (ce sont les itemIds)
            for (String itemId : itemsSection.getKeys(false)) {
                items.add(itemId);
                // Mapper chaque item à sa catégorie
                itemToCategory.put(itemId.toLowerCase(), category.toLowerCase());
            }
            
            categoryItems.put(category.toLowerCase(), items);
            
            plugin.getQuantumLogger().info("  ✓ Loaded category " + category + " with " + items.size() + " items");
        }
        
        plugin.getQuantumLogger().success("✓ Loaded " + itemToCategory.size() + " items from " + categoryItems.size() + " categories");
    }

    /**
     * Détecte automatiquement la catégorie d'un item
     */
    public String getCategoryForItem(String itemId) {
        return itemToCategory.get(itemId.toLowerCase());
    }

    /**
     * Vérifie si un item est autorisé
     */
    public boolean isItemAllowed(String itemId) {
        return itemToCategory.containsKey(itemId.toLowerCase());
    }

    /**
     * Récupère tous les items d'une catégorie
     */
    public List<String> getItemsForCategory(String category) {
        return categoryItems.getOrDefault(category.toLowerCase(), new ArrayList<>());
    }

    /**
     * Récupère toutes les catégories
     */
    public Set<String> getAllCategories() {
        return new HashSet<>(categoryItems.keySet());
    }

    /**
     * Récupère tous les IDs d'items
     */
    public Set<String> getAllItemIds() {
        return new HashSet<>(itemToCategory.keySet());
    }

    /**
     * Récupère tous les items avec leur display name pour tab completion
     */
    public Map<String, String> getAllItemsWithDisplayNames() {
        Map<String, String> result = new HashMap<>();
        
        for (String itemId : itemToCategory.keySet()) {
            // Générer le display name
            String displayName = getDisplayNameForItem(itemId);
            result.put(itemId, displayName);
        }
        
        return result;
    }

    /**
     * Génère le display name pour un item
     */
    private String getDisplayNameForItem(String itemId) {
        // Si c'est un item Nexo
        if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            ItemStack nexoItem = ItemUtils.getNexoItem(nexoId);
            if (nexoItem != null && nexoItem.hasItemMeta() && nexoItem.getItemMeta().hasDisplayName()) {
                return nexoItem.getItemMeta().getDisplayName();
            }
            return nexoId.replace("_", " ");
        }
        
        // Si c'est un item Minecraft
        if (itemId.startsWith("minecraft:")) {
            String minecraftId = itemId.substring(10);
            try {
                Material material = Material.valueOf(minecraftId.toUpperCase());
                return formatMaterialName(material);
            } catch (IllegalArgumentException e) {
                return minecraftId.replace("_", " ");
            }
        }
        
        return itemId.replace("_", " ");
    }

    /**
     * Formate le nom d'un material Minecraft
     */
    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return result.toString();
    }

    /**
     * Récupère la durée d'ordre pour un joueur (en jours)
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

            ConfigurationSection durationsSection = plugin.getConfig().getConfigurationSection("orders.durations");
            if (durationsSection == null) {
                return 3;
            }

            List<Map.Entry<String, Long>> sortedPerms = durationsSection.getKeys(false).stream()
                .filter(key -> !key.equals("default"))
                .map(key -> new AbstractMap.SimpleEntry<>(key, durationsSection.getLong(key)))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

            for (Map.Entry<String, Long> entry : sortedPerms) {
                if (user.getCachedData().getPermissionData().checkPermission(entry.getKey()).asBoolean()) {
                    return entry.getValue();
                }
            }

            return durationsSection.getLong("default", 3);
            
        } catch (Exception e) {
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
            return plugin.getConfig().getInt("orders.max-active-orders.default", 3);
        }
    }

    /**
     * Crée un nouvel ordre
     */
    public Order createOrder(Player player, String itemId, int quantity, double pricePerUnit) {
        long duration = getOrderDurationForPlayer(player);
        String category = getCategoryForItem(itemId);
        
        Order order = new Order(
            player.getUniqueId(),
            player.getName(),
            itemId,
            category,
            quantity,
            pricePerUnit,
            duration
        );
        
        activeOrders.put(order.getOrderId(), order);
        return order;
    }

    /**
     * Récupère tous les ordres actifs pour une catégorie
     */
    public List<Order> getActiveOrdersForCategory(String category) {
        return activeOrders.values().stream()
            .filter(order -> category.equalsIgnoreCase(order.getCategory()))
            .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
            .filter(order -> !order.isExpired())
            .sorted(Comparator.comparingDouble(Order::getPricePerUnit).reversed())
            .collect(Collectors.toList());
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
     * Récupère un ordre par son ID
     */
    public Order getOrder(UUID orderId) {
        return activeOrders.get(orderId);
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
}
