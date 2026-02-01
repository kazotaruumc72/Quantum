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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Gestionnaire d'événements pour les menus Quantum
 * ULTRA PROTECTION MODE: Cancel EVERYTHING first, ask questions later
 */
public class MenuListener implements Listener {

    private final Quantum plugin;
    private final StorageMenuHandler storageHandler;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
        this.storageHandler = new StorageMenuHandler(plugin);
    }

    /**
     * NUCLEAR OPTION: Cancel ABSOLUTELY EVERYTHING when menu is open
     * Priority LOWEST means we run FIRST before any other plugin
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryClickNuclear(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        // Si pas de menu actif, vérifier par titre comme fallback
        if (menu == null) {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        // Si un menu est détecté : CANCEL IMMÉDIATEMENT
        if (menu != null) {
            // DEBUG: Envoyer un message au joueur pour confirmer détection
            player.sendMessage("§c[DEBUG] Menu détecté: " + menu.getId() + " - Click cancelled!");
            event.setCancelled(true);
        } else {
            // DEBUG: Pas de menu détecté
            player.sendMessage("§e[DEBUG] Pas de menu détecté. Title: " + event.getView().getTitle());
        }
    }
    
    /**
     * Main handler - runs AFTER nuclear cancel
     * Only handles button actions, everything is already cancelled
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Utiliser le menu actif pour une détection fiable
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        if (menu == null) {
            // Fallback sur getMenuByTitle si pas de menu actif
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        if (menu == null) return;
        
        // Déjà cancel par le handler LOWEST, on traite juste les boutons
        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getView().getTopInventory();
        
        // Special handling for storage menu
        if (menu.getId().equals("storage")) {
            handleStorageMenu(event, player, menu, clickedInv, topInv);
            return;
        }
        
        // Pour les menus standards : seulement traiter les clics sur boutons
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            if (menuItem != null) {
                // Check requirements
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                // Execute actions
                menuItem.executeActions(player, plugin, event.getClick());
            }
        }
    }
    
    /**
     * Handle storage menu clicks with special interactive behavior
     */
    private void handleStorageMenu(InventoryClickEvent event, Player player, Menu menu, Inventory clickedInv, Inventory topInv) {
        boolean isAdmin = player.hasPermission("quantum.admin");
        
        // If clicking in the storage menu (top inventory)
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            // If clicking on a configured menu item (buttons, decorations)
            if (menuItem != null) {
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                menuItem.executeActions(player, plugin, event.getClick());
            }
            // If clicking on an empty slot or storage item slot
            else if (isAdmin) {
                storageHandler.handleClick(player, slot, event.getClick(), event.getCursor());
            } else {
                player.sendMessage("§cStorage is view-only. Use /qstorage commands or contact an admin.");
            }
        }
        // If clicking in player inventory while storage is open
        else if (clickedInv != null && clickedInv.equals(player.getInventory())) {
            if (event.getClick().isShiftClick()) {
                if (isAdmin) {
                    if (event.getCurrentItem() != null) {
                        storageHandler.handleClick(player, -1, event.getClick(), event.getCurrentItem());
                    }
                } else {
                    player.sendMessage("§cYou don't have permission to deposit items. Use /qstorage transfer or contact an admin.");
                }
            }
        }
    }
    
    /**
     * Nuclear drag protection - priority LOWEST
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryDragNuclear(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu == null) {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        if (menu != null) {
            player.sendMessage("§c[DEBUG] Menu détecté: " + menu.getId() + " - Drag cancelled!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu == null) {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        if (menu != null) {
            // For storage menu, show message if non-admin tries to drag
            if (menu.getId().equals("storage")) {
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
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu != null) {
            plugin.getAnimationManager().stopAnimation(player);
            plugin.getMenuManager().clearActiveMenu(player);
        }
    }
}
