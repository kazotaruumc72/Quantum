package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.apartment.ApartmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Apartment command handler
 * PREPARATION PHASE - Placeholder implementation
 * 
 * Planned commands:
 * /apartment - View your apartment info
 * /apartment create <name> - Create/claim an apartment
 * /apartment upgrade - Upgrade apartment tier
 * /apartment invite <player> - Invite visitor
 * /apartment remove <player> - Remove visitor
 * /apartment lock/unlock - Toggle lock status
 * /apartment tp - Teleport to your apartment
 */
public class ApartmentCommand implements CommandExecutor {

    private final Quantum plugin;
    private final ApartmentManager apartmentManager;

    public ApartmentCommand(Quantum plugin, ApartmentManager apartmentManager) {
        this.plugin = plugin;
        this.apartmentManager = apartmentManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            // Show apartment info
            player.sendMessage("§6§l=== Apartment System ===");
            player.sendMessage("§7This feature is currently in preparation phase.");
            player.sendMessage("§7Coming soon:");
            player.sendMessage("§8- §f/apartment create <name> §7- Create an apartment");
            player.sendMessage("§8- §f/apartment upgrade §7- Upgrade your apartment");
            player.sendMessage("§8- §f/apartment invite <player> §7- Invite visitors");
            player.sendMessage("§8- §f/apartment tp §7- Teleport to apartment");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                player.sendMessage("§cApartment creation is not yet available.");
                player.sendMessage("§7This feature will be implemented in a future update.");
                break;
            
            case "upgrade":
                player.sendMessage("§cApartment upgrades are not yet available.");
                break;
            
            case "invite":
                player.sendMessage("§cVisitor management is not yet available.");
                break;
            
            case "remove":
                player.sendMessage("§cVisitor management is not yet available.");
                break;
            
            case "lock":
            case "unlock":
                player.sendMessage("§cApartment locking is not yet available.");
                break;
            
            case "tp":
            case "teleport":
                player.sendMessage("§cApartment teleportation is not yet available.");
                break;
            
            default:
                player.sendMessage("§cUnknown subcommand. Use §f/apartment§c for help.");
                break;
        }

        return true;
    }
}
