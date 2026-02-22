package com.wynvers.quantum.dungeonutis;

import com.wynvers.quantum.Quantum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for dungeon utils events
 * Integrates with the jobs system to provide bonuses
 */
public class DungeonUtilsListener implements Listener {

    private final Quantum plugin;
    private final DungeonUtils dungeonUtils;

    public DungeonUtilsListener(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonUtils = plugin.getDungeonUtils();
    }

    /**
     * Handle block break events to check if player is using dungeon tools
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!dungeonUtils.isDungeonUtil(item)) return;

        DungeonUtilsType type = dungeonUtils.getType(item);
        if (type == null || !type.isTool()) return;

        // Check if player has a job and if it's compatible
        if (plugin.getJobManager() == null) return;

        String playerJob = plugin.getJobManager().getPlayerJob(player.getUniqueId());
        if (playerJob == null) {
            String message = plugin.getDungeonUtils().getConfig().getString("messages.no_job", "&c✖ Vous devez avoir un métier pour utiliser cet outil!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            event.setCancelled(true);
            return;
        }

        if (!dungeonUtils.canUseForJob(player, item)) {
            String message = plugin.getDungeonUtils().getConfig().getString("messages.incompatible_job", "&c✖ Cet outil n'est pas compatible avec votre métier actuel!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            event.setCancelled(true);
            return;
        }

        // The actual bonus will be applied by the JobActionListener
        // This just validates that the tool can be used
    }

    /**
     * Handle entity damage to check if player is using dungeon weapons
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

        // Check if player has a job and if it's compatible
        if (plugin.getJobManager() == null) return;

        String playerJob = plugin.getJobManager().getPlayerJob(player.getUniqueId());
        if (playerJob == null) {
            String message = plugin.getDungeonUtils().getConfig().getString("messages.no_job", "&c✖ Vous devez avoir un métier pour utiliser cette arme!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            event.setCancelled(true);
            return;
        }

        if (!dungeonUtils.canUseForJob(player, item)) {
            String message = plugin.getDungeonUtils().getConfig().getString("messages.incompatible_job", "&c✖ Cette arme n'est pas compatible avec votre métier actuel!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            event.setCancelled(true);
            return;
        }

        // The actual bonus will be applied by the JobActionListener
        // This just validates that the weapon can be used
    }
}
