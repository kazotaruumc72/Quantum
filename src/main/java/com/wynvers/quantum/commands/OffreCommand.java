package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.OrderManager;
import com.wynvers.quantum.managers.OrderManager.Order;
import com.wynvers.quantum.managers.OrderManager.PriceValidation;
import com.wynvers.quantum.managers.OrderManager.PriceStatus;
import com.wynvers.quantum.managers.OrderManager.ItemPrice;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OffreCommand implements CommandExecutor {
    private final Quantum plugin;
    private final OrderManager orderManager;
    private final Economy economy;
    
    public OffreCommand(Quantum plugin) {
        this.plugin = plugin;
        this.orderManager = plugin.getOrderManager();
        this.economy = plugin.getVaultManager().getEconomy();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Cette commande ne peut être exécutée que par un joueur.", 
                NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sendUsage(player);
            return true;
        }
        
        String itemKey = args[0].toLowerCase();
        int amount;
        double pricePerUnit;
        
        // Parser la quantité
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("La quantité doit être supérieure à 0.", NamedTextColor.GRAY)));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                .append(Component.text("Quantité invalide. Entrez un nombre entier.", NamedTextColor.GRAY)));
            return true;
        }
        
        // Parser le prix
        try {
            pricePerUnit = Double.parseDouble(args[2]);
            if (pricePerUnit <= 0) {
                player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("Le prix doit être supérieur à 0.", NamedTextColor.GRAY)));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                .append(Component.text("Prix invalide. Entrez un nombre décimal.", NamedTextColor.GRAY)));
            return true;
        }
        
        // Vérifier si l'item existe
        if (!orderManager.hasItemPrice(itemKey)) {
            player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                .append(Component.text("Cet item n'est pas disponible pour les ordres.", NamedTextColor.GRAY))
                .append(Component.text("\nUtilisez la complétion automatique pour voir les items disponibles.", 
                    NamedTextColor.DARK_GRAY)));
            return true;
        }
        
        ItemPrice itemPrice = orderManager.getItemPrice(itemKey);
        
        // Valider le prix
        PriceValidation validation = orderManager.validatePrice(itemKey, pricePerUnit);
        
        if (!validation.valid) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.DARK_GRAY));
            
            if (validation.status == PriceStatus.TOO_LOW) {
                player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("Prix trop bas!", NamedTextColor.GRAY, TextDecoration.BOLD)));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Votre prix: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.2f$", pricePerUnit), NamedTextColor.RED)));
                player.sendMessage(Component.text("Prix minimal attendu: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.2f$", validation.minPrice), NamedTextColor.GREEN)));
            } else if (validation.status == PriceStatus.TOO_HIGH) {
                player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                    .append(Component.text("Prix trop élevé!", NamedTextColor.GRAY, TextDecoration.BOLD)));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Votre prix: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.2f$", pricePerUnit), NamedTextColor.RED)));
                player.sendMessage(Component.text("Prix maximal attendu: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.2f$", validation.maxPrice), NamedTextColor.GREEN)));
            }
            
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("💡 ", NamedTextColor.AQUA)
                .append(Component.text("Prix acceptés: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.2f$ - %.2f$", 
                    validation.minPrice, validation.maxPrice), NamedTextColor.GREEN))
                .append(Component.text(" par unité", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.empty());
            return true;
        }
        
        // Calculer le coût total
        double totalCost = amount * pricePerUnit;
        
        // Vérifier le solde du joueur
        if (economy != null && !economy.has(player, totalCost)) {
            player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
                .append(Component.text("Fonds insuffisants!", NamedTextColor.GRAY)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Coût total: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.2f$", totalCost), NamedTextColor.RED)));
            player.sendMessage(Component.text("Votre solde: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.2f$", economy.getBalance(player)), 
                    NamedTextColor.GOLD)));
            player.sendMessage(Component.text("Manque: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.2f$", totalCost - economy.getBalance(player)), 
                    NamedTextColor.RED)));
            return true;
        }
        
        // Débiter le joueur
        if (economy != null) {
            economy.withdrawPlayer(player, totalCost);
        }
        
        // Créer l'ordre
        Order order = orderManager.createOrder(player, itemKey, amount, pricePerUnit);
        
        // Message de confirmation
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  ✓ ", NamedTextColor.GREEN, TextDecoration.BOLD)
            .append(Component.text("Offre créée avec succès!", NamedTextColor.GRAY, TextDecoration.BOLD)));
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("📦 Item: ", NamedTextColor.AQUA)
            .append(Component.text(itemPrice.displayName, NamedTextColor.WHITE)));
        player.sendMessage(Component.text("🔢 Quantité: ", NamedTextColor.AQUA)
            .append(Component.text(amount + "x", NamedTextColor.WHITE)));
        player.sendMessage(Component.text("💰 Prix unitaire: ", NamedTextColor.AQUA)
            .append(Component.text(String.format("%.2f$", pricePerUnit), NamedTextColor.GREEN)));
        player.sendMessage(Component.text("💵 Coût total: ", NamedTextColor.AQUA)
            .append(Component.text(String.format("%.2f$", totalCost), NamedTextColor.GOLD, TextDecoration.BOLD)));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("💡 ", NamedTextColor.YELLOW)
            .append(Component.text("L'argent sera restitué lorsque l'offre sera complétée.", 
                NamedTextColor.GRAY)));
        player.sendMessage(Component.text("═════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        
        return true;
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("❌ ", NamedTextColor.RED)
            .append(Component.text("Usage incorrect!", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  /offre <item> <quantité> <prix>", NamedTextColor.AQUA));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Exemples:", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  /offre nexo:afelia_bark 10 50", NamedTextColor.DARK_AQUA));
        player.sendMessage(Component.text("  /offre minecraft:stone 64 0.5", NamedTextColor.DARK_AQUA));
        player.sendMessage(Component.empty());
    }
}
