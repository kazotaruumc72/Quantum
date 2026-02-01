package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.Order;
import com.wynvers.quantum.utils.ItemUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Commande /offre - Crée une offre d'achat pour items NON STOCKABLES
 * Pour les items stockables, le joueur doit passer par le menu Storage en mode ORDER
 * 
 * Usage: /offre <quantité> <prix_unitaire>
 * Le joueur tient l'item en main (armure, épée, potion, etc.)
 */
public class OffreCommand implements CommandExecutor {
    private final Quantum plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public OffreCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.deserialize("<red>Cette commande est réservée aux joueurs."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<red>Usage: /offre <quantité> <prix_unitaire>"));
            player.sendMessage(mm.deserialize("<gray>Tenez l'item en main et spécifiez la quantité et le prix."));
            player.sendMessage(mm.deserialize("<gray>Exemple: <white>/offre 5 100 <gray>(5 items à 100$ l'unité)"));
            player.sendMessage("");
            player.sendMessage(mm.deserialize("<yellow>⚠ Cette commande est pour les items NON stockables"));
            player.sendMessage(mm.deserialize("<gray>Pour les items stackables, utilisez le Storage en mode ORDER"));
            return true;
        }

        // Vérifier que le joueur tient un item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage(mm.deserialize("<red>Vous devez tenir un item en main!"));
            return true;
        }

        // Récupérer l'ID de l'item
        String itemId = ItemUtils.getItemId(heldItem);
        
        // Vérifier si l'item est autorisé
        if (!plugin.getOrderManager().isItemAllowed(itemId)) {
            player.sendMessage(mm.deserialize("<red>Cet item ne peut pas faire l'objet d'une offre d'achat."));
            return true;
        }

        int quantity;
        double price;

        try {
            quantity = Integer.parseInt(args[0]);
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Quantité et prix doivent être des nombres valides."));
            return true;
        }

        // Vérifications
        if (quantity <= 0) {
            player.sendMessage(mm.deserialize("<red>La quantité doit être positive."));
            return true;
        }

        if (price <= 0) {
            player.sendMessage(mm.deserialize("<red>Le prix doit être positif."));
            return true;
        }

        // Vérifier que le joueur a assez d'items en main
        if (heldItem.getAmount() < quantity) {
            player.sendMessage(mm.deserialize(
                "<red>Vous n'avez que " + heldItem.getAmount() + " item(s) en main!"
            ));
            return true;
        }

        // Vérifier la limite d'ordres actifs
        int currentOrders = plugin.getOrderManager().countActiveOrdersForPlayer(player.getUniqueId());
        int maxOrders = plugin.getOrderManager().getMaxActiveOrdersForPlayer(player);
        
        if (currentOrders >= maxOrders) {
            player.sendMessage(mm.deserialize(
                "<red>Vous avez atteint votre limite d'ordres actifs (" + maxOrders + ")."
            ));
            player.sendMessage(mm.deserialize(
                "<gray>Annulez un ordre existant ou attendez qu'il expire."
            ));
            return true;
        }

        // Vérifier l'économie (Vault)
        double totalCost = quantity * price;
        if (!plugin.getVaultManager().has(player, totalCost)) {
            player.sendMessage(mm.deserialize(
                "<red>Vous n'avez pas assez d'argent! Coût total: <yellow>" + 
                String.format("%.2f$", totalCost)
            ));
            return true;
        }

        // Retirer les items de la main du joueur
        heldItem.setAmount(heldItem.getAmount() - quantity);
        
        // Déduire l'argent
        plugin.getVaultManager().withdraw(player, totalCost);

        // Créer l'ordre
        Order order = plugin.getOrderManager().createOrder(player, itemId, quantity, price);
        long duration = plugin.getOrderManager().getOrderDurationForPlayer(player);
        String category = plugin.getOrderManager().getCategoryForItem(itemId);

        // Confirmation
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        player.sendMessage(mm.deserialize(
            "<green>✓ Offre créée avec succès!"
        ));
        player.sendMessage("");
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Item : <white>" + itemId
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Catégorie : <white>" + category
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Quantité : <white>" + quantity
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Prix unitaire : <green>" + String.format("%.2f$", price)
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Coût total : <yellow>" + String.format("%.2f$", totalCost)
        ));
        player.sendMessage(mm.deserialize(
            "<#32b8c6>Durée : <white>" + duration + " jours"
        ));
        player.sendMessage("");
        player.sendMessage(mm.deserialize(
            "<gray>Votre offre est maintenant visible dans la catégorie <white>" + category + "</white>."
        ));
        player.sendMessage(mm.deserialize(
            "<gray>Les items ont été retirés de votre inventaire."
        ));
        player.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));

        return true;
    }
}
