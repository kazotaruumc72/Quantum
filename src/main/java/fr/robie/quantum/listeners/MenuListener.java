package fr.robie.quantum.listeners;

import fr.robie.quantum.Quantum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {
    
    private final Quantum plugin;
    
    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // TODO: Handle menu clicks
        // Check if inventory is a menu
        // Check requirements
        // Execute actions
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Stop title animation if any
        if (event.getPlayer() != null) {
            plugin.getAnimationManager().stopAnimation((org.bukkit.entity.Player) event.getPlayer());
        }
    }
}
