package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Opens the Quantum Item Attributes Modifier menu when a player
 * shift+right-clicks while holding a Quantum item (dungeon armor or dungeon util).
 */
public class QuantumItemInteractListener implements Listener {

    private final Quantum plugin;

    public QuantumItemInteractListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isQuantumItem(item)) return;

        event.setCancelled(true);

        // Store the held slot as the active item context
        int heldSlot = player.getInventory().getHeldItemSlot();
        plugin.getQuantumItemAttributeManager().setActiveSlot(player, heldSlot);

        Menu menu = plugin.getMenuManager().getMenu("quantum_item_attributes");
        if (menu == null) {
            player.sendMessage("§c§l✗ §cMenu 'quantum_item_attributes' introuvable. Contactez un administrateur.");
            return;
        }
        menu.open(player, plugin);
    }

    private boolean isQuantumItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        return plugin.getDungeonArmor().isDungeonArmor(item)
                || plugin.getDungeonUtils().isDungeonUtil(item);
    }
}
