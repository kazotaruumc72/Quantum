package com.wynvers.quantum.armor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArmorCommand implements CommandExecutor, TabCompleter {
    
    private final DungeonArmor armorManager;
    private final ArmorGUI armorGUI;
    private final RuneItem runeItemManager;
    
    public ArmorCommand(DungeonArmor armorManager, ArmorGUI armorGUI, RuneItem runeItemManager) {
        this.armorManager = armorManager;
        this.armorGUI = armorGUI;
        this.runeItemManager = runeItemManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "gui":
                armorGUI.openMainGUI(player);
                break;
                
            case "give":
                if (!player.hasPermission("quantum.armor.give")) {
                    player.sendMessage("§c✖ Vous n'avez pas la permission.");
                    return true;
                }
                
                if (args.length < 2) {
                    player.sendMessage("§c✖ Usage: /armure give <helmet|chestplate|leggings|boots> [rareté]");
                    player.sendMessage("§7Raretés: " + Arrays.stream(ArmorRarity.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
                    return true;
                }
                
                ArmorRarity rarity = ArmorRarity.COMMON;
                if (args.length >= 3) {
                    try {
                        rarity = ArmorRarity.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§c✖ Rareté invalide ! Utilisation de COMMON par défaut.");
                    }
                }
                
                ItemStack armor = armorManager.createArmorPiece(args[1].toLowerCase(), rarity);
                if (armor != null) {
                    player.getInventory().addItem(armor);
                    player.sendMessage("§a✔ Pièce d'armure reçue !");
                    player.sendMessage("§7Rareté: " + rarity.getColoredName());
                    player.sendMessage("§7Emplacements de runes: §e" + rarity.getMaxRuneSlots());
                } else {
                    player.sendMessage("§c✖ Pièce d'armure invalide !");
                }
                break;
            
            case "giverune":
                if (!player.hasPermission("quantum.armor.giverune")) {
                    player.sendMessage("§c✖ Vous n'avez pas la permission.");
                    return true;
                }
                
                if (args.length < 3) {
                    player.sendMessage("§c✖ Usage: /armure giverune <TYPE> <niveau>");
                    player.sendMessage("§7Types: " + Arrays.stream(RuneType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
                    return true;
                }
                
                try {
                    RuneType runeType = RuneType.valueOf(args[1].toUpperCase());
                    int level = Integer.parseInt(args[2]);
                    
                    if (level < 1 || level > runeType.getMaxLevel()) {
                        player.sendMessage("§c✖ Niveau invalide ! (1-" + runeType.getMaxLevel() + ")");
                        return true;
                    }
                    
                    ItemStack rune = runeItemManager.createRune(runeType, level);
                    if (rune != null) {
                        int chance = runeItemManager.getSuccessChance(rune);
                        player.getInventory().addItem(rune);
                        player.sendMessage("§a✔ Rune créée avec succès !");
                        player.sendMessage("§7Taux de réussite: §e" + chance + "%");
                    } else {
                        player.sendMessage("§c✖ Impossible de créer la rune !");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§c✖ Type de rune invalide !");
                    player.sendMessage("§7Types: " + Arrays.stream(RuneType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
                }
                break;
                
            case "reload":
                if (!player.hasPermission("quantum.armor.reload")) {
                    player.sendMessage("§c✖ Vous n'avez pas la permission.");
                    return true;
                }
                
                armorManager.reload();
                RuneType.init(armorManager.plugin);
                player.sendMessage("§a✔ Configuration rechargée !");
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l⚔ ARMURE DE DONJON ⚔");
        player.sendMessage("");
        player.sendMessage("§e/armure gui §7- Ouvrir le GUI de gestion");
        player.sendMessage("§e/armure give <type> [rareté] §7- Recevoir une pièce d'armure");
        player.sendMessage("§e/armure giverune <TYPE> <niveau> §7- Recevoir une rune");
        player.sendMessage("§e/armure reload §7- Recharger la configuration");
        player.sendMessage("");
        player.sendMessage("§7Raretés: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
        player.sendMessage("");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String input = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        
        if (args.length == 1) {
            // Sous-commandes principales
            List<String> subcommands = Arrays.asList("gui", "give", "giverune", "reload");
            completions.addAll(filterMatches(subcommands, input));
            
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                // Types d'armure
                List<String> armorTypes = Arrays.asList("helmet", "chestplate", "leggings", "boots");
                completions.addAll(filterMatches(armorTypes, input));
                
            } else if (args[0].equalsIgnoreCase("giverune")) {
                // Types de runes
                List<String> runeTypes = Arrays.stream(RuneType.values())
                    .map(rune -> rune.name().toLowerCase())
                    .collect(Collectors.toList());
                completions.addAll(filterMatches(runeTypes, input));
            }
            
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                // Raretés d'armure
                List<String> rarities = Arrays.stream(ArmorRarity.values())
                    .map(rarity -> rarity.name().toLowerCase())
                    .collect(Collectors.toList());
                completions.addAll(filterMatches(rarities, input));
                
            } else if (args[0].equalsIgnoreCase("giverune")) {
                // Niveaux de runes
                try {
                    RuneType type = RuneType.valueOf(args[1].toUpperCase());
                    List<String> levels = new ArrayList<>();
                    for (int i = 1; i <= type.getMaxLevel(); i++) {
                        levels.add(String.valueOf(i));
                    }
                    completions.addAll(filterMatches(levels, input));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Filtre les suggestions qui commencent par l'input du joueur
     */
    private List<String> filterMatches(List<String> options, String input) {
        return options.stream()
            .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }
}
