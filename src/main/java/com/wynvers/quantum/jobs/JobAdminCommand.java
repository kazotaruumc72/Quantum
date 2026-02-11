package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Commande /jobadmin pour administrer les métiers
 */
public class JobAdminCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final JobManager jobManager;
    
    public JobAdminCommand(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("quantum.job.admin")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jobadmin set <joueur> <métier>");
                    return true;
                }
                setPlayerJob(sender, args[1], args[2]);
                break;
                
            case "addexp":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jobadmin addexp <joueur> <quantité>");
                    return true;
                }
                addExpToPlayer(sender, args[1], args[2]);
                break;
                
            case "setlevel":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jobadmin setlevel <joueur> <niveau>");
                    return true;
                }
                setPlayerLevel(sender, args[1], args[2]);
                break;
                
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jobadmin reset <joueur>");
                    return true;
                }
                resetPlayerJob(sender, args[1]);
                break;
                
            case "reload":
                reloadConfig(sender);
                break;
                
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jobadmin info <joueur>");
                    return true;
                }
                showPlayerInfo(sender, args[1]);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== JobAdmin - Commandes ===");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin set <joueur> <métier>" + ChatColor.GRAY + " - Définir le métier d'un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin addexp <joueur> <quantité>" + ChatColor.GRAY + " - Ajouter de l'XP");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin setlevel <joueur> <niveau>" + ChatColor.GRAY + " - Définir le niveau");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin reset <joueur>" + ChatColor.GRAY + " - Réinitialiser le métier");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin info <joueur>" + ChatColor.GRAY + " - Voir les infos d'un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/jobadmin reload" + ChatColor.GRAY + " - Recharger la configuration");
    }
    
    private void setPlayerJob(CommandSender sender, String playerName, String jobId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur non trouvé: " + playerName);
            return;
        }
        
        Job job = jobManager.getJob(jobId.toLowerCase());
        if (job == null) {
            sender.sendMessage(ChatColor.RED + "Métier inconnu: " + jobId);
            return;
        }
        
        if (jobManager.setJob(target.getUniqueId(), jobId.toLowerCase())) {
            sender.sendMessage(ChatColor.GREEN + "Métier de " + playerName + " défini sur: " + 
                             ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
            target.sendMessage(ChatColor.GREEN + "Votre métier a été défini sur: " + 
                             ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
        }
    }
    
    private void addExpToPlayer(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur non trouvé: " + playerName);
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Quantité invalide: " + amountStr);
            return;
        }
        
        JobData jobData = jobManager.getPlayerJob(target.getUniqueId());
        if (jobData == null) {
            sender.sendMessage(ChatColor.RED + playerName + " n'a pas de métier actif.");
            return;
        }
        
        jobManager.addExp(target.getUniqueId(), amount);
        sender.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " XP ajoutés à " + playerName);
    }
    
    private void setPlayerLevel(CommandSender sender, String playerName, String levelStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur non trouvé: " + playerName);
            return;
        }
        
        int level;
        try {
            level = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Niveau invalide: " + levelStr);
            return;
        }
        
        JobData jobData = jobManager.getPlayerJob(target.getUniqueId());
        if (jobData == null) {
            sender.sendMessage(ChatColor.RED + playerName + " n'a pas de métier actif.");
            return;
        }
        
        Job job = jobManager.getJob(jobData.getJobId());
        if (job != null && level > job.getMaxLevel()) {
            level = job.getMaxLevel();
        }
        
        jobData.setLevel(level);
        jobData.setExp(0);
        
        sender.sendMessage(ChatColor.GREEN + "Niveau de " + playerName + " défini à " + level);
        target.sendMessage(ChatColor.GREEN + "Votre niveau de métier a été défini à " + level);
    }
    
    private void resetPlayerJob(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur non trouvé: " + playerName);
            return;
        }
        
        jobManager.unloadPlayer(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Métier de " + playerName + " réinitialisé.");
        target.sendMessage(ChatColor.YELLOW + "Votre métier a été réinitialisé.");
    }
    
    private void showPlayerInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur non trouvé: " + playerName);
            return;
        }
        
        JobData jobData = jobManager.getPlayerJob(target.getUniqueId());
        if (jobData == null) {
            sender.sendMessage(ChatColor.YELLOW + playerName + " n'a pas de métier actif.");
            return;
        }
        
        Job job = jobManager.getJob(jobData.getJobId());
        if (job == null) {
            sender.sendMessage(ChatColor.RED + "Erreur: métier invalide");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Info Métier: " + playerName + " ===");
        sender.sendMessage(ChatColor.GRAY + "Métier: " + ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
        sender.sendMessage(ChatColor.GRAY + "Niveau: " + ChatColor.WHITE + jobData.getLevel() + 
                         ChatColor.GRAY + "/" + ChatColor.WHITE + job.getMaxLevel());
        sender.sendMessage(ChatColor.GRAY + "Expérience: " + ChatColor.WHITE + jobData.getExp() + 
                         ChatColor.GRAY + "/" + ChatColor.WHITE + jobManager.getRequiredExp(jobData.getLevel()));
    }
    
    private void reloadConfig(CommandSender sender) {
        jobManager.reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration des métiers rechargée.");
    }
}
