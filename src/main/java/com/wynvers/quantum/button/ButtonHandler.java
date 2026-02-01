package com.wynvers.quantum.button;

import com.wynvers.quantum.Quantum;
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
