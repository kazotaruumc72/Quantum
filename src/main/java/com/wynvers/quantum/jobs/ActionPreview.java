package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Système de preview des actions pour les métiers
 * Inspiré de UniverseJobs - affiche les récompenses potentielles avant l'action
 */
public class ActionPreview {
    
    private final Quantum plugin;
    private final JobManager jobManager;
    
    public ActionPreview(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }
    
    /**
     * Affiche une preview des prochaines récompenses de niveau
     * @param player Le joueur
     * @param levelsAhead Nombre de niveaux à afficher (par défaut: 3)
     */
    public void showNextRewardsPreview(Player player, int levelsAhead) {
        JobData jobData = jobManager.getPlayerJob(player.getUniqueId());
        
        if (jobData == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas de métier sélectionné.");
            return;
        }
        
        Job job = jobManager.getJob(jobData.getJobId());
        if (job == null) return;
        
        int currentLevel = jobData.getLevel();
        int currentExp = jobData.getExp();
        int requiredExp = jobManager.getRequiredExp(currentLevel);
        
        // Header avec progression
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╔═══════════════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "Aperçu des Récompenses" + 
                          ChatColor.GOLD + "              ║");
        player.sendMessage(ChatColor.GOLD + "╚═══════════════════════════════════════╝");
        player.sendMessage("");
        
        // Niveau actuel et progression
        player.sendMessage(ChatColor.GRAY + "Métier: " + 
                          ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
        player.sendMessage(ChatColor.GRAY + "Niveau: " + ChatColor.WHITE + currentLevel + 
                          ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + job.getMaxLevel());
        
        // Barre de progression
        float progress = (float) currentExp / (float) requiredExp;
        String progressBar = createProgressBar(progress, 30);
        player.sendMessage(ChatColor.GRAY + "XP: " + ChatColor.WHITE + currentExp + 
                          ChatColor.DARK_GRAY + "/" + ChatColor.WHITE + requiredExp);
        player.sendMessage(progressBar);
        player.sendMessage("");
        
        // Afficher les prochaines récompenses
        player.sendMessage(ChatColor.YELLOW + "▸ Prochaines récompenses:");
        player.sendMessage("");
        
        boolean foundReward = false;
        int rewardsShown = 0;
        
        for (int level = currentLevel + 1; level <= job.getMaxLevel() && rewardsShown < levelsAhead; level++) {
            if (!job.getLevelRewards(level).isEmpty()) {
                // Calculer l'XP total nécessaire
                int totalExpNeeded = calculateTotalExpNeeded(currentLevel, currentExp, level);
                
                player.sendMessage(ChatColor.GOLD + "  ◆ Niveau " + level + 
                                 ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + totalExpNeeded + " XP restants" + 
                                 ChatColor.DARK_GRAY + ")");
                
                for (JobReward reward : job.getLevelRewards(level)) {
                    String rewardDesc = getDetailedRewardDescription(reward);
                    player.sendMessage(ChatColor.DARK_GRAY + "    • " + rewardDesc);
                }
                
                player.sendMessage("");
                foundReward = true;
                rewardsShown++;
            }
        }
        
        if (!foundReward) {
            player.sendMessage(ChatColor.GRAY + "  Aucune récompense dans les " + levelsAhead + " prochains niveaux.");
            player.sendMessage("");
        }
        
        // Footer
        player.sendMessage(ChatColor.DARK_GRAY + "Utilisez " + ChatColor.WHITE + "/job rewards" + 
                          ChatColor.DARK_GRAY + " pour voir plus de récompenses.");
    }
    
    /**
     * Crée une barre de progression visuelle
     */
    private String createProgressBar(float progress, int length) {
        int filled = (int) (progress * length);
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN).append("[");
        
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }
        
        bar.append(ChatColor.GREEN).append("]");
        bar.append(ChatColor.YELLOW).append(" ").append(String.format("%.1f", progress * 100)).append("%");
        
        return bar.toString();
    }
    
    /**
     * Calcule l'XP totale nécessaire pour atteindre un niveau cible
     */
    private int calculateTotalExpNeeded(int currentLevel, int currentExp, int targetLevel) {
        int total = jobManager.getRequiredExp(currentLevel) - currentExp;
        
        for (int level = currentLevel + 1; level < targetLevel; level++) {
            total += jobManager.getRequiredExp(level);
        }
        
        return total;
    }
    
    /**
     * Retourne une description détaillée d'une récompense avec icônes
     */
    private String getDetailedRewardDescription(JobReward reward) {
        switch (reward.getType()) {
            case "money":
                return ChatColor.GREEN + "💰 " + reward.getValue() + "$" + 
                       ChatColor.GRAY + " d'argent";
                
            case "nexo_item":
                return ChatColor.AQUA + "📦 " + reward.getValue() + 
                       ChatColor.GRAY + " x" + reward.getAmount() + 
                       ChatColor.DARK_GRAY + " (Item Nexo)";
                
            case "mythicmobs_item":
                return ChatColor.LIGHT_PURPLE + "⚔ " + reward.getValue() + 
                       ChatColor.GRAY + " x" + reward.getAmount() + 
                       ChatColor.DARK_GRAY + " (Item MythicMobs)";
                
            case "exp_booster":
                String expDuration = formatDuration(reward.getDuration());
                String expNote = reward.isDungeonOnly() ? 
                    ChatColor.RED + " (Donjon uniquement)" : "";
                return ChatColor.GOLD + "✦ Booster XP x" + reward.getValue() + 
                       ChatColor.GRAY + " - " + expDuration + expNote;
                
            case "money_booster":
                String moneyDuration = formatDuration(reward.getDuration());
                String moneyNote = reward.isDungeonOnly() ? 
                    ChatColor.RED + " (Donjon uniquement)" : "";
                return ChatColor.GREEN + "✦ Booster Argent x" + reward.getValue() + 
                       ChatColor.GRAY + " - " + moneyDuration + moneyNote;
                
            case "console_command":
                return ChatColor.YELLOW + "⚙ Commande spéciale";
                
            case "player_command":
                return ChatColor.YELLOW + "⚙ Action joueur";
                
            default:
                return ChatColor.WHITE + reward.getType();
        }
    }
    
    /**
     * Formate une durée en secondes en format lisible
     */
    private String formatDuration(int seconds) {
        if (seconds <= 0) {
            return "0 min";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        
        if (hours > 0) {
            return hours + "h" + (minutes > 0 ? minutes + "m" : "");
        } else if (minutes > 0) {
            return minutes + " min";
        } else {
            return "< 1 min";
        }
    }
    
    /**
     * Envoie un message dans l'action bar du joueur
     */
    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }
    
    /**
     * Vérifie si un joueur est dans un donjon
     */
    private boolean isPlayerInDungeon(Player player) {
        if (plugin.getTowerManager() != null) {
            return plugin.getTowerManager().getPlayerTower(player) != null;
        }
        return false;
    }
}
