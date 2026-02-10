package com.wynvers.quantum.button;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

/**
 * Gestionnaire des actions des boutons personnalisés Quantum
 */
public class ButtonHandler {

    private final Quantum plugin;
    private static final int MIN_SELL_QUANTITY = 1;

    public ButtonHandler(Quantum plugin) {
        this.plugin = plugin;
    }

    /**
     * Gère le clic sur un bouton personnalisé
     * @param event L'événement de clic
     * @param type Le type de bouton
     * @param parameters Les paramètres du bouton
     */
    public void handleButtonClick(InventoryClickEvent event, ButtonType type, Map<String, String> parameters) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        switch (type) {
            case QUANTUM_STORAGE:
                handleStorageButton(player, parameters);
                break;
            case QUANTUM_SELL:
                handleSellButton(player, parameters);
                break;
            case QUANTUM_SELL_POURCENTAGE:
                handleSellPercentageButton(player, parameters);
                break;
            case QUANTUM_CHANGE_MODE:
                handleChangeModeButton(player, parameters);
                break;
            case DEFAULT:
            default:
                // Bouton par défaut, pas d'action spéciale
                break;
        }
    }

    /**
     * Gère le clic sur un bouton de stockage
     */
    private void handleStorageButton(Player player, Map<String, String> parameters) {
        // Ouvre le menu de stockage pour le joueur
        if (plugin.getMenuManager().getMenu("storage") != null) {
            plugin.getMenuManager().getMenu("storage").open(player, plugin);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else {
            plugin.getQuantumLogger().warning("Menu 'storage' non trouvé!");
            player.sendMessage("§cErreur: Le menu de stockage n'est pas configuré.");
        }
    }

    /**
     * Gère le clic sur un bouton de vente
     */
    private void handleSellButton(Player player, Map<String, String> parameters) {
        // Bascule entre les modes STORAGE et VENTE
        StorageMode.Mode currentMode = StorageMode.getMode(player);
        StorageMode.Mode newMode;
        
        if (currentMode == StorageMode.Mode.SELL) {
            // Si déjà en mode SELL, retourner en STORAGE
            newMode = StorageMode.Mode.STORAGE;
        } else {
            // Sinon, passer en mode SELL
            newMode = StorageMode.Mode.SELL;
        }
        
        StorageMode.setMode(player, newMode);
        
        // Feedback sonore
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        
        // Message de confirmation
        String modeDisplay = newMode.getDisplayName();
        player.sendMessage("§8[§6Quantum§8] §7Mode changé en " + modeDisplay);
        
        // Rafraîchir le menu storage si ouvert
        if (plugin.getMenuManager().getMenu("storage") != null) {
            plugin.getMenuManager().getMenu("storage").refresh(player, plugin);
        }
    }

    /**
     * Gère le clic sur un bouton de pourcentage de vente
     */
    private void handleSellPercentageButton(Player player, Map<String, String> parameters) {
        // Définit le pourcentage d'items à vendre (25%, 50%, 75%, 100%)
        String percentage = parameters.getOrDefault("percentage", "100");
        
        // Récupérer la session de vente active et la stocker pour éviter les problèmes de concurrence
        SellSession sellSession = plugin.getSellManager().getSession(player);
        if (sellSession == null) {
            player.sendMessage("§cErreur: Aucune session de vente active.");
            return;
        }
        
        try {
            int percentValue = Integer.parseInt(percentage);
            
            // Valider le pourcentage
            if (percentValue <= 0 || percentValue > 100) {
                player.sendMessage("§cErreur: Pourcentage invalide (doit être entre 1 et 100).");
                return;
            }
            
            // Vérifier que maxQuantity est valide
            int maxQuantity = sellSession.getMaxQuantity();
            if (maxQuantity <= 0) {
                player.sendMessage("§cErreur: Quantité maximale invalide.");
                return;
            }
            
            // Calculer la nouvelle quantité basée sur le pourcentage
            int newQuantity = Math.max(MIN_SELL_QUANTITY, (int) Math.ceil(maxQuantity * percentValue / 100.0));
            
            // Mettre à jour la quantité dans la session
            sellSession.setQuantity(newQuantity);
            
            // Feedback sonore
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            
            // Message de confirmation
            player.sendMessage("§8[§6Quantum§8] §7Quantité ajustée à " + percentValue + "% (§e" + newQuantity + "§7 items)");
            
            // Rafraîchir le menu de vente si ouvert
            if (plugin.getMenuManager().getMenu("sell") != null) {
                plugin.getMenuManager().getMenu("sell").refresh(player, plugin);
            }
            
        } catch (NumberFormatException e) {
            plugin.getQuantumLogger().warning("Pourcentage invalide: " + percentage);
            player.sendMessage("§cErreur: Pourcentage invalide!");
        }
    }

    /**
     * Gère le clic sur un bouton de changement de mode
     */
    private void handleChangeModeButton(Player player, Map<String, String> parameters) {
        // Récupérer le mode cible depuis les paramètres
        String targetMode = parameters.getOrDefault("mode", "STORAGE").toUpperCase();
        
        try {
            // Convertir en enum
            StorageMode.Mode newMode = StorageMode.Mode.valueOf(targetMode);
            
            // Définir le nouveau mode
            StorageMode.setMode(player, newMode);
            
            // Feedback sonore
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            
            // Message de confirmation
            String modeDisplay = newMode.getDisplayName();
            player.sendMessage("§8[§6Quantum§8] §7Mode changé en " + modeDisplay);
            
            // Rafraîchir le menu storage pour afficher le nouveau mode
            if (plugin.getMenuManager().getMenu("storage") != null) {
                plugin.getMenuManager().getMenu("storage").open(player, plugin);
            }
            
        } catch (IllegalArgumentException e) {
            plugin.getQuantumLogger().warning("Mode invalide: " + targetMode);
            player.sendMessage("§cMode invalide!");
        }
    }
}
