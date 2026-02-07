package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.armor.RuneType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuantumArmorRuneTabCompleter implements TabCompleter {

    private static final List<String> SLOTS = Arrays.asList("helmet", "chestplate", "leggings", "boots");
    private static final List<String> RARITIES = Arrays.asList("common", "uncommon", "rare", "epic", "legendary");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmd.equals("rune")) {
            if (args.length == 1) {
                completions.add("give");
                completions.add("help");
            } else if (args.length == 2 && args[0].equals("give")) {
                for (RuneType type : RuneType.values()) completions.add(type.name());
            } else if (args.length == 3 && args[0].equals("give")) {
                completions.add("1");
                completions.add("2");
                completions.add("3");
            } else if (args.length == 4 && args[0].equals("give")) {
                completions.add("100");
                completions.add("75");
                completions.add("50");
                completions.add("25");
                completions.add("random");
            }
        } else if (cmd.equals("armor")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("equip", "unequip", "info", "runes", "help"));
            } else if (args.length == 2) {
                if (args[0].equals("equip") || args[0].equals("unequip") || args[0].equals("info")) {
                    completions.addAll(SLOTS);
                } else if (args[0].equals("runes")) {
                    completions.addAll(Arrays.asList("list", "clear"));
                }
            } else if (args.length == 3 && args[0].equals("equip")) {
                completions.addAll(RARITIES);
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
