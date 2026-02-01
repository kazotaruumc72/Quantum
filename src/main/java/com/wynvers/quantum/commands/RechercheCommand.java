package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.Order;
import com.wynvers.quantum.orders.OrderItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Commande /recherche - Affiche les ordres actifs pour un item
 * Avec chronomètres affichés via placeholders
 */
public class RechercheCommand implements CommandExecutor {
    private final Quantum plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RechercheCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.deserialize("<red>Cette commande est réservée aux joueurs."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(mm.deserialize("<red>Usage: /recherche <item>"));
            player.sendMessage(mm.deserialize("<gray>Exemple: <white>/recherche minecraft:diamond"));
            return true;
        }

        String itemId = args[0].toLowerCase();

        // Vérifier si l'item existe
        if (!plugin.getOrderManager().hasItem(itemId)) {
            player.sendMessage(mm.deserialize("<red>Item inconnu: " + itemId));
            player.sendMessage(mm.deserialize("<gray>Utilisez <white>/menu orders_categories</white> pour voir les items disponibles."));
            return true;
        }

        OrderItem item = plugin.getOrderManager().getItem(itemId);
        List<Order> orders = plugin.getOrderManager().getActiveOrdersForItem(itemId);

        if (orders.isEmpty()) {
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            player.sendMessage(mm.deserialize(
                "<#32b8c6>➥ Recherche : <white>" + item.getDisplayName()
            ));
            player.sendMessage("");
            player.sendMessage(mm.deserialize("<yellow>⚠ Aucune offre active pour cet item."));
            player.sendMessage(mm.deserialize("<gray>Soyez le premier à créer une offre !"));
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            return true;
        }

        // Affichage des ordres avec chronomètre
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>➥ Recherche : <white>" + item.getDisplayName() + " <gray>(" + orders.size() + " offres)"
        ));
        player.sendMessage("");

        for (int i = 0; i < orders.size() && i < 10; i++) {
            Order order = orders.get(i);
            String timeRemaining = plugin.getOrderManager().formatTimeRemaining(order);
            
            player.sendMessage(mm.deserialize(
                "<white>" + (i + 1) + ". <#32b8c6>" + order.getPlayerName() + " <gray>- <green>" + 
                String.format("%.2f$", order.getPricePerUnit()) + "<gray>/unité <dark_gray>x" + order.getQuantity()
            ));
            player.sendMessage(mm.deserialize(
                "   <gray>Total: <yellow>" + String.format("%.2f$", order.getTotalPrice()) + 
                " <dark_gray>| <gray>Expire dans: " + timeRemaining
            ));
        }

        if (orders.size() > 10) {
            player.sendMessage("");
            player.sendMessage(mm.deserialize(
                "<gray>... et " + (orders.size() - 10) + " autres offres"
            ));
        }

        player.sendMessage("");
        player.sendMessage(mm.deserialize(
            "<#32b8c6>➥ Pour créer une offre : <white>/offre " + itemId + " <quantité> <prix>"
        ));
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));

        return true;
    }
}
