package com.wynvers.quantum.button;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

/**
 * Gestionnaire des actions des boutons personnalisés Quantum
 */
public class ButtonHandler {

    private final Quantum plugin;
    private final StorageManager storageManager;

    public ButtonHandler(Quantum plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
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
        // TODO: Implémenter l'ouverture du menu storage
    }

    /**
     * Gère le clic sur un bouton de vente
     */
    private void handleSellButton(Player player, Map<String, String> parameters) {
        // Bascule entre les modes STORAGE et VENTE
        // TODO: Implémenter le basculement de mode
    }

    /**
     * Gère le clic sur un bouton de pourcentage de vente
     */
    private void handleSellPercentageButton(Player player, Map<String, String> parameters) {
        // Définit le pourcentage d'items à vendre (25%, 50%, 75%, 100%)
        String percentage = parameters.getOrDefault("percentage", "100");
        // TODO: Implémenter la sélection de pourcentage
    }

    /**
     * Gère le clic sur un bouton de changement de mode
     */
    private void handleChangeModeButton(Player player, Map<String, String> parameters) {
        // Change le mode (paramètre: mode:<mode>)
        String mode = parameters.getOrDefault("mode", "STORAGE");
        // TODO: Implémenter le changement de mode
    }
}
