package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.dungeonutis.DungeonUtils;
import com.wynvers.quantum.dungeonutis.DungeonUtilsRarity;
import com.wynvers.quantum.dungeonutis.DungeonUtilsType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeaponCommand implements CommandExecutor {

    private final Quantum plugin;
    private final DungeonUtils dungeonUtils;

    public WeaponCommand(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonUtils = plugin.getDungeonUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            return handleGive(player, args);
        } else {
            sendHelp(player);
            return true;
        }
    }

    private boolean handleGive(Player player, String[] args) {
        if (!player.hasPermission("quantum.weapon.give")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /weapon give <sword|bow|katana|broadsword|shield> [rareté]");
            return true;
        }

        String weaponType = args[1].toLowerCase();
        DungeonUtilsType type;

        switch (weaponType) {
            case "sword":
            case "epee":
            case "épée":
                type = DungeonUtilsType.SWORD;
                break;
            case "bow":
            case "arc":
                type = DungeonUtilsType.BOW;
                break;
            case "katana":
                type = DungeonUtilsType.KATANA;
                break;
            case "broadsword":
            case "épéelarge":
            case "epeelarge":
                type = DungeonUtilsType.BROADSWORD;
                break;
            case "shield":
            case "bouclier":
                type = DungeonUtilsType.SHIELD;
                break;
            default:
                player.sendMessage("§cType invalide. Utilisez: sword, bow, katana, broadsword, shield");
                return true;
        }

        // Default rarity is COMMON
        DungeonUtilsRarity rarity = DungeonUtilsRarity.COMMON;

        // If player specified a rarity
        if (args.length >= 3) {
            try {
                rarity = DungeonUtilsRarity.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cRareté inconnue ! Choix: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
                return true;
            }
        }

        // Create the weapon
        ItemStack weapon = dungeonUtils.createItem(type, rarity);

        if (weapon == null) {
            player.sendMessage("§cErreur lors de la création de l'arme (Vérifiez la config Nexo).");
            return true;
        }

        player.getInventory().addItem(weapon);
        player.sendMessage("§a§l✓ §aReçu : " + type.getDisplayName() + " §7(" + rarity.getFormattedName() + "§7)");
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§6§lCOMMANDES ARMES DE DONJON");
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§e/weapon give <type> [rareté] §7- Donne une arme");
        player.sendMessage("§7Types disponibles: sword, bow, katana, broadsword, shield");
        player.sendMessage("§7Raretés: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
        player.sendMessage("§6§l─────────────────────────────");
    }
}
