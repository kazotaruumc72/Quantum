package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.Order;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Commande /recherche - Affiche les ordres actifs pour un item ou une catégorie
 * Sans argument : ouvre le menu des catégories
 * Avec catégorie : affiche tous les ordres de la catégorie
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
            // Ouvrir le menu des catégories
            plugin.getMenuManager().openMenu(player, "orders_categories");
            return true;
        }

        String input = args[0].toLowerCase();

        // Vérifier si c'est une catégorie
        if (plugin.getOrderManager().getAllCategories().contains(input)) {
            // Afficher les ordres de la catégorie
            displayCategoryOrders(player, input);
        } 
        // Sinon vérifier si c'est un item
        else if (plugin.getOrderManager().isItemAllowed(input)) {
            // Afficher les ordres pour cet item spécifique
            displayItemOrders(player, input);
        } 
        else {
            player.sendMessage(mm.deserialize("<red>Catégorie ou item inconnu: " + input));
            player.sendMessage(mm.deserialize("<gray>Utilisez <white>/rechercher</white> pour ouvrir le menu."));
        }

        return true;
    }

    private void displayCategoryOrders(Player player, String category) {
        List<Order> orders = plugin.getOrderManager().getActiveOrdersForCategory(category);

        if (orders.isEmpty()) {
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            player.sendMessage(mm.deserialize(
                "<#32b8c6>➥ Catégorie : <white>" + category
            ));
            player.sendMessage("");
            player.sendMessage(mm.deserialize("<yellow>⚠ Aucune offre active dans cette catégorie."));
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            return;
        }

        // Affichage des ordres
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>➥ Catégorie : <white>" + category + " <gray>(" + orders.size() + " offres)"
        ));
        player.sendMessage("");

        for (int i = 0; i < orders.size() && i < 10; i++) {
            Order order = orders.get(i);
            String timeRemaining = order.getFormattedTimeRemaining("<#32b8c6>%days%j %hours%h %minutes%m");
            
            player.sendMessage(mm.deserialize(
                "<white>" + (i + 1) + ". <#32b8c6>" + order.getItemId() + " <gray>par <white>" + order.getPlayerName()
            ));
            player.sendMessage(mm.deserialize(
                "   <gray>Quantité: <white>" + order.getRemainingQuantity() + " <dark_gray>| " +
                "<gray>Prix: <green>" + String.format("%.2f$", order.getPricePerUnit()) + "<gray>/u <dark_gray>| " +
                "<gray>Total: <yellow>" + String.format("%.2f$", order.getTotalPrice())
            ));
            player.sendMessage(mm.deserialize(
                "   <gray>Expire dans: " + timeRemaining
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
            "<gray>Utilisez le menu pour vendre vos items: <white>/rechercher"
        ));
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }

    private void displayItemOrders(Player player, String itemId) {
        List<Order> orders = plugin.getOrderManager().getActiveOrdersForItem(itemId);

        if (orders.isEmpty()) {
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            player.sendMessage(mm.deserialize(
                "<#32b8c6>➥ Recherche : <white>" + itemId
            ));
            player.sendMessage("");
            player.sendMessage(mm.deserialize("<yellow>⚠ Aucune offre active pour cet item."));
            player.sendMessage(mm.deserialize("<gray>Soyez le premier à créer une offre !"));
            player.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            return;
        }

        // Affichage des ordres
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>➥ Recherche : <white>" + itemId + " <gray>(" + orders.size() + " offres)"
        ));
        player.sendMessage("");

        for (int i = 0; i < orders.size() && i < 10; i++) {
            Order order = orders.get(i);
            String timeRemaining = order.getFormattedTimeRemaining("<#32b8c6>%days%j %hours%h %minutes%m");
            
            player.sendMessage(mm.deserialize(
                "<white>" + (i + 1) + ". <#32b8c6>" + order.getPlayerName() + " <gray>- <green>" + 
                String.format("%.2f$", order.getPricePerUnit()) + "<gray>/unité <dark_gray>x" + order.getRemainingQuantity()
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
    }
}
