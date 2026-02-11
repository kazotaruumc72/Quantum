package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /job pour gérer les métiers
 */
public class JobCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final JobManager jobManager;
    
    public JobCommand(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est réservée aux joueurs.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showJobInfo(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "select":
            case "choose":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /job select <métier>");
                    return true;
                }
                selectJob(player, args[1]);
                break;
                
            case "list":
                listJobs(player);
                break;
                
            case "info":
                if (args.length < 2) {
                    showJobInfo(player);
                } else {
                    showJobDetails(player, args[1]);
                }
                break;
                
            case "rewards":
                if (args.length > 1 && args[1].equalsIgnoreCase("preview")) {
                    // Utiliser le système de preview amélioré
                    int levels = 3;  // Par défaut 3 niveaux
                    if (args.length > 2) {
                        try {
                            levels = Integer.parseInt(args[2]);
                            levels = Math.min(10, Math.max(1, levels)); // Entre 1 et 10
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    jobManager.getActionPreview().showNextRewardsPreview(player, levels);
                } else {
                    showRewards(player);
                }
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Sous-commande inconnue. Usage:");
                player.sendMessage(ChatColor.GRAY + "/job - Afficher vos informations de métier");
                player.sendMessage(ChatColor.GRAY + "/job select|choose <métier> - Choisir un métier");
                player.sendMessage(ChatColor.GRAY + "/job list - Lister tous les métiers");
                player.sendMessage(ChatColor.GRAY + "/job info [métier] - Info sur un métier");
                player.sendMessage(ChatColor.GRAY + "/job rewards - Voir les prochaines récompenses");
                player.sendMessage(ChatColor.GRAY + "/job rewards preview [niveaux] - Preview détaillé des récompenses");
                break;
        }
        
        return true;
    }
    
    private void showJobInfo(Player player) {
        JobData jobData = jobManager.getPlayerJob(player.getUniqueId());
        
        if (jobData == null) {
            player.sendMessage(ChatColor.YELLOW + "Vous n'avez pas encore choisi de métier.");
            player.sendMessage(ChatColor.GRAY + "Utilisez " + ChatColor.WHITE + "/job list" + 
                             ChatColor.GRAY + " pour voir les métiers disponibles.");
            return;
        }
        
        Job job = jobManager.getJob(jobData.getJobId());
        if (job == null) return;
        
        int exp = jobData.getExp();
        int level = jobData.getLevel();
        int needed = jobManager.getRequiredExp(level);
        
        player.sendMessage(ChatColor.GOLD + "=== Votre Métier ===");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', job.getDisplayName()) + 
                         ChatColor.GRAY + " - Niveau " + ChatColor.WHITE + level + 
                         ChatColor.GRAY + "/" + ChatColor.WHITE + job.getMaxLevel());
        player.sendMessage(ChatColor.GRAY + "Expérience: " + ChatColor.WHITE + exp + 
                         ChatColor.GRAY + "/" + ChatColor.WHITE + needed);
        
        float progress = (float) exp / (float) needed;
        int barLength = 20;
        int filled = (int) (progress * barLength);
        
        StringBuilder bar = new StringBuilder(ChatColor.GREEN + "[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }
        bar.append(ChatColor.GREEN).append("]");
        
        player.sendMessage(bar.toString());
    }
    
    private void selectJob(Player player, String jobId) {
        JobData currentJob = jobManager.getPlayerJob(player.getUniqueId());
        
        if (currentJob != null) {
            player.sendMessage(jobManager.getConfig().getString("messages.already_has_job", ""));
            return;
        }
        
        Job job = jobManager.getJob(jobId.toLowerCase());
        if (job == null) {
            player.sendMessage(ChatColor.RED + "Métier inconnu: " + jobId);
            player.sendMessage(ChatColor.GRAY + "Utilisez /job list pour voir les métiers disponibles.");
            return;
        }
        
        if (jobManager.setJob(player.getUniqueId(), jobId.toLowerCase())) {
            String message = jobManager.getConfig().getString("messages.job_selected", "")
                .replace("{job_name}", job.getDisplayName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    private void listJobs(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Métiers Disponibles ===");
        
        for (Job job : jobManager.getAllJobs()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
            for (String line : job.getDescription()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
            player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + job.getId());
        }
    }
    
    private void showJobDetails(Player player, String jobId) {
        Job job = jobManager.getJob(jobId.toLowerCase());
        
        if (job == null) {
            player.sendMessage(ChatColor.RED + "Métier inconnu: " + jobId);
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== " + 
                         ChatColor.translateAlternateColorCodes('&', job.getDisplayName()) + 
                         ChatColor.GOLD + " ===");
        
        for (String line : job.getDescription()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Niveau maximum: " + ChatColor.WHITE + job.getMaxLevel());
        player.sendMessage(ChatColor.GRAY + "Structures valides:");
        for (String structure : job.getValidStructures()) {
            player.sendMessage(ChatColor.DARK_GRAY + "  - " + ChatColor.WHITE + structure);
        }
    }
    
    private void showRewards(Player player) {
        JobData jobData = jobManager.getPlayerJob(player.getUniqueId());
        
        if (jobData == null) {
            player.sendMessage(jobManager.getConfig().getString("messages.no_job_selected", ""));
            return;
        }
        
        Job job = jobManager.getJob(jobData.getJobId());
        if (job == null) return;
        
        int currentLevel = jobData.getLevel();
        
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "✦ Prochaines Récompenses");
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        player.sendMessage("");
        
        boolean foundReward = false;
        for (int level = currentLevel + 1; level <= Math.min(currentLevel + 10, job.getMaxLevel()); level++) {
            if (!job.getLevelRewards(level).isEmpty()) {
                player.sendMessage(ChatColor.GOLD + "▸ Niveau " + level + ChatColor.DARK_GRAY + ":");
                for (JobReward reward : job.getLevelRewards(level)) {
                    String rewardDesc = getEnhancedRewardDescription(reward);
                    player.sendMessage(ChatColor.GRAY + "  • " + rewardDesc);
                }
                player.sendMessage("");
                foundReward = true;
            }
        }
        
        if (!foundReward) {
            player.sendMessage(ChatColor.GRAY + "Aucune récompense dans les 10 prochains niveaux.");
            player.sendMessage("");
        }
        
        player.sendMessage(ChatColor.DARK_GRAY + "Astuce: Utilisez " + ChatColor.WHITE + 
                          "/job rewards preview" + ChatColor.DARK_GRAY + " pour un aperçu détaillé!");
    }
    
    /**
     * Retourne une description améliorée d'une récompense
     */
    private String getEnhancedRewardDescription(JobReward reward) {
        switch (reward.getType()) {
            case "money":
                return ChatColor.GREEN + "💰 " + reward.getValue() + "$";
            case "nexo_item":
                return ChatColor.AQUA + "📦 " + reward.getValue() + 
                       ChatColor.GRAY + " x" + reward.getAmount() + 
                       ChatColor.DARK_GRAY + " (Nexo)";
            case "mythicmobs_item":
                return ChatColor.LIGHT_PURPLE + "⚔ " + reward.getValue() + 
                       ChatColor.GRAY + " x" + reward.getAmount() + 
                       ChatColor.DARK_GRAY + " (MythicMobs)";
            case "exp_booster":
                String expNote = reward.isDungeonOnly() ? 
                    ChatColor.RED + " (Donjon)" : "";
                return ChatColor.GOLD + "✦ Booster XP x" + reward.getValue() + 
                       ChatColor.GRAY + " (" + (reward.getDuration() / 60) + " min)" + expNote;
            case "money_booster":
                String moneyNote = reward.isDungeonOnly() ? 
                    ChatColor.RED + " (Donjon)" : "";
                return ChatColor.GREEN + "✦ Booster $ x" + reward.getValue() + 
                       ChatColor.GRAY + " (" + (reward.getDuration() / 60) + " min)" + moneyNote;
            case "console_command":
                return ChatColor.YELLOW + "⚙ Commande spéciale";
            case "player_command":
                return ChatColor.YELLOW + "⚙ Action joueur";
            default:
                return ChatColor.WHITE + reward.getType();
        }
    }
    
    private String getRewardDescription(JobReward reward) {
        switch (reward.getType()) {
            case "money":
                return ChatColor.GREEN + reward.getValue() + "$";
            case "nexo_item":
                return ChatColor.AQUA + "Item Nexo: " + reward.getValue() + " x" + reward.getAmount();
            case "mythicmobs_item":
                return ChatColor.LIGHT_PURPLE + "Item MythicMobs: " + reward.getValue() + " x" + reward.getAmount();
            case "exp_booster":
                return ChatColor.GOLD + "Booster XP x" + reward.getValue() + " (" + 
                      (reward.getDuration() / 60) + " min)";
            case "money_booster":
                return ChatColor.GREEN + "Booster Argent x" + reward.getValue() + " (" + 
                      (reward.getDuration() / 60) + " min)";
            case "console_command":
                return ChatColor.YELLOW + "Commande console";
            case "player_command":
                return ChatColor.YELLOW + "Commande joueur";
            default:
                return ChatColor.WHITE + reward.getType();
        }
    }
}
