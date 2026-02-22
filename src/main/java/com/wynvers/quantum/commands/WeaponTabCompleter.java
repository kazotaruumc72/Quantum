package com.wynvers.quantum.commands;

import com.wynvers.quantum.dungeonutis.DungeonUtilsRarity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Argument 1: Subcommands
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("give");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
        }

        // Argument 2: Weapon types
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                List<String> weaponTypes = Arrays.asList("sword", "bow", "katana", "broadsword", "shield");
                StringUtil.copyPartialMatches(args[1], weaponTypes, completions);
            }
            return completions;
        }

        // Argument 3: Rarities
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                List<String> rarities = Arrays.stream(DungeonUtilsRarity.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[2], rarities, completions);
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
