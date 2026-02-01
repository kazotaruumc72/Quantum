package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Commande /rechercher - Ouvre le menu des catégories d'ordres
 * Permet de naviguer dans les différentes catégories d'items disponibles
 */
public class RechercherCommand implements CommandExecutor {
    private final Quantum plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RechercherCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.deserialize("<red>Cette commande est réservée aux joueurs."));
            return true;
        }

        Player player = (Player) sender;

        // Vérifier la permission
        if (!player.hasPermission("quantum.orders.search")) {
            player.sendMessage(mm.deserialize("<red>Vous n'avez pas la permission d'utiliser cette commande."));
            return true;
        }

        // Récupérer et ouvrir le menu des catégories
        Menu menu = plugin.getMenuManager().getMenu("orders_categories");
        
        if (menu != null) {
            menu.open(player, plugin);
        } else {
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            player.sendMessage(mm.deserialize(
                "<red>✗ Menu des catégories introuvable!"
            ));
            player.sendMessage("");
            player.sendMessage(mm.deserialize(
                "<yellow>Le fichier <white>menus/orders_categories.yml</white> est manquant."
            ));
            player.sendMessage(mm.deserialize(
                "<gray>Contactez un administrateur ou utilisez <white>/quantum reload</white>."
            ));
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
        }

        return true;
    }
}
