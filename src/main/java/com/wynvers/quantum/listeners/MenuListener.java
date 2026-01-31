package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.button.ButtonHandler;
import com.wynvers.quantum.button.ButtonType;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

/**
 * Gestionnaire d'événements pour les menus Quantum
 */
public class MenuListener implements Listener {

    private final Quantum plugin;
    private final ButtonHandler buttonHandler;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
        this.buttonHandler = new ButtonHandler(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) return;
        
        // Vérifier si c'est un menu Quantum
        Menu menu = plugin.getMenuManager().getOpenMenu(player);
        if (menu == null) return;
        
        // Annuler l'événement par défaut
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        // Gérer le clic dans le menu
        menu.handleClick(player, slot, event);
    }
}
