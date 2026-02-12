package com.wynvers.quantum.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab completer pour la commande /chat
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChatTabCompleter implements TabCompleter {
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Sous-commandes disponibles
            if (sender.hasPermission("quantum.chat.reload") || sender.hasPermission("quantum.chat.admin")) {
                completions.add("reload");
            }
        }
        
        return completions;
    }
}
