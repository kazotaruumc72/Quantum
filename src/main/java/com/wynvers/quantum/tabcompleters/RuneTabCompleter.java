package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.armor.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RuneTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // /rune <give>
            completions.add("give");
            return filterStartingWith(completions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // /rune give <type>
            completions = Arrays.stream(RuneType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
            return filterStartingWith(completions, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // /rune give <type> <level>
            completions.addAll(Arrays.asList("1", "2", "3"));
            return filterStartingWith(completions, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            // /rune give <type> <level> <joueur>
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
