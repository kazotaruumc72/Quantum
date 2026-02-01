package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.ButtonType;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Gère les interactions avec les boutons des menus d'ordre
 */
public class OrderButtonHandler {
    
    private final Quantum plugin;
    
    public OrderButtonHandler(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gère un clic sur un bouton d'ordre
     */
    public void handle(Player player, MenuItem item) {
        OrderCreationSession session = plugin.getOrderCreationManager().getSession(player);
        
        // Vérifier qu'une session existe
        if (session == null && item.getButtonType() != ButtonType.QUANTUM_CANCEL_ORDER) {
            player.sendMessage("§c⚠ Aucune session de création d'ordre active!");
            player.closeInventory();
            return;
        }
        
        ButtonType type = item.getButtonType();
        Map<String, Object> params = item.getParameters();
        
        switch (type) {
            case QUANTUM_ADJUST_QUANTITY:
                handleAdjustQuantity(player, session, params);
                break;
                
            case QUANTUM_SET_QUANTITY_MAX:
                handleSetQuantityMax(player, session);
                break;
                
            case QUANTUM_VALIDATE_QUANTITY:
                handleValidateQuantity(player, session);
                break;
                
            case QUANTUM_ADJUST_PRICE:
                handleAdjustPrice(player, session, params);
                break;
                
            case QUANTUM_SET_PRICE_MAX:
                handleSetPriceMax(player, session);
                break;
                
            case QUANTUM_FINALIZE_ORDER:
                handleFinalizeOrder(player, session);
                break;
                
            case QUANTUM_CANCEL_ORDER:
                handleCancelOrder(player);
                break;
                
            default:
                player.sendMessage("§c⚠ Type de bouton inconnu: " + type);
                break;
        }
    }
    
    /**
     * Ajuster la quantité (param: amount)
     */
    private void handleAdjustQuantity(Player player, OrderCreationSession session, Map<String, Object> params) {
        // Récupérer le paramètre "amount"
        Object amountObj = params.get("amount");
        if (amountObj == null) {
            player.sendMessage("§c⚠ Paramètre 'amount' manquant!");
            return;
        }
        
        int amount;
        if (amountObj instanceof Integer) {
            amount = (Integer) amountObj;
        } else if (amountObj instanceof String) {
            try {
                amount = Integer.parseInt((String) amountObj);
            } catch (NumberFormatException e) {
                player.sendMessage("§c⚠ Paramètre 'amount' invalide!");
                return;
            }
        } else {
            player.sendMessage("§c⚠ Paramètre 'amount' invalide!");
            return;
        }
        
        // Ajuster la quantité
        session.adjustQuantity(amount);
        
        // Son de clic
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        
        // Rafraîchir le menu
        refreshMenu(player, session);
    }
    
    /**
     * Définir la quantité au maximum
     */
    private void handleSetQuantityMax(Player player, OrderCreationSession session) {
        session.setQuantity(session.getMaxQuantity());
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        refreshMenu(player, session);
    }
    
    /**
     * Valider la quantité et passer au prix
     */
    private void handleValidateQuantity(Player player, OrderCreationSession session) {
        // Vérifier qu'une quantité est définie
        if (session.getQuantity() <= 0) {
            player.sendMessage("§c⚠ La quantité doit être supérieure à 0!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Passer à l'étape prix
        session.setStep(OrderCreationSession.Step.PRICE);
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        // Fermer et ouvrir le menu prix
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("menu order_price");
        }, 2L);
    }
    
    /**
     * Ajuster le prix (param: percentage)
     */
    private void handleAdjustPrice(Player player, OrderCreationSession session, Map<String, Object> params) {
        // Récupérer le paramètre "percentage"
        Object percentageObj = params.get("percentage");
        if (percentageObj == null) {
            player.sendMessage("§c⚠ Paramètre 'percentage' manquant!");
            return;
        }
        
        int percentage;
        if (percentageObj instanceof Integer) {
            percentage = (Integer) percentageObj;
        } else if (percentageObj instanceof String) {
            try {
                percentage = Integer.parseInt((String) percentageObj);
            } catch (NumberFormatException e) {
                player.sendMessage("§c⚠ Paramètre 'percentage' invalide!");
                return;
            }
        } else {
            player.sendMessage("§c⚠ Paramètre 'percentage' invalide!");
            return;
        }
        
        // Ajuster le prix
        session.adjustPrice(percentage);
        
        // Son de clic
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        
        // Rafraîchir le menu
        refreshMenu(player, session);
    }
    
    /**
     * Définir le prix au maximum
     */
    private void handleSetPriceMax(Player player, OrderCreationSession session) {
        session.setPrice(session.getMaxPrice());
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        refreshMenu(player, session);
    }
    
    /**
     * Finaliser et créer l'ordre
     */
    private void handleFinalizeOrder(Player player, OrderCreationSession session) {
        // Vérifier les valeurs
        if (session.getQuantity() <= 0) {
            player.sendMessage("§c⚠ La quantité doit être supérieure à 0!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        if (session.getPrice() <= 0) {
            player.sendMessage("§c⚠ Le prix doit être supérieur à 0!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Calculer le prix total
        double totalPrice = session.getQuantity() * session.getPrice();
        
        // Vérifier que le joueur a assez d'argent (TODO: intégrer Vault)
        // Pour l'instant, on suppose qu'il a assez
        
        // Créer l'ordre
        boolean success = plugin.getOrderCreationManager().finalizeOrder(player);
        
        if (success) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage("§a§l✓ §aOffre créée avec succès!");
            player.sendMessage("§e" + session.getQuantity() + "x §f" + session.getItemName() + " §eà " + session.getPrice() + "€/u");
            player.sendMessage("§e§lTotal: " + String.format("%.2f", totalPrice) + "€");
            
            player.closeInventory();
        } else {
            player.sendMessage("§c⚠ Erreur lors de la création de l'offre!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
    
    /**
     * Annuler la création d'ordre
     */
    private void handleCancelOrder(Player player) {
        plugin.getOrderCreationManager().cancelOrder(player);
        
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        player.sendMessage("§e⚠ Création d'offre annulée.");
        
        player.closeInventory();
    }
    
    /**
     * Rafraîchir le menu avec les placeholders de session
     */
    private void refreshMenu(Player player, OrderCreationSession session) {
        Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
        if (activeMenu != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                activeMenu.refresh(player, plugin, session.getPlaceholders());
            }, 1L);
        }
    }
}
