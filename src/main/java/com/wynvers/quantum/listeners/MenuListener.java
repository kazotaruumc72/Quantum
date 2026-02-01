package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.ButtonType;
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
        
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu != null) {
            event.setCancelled(true);
        } else {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
            
            if (menu != null) {
                event.setCancelled(true);
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
        
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu == null) {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        if (menu == null) return;
        
        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getView().getTopInventory();
        
        if (menu.getId().equals("storage")) {
            handleStorageMenu(event, player, menu, clickedInv, topInv);
            return;
        }
        
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            if (menuItem != null) {
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                menuItem.executeActions(player, plugin, event.getClick());
            }
        }
    }
    
    /**
     * Handle storage menu clicks with special interactive behavior
     */
    private void handleStorageMenu(InventoryClickEvent event, Player player, Menu menu, Inventory clickedInv, Inventory topInv) {
        boolean isAdmin = player.hasPermission("quantum.admin");
        
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            
            // Vérifier si c'est un bouton spécial (avec buttonType ou actions)
            if (menuItem != null && isSpecialButton(menuItem)) {
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                menuItem.executeActions(player, plugin, event.getClick());
            }
            // Sinon, c'est un item de storage normal
            else {
                storageHandler.handleClick(player, slot, event.getClick(), event.getCursor());
            }
        }
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
     * Vérifie si un MenuItem est un bouton spécial (pas un item de storage normal)
     */
    private boolean isSpecialButton(MenuItem menuItem) {
        // Si le MenuItem a un buttonType spécial (pas null et pas STANDARD), c'est un bouton
        ButtonType buttonType = menuItem.getButtonType();
        if (buttonType != null && buttonType != ButtonType.STANDARD) {
            return true;
        }
        
        // Si le MenuItem a des actions (clic gauche ou droit), c'est un bouton
        if (!menuItem.getLeftClickActions().isEmpty() || !menuItem.getRightClickActions().isEmpty()) {
            return true;
        }
        
        // Sinon, c'est un item de storage normal
        return false;
    }
    
    /**
     * Nuclear drag protection - priority LOWEST
     * USES getActiveMenu() as primary detection
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryDragNuclear(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu != null) {
            event.setCancelled(true);
        } else {
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
        
        Menu menu = plugin.getMenuManager().getActiveMenu(player);
        
        if (menu == null) {
            String title = event.getView().getTitle();
            menu = plugin.getMenuManager().getMenuByTitle(title);
        }
        
        if (menu != null) {
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
            plugin.getAnimationManager().stopAnimation(player);
            
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    plugin.getMenuManager().clearActiveMenu(player);
                }
            }, 1L);
        }
    }
}
