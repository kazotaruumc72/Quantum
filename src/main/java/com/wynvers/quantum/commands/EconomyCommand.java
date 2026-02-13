package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.economy.QuantumEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyCommand {
    
    private final Quantum plugin;
    
    public EconomyCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "balance":
            case "bal":
                return handleBalance(sender, args);
            case "give":
            case "add":
                return handleGive(sender, args);
            case "take":
            case "remove":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "currencies":
            case "list":
                return handleListCurrencies(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Resolve which currency to use from args.
     * Returns the primary QuantumEconomy if no currency is specified.
     */
    private QuantumEconomy resolveCurrency(CommandSender sender, String[] args, int currencyArgIndex) {
        if (args.length > currencyArgIndex) {
            String currencyId = args[currencyArgIndex].toLowerCase();
            QuantumEconomy eco = plugin.getVaultManager().getCurrency(currencyId);
            if (eco == null) {
                sender.sendMessage("§c⚠ Monnaie inconnue: " + currencyId);
                sender.sendMessage("§7Monnaies disponibles: §f" + String.join(", ", plugin.getVaultManager().getCurrencyIds()));
                return null;
            }
            return eco;
        }
        return plugin.getVaultManager().getQuantumEconomy();
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /quantum eco create <joueur> [monnaie]");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        QuantumEconomy eco = resolveCurrency(sender, args, 3);
        if (eco == null) return true;
        
        if (eco.createPlayerAccount(target)) {
            sender.sendMessage("§a§l✓ §aCompte " + eco.getSymbol() + " " + eco.currencyNameSingular() + " créé pour " + playerName + " !");
        } else {
            sender.sendMessage("§c⚠ Le compte existe déjà ou une erreur s'est produite !");
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /quantum eco delete <joueur> [monnaie]");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        QuantumEconomy eco = resolveCurrency(sender, args, 3);
        if (eco == null) return true;
        
        if (!eco.hasAccount(target)) {
            sender.sendMessage("§c⚠ Ce compte n'existe pas !");
            return true;
        }
        
        if (eco.deletePlayerAccount(target)) {
            sender.sendMessage("§a§l✓ §aCompte " + eco.getSymbol() + " " + eco.currencyNameSingular() + " supprimé pour " + playerName + " !");
        } else {
            sender.sendMessage("§c⚠ Erreur lors de la suppression du compte !");
        }
        
        return true;
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        OfflinePlayer target;
        int currencyArgIndex;
        
        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUtilisation: /quantum eco balance <joueur> [monnaie]");
                return true;
            }
            target = (Player) sender;
            currencyArgIndex = 2;
        } else {
            target = Bukkit.getOfflinePlayer(args[2]);
            currencyArgIndex = 3;
        }
        
        QuantumEconomy eco = resolveCurrency(sender, args, currencyArgIndex);
        if (eco == null) return true;
        
        double balance = eco.getBalance(target);
        sender.sendMessage("§e§lBALANCE §7» §f" + target.getName() + " : §a" + eco.format(balance));
        
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUtilisation: /quantum eco give <joueur> <montant> [monnaie]");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        QuantumEconomy eco = resolveCurrency(sender, args, 4);
        if (eco == null) return true;
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount <= 0) {
                sender.sendMessage("§cLe montant doit être positif !");
                return true;
            }
            
            if (eco.depositPlayer(target, amount).transactionSuccess()) {
                sender.sendMessage("§a§l✓ §a" + eco.format(amount) + 
                    " ajouté(s) au compte de " + playerName + " !");
                
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§a§l+ " + eco.format(amount));
                }
            } else {
                sender.sendMessage("§c⚠ Erreur lors du dépôt !");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMontant invalide !");
        }
        
        return true;
    }
    
    private boolean handleTake(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUtilisation: /quantum eco take <joueur> <montant> [monnaie]");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        QuantumEconomy eco = resolveCurrency(sender, args, 4);
        if (eco == null) return true;
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount <= 0) {
                sender.sendMessage("§cLe montant doit être positif !");
                return true;
            }
            
            if (eco.withdrawPlayer(target, amount).transactionSuccess()) {
                sender.sendMessage("§a§l✓ §a" + eco.format(amount) + 
                    " retiré(s) du compte de " + playerName + " !");
                
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§c§l- " + eco.format(amount));
                }
            } else {
                sender.sendMessage("§c⚠ Fonds insuffisants ou erreur !");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMontant invalide !");
        }
        
        return true;
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUtilisation: /quantum eco set <joueur> <montant> [monnaie]");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        QuantumEconomy eco = resolveCurrency(sender, args, 4);
        if (eco == null) return true;
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount < 0) {
                sender.sendMessage("§cLe montant ne peut pas être négatif !");
                return true;
            }
            
            double currentBalance = eco.getBalance(target);
            double difference = amount - currentBalance;
            
            boolean success;
            if (difference > 0) {
                success = eco.depositPlayer(target, difference).transactionSuccess();
            } else if (difference < 0) {
                success = eco.withdrawPlayer(target, Math.abs(difference)).transactionSuccess();
            } else {
                sender.sendMessage("§e⚠ Le solde est déjà à " + eco.format(amount));
                return true;
            }
            
            if (success) {
                sender.sendMessage("§a§l✓ §aSolde de " + playerName + " défini à " + 
                    eco.format(amount) + " !");
            } else {
                sender.sendMessage("§c⚠ Erreur lors de la modification du solde !");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMontant invalide !");
        }
        
        return true;
    }
    
    private boolean handleListCurrencies(CommandSender sender) {
        sender.sendMessage("§6§lMONNAIES DISPONIBLES");
        for (java.util.Map.Entry<String, QuantumEconomy> entry : plugin.getVaultManager().getCurrencies().entrySet()) {
            QuantumEconomy eco = entry.getValue();
            String primary = entry.getKey().equals(plugin.getVaultManager().getQuantumEconomy().getCurrencyId()) ? " §a(principale)" : "";
            sender.sendMessage("§e " + eco.getSymbol() + " §7" + entry.getKey() + " §f- " + eco.currencyNameSingular() + "/" + eco.currencyNamePlural() + primary);
        }
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES ÉCONOMIE QUANTUM");
        sender.sendMessage("§e/quantum eco create <joueur> [monnaie] §7- Créer un compte économique");
        sender.sendMessage("§e/quantum eco delete <joueur> [monnaie] §7- Supprimer un compte économique");
        sender.sendMessage("§e/quantum eco balance [joueur] [monnaie] §7- Voir le solde");
        sender.sendMessage("§e/quantum eco give <joueur> <montant> [monnaie] §7- Donner de l'argent");
        sender.sendMessage("§e/quantum eco take <joueur> <montant> [monnaie] §7- Retirer de l'argent");
        sender.sendMessage("§e/quantum eco set <joueur> <montant> [monnaie] §7- Définir le solde");
        sender.sendMessage("§e/quantum eco currencies §7- Lister les monnaies disponibles");
    }
}
