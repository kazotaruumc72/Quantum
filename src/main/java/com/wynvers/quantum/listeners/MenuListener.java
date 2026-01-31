package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Gestionnaire d'événements pour les menus Quantum
 */
public class MenuListener implements Listener {

    private final Quantum plugin;

    public MenuListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // TODO: Implémenter la gestion des clics dans les menus
        // Pour le moment, ce listener est un placeholder
    }
}
