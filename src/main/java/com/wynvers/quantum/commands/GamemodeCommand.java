package com.wynvers.quantum.commands;

import com.wynvers.quantum.managers.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Gamemode shortcuts command handler
 * Handles all gamemode change commands: /gmc, /gms, /gmsp, /gma
 * Uses the command label to determine which gamemode to apply
 * Messages are loaded from messages.yml (section: gamemode)
 */
public class GamemodeCommand implements CommandExecutor {

    private final MessageManager messageManager;

    public GamemodeCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (messageManager != null) {
                messageManager.sendPrefixedMessage(sender, "gamemode.player-only");
            } else {
                sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            }
            return true;
        }

        // Determine gamemode from command label
        GameMode gameMode = switch (label.toLowerCase()) {
            case "gmc" -> GameMode.CREATIVE;
            case "gms" -> GameMode.SURVIVAL;
            case "gmsp" -> GameMode.SPECTATOR;
            case "gma" -> GameMode.ADVENTURE;
            default -> null;
        };

        if (gameMode == null) {
            if (messageManager != null) {
                messageManager.sendPrefixedMessage(player, "gamemode.unknown-command");
            } else {
                player.sendMessage("§cCommande de gamemode inconnue.");
            }
            return true;
        }

        // Check permission
        String permission = "quantum.gamemode." + gameMode.name().toLowerCase();
        if (!player.hasPermission(permission) && !player.hasPermission("quantum.gamemode.*")) {
            if (messageManager != null) {
                messageManager.sendPrefixedMessage(player, "gamemode.no-permission",
                        Map.of("mode", gameMode.name()));
            } else {
                player.sendMessage("§cVous n'avez pas la permission de changer en mode " + gameMode.name() + ".");
            }
            return true;
        }

        // Change gamemode
        player.setGameMode(gameMode);
        if (messageManager != null) {
            messageManager.sendPrefixedMessage(player, "gamemode.changed",
                    Map.of("mode", gameMode.name()));
        } else {
            player.sendMessage("§aGamemode changé en §e" + gameMode.name() + "§a.");
        }
        
        return true;
    }
}
