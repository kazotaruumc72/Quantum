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
        
        // Cancel all clicks in menu inventories to prevent item movement
        Menu menu = plugin.getMenuManager().getOpenMenu(player);
        if (menu != null) {
            event.setCancelled(true);
            
            int slot = event.getSlot();
            MenuItem menuItem = menu.getMenuItem(slot);
            
            if (menuItem == null) return;
            
            // Execute actions for the clicked item
            List<MenuAction> actions = menuItem.getActions();
            if (actions != null && !actions.isEmpty()) {
                for (MenuAction action : actions) {
                    executeAction(player, menu, action, event.getClick());
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        plugin.getMenuManager().closeMenu(player);
    }
    
    private void executeAction(Player player, Menu menu, MenuAction action, ClickType clickType) {
        String actionType = action.getType();
        String actionValue = action.getValue();
        
        switch (actionType.toLowerCase()) {
            case "command":
                if (actionValue.startsWith("console:")) {
                    String command = actionValue.substring(8).replace("%player%", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                } else if (actionValue.startsWith("player:")) {
                    String command = actionValue.substring(7).replace("%player%", player.getName());
                    player.performCommand(command);
                } else {
                    player.performCommand(actionValue.replace("%player%", player.getName()));
                }
                break;
                
            case "close":
                player.closeInventory();
                break;
                
            case "openmenu":
                plugin.getMenuManager().openMenu(actionValue, player);
                break;
                
            case "message":
                player.sendMessage(plugin.getMessagesManager().getMessage(actionValue));
                break;
                
            case "button":
                handleButtonAction(player, menu, actionValue, clickType);
                break;
        }
    }
    
    private void handleButtonAction(Player player, Menu menu, String buttonTypeStr, ClickType clickType) {
        try {
            ButtonType buttonType = ButtonType.valueOf(buttonTypeStr.toUpperCase());
            
            switch (buttonType) {
                case QUANTUM_STORAGE:
                    // Open storage menu
                    plugin.getMenuManager().openMenu("storage", player);
                    break;
                    
                case QUANTUM_SELL:
                    // Handle sell all items in storage
                    // TODO: Implement sell logic
                    player.sendMessage(plugin.getMessagesManager().getMessage("storage.sell"));
                    break;
                    
                case QUANTUM_SELL_POURCENTAGE:
                    // Handle sell percentage of items
                    // TODO: Implement percentage sell logic
                    player.sendMessage(plugin.getMessagesManager().getMessage("storage.sell-percentage"));
                    break;
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown button type: " + buttonTypeStr);
        }
    }
}
