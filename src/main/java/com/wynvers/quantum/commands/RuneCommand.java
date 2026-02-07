package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.RuneItem;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RuneCommand implements CommandExecutor {

    private final Quantum plugin;
    private final RuneItem runeItem;

    public RuneCommand(Quantum plugin) {
        this.plugin = plugin;
        this.runeItem = new RuneItem(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "⚠ Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // /rune give <type> <level> [player]
        if (args[0].equalsIgnoreCase("give")) {
            return handleGiveRune(player, args);
        }

        sendHelp(player);
        return true;
    }

    private boolean handleGiveRune(Player player, String[] args) {
        if (!player.hasPermission("quantum.rune.give")) {
            player.sendMessage(ChatColor.RED + "⚠ Vous n'avez pas la permission.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rune give <type> <level> [joueur]");
            player.sendMessage(ChatColor.GRAY + "Types: FORCE, SPEED, RESISTANCE, CRITICAL, VAMPIRISM, REGENERATION, STRENGTH, DEFENSE, AGILITY");
            return true;
        }

        // Parse rune type
        RuneType type;
        try {
            type = RuneType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "⚠ Type de rune invalide: " + args[1]);
            player.sendMessage(ChatColor.GRAY + "Types valides: FORCE, SPEED, RESISTANCE, CRITICAL, VAMPIRISM, REGENERATION, STRENGTH, DEFENSE, AGILITY");
            return true;
        }

        // Parse level
        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level < 1 || level > type.getMaxLevel()) {
                player.sendMessage(ChatColor.RED + "⚠ Niveau invalide. Doit être entre 1 et " + type.getMaxLevel());
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "⚠ Niveau invalide: " + args[2]);
            return true;
        }

        // Target player
        Player target = player;
        if (args.length >= 4) {
            target = plugin.getServer().getPlayer(args[3]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "⚠ Joueur introuvable: " + args[3]);
                return true;
            }
        }

        // Create rune with Nexo integration
        ItemStack rune = runeItem.createRune(type, level);
        if (rune == null) {
            player.sendMessage(ChatColor.RED + "⚠ Erreur lors de la création de la rune. Vérifiez la config Nexo.");
            plugin.getLogger().severe("⚠ Impossible de créer la rune " + type.name() + " niveau " + level);
            return true;
        }

        // Give to target
        target.getInventory().addItem(rune);

        String runeName = type.getDisplay() + " " + ChatColor.GRAY + toRoman(level);
        if (target.equals(player)) {
            player.sendMessage(ChatColor.GREEN + "✔ Vous avez reçu : " + runeName);
        } else {
            player.sendMessage(ChatColor.GREEN + "✔ Donné " + runeName + ChatColor.GREEN + " à " + ChatColor.YELLOW + target.getName());
            target.sendMessage(ChatColor.GREEN + "✔ Vous avez reçu : " + runeName);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "───────────────────────────");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "COMMANDES RUNES");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "───────────────────────────");
        if (player.hasPermission("quantum.rune.give")) {
            player.sendMessage(ChatColor.YELLOW + "/rune give <type> <level> [joueur]" + ChatColor.GRAY + " - Donne une rune");
            player.sendMessage(ChatColor.GRAY + "  Types: FORCE, SPEED, RESISTANCE, CRITICAL, VAMPIRISM,");
            player.sendMessage(ChatColor.GRAY + "         REGENERATION, STRENGTH, DEFENSE, AGILITY");
            player.sendMessage(ChatColor.GRAY + "  Niveaux: 1-3");
        }
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "───────────────────────────");
    }

    private String toRoman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(level);
        }
    }
}
