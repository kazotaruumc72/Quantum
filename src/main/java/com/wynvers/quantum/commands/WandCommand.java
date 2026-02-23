package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.listeners.DoorSelectionListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /quantum wand pour gérer différents types de baguettes
 */
public class WandCommand implements CommandExecutor {

    private final Quantum plugin;

    public WandCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /quantum wand door");
            return true;
        }

        String wandType = args[1].toLowerCase();

        switch (wandType) {
            case "door":
                return handleDoorWand(sender);
            default:
                sender.sendMessage("§cType de baguette inconnu: " + wandType);
                sender.sendMessage("§7Types disponibles: door");
                return true;
        }
    }

    private boolean handleDoorWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        if (!sender.hasPermission("quantum.tower.door.wand")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(DoorSelectionListener.createWand());
        player.sendMessage("§a§l[Doors] §aHache de sélection reçue!");
        player.sendMessage("§7Clic gauche: Position 1 | Clic droit: Position 2");

        return true;
    }
}
