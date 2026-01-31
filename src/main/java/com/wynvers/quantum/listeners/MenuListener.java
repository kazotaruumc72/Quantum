package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import com.wynvers.quantum.menu.MenuAction;
import com.wynvers.quantum.button.ButtonType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuListener implements Listener {

    private final Quantum plugin;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) return;
        
        // Check if this is a menu inventory
        String inventoryTitle = event.getView().getTitle();
        Menu menu = findMenuByTitle(inventoryTitle);
        
        if (menu == null) return;
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null) return;
        
        // Find the MenuItem for this slot
        MenuItem menuItem = findMenuItemBySlot(menu, slot);
        
        if (menuItem == null) return;
        
        // Check click requirements
        // TODO: Implement requirement checking
        
        // Get button type if specified
        ButtonType buttonType = getButtonType(menuItem);
        
        // Handle button type actions
        if (buttonType != null && buttonType != ButtonType.DEFAULT) {
            handleButtonTypeAction(player, menu, menuItem, buttonType, event.getClick());
        }
        
        // Execute actions based on click type
        List<MenuAction> actions = null;
        
        if (event.getClick() == ClickType.LEFT) {
            actions = menuItem.getLeftClickActions();
        } else if (event.getClick() == ClickType.RIGHT) {
            actions = menuItem.getRightClickActions();
        } else if (event.getClick() == ClickType.MIDDLE) {
            actions = menuItem.getMiddleClickActions();
        }
        
        if (actions != null && !actions.isEmpty()) {
            for (MenuAction action : actions) {
                executeAction(player, action);
            }
        }
    }

    /**
     * Find a menu by its inventory title
     */
    private Menu findMenuByTitle(String title) {
        for (Menu menu : plugin.getMenuManager().getAllMenus()) {
            if (menu.getTitle().equals(title)) {
                return menu;
            }
        }
        return null;
    }

    /**
     * Find a MenuItem by slot in a menu
     */
    private MenuItem findMenuItemBySlot(Menu menu, int slot) {
        for (MenuItem item : menu.getItems().values()) {
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get button type from MenuItem
     */
    private ButtonType getButtonType(MenuItem menuItem) {
        // TODO: Add type field to MenuItem or retrieve from config
        // For now, return DEFAULT
        return ButtonType.DEFAULT;
    }

    /**
     * Handle special button type actions
     */
    private void handleButtonTypeAction(Player player, Menu menu, MenuItem menuItem, ButtonType buttonType, ClickType clickType) {
        switch (buttonType) {
            case QUANTUM_STORAGE:
                // TODO: Handle storage button (show stored items)
                plugin.getQuantumLogger().debug("Storage button clicked by " + player.getName());
                break;
                
            case QUANTUM_SELL:
                // TODO: Toggle between STORAGE and VENTE modes
                plugin.getQuantumLogger().debug("Sell mode toggle clicked by " + player.getName());
                break;
                
            case QUANTUM_SELL_POURCENTAGE:
                // TODO: Handle percentage selection
                plugin.getQuantumLogger().debug("Sell percentage button clicked by " + player.getName());
                break;
                
            default:
                break;
        }
    }

    /**
     * Execute a menu action
     */
    private void executeAction(Player player, MenuAction action) {
        if (action == null) return;
        
        switch (action.getType()) {
            case CLOSE:
                player.closeInventory();
                break;
                
            case COMMAND:
                // Execute command as player
                String command = action.getData();
                if (command != null) {
                    player.performCommand(command);
                }
                break;
                
            case CONSOLE_COMMAND:
                // Execute command as console
                String consoleCmd = action.getData();
                if (consoleCmd != null) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), 
                        consoleCmd.replace("%player%", player.getName()));
                }
                break;
                
            case MESSAGE:
                // Send message to player
                String message = action.getData();
                if (message != null) {
                    player.sendMessage(message);
                }
                break;
                
            case SOUND:
                // Play sound
                // TODO: Implement sound playing
                break;
                
            case OPEN_MENU:
                // Open another menu
                String menuId = action.getData();
                if (menuId != null) {
                    Menu targetMenu = plugin.getMenuManager().getMenu(menuId);
                    if (targetMenu != null) {
                        targetMenu.open(player, plugin);
                    }
                }
                break;
                
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Stop title animation if any
        if (event.getPlayer() != null) {
            plugin.getAnimationManager().stopAnimation((org.bukkit.entity.Player) event.getPlayer());
        }
    }
}
