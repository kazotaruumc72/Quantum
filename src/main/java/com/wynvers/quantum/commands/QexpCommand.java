package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * Commande admin pour gérer l'expérience des joueurs
 * Usage: /qexp <player> <give|take|set|reset> <amount>
 */
public class QexpCommand implements CommandExecutor {

    private final Quantum plugin;
    private final PlayerLevelManager levelManager;

    public QexpCommand(Quantum plugin, PlayerLevelManager levelManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("quantum.exp.admin")) {
            sender.sendMessage("§c§l✗ §cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§6§l[EXP] §7Usage: §f/qexp <joueur> <give|take|set|reset> [montant]");
            return true;
        }

        String playerName = args[0];
        String action = args[1].toLowerCase();

        // Récupérer le joueur (online ou offline)
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c§l✗ §cJoueur introuvable: §f" + playerName);
            return true;
        }

        UUID uuid = target.getUniqueId();

        switch (action) {
            case "give":
                if (args.length < 3) {
                    sender.sendMessage("§c§l✗ §cUsage: §f/qexp <joueur> give <montant>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§c§l✗ §cLe montant doit être positif.");
                        return true;
                    }
                    levelManager.addExp(uuid, amount);
                    sender.sendMessage("§a§l✓ §7Vous avez donné §f" + amount + " §7XP à §f" + playerName + "§7.");
                    notifyPlayer(target, "§a§l+ §f" + amount + " §7XP (admin)");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c§l✗ §cMontant invalide.");
                }
                break;

            case "take":
                if (args.length < 3) {
                    sender.sendMessage("§c§l✗ §cUsage: §f/qexp <joueur> take <montant>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§c§l✗ §cLe montant doit être positif.");
                        return true;
                    }
                    levelManager.removeExp(uuid, amount);
                    sender.sendMessage("§a§l✓ §7Vous avez retiré §f" + amount + " §7XP à §f" + playerName + "§7.");
                    notifyPlayer(target, "§c§l- §f" + amount + " §7XP (admin)");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c§l✗ §cMontant invalide.");
                }
                break;

            case "set":
                if (args.length < 3) {
                    sender.sendMessage("§c§l✗ §cUsage: §f/qexp <joueur> set <montant>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount < 0) {
                        sender.sendMessage("§c§l✗ §cLe montant ne peut pas être négatif.");
                        return true;
                    }
                    levelManager.setExp(uuid, amount);
                    sender.sendMessage("§a§l✓ §7L'exp de §f" + playerName + " §7a été défini à §f" + amount + "§7.");
                    notifyPlayer(target, "§e§l! §7Votre EXP a été définie à §f" + amount + " §7(admin)");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c§l✗ §cMontant invalide.");
                }
                break;

            case "reset":
                levelManager.resetExp(uuid);
                sender.sendMessage("§a§l✓ §7L'exp de §f" + playerName + " §7a été réinitialisée.");
                notifyPlayer(target, "§c§l✗ §7Votre EXP a été réinitialisée (admin)");
                break;

            default:
                sender.sendMessage("§c§l✗ §cAction invalide. Utilisez: §fgive, take, set, reset");
                break;
        }

        return true;
    }

    private void notifyPlayer(OfflinePlayer player, String message) {
        if (player.isOnline() && player.getPlayer() != null) {
            player.getPlayer().sendMessage("§6§l[EXP] " + message);
        }
    }
}