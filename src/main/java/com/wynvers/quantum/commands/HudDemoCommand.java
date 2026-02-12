package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.betterhud.BetterHudUtil;
import com.wynvers.quantum.betterhud.QuantumBetterHudManager;
import com.wynvers.quantum.betterhud.QuantumCompassManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Example command demonstrating BetterHud integration.
 * Commands:
 * - /huddemo popup <popup_name> - Show a popup
 * - /huddemo waypoint add <name> - Add a waypoint at current location
 * - /huddemo waypoint remove <name> - Remove a waypoint
 * - /huddemo waypoint clear - Clear all waypoints
 */
public class HudDemoCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public HudDemoCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
        QuantumCompassManager compassManager = plugin.getCompassManager();
        
        if (hudManager == null || !hudManager.isAvailable()) {
            player.sendMessage("§cBetterHud is not available! Please install BetterHud plugin.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "popup":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /huddemo popup <popup_name> [key:value...]");
                    return true;
                }
                
                String popupName = args[1];
                Map<String, String> variables = null;
                
                // Parse optional variables (format: key:value)
                if (args.length > 2) {
                    String[] varPairs = new String[(args.length - 2) * 2];
                    int index = 0;
                    for (int i = 2; i < args.length; i++) {
                        String[] parts = args[i].split(":", 2);
                        if (parts.length == 2) {
                            varPairs[index++] = parts[0];
                            varPairs[index++] = parts[1];
                        }
                    }
                    
                    // Only create variables if we have at least one pair
                    if (index > 0) {
                        // Trim array if some pairs were invalid
                        if (index < varPairs.length) {
                            String[] trimmed = new String[index];
                            System.arraycopy(varPairs, 0, trimmed, 0, index);
                            varPairs = trimmed;
                        }
                        variables = BetterHudUtil.createVariables(varPairs);
                    }
                }
                
                boolean shown = variables != null 
                    ? hudManager.showPopup(player, popupName, variables)
                    : hudManager.showPopup(player, popupName);
                
                if (shown) {
                    player.sendMessage("§aShowing popup: §f" + popupName);
                } else {
                    player.sendMessage("§cFailed to show popup (might be on cooldown or not found)");
                }
                break;
                
            case "waypoint":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /huddemo waypoint <add|remove|clear> [name]");
                    return true;
                }
                
                String action = args[1].toLowerCase();
                
                switch (action) {
                    case "add":
                        if (args.length < 3) {
                            player.sendMessage("§cUsage: /huddemo waypoint add <name> [icon]");
                            return true;
                        }
                        
                        String waypointName = args[2];
                        String icon = args.length > 3 ? args[3] : null;
                        Location loc = player.getLocation();
                        
                        if (compassManager.addWaypoint(player, waypointName, loc, icon)) {
                            player.sendMessage("§aAdded waypoint: §f" + waypointName + 
                                " §aat §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        } else {
                            player.sendMessage("§cFailed to add waypoint!");
                        }
                        break;
                        
                    case "remove":
                        if (args.length < 3) {
                            player.sendMessage("§cUsage: /huddemo waypoint remove <name>");
                            return true;
                        }
                        
                        String removeWaypoint = args[2];
                        
                        if (compassManager.removeWaypoint(player, removeWaypoint)) {
                            player.sendMessage("§aRemoved waypoint: §f" + removeWaypoint);
                        } else {
                            player.sendMessage("§cFailed to remove waypoint!");
                        }
                        break;
                        
                    case "clear":
                        if (compassManager.clearWaypoints(player)) {
                            player.sendMessage("§aCleared all waypoints!");
                        } else {
                            player.sendMessage("§cFailed to clear waypoints!");
                        }
                        break;
                        
                    case "list":
                        Map<String, QuantumCompassManager.CompassPoint> waypoints = compassManager.getWaypoints(player);
                        if (waypoints.isEmpty()) {
                            player.sendMessage("§7You have no active waypoints.");
                        } else {
                            player.sendMessage("§aYour waypoints:");
                            waypoints.forEach((name, point) -> {
                                Location wpLoc = point.getLocation();
                                player.sendMessage("  §f" + name + " §7- " + 
                                    wpLoc.getBlockX() + ", " + wpLoc.getBlockY() + ", " + wpLoc.getBlockZ());
                            });
                        }
                        break;
                        
                    default:
                        player.sendMessage("§cUnknown waypoint action: " + action);
                        break;
                }
                break;
                
            case "test":
                // Show a test popup with example variables
                Map<String, String> testVars = BetterHudUtil.createVariables(
                    "player", player.getName(),
                    "health", BetterHudUtil.formatNumber((long) player.getHealth()),
                    "level", String.valueOf(player.getLevel())
                );
                
                if (hudManager.showPopup(player, "test_popup", testVars)) {
                    player.sendMessage("§aShowing test popup with variables!");
                } else {
                    player.sendMessage("§cFailed to show test popup. Make sure 'test_popup' exists in BetterHud config.");
                }
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== BetterHud Demo Commands ===");
        player.sendMessage("§e/huddemo popup <name> §7- Show a popup");
        player.sendMessage("§e/huddemo waypoint add <name> [icon] §7- Add waypoint at current location");
        player.sendMessage("§e/huddemo waypoint remove <name> §7- Remove a waypoint");
        player.sendMessage("§e/huddemo waypoint clear §7- Clear all waypoints");
        player.sendMessage("§e/huddemo waypoint list §7- List all waypoints");
        player.sendMessage("§e/huddemo test §7- Show test popup");
    }
}
