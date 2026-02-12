package com.wynvers.quantum.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab completer for /spawn command
 * No arguments needed for this command
 */
public class SpawnTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No tab completion needed for /spawn command (no arguments)
        return new ArrayList<>();
    }
}
