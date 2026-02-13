package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
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
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /quantum eco create <joueur>");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        if (plugin.getVaultManager().getQuantumEconomy().createPlayerAccount(target)) {
            sender.sendMessage("§a§l✓ §aCompte économique créé pour " + playerName + " !");
        } else {
            sender.sendMessage("§c⚠ Le compte existe déjà ou une erreur s'est produite !");
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /quantum eco delete <joueur>");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        if (!plugin.getVaultManager().getQuantumEconomy().hasAccount(target)) {
            sender.sendMessage("§c⚠ Ce compte n'existe pas !");
            return true;
        }
        
        if (plugin.getVaultManager().getQuantumEconomy().deletePlayerAccount(target)) {
            sender.sendMessage("§a§l✓ §aCompte économique supprimé pour " + playerName + " !");
        } else {
            sender.sendMessage("§c⚠ Erreur lors de la suppression du compte !");
        }
        
        return true;
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        OfflinePlayer target;
        
        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUtilisation: /quantum eco balance <joueur>");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getOfflinePlayer(args[2]);
        }
        
        double balance = plugin.getVaultManager().getBalance(target);
        sender.sendMessage("§e§lBALANCE §7» §f" + target.getName() + " : §a" + 
            plugin.getVaultManager().format(balance));
        
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUtilisation: /quantum eco give <joueur> <montant>");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount <= 0) {
                sender.sendMessage("§cLe montant doit être positif !");
                return true;
            }
            
            if (plugin.getVaultManager().deposit(target, amount)) {
                sender.sendMessage("§a§l✓ §a" + plugin.getVaultManager().format(amount) + 
                    " ajouté(s) au compte de " + playerName + " !");
                
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§a§l+ " + plugin.getVaultManager().format(amount));
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
            sender.sendMessage("§cUtilisation: /quantum eco take <joueur> <montant>");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount <= 0) {
                sender.sendMessage("§cLe montant doit être positif !");
                return true;
            }
            
            if (plugin.getVaultManager().withdraw(target, amount)) {
                sender.sendMessage("§a§l✓ §a" + plugin.getVaultManager().format(amount) + 
                    " retiré(s) du compte de " + playerName + " !");
                
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§c§l- " + plugin.getVaultManager().format(amount));
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
            sender.sendMessage("§cUtilisation: /quantum eco set <joueur> <montant>");
            return true;
        }
        
        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        
        try {
            double amount = Double.parseDouble(args[3]);
            
            if (amount < 0) {
                sender.sendMessage("§cLe montant ne peut pas être négatif !");
                return true;
            }
            
            double currentBalance = plugin.getVaultManager().getBalance(target);
            double difference = amount - currentBalance;
            
            boolean success;
            if (difference > 0) {
                success = plugin.getVaultManager().deposit(target, difference);
            } else if (difference < 0) {
                success = plugin.getVaultManager().withdraw(target, Math.abs(difference));
            } else {
                sender.sendMessage("§e⚠ Le solde est déjà à " + plugin.getVaultManager().format(amount));
                return true;
            }
            
            if (success) {
                sender.sendMessage("§a§l✓ §aSolde de " + playerName + " défini à " + 
                    plugin.getVaultManager().format(amount) + " !");
            } else {
                sender.sendMessage("§c⚠ Erreur lors de la modification du solde !");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMontant invalide !");
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES ÉCONOMIE QUANTUM");
        sender.sendMessage("§e/quantum eco create <joueur> §7- Créer un compte économique");
        sender.sendMessage("§e/quantum eco delete <joueur> §7- Supprimer un compte économique");
        sender.sendMessage("§e/quantum eco balance [joueur] §7- Voir le solde");
        sender.sendMessage("§e/quantum eco give <joueur> <montant> §7- Donner de l'argent");
        sender.sendMessage("§e/quantum eco take <joueur> <montant> §7- Retirer de l'argent");
        sender.sendMessage("§e/quantum eco set <joueur> <montant> §7- Définir le solde");
    }
}
