package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
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
 * 
 * NOTE: Cette classe est obsolète et remplacée par OrderButtonHandler.
 * Gardée pour compatibilité au cas où.
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
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        session.adjustQuantity(adjustment);
        return true;
    }
    
    /**
     * Définit la quantité au maximum
     */
    public boolean setQuantityMax(Player player) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        session.setQuantity(session.getMaxQuantity());
        return true;
    }
    
    /**
     * Valide la quantité et prépare le passage au menu prix
     * Initialise le prix au milieu de la fourchette (50%)
     */
    public boolean validateQuantity(Player player) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        if (session.getQuantity() <= 0) {
            player.sendMessage("§cLa quantité doit être supérieure à 0!");
            return false;
        }
        
        // Le prix est déjà initialisé dans OrderCreationSession
        session.setStep(OrderCreationSession.Step.PRICE);
        
        return true;
    }
    
    /**
     * Ajuste le prix de l'offre en cours (en pourcentage)
     * @param adjustment Pourcentage d'ajustement (+5, +20, -5, -20)
     */
    public boolean adjustPrice(Player player, int adjustment) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        session.adjustPrice(adjustment);
        return true;
    }
    
    /**
     * Définit le prix au maximum
     */
    public boolean setPriceMax(Player player) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        session.setPrice(session.getMaxPrice());
        return true;
    }
    
    /**
     * Finalise la création de l'offre
     */
    public boolean finalizeOrder(Player player) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune offre en cours!");
            return false;
        }
        
        if (session.getQuantity() <= 0) {
            player.sendMessage("§cLa quantité doit être supérieure à 0!");
            return false;
        }
        
        if (session.getPrice() <= 0) {
            player.sendMessage("§cLe prix doit être supérieur à 0!");
            return false;
        }
        
        // Utiliser la méthode existante pour finaliser
        return orderManager.finalizeOrder(player);
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
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) return "N/A";
        
        switch (placeholder) {
            case "quantum_order_item_name":
                return session.getItemName();
            case "quantum_order_current_quantity":
                return String.valueOf(session.getQuantity());
            case "quantum_order_max_quantity":
                return String.valueOf(session.getMaxQuantity());
            case "quantum_order_price_min":
                return priceFormat.format(session.getMinPrice());
            case "quantum_order_price_max":
                return priceFormat.format(session.getMaxPrice());
            case "quantum_order_total_min":
                double totalMin = session.getQuantity() * session.getMinPrice();
                return priceFormat.format(totalMin);
            case "quantum_order_total_max":
                double totalMax = session.getQuantity() * session.getMaxPrice();
                return priceFormat.format(totalMax);
            default:
                return null;
        }
    }
    
    /**
     * Obtient un placeholder pour le menu de prix
     */
    public String getPricePlaceholder(Player player, String placeholder) {
        OrderCreationSession session = orderManager.getSession(player);
        if (session == null) return "N/A";
        
        switch (placeholder) {
            case "quantum_order_item_name":
                return session.getItemName();
            case "quantum_order_current_quantity":
                return String.valueOf(session.getQuantity());
            case "quantum_order_current_price":
                return priceFormat.format(session.getPrice());
            case "quantum_order_current_total":
                return priceFormat.format(session.getTotalPrice());
            case "quantum_order_price_min":
                return priceFormat.format(session.getMinPrice());
            case "quantum_order_price_max":
                return priceFormat.format(session.getMaxPrice());
            case "quantum_order_total_min":
                double totalMin = session.getQuantity() * session.getMinPrice();
                return priceFormat.format(totalMin);
            case "quantum_order_total_max":
                double totalMax = session.getQuantity() * session.getMaxPrice();
                return priceFormat.format(totalMax);
            default:
                return null;
        }
    }
}
