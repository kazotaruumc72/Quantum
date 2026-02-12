package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
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
 * Tab completer pour la commande /job
 */
public class JobTabCompleter implements TabCompleter {
    
    private final JobManager jobManager;
    
    public JobTabCompleter(JobManager jobManager) {
        this.jobManager = jobManager;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("join", "leave", "select", "list", "info", "rewards", "top"));
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("select") || subCommand.equals("choose") || 
                subCommand.equals("join") || subCommand.equals("info")) {
                for (Job job : jobManager.getAllJobs()) {
                    completions.add(job.getId());
                }
                return filterCompletions(completions, args[1]);
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
