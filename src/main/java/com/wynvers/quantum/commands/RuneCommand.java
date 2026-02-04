package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.RuneType;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
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
            Menu menu = plugin.getMenuManager().getMenu("rune_equipment");
            if (menu != null) {
                menu.open(player, plugin, new HashMap<>());
            } else {
                player.sendMessage(ChatColor.RED + "Rune equipment menu not found!");
            }
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
                player.sendMessage(ChatColor.GRAY + "FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(levelStr);
                if (level < 1 || level > type.getMaxLevel()) {
                    player.sendMessage(ChatColor.RED + "Invalid level! Must be between 1 and " + type.getMaxLevel() + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid level! Must be a number.");
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

            String runeName = type.getDisplay() + " " + getRomanNumeral(level);
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
            player.sendMessage(ChatColor.GRAY + "Types: FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
            player.sendMessage(ChatColor.GRAY + "Levels: 1-3 (I, II, III)");
        }
        return true;
    }

    /**
     * Create a physical rune item
     */
    private ItemStack createRuneItem(RuneType type, int level) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Display name
            String romanLevel = getRomanNumeral(level);
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + type.getDisplay() + " " + romanLevel);

            // Lore with stats
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Rune Type: " + ChatColor.YELLOW + type.getDisplay());
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.YELLOW + romanLevel);
            lore.add("");

            // Add description if available
            String description = type.getDescription();
            if (description != null && !description.isEmpty()) {
                lore.add(ChatColor.GRAY + description);
                lore.add("");
            }

            // Add bonus info based on rune type
            lore.add(ChatColor.GREEN + "Bonus:");
            lore.addAll(getRuneBonusLore(type, level));
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Drag this rune onto a dungeon");
            lore.add(ChatColor.DARK_GRAY + "armor piece to apply its bonus.");
            lore.add("");
            lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Mythical Rune");

            meta.setLore(lore);

            // Add custom model data if configured
            String nexoId = type.getNexoId(level);
            if (nexoId != null) {
                // Si un nexo_id est configuré, on pourrait l'utiliser ici
                // Pour l'instant on utilise juste custom_model_data si disponible
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Get bonus lore lines based on rune type and level
     */
    private List<String> getRuneBonusLore(RuneType type, int level) {
        List<String> bonusLore = new ArrayList<>();

        switch (type) {
            case FORCE:
                double damageBonus = type.getDamageBonus(level);
                int damagePercent = (int)((damageBonus - 1.0) * 100);
                bonusLore.add(ChatColor.RED + "  +" + damagePercent + "% Damage");
                break;

            case SPEED:
                double speedBonus = type.getSpeedBonus(level);
                int speedPercent = (int)((speedBonus - 1.0) * 100);
                bonusLore.add(ChatColor.AQUA + "  +" + speedPercent + "% Speed");
                break;

            case RESISTANCE:
                double reduction = type.getDamageReduction(level);
                int reductionPercent = (int)(reduction * 100);
                bonusLore.add(ChatColor.GREEN + "  +" + reductionPercent + "% Damage Reduction");
                break;

            case CRITICAL:
                double critChance = type.getCriticalChance(level);
                int critPercent = (int)(critChance * 100);
                bonusLore.add(ChatColor.BLUE + "  +" + critPercent + "% Crit Chance");
                break;

            case REGENERATION:
                double regen = type.getRegeneration(level);
                bonusLore.add(ChatColor.RED + "  +" + regen + " HP/s Regeneration");
                break;

            case VAMPIRISM:
                double vampirism = type.getVampirismPercent(level);
                int vampirismPercent = (int)(vampirism * 100);
                bonusLore.add(ChatColor.DARK_RED + "  +" + vampirismPercent + "% Lifesteal");
                break;

            case THORNS:
                double thorns = type.getThornsPercent(level);
                int thornsPercent = (int)(thorns * 100);
                bonusLore.add(ChatColor.DARK_GREEN + "  +" + thornsPercent + "% Thorns Damage");
                break;

            case WISDOM:
                double xpMultiplier = type.getXpMultiplier(level);
                int xpPercent = (int)((xpMultiplier - 1.0) * 100);
                bonusLore.add(ChatColor.LIGHT_PURPLE + "  +" + xpPercent + "% XP Gain");
                break;

            case LUCK:
                double luckChance = type.getRareLootChance(level);
                int luckPercent = (int)(luckChance * 100);
                bonusLore.add(ChatColor.YELLOW + "  +" + luckPercent + "% Rare Loot Chance");
                break;
        }

        return bonusLore;
    }

    /**
     * Convert level to Roman numeral
     */
    private String getRomanNumeral(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(level);
        }
    }
}
