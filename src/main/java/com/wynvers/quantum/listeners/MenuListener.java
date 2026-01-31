package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Gestionnaire d'événements pour les menus Quantum
 */
public class MenuListener implements Listener {

    private final Quantum plugin;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        String title = view.getTitle();
        
        // Check if this is a Quantum menu
        Menu menu = plugin.getMenuManager().getMenuByTitle(title);
        if (menu == null) return;
        
        // Cancel all clicks in menu inventory
        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = view.getTopInventory();
        
        // If clicking in the menu inventory (top inventory)
        if (clickedInv != null && clickedInv.equals(topInv)) {
            event.setCancelled(true);
            
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            if (menuItem != null) {
                // Check requirements
                if (!menuItem.meetsRequirements(player, plugin)) {
                    // Send deny message if configured
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                // Execute actions
                menuItem.executeActions(player, plugin);
            }
        }
        // If clicking in player inventory while menu is open, prevent shift-click to menu
        else if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        InventoryView view = event.getView();
        String title = view.getTitle();
        
        // Check if this is a Quantum menu
        Menu menu = plugin.getMenuManager().getMenuByTitle(title);
        if (menu != null) {
            // Cancel all drag events in menu
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        // Check if this is a Quantum menu
        Menu menu = plugin.getMenuManager().getMenuByTitle(title);
        if (menu != null) {
            // Stop title animation if any
            plugin.getAnimationManager().stopAnimation(player);
        }
    }
}
