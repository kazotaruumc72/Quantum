package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StorageCommand implements CommandExecutor {

    private final Quantum plugin;

    public StorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("quantum.storage")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        // Si aucun argument ou "open", ouvrir le menu
        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            Menu storageMenu = plugin.getMenuManager().getMenu("storage");
            if (storageMenu != null) {
                storageMenu.open(player, plugin);
            } else {
                player.sendMessage("§cErreur: Menu storage non trouvé!");
            }
            return true;
        }

        // Commandes admin
        if (!player.hasPermission("quantum.storage.admin")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "view":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation: /storage view <joueur>");
                    return true;
                }
                // TODO: Implémenter la visualisation du storage d'un joueur
                player.sendMessage("§aVisualisez le storage de " + args[1]);
                break;

            case "clear":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation: /storage clear <joueur>");
                    return true;
                }
                // TODO: Implémenter la suppression du storage d'un joueur
                player.sendMessage("§aStorage de " + args[1] + " supprimé!");
                break;

            case "reload":
                // TODO: Implémenter le rechargement du storage
                player.sendMessage("§aStorage rechargé!");
                break;

            case "transfert":
                if (args.length < 4) {
                    player.sendMessage("§cUtilisation: /storage transfert <joueur> <nexo:id|material> <quantité>");
                    return true;
                }
                handleTransfert(player, args);
                break;

            case "remove":
                if (args.length < 4) {
                    player.sendMessage("§cUtilisation: /storage remove <joueur> <nexo:id|material> <quantité>");
                    return true;
                }
                handleRemove(player, args);
                break;

            default:
                player.sendMessage("§cCommande inconnue. Utilisez: open, view, clear, reload, transfert, remove");
                break;
        }

        return true;
    }

    /**
     * Gère la commande transfert
     */
    private void handleTransfert(Player sender, String[] args) {
        String targetPlayer = args[1];
        String itemType = args[2];
        int amount;
        
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLa quantité doit être un nombre!");
            return;
        }

        // TODO: Implémenter le transfert d'items vers le storage du joueur
        // Vérifier si c'est un item Nexo (commence par "nexo:") ou un material Minecraft
        sender.sendMessage("§aTransfert de " + amount + " " + itemType + " vers le storage de " + targetPlayer);
    }

    /**
     * Gère la commande remove
     */
    private void handleRemove(Player sender, String[] args) {
        String targetPlayer = args[1];
        String itemType = args[2];
        int amount;
        
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLa quantité doit être un nombre!");
            return;
        }

        // TODO: Implémenter la suppression d'items du storage du joueur
        // Vérifier si c'est un item Nexo (commence par "nexo:") ou un material Minecraft
        sender.sendMessage("§aSuppression de " + amount + " " + itemType + " du storage de " + targetPlayer);
    }
}
