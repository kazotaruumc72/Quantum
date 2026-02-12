package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.home.Home;
import com.wynvers.quantum.home.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for home commands
 * Provides auto-completion for player's home names in /home, /sethome, and /delhome commands
 */
public class HomeTabCompleter implements TabCompleter {

    private final HomeManager homeManager;

    public HomeTabCompleter(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Get list of player's homes for tab completion
            List<Home> homes = homeManager.getHomes(player.getUniqueId());
            String partial = args[0].toLowerCase();
            
            return homes.stream()
                .map(Home::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
