package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MenuTabCompleter implements TabCompleter {

    private final Quantum plugin;

    public MenuTabCompleter(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Return all menu names for first argument
            completions.addAll(plugin.getMenuManager().getMenuNames());

            // Filter based on what user has typed
            String input = args[0].toLowerCase();
            completions.removeIf(name -> !name.toLowerCase().startsWith(input));
        }

        return completions;
    }
}
