package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.menu.StorageMenuHandler;
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
    private final StorageMenuHandler storageHandler;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
        this.storageHandler = new StorageMenuHandler(plugin);
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
        
        // Special handling for storage menu
        if (menu.getId().equals("storage")) {
            handleStorageMenu(event, player, menu);
            return;
        }
        
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
                
                // Execute actions avec le ClickType
                menuItem.executeActions(player, plugin, event.getClick());
            }
        }
        // If clicking in player inventory while menu is open, prevent shift-click to menu
        else if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle storage menu clicks with special interactive behavior
     * 
     * GUI is READ-ONLY for normal players
     * Only admins with 'quantum.admin' permission can deposit/withdraw interactively
     * Console commands and admins can use /qstorage transfer/remove for management
     */
    private void handleStorageMenu(InventoryClickEvent event, Player player, Menu menu) {
        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getView().getTopInventory();
        
        // Check if player has admin permission for interactive storage
        boolean isAdmin = player.hasPermission("quantum.admin");
        
        // If clicking in the storage menu (top inventory)
        if (clickedInv != null && clickedInv.equals(topInv)) {
            event.setCancelled(true);
            
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            // If clicking on a configured menu item (buttons, decorations)
            if (menuItem != null) {
                // Check requirements
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                // Execute actions avec le ClickType
                menuItem.executeActions(player, plugin, event.getClick());
            }
            // If clicking on an empty slot or storage item slot
            else if (isAdmin) {
                // Only admins can deposit/withdraw via GUI
                storageHandler.handleClick(player, slot, event.getClick(), event.getCursor());
            } else {
                // Non-admins: show message about read-only access
                player.sendMessage("§cStorage is view-only. Use /qstorage commands or contact an admin.");
            }
        }
        // If clicking in player inventory while storage is open
        else if (clickedInv != null && clickedInv.equals(player.getInventory())) {
            // Allow shift-click to deposit items to storage (admin only)
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
                
                if (isAdmin) {
                    // Admin: allow shift-click deposit
                    if (event.getCurrentItem() != null) {
                        storageHandler.handleClick(player, -1, event.getClick(), event.getCurrentItem());
                    }
                } else {
                    // Non-admin: show message
                    player.sendMessage("§cYou don't have permission to deposit items. Use /qstorage transfer or contact an admin.");
                }
            }
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
            
            // For storage menu, show message if non-admin tries to drag
            if (menu.getId().equals("storage")) {
                Player player = (Player) event.getWhoClicked();
                if (!player.hasPermission("quantum.admin")) {
                    player.sendMessage("§cStorage is view-only. Use /qstorage commands or contact an admin.");
                }
            }
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
