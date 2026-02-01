package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.OrderManager;
import com.wynvers.quantum.managers.OrderManager.Order;
import com.wynvers.quantum.managers.OrderManager.ItemPrice;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RechercheCommand implements CommandExecutor {
    private final Quantum plugin;
    private final OrderManager orderManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public RechercheCommand(Quantum plugin) {
        this.plugin = plugin;
        this.orderManager = plugin.getOrderManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Cette commande ne peut être exécutée que par un joueur.", 
                NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        
        String itemKey = args[0].toLowerCase();
        
        // Vérifier si l'item existe dans la configuration
        if (!orderManager.hasItemPrice(itemKey)) {
            player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                .append(Component.text("Cet item n'est pas disponible pour les ordres.", NamedTextColor.GRAY))
                .append(Component.text("\nUtilisez la complétion automatique pour voir les items disponibles.", 
                    NamedTextColor.DARK_GRAY)));
            return true;
        }
        
        ItemPrice itemPrice = orderManager.getItemPrice(itemKey);
        List<Order> orders = orderManager.getOrdersForItem(itemKey);
        
        // En-tête
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  Ordres pour ", NamedTextColor.GRAY)
            .append(Component.text(itemPrice.displayName, NamedTextColor.AQUA, TextDecoration.BOLD)));
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        
        // Informations sur les prix
        player.sendMessage(Component.text("💰 ", NamedTextColor.GOLD)
            .append(Component.text("Prix acceptés: ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.2f$", itemPrice.minPrice), NamedTextColor.GREEN))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text(String.format("%.2f$", itemPrice.maxPrice), NamedTextColor.GREEN))
            .append(Component.text(" par unité", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        
        // Liste des ordres
        if (orders.isEmpty()) {
            player.sendMessage(Component.text("📭 ", NamedTextColor.YELLOW)
                .append(Component.text("Aucune offre active pour cet item.", NamedTextColor.GRAY)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("💡 ", NamedTextColor.AQUA)
                .append(Component.text("Soyez le premier à créer une offre avec: ", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("   /offre " + itemKey + " <quantité> <prix>", 
                NamedTextColor.DARK_AQUA));
        } else {
            player.sendMessage(Component.text("📦 ", NamedTextColor.AQUA)
                .append(Component.text("Offres actives (" + orders.size() + "):", NamedTextColor.GRAY)));
            player.sendMessage(Component.empty());
            
            int displayCount = Math.min(orders.size(), 10); // Afficher max 10 offres
            
            for (int i = 0; i < displayCount; i++) {
                Order order = orders.get(i);
                String date = dateFormat.format(new Date(order.createdAt));
                
                player.sendMessage(Component.text("  " + (i + 1) + ". ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(order.playerName, NamedTextColor.YELLOW))
                    .append(Component.text(" • ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(order.amount + "x", NamedTextColor.WHITE))
                    .append(Component.text(" • ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(String.format("%.2f$", order.pricePerUnit), NamedTextColor.GREEN))
                    .append(Component.text("/unité", NamedTextColor.GRAY)));
                player.sendMessage(Component.text("     Total: ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(String.format("%.2f$", order.getTotalPrice()), 
                        NamedTextColor.GOLD))
                    .append(Component.text(" • ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Créé: " + date, NamedTextColor.DARK_GRAY)));
                player.sendMessage(Component.empty());
            }
            
            if (orders.size() > 10) {
                player.sendMessage(Component.text("  ... et " + (orders.size() - 10) + 
                    " autre(s) offre(s)", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
                player.sendMessage(Component.empty());
            }
        }
        
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        
        return true;
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
            .append(Component.text("Usage incorrect!", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  /recherche <item>", NamedTextColor.AQUA));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Exemples:", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  /recherche nexo:afelia_bark", NamedTextColor.DARK_AQUA));
        player.sendMessage(Component.text("  /recherche minecraft:stone", NamedTextColor.DARK_AQUA));
        player.sendMessage(Component.empty());
    }
}
