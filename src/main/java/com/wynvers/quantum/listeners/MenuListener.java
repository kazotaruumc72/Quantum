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
import java.util.Map;
import java.util.UUID;

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
     * USES getActiveMenu() as primary detection - works with animated titles!
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryClickNuclear(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        
        // TOUJOURS utiliser getActiveMenu() EN PREMIER
        // Car il fonctionne avec les titres animés et dynamiques !
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        // Si un menu est détecté : CANCEL IMMÉDIATEMENT
        if (menu != null) {
            event.setCancelled(true);
        } else {
            menu = plugin.getMenuManager().getMenuByTitle(title);
            
            if (menu != null) {
                event.setCancelled(true);
            } else {
            }
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
        
        // TOUJOURS utiliser le menu actif comme source primaire
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu == null) {
            // Fallback sur getMenuByTitle si vraiment nécessaire
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
     * USES getActiveMenu() as primary detection
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryDragNuclear(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // TOUJOURS utiliser getActiveMenu() EN PREMIER
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu != null) {
            event.setCancelled(true);
        } else {
            // Fallback
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
            
            if (menu != null) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // TOUJOURS utiliser le menu actif comme source primaire
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
        @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Menu menu = plugin.getMenuManager().getActiveMenu(player);

        if (menu != null) {
            // DEBUG: Log avant la suppression
            
            plugin.getAnimationManager().stopAnimation(player);
            
            // CRITICAL FIX: Attendre 1 tick avant de supprimer le menu
            // Car InventoryCloseEvent se déclenche AVANT l'ouverture du prochain inventaire
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Vérifier si le joueur a toujours un inventaire ouvert
                (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    // Le joueur n'a vraiment aucun menu ouvert maintenant
                    plugin.getMenuManager().clearActiveMenu(player);
                    }
            }, 1L);
        }
}
    }
