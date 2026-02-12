package com.wynvers.quantum.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.worldguard.gui.ZoneGUIManager;
import com.wynvers.quantum.worldguard.gui.ZoneSettingsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for managing WorldGuard zones with GUI
 * Usage: /zonegui [region_name]
 */
public class ZoneGUICommand implements CommandExecutor {

    private final Quantum plugin;
    private final ZoneGUIManager zoneManager;
    private final ZoneSettingsGUI settingsGUI;

    public ZoneGUICommand(Quantum plugin, ZoneGUIManager zoneManager, ZoneSettingsGUI settingsGUI) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        this.settingsGUI = settingsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("quantum.zone.configure")) {
            player.sendMessage("§cYou don't have permission to configure zones.");
            return true;
        }

        if (args.length == 0) {
            // Try to get current region
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

            if (regionManager == null) {
                player.sendMessage("§cNo regions found in this world.");
                return true;
            }

            var regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
            if (regions.size() == 0) {
                player.sendMessage("§cYou are not in any region. Usage: /zonegui <region_name>");
                return true;
            }

            // Get first region
            ProtectedRegion region = regions.iterator().next();
            settingsGUI.openZoneSettings(player, region.getId());
            return true;
        }

        String regionName = args[0];
        
        // Verify region exists
        RegionManager regionManager = WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            player.sendMessage("§cNo regions found in this world.");
            return true;
        }

        if (!regionManager.hasRegion(regionName)) {
            player.sendMessage("§cRegion '" + regionName + "' not found in this world.");
            return true;
        }

        settingsGUI.openZoneSettings(player, regionName);
        return true;
    }
}
