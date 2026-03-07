package com.wynvers.quantum.dungeonutis;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for dungeon utils events
 */
public class DungeonUtilsListener implements Listener {

    private final Quantum plugin;
    private final DungeonUtils dungeonUtils;

    public DungeonUtilsListener(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonUtils = plugin.getDungeonUtils();
    }

    /**
     * Handle block break events to add exp to dungeon tools
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!dungeonUtils.isDungeonUtil(item)) return;

        DungeonUtilsType type = dungeonUtils.getType(item);
        if (type == null || !type.isTool()) return;

        // Tools can be used without restrictions now
        // No job system checks needed
    }

    /**
     * Handle entity damage to add exp to dungeon weapons
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!dungeonUtils.isDungeonUtil(item)) return;

        DungeonUtilsType type = dungeonUtils.getType(item);
        if (type == null || !type.isWeapon()) return;

        // Weapons can be used without restrictions now
        // No job system checks needed
    }
}
