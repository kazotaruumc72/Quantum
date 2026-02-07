package com.wynvers.quantum.tabcompleters;

import com.wynvers.quantum.armor.RuneType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QuantumArmorRuneTabCompleter implements TabCompleter {

    // Slots d'armure disponibles
    private static final List<String> ARMOR_SLOTS = Arrays.asList(
        "helmet", "chestplate", "leggings", "boots"
    );

    // Raretés d'armure
    private static final List<String> ARMOR_RARITIES = Arrays.asList(
        "common", "uncommon", "rare", "epic", "legendary", "mythic"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmd.equals("rune")) {
            handleRuneCommand(args, completions);
        } else if (cmd.equals("armor") || cmd.equals("armure")) {
            handleArmorCommand(args, completions);
        }

        // Filtrer selon ce que l'utilisateur a déjà tapé
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private void handleRuneCommand(String[] args, List<String> completions) {
        if (args.length == 1) {
            // Sous-commandes /rune
            completions.add("give");
            completions.add("menu");
            completions.add("info");
            completions.add("help");
            
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Types de runes
            for (RuneType type : RuneType.values()) {
                completions.add(type.name());
            }
            
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Niveaux 1-3
            completions.add("1");
            completions.add("2");
            completions.add("3");
            
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            // Pourcentage de réussite (0-100, par tranche de 5)
            completions.add("100");
            completions.add("95");
            completions.add("90");
            completions.add("85");
            completions.add("80");
            completions.add("75");
            completions.add("70");
            completions.add("65");
            completions.add("60");
            completions.add("55");
            completions.add("50");
            completions.add("45");
            completions.add("40");
            completions.add("30");
            completions.add("20");
            completions.add("10");
            completions.add("random");
        }
    }

    private void handleArmorCommand(String[] args, List<String> completions) {
        if (args.length == 1) {
            // Sous-commandes /armor
            completions.add("equip");
            completions.add("unequip");
            completions.add("info");
            completions.add("runes");
            completions.add("slot");
            completions.add("rarity");
            completions.add("help");
            
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "equip":
                case "unequip":
                case "slot":
                    // Slots d'armure
                    completions.addAll(ARMOR_SLOTS);
                    break;
                    
                case "rarity":
                    // Raretés
                    completions.addAll(ARMOR_RARITIES);
                    break;
                    
                case "runes":
                    // Actions sur les runes
                    completions.add("list");
                    completions.add("clear");
                    completions.add("upgrade");
                    break;
                    
                case "info":
                    // Info sur un slot spécifique
                    completions.addAll(ARMOR_SLOTS);
                    break;
            }
            
        } else if (args.length == 3 && args[0].equalsIgnoreCase("equip")) {
            // Rareté pour l'équipement
            completions.addAll(ARMOR_RARITIES);
            
        } else if (args.length == 4 && args[0].equalsIgnoreCase("equip")) {
            // Types de runes suggérées selon le slot
            completions.add("FORCE");
            completions.add("DEFENSE");
            completions.add("RESISTANCE");
            completions.add("SPEED");
            completions.add("CRITICAL");
            completions.add("REGENERATION");
            completions.add("VAMPIRISM");
            completions.add("STRENGTH");
            completions.add("AGILITY");
        }
    }
}
