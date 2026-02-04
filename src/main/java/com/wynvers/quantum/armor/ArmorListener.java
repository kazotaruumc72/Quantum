package com.wynvers.quantum.armor;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Écoute les changements d'armure pour appliquer/retirer les bonus
 * Empêche aussi le déséquipement de l'armure dans les zones de donjon
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ArmorListener implements Listener {
    
    private final Quantum plugin;
    private final DungeonArmor dungeonArmor;
    private final ArmorManager armorManager;
    
    public ArmorListener(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonArmor = plugin.getDungeonArmor();
        this.armorManager = plugin.getArmorManager();
    }
    
    /**
     * Applique les bonus au joueur quand il se connecte
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Délai de 1 tick pour être sûr que l'inventaire est chargé
        new BukkitRunnable() {
            @Override
            public void run() {
                armorManager.applyArmorBonuses(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * Retire les bonus quand le joueur se déconnecte
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        armorManager.clearArmorBonuses(event.getPlayer());
    }
    
    /**
     * Détecte quand un joueur équipe/déséquipe une armure
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        // Vérifier si c'est un slot d'armure
        int slot = event.getSlot();
        boolean isArmorSlot = slot >= 36 && slot <= 39; // Slots 36-39 = armor slots
        
        if (!isArmorSlot) {
            // Shift-click depuis inventaire vers armure
            if (event.isShiftClick() && clicked != null) {
                if (isArmorPiece(clicked)) {
                    scheduleArmorUpdate(player);
                }
            }
            return;
        }
        
        // Changement dans un slot d'armure
        scheduleArmorUpdate(player);
    }
    
    /**
     * Programme une mise à jour des bonus d'armure (1 tick après)
     */
    private void scheduleArmorUpdate(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                armorManager.applyArmorBonuses(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * Vérifie si un item est une pièce d'armure
     */
    private boolean isArmorPiece(ItemStack item) {
        if (item == null) return false;
        
        String type = item.getType().name();
        return type.endsWith("_HELMET") || 
               type.endsWith("_CHESTPLATE") || 
               type.endsWith("_LEGGINGS") || 
               type.endsWith("_BOOTS");
    }
}
