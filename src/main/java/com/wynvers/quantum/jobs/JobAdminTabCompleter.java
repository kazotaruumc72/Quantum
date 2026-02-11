package com.wynvers.quantum.jobs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer pour la commande /jobadmin
 */
public class JobAdminTabCompleter implements TabCompleter {
    
    private final JobManager jobManager;
    
    public JobAdminTabCompleter(JobManager jobManager) {
        this.jobManager = jobManager;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("quantum.job.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "addexp", "setlevel", "reset", "info", "reload"));
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("set") || subCommand.equals("addexp") || 
                subCommand.equals("setlevel") || subCommand.equals("reset") || 
                subCommand.equals("info")) {
                // Liste des joueurs en ligne
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                return filterCompletions(completions, args[1]);
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("set")) {
                // Liste des métiers disponibles
                for (Job job : jobManager.getAllJobs()) {
                    completions.add(job.getId());
                }
                return filterCompletions(completions, args[2]);
            }
        }
        
        return completions;
    }
    
    private List<String> filterCompletions(List<String> completions, String prefix) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
