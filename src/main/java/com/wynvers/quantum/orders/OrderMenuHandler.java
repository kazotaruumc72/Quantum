package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationManager.PendingOrder;
import com.wynvers.quantum.orders.OrderCreationManager.PriceRange;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * Gère les interactions dans les menus de création d'offres
 * (order_quantity.yml et order_price.yml)
 * 
 * Responsabilités:
 * - Ajustement de la quantité (+5, +50, -5, -50, max)
 * - Ajustement du prix (+5%, +20%, -5%, -20%, max)
 * - Validation de la quantité (passage au menu prix)
 * - Finalisation de l'offre
 * - Annulation de l'offre
 */
public class OrderMenuHandler {
    
    private final Quantum plugin;
    private final OrderCreationManager orderManager;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
    
    public OrderMenuHandler(Quantum plugin) {
        this.plugin = plugin;
        this.orderManager = plugin.getOrderCreationManager();
    }
    
    /**
     * Ajuste la quantité de l'offre en cours
     * @param adjustment Valeur d'ajustement (+5, +50, -5, -50)
     */
    public boolean adjustQuantity(Player player, int adjustment) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        int currentQty = order.getQuantity();
        int newQty = Math.max(1, Math.min(currentQty + adjustment, order.getMaxQuantity()));
        
        order.setQuantity(newQty);
        return true;
    }
    
    /**
     * Définit la quantité au maximum
     */
    public boolean setQuantityMax(Player player) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        order.setQuantity(order.getMaxQuantity());
        return true;
    }
    
    /**
     * Valide la quantité et prépare le passage au menu prix
     * Initialise le prix au milieu de la fourchette (50%)
     */
    public boolean validateQuantity(Player player) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        if (order.getQuantity() <= 0) {
            player.sendMessage("§cLa quantité doit être supérieure à 0!");
            return false;
        }
        
        // Initialiser le prix au milieu de la fourchette
        PriceRange range = orderManager.getPriceRange(order.getItemId());
        if (range != null) {
            double initialPrice = range.getPrice50(); // Prix moyen
            order.setPricePerUnit(initialPrice);
        }
        
        return true;
    }
    
    /**
     * Ajuste le prix de l'offre en cours (en pourcentage)
     * @param adjustment Pourcentage d'ajustement (+5, +20, -5, -20)
     */
    public boolean adjustPrice(Player player, int adjustment) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        PriceRange range = orderManager.getPriceRange(order.getItemId());
        if (range == null) {
            player.sendMessage("§cErreur: Impossible de récupérer la fourchette de prix!");
            return false;
        }
        
        double currentPrice = order.getPricePerUnit();
        double priceRange = range.getMaxPrice() - range.getMinPrice();
        double adjustmentAmount = priceRange * (adjustment / 100.0);
        
        double newPrice = currentPrice + adjustmentAmount;
        newPrice = Math.max(range.getMinPrice(), Math.min(newPrice, range.getMaxPrice()));
        
        order.setPricePerUnit(newPrice);
        return true;
    }
    
    /**
     * Définit le prix au maximum
     */
    public boolean setPriceMax(Player player) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        PriceRange range = orderManager.getPriceRange(order.getItemId());
        if (range == null) {
            player.sendMessage("§cErreur: Impossible de récupérer la fourchette de prix!");
            return false;
        }
        
        order.setPricePerUnit(range.getMaxPrice());
        return true;
    }
    
    /**
     * Finalise la création de l'offre
     */
    public boolean finalizeOrder(Player player) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        if (order.getQuantity() <= 0) {
            player.sendMessage("§cLa quantité doit être supérieure à 0!");
            return false;
        }
        
        if (order.getPricePerUnit() <= 0) {
            player.sendMessage("§cLe prix doit être supérieur à 0!");
            return false;
        }
        
        // Utiliser la méthode existante pour finaliser
        return orderManager.setOrderPriceAndFinalize(player, order.getPricePerUnit());
    }
    
    /**
     * Annule l'offre en cours
     */
    public void cancelOrder(Player player) {
        orderManager.cancelOrder(player);
    }
    
    /**
     * Obtient un placeholder pour le menu de quantité
     */
    public String getQuantityPlaceholder(Player player, String placeholder) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) return "N/A";
        
        switch (placeholder) {
            case "quantum_order_item_name":
                return formatItemName(order.getItemId());
            case "quantum_order_current_quantity":
                return String.valueOf(order.getQuantity());
            case "quantum_order_max_quantity":
                return String.valueOf(order.getMaxQuantity());
            case "quantum_order_price_min":
                PriceRange rangeMin = orderManager.getPriceRange(order.getItemId());
                return rangeMin != null ? priceFormat.format(rangeMin.getMinPrice()) : "N/A";
            case "quantum_order_price_max":
                PriceRange rangeMax = orderManager.getPriceRange(order.getItemId());
                return rangeMax != null ? priceFormat.format(rangeMax.getMaxPrice()) : "N/A";
            case "quantum_order_total_min":
                PriceRange rangeTMin = orderManager.getPriceRange(order.getItemId());
                if (rangeTMin != null) {
                    double total = order.getQuantity() * rangeTMin.getMinPrice();
                    return priceFormat.format(total);
                }
                return "N/A";
            case "quantum_order_total_max":
                PriceRange rangeTMax = orderManager.getPriceRange(order.getItemId());
                if (rangeTMax != null) {
                    double total = order.getQuantity() * rangeTMax.getMaxPrice();
                    return priceFormat.format(total);
                }
                return "N/A";
            default:
                return null;
        }
    }
    
    /**
     * Obtient un placeholder pour le menu de prix
     */
    public String getPricePlaceholder(Player player, String placeholder) {
        PendingOrder order = orderManager.getPendingOrder(player);
        if (order == null) return "N/A";
        
        switch (placeholder) {
            case "quantum_order_item_name":
                return formatItemName(order.getItemId());
            case "quantum_order_current_quantity":
                return String.valueOf(order.getQuantity());
            case "quantum_order_current_price":
                return priceFormat.format(order.getPricePerUnit());
            case "quantum_order_current_total":
                return priceFormat.format(order.getQuantity() * order.getPricePerUnit());
            case "quantum_order_price_min":
                PriceRange rangeMin = orderManager.getPriceRange(order.getItemId());
                return rangeMin != null ? priceFormat.format(rangeMin.getMinPrice()) : "N/A";
            case "quantum_order_price_max":
                PriceRange rangeMax = orderManager.getPriceRange(order.getItemId());
                return rangeMax != null ? priceFormat.format(rangeMax.getMaxPrice()) : "N/A";
            case "quantum_order_total_min":
                PriceRange rangeTMin = orderManager.getPriceRange(order.getItemId());
                if (rangeTMin != null) {
                    double total = order.getQuantity() * rangeTMin.getMinPrice();
                    return priceFormat.format(total);
                }
                return "N/A";
            case "quantum_order_total_max":
                PriceRange rangeTMax = orderManager.getPriceRange(order.getItemId());
                if (rangeTMax != null) {
                    double total = order.getQuantity() * rangeTMax.getMaxPrice();
                    return priceFormat.format(total);
                }
                return "N/A";
            default:
                return null;
        }
    }
    
    /**
     * Formate joliment un itemId pour l'affichage
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return "[Nexo] " + itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            String name = itemId.substring(10).replace("_", " ");
            // Capitalize first letter of each word
            String[] words = name.split(" ");
            StringBuilder formatted = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                             .append(word.substring(1).toLowerCase())
                             .append(" ");
                }
            }
            return formatted.toString().trim();
        }
        return itemId;
    }
}
