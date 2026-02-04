package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Command handler for /rune
 * Allows players to obtain physical rune items and manage rune equipment
 */
public class RuneCommand implements CommandExecutor {

    private final Quantum plugin;

    public RuneCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // /rune equip - Open rune equipment menu
        if (args.length == 1 && args[0].equalsIgnoreCase("equip")) {
            plugin.getMenuManager().openMenu(player, "rune_equipment");
            return true;
        }

        // /rune give <type> <level> [player] - Give physical rune item
        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            if (!player.hasPermission("quantum.rune.give")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }

            String typeStr = args[1].toUpperCase();
            String levelStr = args[2];

            RuneType type;
            try {
                type = RuneType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid rune type! Available types:");
                player.sendMessage(ChatColor.GRAY + "HEALTH, DAMAGE, DEFENSE, SPEED, CRIT_CHANCE, CRIT_DAMAGE, MAGIC_FIND, HEALTH_REGEN, MANA_REGEN");
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(levelStr);
                if (level < 1 || level > 3) {
                    player.sendMessage(ChatColor.RED + "Invalid level! Must be between 1 and 3.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid level! Must be a number between 1 and 3.");
                return true;
            }

            // Determine target player
            Player target = player;
            if (args.length >= 4) {
                target = plugin.getServer().getPlayer(args[3]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found: " + args[3]);
                    return true;
                }
            }

            // Create and give rune item
            ItemStack runeItem = createRuneItem(type, level);
            target.getInventory().addItem(runeItem);

            String runeName = type.getDisplayName() + " " + getRomanNumeral(level);
            if (target.equals(player)) {
                player.sendMessage(ChatColor.GREEN + "You received: " + ChatColor.GOLD + runeName);
            } else {
                player.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.GOLD + runeName + ChatColor.GREEN + " to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You received: " + ChatColor.GOLD + runeName);
            }

            return true;
        }

        // Show usage
        player.sendMessage(ChatColor.GOLD + "===== Rune Commands =====");
        player.sendMessage(ChatColor.YELLOW + "/rune equip" + ChatColor.GRAY + " - Open rune equipment menu");
        if (player.hasPermission("quantum.rune.give")) {
            player.sendMessage(ChatColor.YELLOW + "/rune give <type> <level> [player]" + ChatColor.GRAY + " - Give a physical rune item");
            player.sendMessage(ChatColor.GRAY + "Types: HEALTH, DAMAGE, DEFENSE, SPEED, CRIT_CHANCE, CRIT_DAMAGE, MAGIC_FIND, HEALTH_REGEN, MANA_REGEN");
            player.sendMessage(ChatColor.GRAY + "Levels: 1, 2, 3 (I, II, III)");
        }
        return true;
    }

    /**
     * Create a physical rune item
     */
    private ItemStack createRuneItem(RuneType type, int level) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD); // Éclat d'écho comme item de base
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Display name
            String romanLevel = getRomanNumeral(level);
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + type.getDisplayName() + " " + romanLevel);

            // Lore with stats
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Rune Type: " + ChatColor.YELLOW + type.getDisplayName());
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.YELLOW + romanLevel);
            lore.add("");

            // Add bonus info
            double bonus = plugin.getDungeonArmor().getRuneBonus(type, level);
            String bonusStr = formatBonus(type, bonus);
            lore.add(ChatColor.GREEN + "Bonus: " + bonusStr);
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Drag this rune onto a dungeon");
            lore.add(ChatColor.DARK_GRAY + "armor piece to apply its bonus.");
            lore.add("");
            lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Mythical Rune");

            meta.setLore(lore);

            // Add custom model data if configured
            String configPath = "runes." + type.name().toLowerCase() + ".levels." + level + ".custom_model_data";
            if (plugin.getConfig().contains(configPath)) {
                int customModelData = plugin.getConfig().getInt(configPath);
                meta.setCustomModelData(customModelData);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Format bonus value based on rune type
     */
    private String formatBonus(RuneType type, double bonus) {
        switch (type) {
            case HEALTH:
                return ChatColor.RED + "+" + (int)bonus + " ❤ Health";
            case DAMAGE:
                return ChatColor.RED + "+" + (int)bonus + " ⚔ Damage";
            case DEFENSE:
                return ChatColor.GREEN + "+" + (int)bonus + " 🛡 Defense";
            case SPEED:
                return ChatColor.AQUA + "+" + (int)bonus + "% ⚡ Speed";
            case CRIT_CHANCE:
                return ChatColor.BLUE + "+" + (int)bonus + "% ☄ Crit Chance";
            case CRIT_DAMAGE:
                return ChatColor.BLUE + "+" + (int)bonus + "% ✦ Crit Damage";
            case MAGIC_FIND:
                return ChatColor.LIGHT_PURPLE + "+" + (int)bonus + "% ✪ Magic Find";
            case HEALTH_REGEN:
                return ChatColor.RED + "+" + (int)bonus + " ❤ Health Regen";
            case MANA_REGEN:
                return ChatColor.AQUA + "+" + (int)bonus + " ✎ Mana Regen";
            default:
                return ChatColor.YELLOW + "+" + bonus;
        }
    }

    /**
     * Convert level to Roman numeral
     */
    private String getRomanNumeral(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(level);
        }
    }
}
