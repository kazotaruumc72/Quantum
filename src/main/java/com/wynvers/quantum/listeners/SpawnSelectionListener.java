package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.SpawnSelectionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Permet aux OPs avec une hache en netherite de sélectionner une zone de spawn.
 */
public class SpawnSelectionListener implements Listener {

    private final Quantum plugin;
    private final SpawnSelectionManager selectionManager;

    public SpawnSelectionListener(Quantum plugin, SpawnSelectionManager selectionManager) {
        this.plugin = plugin;
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.NETHERITE_AXE) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            selectionManager.setPos1(player.getUniqueId(), event.getClickedBlock().getLocation());
            player.sendMessage("§a[Quantum] Position 1 définie pour la zone de spawn.");
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            selectionManager.setPos2(player.getUniqueId(), event.getClickedBlock().getLocation());
            player.sendMessage("§a[Quantum] Position 2 définie pour la zone de spawn.");
            event.setCancelled(true);
        }
    }
}
