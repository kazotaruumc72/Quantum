package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.ButtonType;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.menu.StorageMenuHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import java.util.UUID;

/**
 * Gestionnaire d'événements pour les menus Quantum
 * ULTRA PROTECTION MODE: Cancel EVERYTHING first, ask questions later
 */
public class MenuListener implements Listener {

    private final Quantum plugin;
    private final StorageMenuHandler storageHandler;
    private final NamespacedKey buttonTypeKey;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
        this.storageHandler = new StorageMenuHandler(plugin);
        this.buttonTypeKey = new NamespacedKey(plugin, "button_type");
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
            ItemStack clickedItem = topInv.getItem(slot);
            
            // ========================================
            // FILTRAGE INSPIRÉ DE zMenu
            // ========================================
            
            // 1. Vérifier si c'est un bouton via MenuItem
            if (menuItem != null && isSpecialButton(menuItem)) {
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                menuItem.executeActions(player, plugin, event.getClick());
                return; // IMPORTANT: return ici pour ne pas traiter comme item de storage
            }
            
            // 2. Vérifier si c'est un bouton via PDC (QUANTUM_CHANGE_MODE, etc.)
            if (clickedItem != null && hasButtonTypePDC(clickedItem)) {
                // C'est un bouton détecté via PDC, le laisser au système de menus
                // Ne PAS appeler storageHandler
                return;
            }
            
            // 3. Vérifier si c'est un item décoratif (STAINED_GLASS_PANE)
            if (clickedItem != null && isDecorativeItem(clickedItem)) {
                // Item décoratif → ignorer
                return;
            }
            
            // 4. Vérifier que le slot contient un item valide
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            // 5. Vérifier que le slot est dans la zone de storage (9-44)
            if (slot < 9 || slot > 44) {
                // Hors zone de storage → ignorer
                return;
            }
            
            // ========================================
            // SI ON ARRIVE ICI : C'EST UN ITEM DE STORAGE VALIDE
            // ========================================
            storageHandler.handleClick(player, slot, event.getClick(), event.getCursor());
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
     * Vérifie si un ItemStack a un tag button_type dans son PDC (PersistentDataContainer)
     * Cela permet de détecter les boutons QUANTUM_CHANGE_MODE et autres boutons Quantum
     * 
     * @param item L'ItemStack à vérifier
     * @return true si l'item a un button_type dans son PDC, false sinon
     */
    private boolean hasButtonTypePDC(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        // Vérifier si l'item a le tag button_type dans son PersistentDataContainer
        return meta.getPersistentDataContainer().has(buttonTypeKey, PersistentDataType.STRING);
    }
    
    /**
     * Vérifie si un ItemStack est un item décoratif (STAINED_GLASS_PANE)
     * 
     * @param item L'ItemStack à vérifier
     * @return true si c'est un item décoratif, false sinon
     */
    private boolean isDecorativeItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        String materialName = item.getType().name();
        
        // Détecter les bordures (STAINED_GLASS_PANE)
        return materialName.endsWith("_STAINED_GLASS_PANE") || materialName.equals("GLASS_PANE");
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
