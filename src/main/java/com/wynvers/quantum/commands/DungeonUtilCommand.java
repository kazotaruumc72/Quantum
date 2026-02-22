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

public class DungeonUtilCommand implements CommandExecutor {

    private final Quantum plugin;
    private final DungeonUtils dungeonUtils;

    public DungeonUtilCommand(Quantum plugin) {
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
        if (!player.hasPermission("quantum.dutil.give")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /dutil give <pickaxe|axe|hoe> [rareté]");
            return true;
        }

        String toolType = args[1].toLowerCase();
        DungeonUtilsType type;

        switch (toolType) {
            case "pickaxe":
            case "pioche":
                type = DungeonUtilsType.PICKAXE;
                break;
            case "axe":
            case "hache":
                type = DungeonUtilsType.AXE;
                break;
            case "hoe":
            case "houe":
                type = DungeonUtilsType.HOE;
                break;
            default:
                player.sendMessage("§cType invalide. Utilisez: pickaxe, axe, hoe");
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

        // Create the tool
        ItemStack tool = dungeonUtils.createItem(type, rarity);

        if (tool == null) {
            player.sendMessage("§cErreur lors de la création de l'outil (Vérifiez la config Nexo).");
            return true;
        }

        player.getInventory().addItem(tool);
        player.sendMessage("§a§l✓ §aReçu : " + type.getDisplayName() + " §7(" + rarity.getFormattedName() + "§7)");
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§6§lCOMMANDES OUTILS DE DONJON");
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§e/dutil give <type> [rareté] §7- Donne un outil de donjon");
        player.sendMessage("§7Types disponibles: pickaxe, axe, hoe");
        player.sendMessage("§7Raretés: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
        player.sendMessage("§6§l─────────────────────────────");
    }
}
