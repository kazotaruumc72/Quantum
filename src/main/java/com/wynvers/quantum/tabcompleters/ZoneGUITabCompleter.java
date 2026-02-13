package com.wynvers.quantum.tabcompleters;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TabCompleter for /zonegui command
 * Provides auto-completion for WorldGuard region names
 */
public class ZoneGUITabCompleter implements TabCompleter {
    
    private final Quantum plugin;
    
    public ZoneGUITabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("quantum.zone.configure")) {
            return completions;
        }
        
        if (args.length == 1 && sender instanceof Player player) {
            // Get all regions in the player's world
            try {
                RegionManager regionManager = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(player.getWorld()));
                
                if (regionManager != null) {
                    completions.addAll(regionManager.getRegions().keySet());
                }
            } catch (Exception e) {
                // Silently ignore errors
            }
        }
        
        // Filter based on input
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
