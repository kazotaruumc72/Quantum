package com.wynvers.quantum.tabcompleters;

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
 * Tab completer for apartment commands
 */
public class ApartmentTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "upgrade", "invite", "remove", "lock", "unlock", "tp", "teleport", "contrat", "catalogue",
            "door", "porte"
    );

    private static final List<String> SIZES = Arrays.asList("petit", "moyen", "grand");

    private static final List<String> CONTRACT_SUBS = Arrays.asList("adddeadline");

    private static final List<String> DOOR_SUBS = Arrays.asList("wand", "set", "delete", "open");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            switch (sub) {
                case "invite", "remove" -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(input)) {
                            completions.add(player.getName());
                        }
                    }
                }
                case "create" -> completions.add("<nom>");
                case "contrat" -> completions = CONTRACT_SUBS.stream()
                        .filter(s -> s.startsWith(input))
                        .collect(Collectors.toList());
                case "door", "porte" -> completions = DOOR_SUBS.stream()
                        .filter(s -> s.startsWith(input))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String input = args[2].toLowerCase();

            if ("create".equals(sub)) {
                completions = SIZES.stream()
                        .filter(s -> s.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
