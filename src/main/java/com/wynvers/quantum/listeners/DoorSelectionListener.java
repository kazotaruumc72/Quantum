package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerDoorManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Listener pour la hache en cuivre qui permet de sélectionner les zones de portes
 */
public class DoorSelectionListener implements Listener {
    
    private final Quantum plugin;
    private final TowerDoorManager doorManager;
    
    // Nom custom de l'outil
    private static final String WAND_NAME = "§e§lHache de Sélection";
    private static final Material WAND_MATERIAL = Material.COPPER_AXE;
    
    public DoorSelectionListener(Quantum plugin, TowerDoorManager doorManager) {
        this.plugin = plugin;
        this.doorManager = doorManager;
    }
    
    /**
     * Créer l'item de sélection
     */
    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            meta.setLore(Arrays.asList(
                "§7Clic gauche: §fPosition 1",
                "§7Clic droit: §fPosition 2",
                "",
                "§ePour créer une porte de tour"
            ));
            meta.setUnbreakable(true);
            wand.setItemMeta(meta);
        }
        
        return wand;
    }
    
    /**
     * Vérifie si un item est la baguette de sélection
     */
    private boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return WAND_NAME.equals(meta.getDisplayName());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Vérifier si c'est la baguette de sélection
        if (!isWand(item)) {
            return;
        }
        
        // Vérifier la permission
        if (!player.hasPermission("quantum.tower.door.select")) {
            player.sendMessage("§c§l[Doors] §cVous n'avez pas la permission!");
            event.setCancelled(true);
            return;
        }
        
        Action action = event.getAction();
        
        // Position 1 - Clic gauche sur block
        if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            doorManager.setPos1(player, event.getClickedBlock().getLocation());
        }
        // Position 2 - Clic droit sur block
        else if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            doorManager.setPos2(player, event.getClickedBlock().getLocation());
        }
        // Empêcher l'utilisation normale
        else if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            player.sendMessage("§e§l[Doors] §7Cliquez sur un block pour sélectionner!");
        }
    }
}
