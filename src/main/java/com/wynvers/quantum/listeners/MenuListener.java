package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.ButtonType;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.menu.OrderButtonHandler;
import com.wynvers.quantum.menu.StorageMenuHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import org.bukkit.Sound;
import java.io.File;
import java.util.UUID;

/**
 * Gestionnaire d'événements pour les menus Quantum
 * ULTRA PROTECTION MODE: Cancel EVERYTHING first, ask questions later
 */
public class MenuListener implements Listener {

    private final Quantum plugin;
    private final StorageMenuHandler storageHandler;
    private final OrderButtonHandler orderButtonHandler; // NOUVEAU
    private final NamespacedKey buttonTypeKey;
    private final NamespacedKey orderIdKey;
    private final NamespacedKey ordererUuidKey;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
        this.storageHandler = new StorageMenuHandler(plugin);
        this.orderButtonHandler = new OrderButtonHandler(plugin); // NOUVEAU
        this.buttonTypeKey = new NamespacedKey(plugin, "button_type");
        this.orderIdKey = new NamespacedKey(plugin, "quantum_order_id");
        this.ordererUuidKey = new NamespacedKey(plugin, "orderer_uuid");
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
        
        // === STORAGE MENU HANDLER ===
        if (menu.getId().equals("storage")) {
            handleStorageMenu(event, player, menu, clickedInv, topInv);
            return;
        }
        
        // === ORDERS MENU HANDLER (orders_autre, orders_minerais, etc.) ===
        if (menu.getId().startsWith("orders_")) {
            handleOrdersMenu(event, player, menu, clickedInv, topInv);
            return;
        }
        
        // === ORDER_CONFIRM MENU HANDLER ===
        if (menu.getId().equals("order_confirm")) {
            handleOrderConfirmMenu(event, player, menu, clickedInv, topInv);
            return;
        }
        
        // === STANDARD MENU HANDLER ===
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
     * Handle orders menu clicks (orders_autre, orders_minerais, etc.)
     * 
     * Gestion des clics:
     * - Clic Normal: Ouvrir order_confirm (vendre l'item)
     * - Shift + Clic Gauche (Admin): Retirer l'ordre de la recherche
     * - Shift + Clic Droit (Propriétaire): Supprimer sa propre recherche
     */
    private void handleOrdersMenu(InventoryClickEvent event, Player player, Menu menu, Inventory clickedInv, Inventory topInv) {
        boolean isAdmin = player.hasPermission("quantum.admin");
        
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            MenuItem menuItem = menu.getItemAt(slot);
            ItemStack clickedItem = topInv.getItem(slot);
            
            // Vérifier si c'est un bouton standard (pas un order item)
            if (menuItem != null && isSpecialButton(menuItem)) {
                if (!menuItem.meetsRequirements(player, plugin)) {
                    if (menuItem.getDenyMessage() != null && !menuItem.getDenyMessage().isEmpty()) {
                        player.sendMessage(menuItem.getDenyMessage());
                    }
                    return;
                }
                
                menuItem.executeActions(player, plugin, event.getClick());
                return;
            }
            
            // Vérifier si c'est un item décoratif
            if (clickedItem != null && isDecorativeItem(clickedItem)) {
                return;
            }
            
            // Vérifier si c'est un item valide
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            ClickType clickType = event.getClick();
            
            // === GESTION DES SHIFT-CLICS SUR LES ORDRES (SUPPRESSION) ===
            
            // SHIFT + CLIC GAUCHE (ADMIN) : Supprimer n'importe quel ordre
            if (clickType == ClickType.SHIFT_LEFT && isAdmin) {
                handleAdminDeleteOrder(player, clickedItem, menu);
                return;
            }
            
            // SHIFT + CLIC DROIT (PROPRIÉTAIRE) : Supprimer son propre ordre
            if (clickType == ClickType.SHIFT_RIGHT) {
                handleOwnerDeleteOrder(player, clickedItem, menu);
                return;
            }
            
            // === NOUVEAU: CLIC NORMAL SUR UN ORDRE (VENDRE) ===
            // Détecter si l'item a le tag quantum_order_id
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                String orderId = meta.getPersistentDataContainer().get(orderIdKey, PersistentDataType.STRING);
                
                if (orderId != null && !orderId.isEmpty()) {
                    // Extraire la catégorie depuis l'ID du menu ("orders_cultures" -> "cultures")
                    String category = menu.getId().substring(7);
                    
                    // Ouvrir order_confirm via OrderButtonHandler
                    orderButtonHandler.handleOrderClick(player, category, orderId);
                    return;
                }
            }
            
            // Clic normal sur un ordre (ancien système, fallback)
            if (menuItem != null && menuItem.getButtonType() == ButtonType.QUANTUM_ORDERS_ITEM) {
                menuItem.executeActions(player, plugin, clickType);
            }
        }
    }
    
    /**
     * NOUVEAU: Handle order_confirm menu clicks
     * Gère les boutons VENDRE et REFUSER
     */
    private void handleOrderConfirmMenu(InventoryClickEvent event, Player player, Menu menu, Inventory clickedInv, Inventory topInv) {
        plugin.getLogger().info("[ORDER_CONFIRM] Click detected in order_confirm menu");
        plugin.getLogger().info("[ORDER_CONFIRM] Clicked inventory: " + (clickedInv != null ? "YES" : "NULL"));
        plugin.getLogger().info("[ORDER_CONFIRM] Is top inventory: " + (clickedInv != null && clickedInv.equals(topInv)));
        
        if (clickedInv != null && clickedInv.equals(topInv)) {
            int slot = event.getSlot();
            plugin.getLogger().info("[ORDER_CONFIRM] Slot clicked: " + slot);
            
            MenuItem menuItem = menu.getItemAt(slot);
            plugin.getLogger().info("[ORDER_CONFIRM] MenuItem exists: " + (menuItem != null));
            
            if (menuItem != null) {
                ButtonType buttonType = menuItem.getButtonType();
                plugin.getLogger().info("[ORDER_CONFIRM] ButtonType: " + (buttonType != null ? buttonType.name() : "NULL"));
                
                if (buttonType == ButtonType.QUANTUM_CONFIRM_ORDER_SELL) {
                    plugin.getLogger().info("[ORDER_CONFIRM] VENDRE button detected! Calling handleConfirmSell...");
                    // Bouton VENDRE : exécuter la transaction
                    event.setCancelled(true);
                    orderButtonHandler.handleConfirmSell(player);
                    plugin.getLogger().info("[ORDER_CONFIRM] handleConfirmSell completed");
                    return;
                }
                
                if (buttonType == ButtonType.QUANTUM_CANCEL_ORDER_CONFIRM) {
                    plugin.getLogger().info("[ORDER_CONFIRM] REFUSER button detected! Calling handleCancelConfirm...");
                    // Bouton REFUSER : retourner au menu catégorie
                    event.setCancelled(true);
                    orderButtonHandler.handleCancelConfirm(player);
                    return;
                }
                
                if (buttonType == ButtonType.QUANTUM_ORDER_CONFIRM_DISPLAY) {
                    plugin.getLogger().info("[ORDER_CONFIRM] Display item clicked (no action)");
                    // Item de démonstration : ne rien faire
                    event.setCancelled(true);
                    return;
                }
                
                plugin.getLogger().warning("[ORDER_CONFIRM] Unknown button type: " + buttonType);
            } else {
                plugin.getLogger().warning("[ORDER_CONFIRM] No MenuItem found at slot " + slot);
            }
        } else {
            plugin.getLogger().info("[ORDER_CONFIRM] Click was not in top inventory");
        }
    }
    
    /**
     * Admin supprime n'importe quel ordre (Shift + Clic Gauche)
     */
    private void handleAdminDeleteOrder(Player admin, ItemStack orderItem, Menu menu) {
        // Extraire la catégorie depuis l'ID du menu
        String category = menu.getId().substring(7); // Enlever "orders_"
        
        // Vérifier dans la lore pour trouver l'information de l'ordre
        ItemMeta meta = orderItem.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            admin.sendMessage("§cErreur: Impossible de déterminer l'ordre à supprimer.");
            return;
        }
        
        // Extraire les infos depuis la lore
        String orderer = null;
        for (String line : meta.getLore()) {
            if (line.contains("Commandé par:")) {
                // Ex: "§e⚡ Commandé par: §fKazotaruu_"
                orderer = line.split(":")[1].trim().replaceAll("§.", "");
                break;
            }
        }
        
        if (orderer == null) {
            admin.sendMessage("§cErreur: Impossible de trouver le propriétaire de l'ordre.");
            return;
        }
        
        // Supprimer l'ordre
        boolean deleted = deleteOrderFromFile(category, orderer, null);
        
        if (deleted) {
            admin.sendMessage("§8[§6Quantum§8] §aOrdre supprimé avec succès !");
            admin.playSound(admin.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            
            // Rafraîchir le menu
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                menu.open(admin, plugin);
            }, 1L);
        } else {
            admin.sendMessage("§cErreur: Impossible de supprimer l'ordre.");
        }
    }
    
    /**
     * Propriétaire supprime son propre ordre (Shift + Clic Droit)
     */
    private void handleOwnerDeleteOrder(Player player, ItemStack orderItem, Menu menu) {
        // Extraire la catégorie depuis l'ID du menu
        String category = menu.getId().substring(7); // Enlever "orders_"
        
        // Vérifier dans la lore pour trouver l'information de l'ordre
        ItemMeta meta = orderItem.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            player.sendMessage("§cErreur: Impossible de déterminer l'ordre à supprimer.");
            return;
        }
        
        // Extraire les infos depuis la lore
        String orderer = null;
        for (String line : meta.getLore()) {
            if (line.contains("Commandé par:")) {
                orderer = line.split(":")[1].trim().replaceAll("§.", "");
                break;
            }
        }
        
        if (orderer == null) {
            player.sendMessage("§cErreur: Impossible de trouver le propriétaire de l'ordre.");
            return;
        }
        
        // Vérifier que c'est bien le propriétaire
        if (!orderer.equalsIgnoreCase(player.getName())) {
            player.sendMessage("§cVous ne pouvez supprimer que vos propres ordres!");
            return;
        }
        
        // Supprimer l'ordre
        boolean deleted = deleteOrderFromFile(category, orderer, player.getUniqueId());
        
        if (deleted) {
            player.sendMessage("§8[§6Quantum§8] §aVotre ordre a été supprimé avec succès !");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            
            // Rafraîchir le menu
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                menu.open(player, plugin);
            }, 1L);
        } else {
            player.sendMessage("§cErreur: Impossible de supprimer l'ordre.");
        }
    }
    
    /**
     * Supprime un ordre du fichier orders.yml
     * @param category Catégorie de l'ordre (ex: "autre", "minerais")
     * @param orderer Nom du joueur qui a créé l'ordre
     * @param ownerUuid UUID du propriétaire (optionnel, pour vérification)
     * @return true si supprimé avec succès, false sinon
     */
    private boolean deleteOrderFromFile(String category, String orderer, UUID ownerUuid) {
        try {
            File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
            if (!ordersFile.exists()) {
                return false;
            }
            
            YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
            
            // Trouver l'ordre correspondant
            String targetKey = null;
            for (String key : ordersConfig.getConfigurationSection(category).getKeys(false)) {
                String path = category + "." + key;
                String orderName = ordersConfig.getString(path + ".orderer", "");
                
                if (orderName.equalsIgnoreCase(orderer)) {
                    // Vérifier l'UUID si fourni
                    if (ownerUuid != null) {
                        String storedUuid = ordersConfig.getString(path + ".orderer_uuid", "");
                        if (!storedUuid.equals(ownerUuid.toString())) {
                            continue; // Pas le bon UUID
                        }
                    }
                    
                    targetKey = key;
                    break;
                }
            }
            
            if (targetKey == null) {
                return false;
            }
            
            // Supprimer l'ordre
            ordersConfig.set(category + "." + targetKey, null);
            ordersConfig.save(ordersFile);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de la suppression de l'ordre: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie si un MenuItem est un bouton spécial (pas un item de storage normal)
     */
    private boolean isSpecialButton(MenuItem menuItem) {
        // Si le MenuItem a un buttonType spécial (pas null et pas STANDARD), c'est un bouton
        ButtonType buttonType = menuItem.getButtonType();
        if (buttonType != null && buttonType != ButtonType.STANDARD && buttonType != ButtonType.QUANTUM_ORDERS_ITEM) {
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
            
            // NOUVEAU: Nettoyer le cache OrderButtonHandler
            orderButtonHandler.clearCache(player);
            
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    plugin.getMenuManager().clearActiveMenu(player);
                }
            }, 1L);
        }
    }
}
