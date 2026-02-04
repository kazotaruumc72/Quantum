package com.wynvers.quantum.commands;

import com.wynvers.quantum.armor.ArmorRarity;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArmorTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Argument 1 : Les sous-commandes (give, info, apply, debug)
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("give", "info", "apply", "debug");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
        }

        // Argument 2 : Dépend de la sous-commande choisie
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                // Proposer les types d'armure
                List<String> armorTypes = Arrays.asList("helmet", "chestplate", "leggings", "boots");
                StringUtil.copyPartialMatches(args[1], armorTypes, completions);
            } 
            else if (args[0].equalsIgnoreCase("apply")) {
                // Proposer la liste des Runes dynamiquement depuis l'Enum
                List<String> runes = Arrays.stream(RuneType.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], runes, completions);
            }
            return completions;
        }

        // Argument 3 : Dépend encore de la sous-commande
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                // 🔥 C'est ici que la magie opère : Proposer les Raretés !
                List<String> rarities = Arrays.stream(ArmorRarity.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[2], rarities, completions);
            }
            else if (args[0].equalsIgnoreCase("apply")) {
                // Proposer les niveaux de rune (1, 2, 3)
                List<String> levels = Arrays.asList("1", "2", "3");
                StringUtil.copyPartialMatches(args[2], levels, completions);
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
