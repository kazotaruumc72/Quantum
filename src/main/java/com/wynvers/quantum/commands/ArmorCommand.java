package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Commande pour gérer l'armure de donjon
 * /armor give <helmet|chestplate|leggings|boots> - Donne une pièce d'armure
 * /armor info - Affiche les infos de l'armure équipée
 * /armor apply <rune> <niveau> - Applique une rune sur l'armure en main
 * /armor upgrade <rune> - Améliore une rune sur l'armure en main
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ArmorCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final DungeonArmor dungeonArmor;
    
    public ArmorCommand(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonArmor = plugin.getDungeonArmor();
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
        
        switch (args[0].toLowerCase()) {
            case "give":
                return handleGive(player, args);
            
            case "info":
                return handleInfo(player);
            
            case "apply":
                return handleApply(player, args);
            
            case "upgrade":
                return handleUpgrade(player, args);
            
            default:
                sendHelp(player);
                return true;
        }
    }
    
    /**
     * Donne une pièce d'armure au joueur
     */
    private boolean handleGive(Player player, String[] args) {
        if (!player.hasPermission("quantum.armor.give")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /armor give <helmet|chestplate|leggings|boots>");
            return true;
        }
        
        Material material;
        switch (args[1].toLowerCase()) {
            case "helmet":
                material = Material.DIAMOND_HELMET;
                break;
            case "chestplate":
                material = Material.DIAMOND_CHESTPLATE;
                break;
            case "leggings":
                material = Material.DIAMOND_LEGGINGS;
                break;
            case "boots":
                material = Material.DIAMOND_BOOTS;
                break;
            default:
                player.sendMessage("§cType invalide. Utilisez: helmet, chestplate, leggings, boots");
                return true;
        }
        
        ItemStack armor = dungeonArmor.createArmorPiece(material);
        player.getInventory().addItem(armor);
        player.sendMessage("§a§l✓ §aVous avez reçu une pièce d'armure de donjon !");
        return true;
    }
    
    /**
     * Affiche les infos de l'armure équipée
     */
    private boolean handleInfo(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§6§lARMURE DE DONJON - INFORMATIONS");
        player.sendMessage("§6§l─────────────────────────────");
        
        displayArmorInfo(player, "Casque", helmet);
        displayArmorInfo(player, "Plastron", chestplate);
        displayArmorInfo(player, "Jambières", leggings);
        displayArmorInfo(player, "Bottes", boots);
        
        boolean hasComplete = dungeonArmor.hasCompleteArmor(player);
        player.sendMessage("");
        if (hasComplete) {
            player.sendMessage("§a§l✓ §aArmure complète ! Bonus actifs.");
        } else {
            player.sendMessage("§c§l✗ §cArmure incomplète. Équipez toutes les pièces.");
        }
        player.sendMessage("§6§l─────────────────────────────");
        
        return true;
    }
    
    /**
     * Affiche les infos d'une pièce d'armure
     */
    private void displayArmorInfo(Player player, String name, ItemStack armor) {
        if (!dungeonArmor.isDungeonArmor(armor)) {
            player.sendMessage("§7" + name + ": §c✗ Aucune");
            return;
        }
        
        int level = dungeonArmor.getArmorLevel(armor);
        Map<RuneType, Integer> runes = dungeonArmor.getAppliedRunesWithLevels(armor);
        
        player.sendMessage("§7" + name + ": §aNiveau " + level + " §8(§e" + runes.size() + "/9 §runes§8)");
        
        if (!runes.isEmpty()) {
            for (Map.Entry<RuneType, Integer> entry : runes.entrySet()) {
                player.sendMessage("§7  • " + entry.getKey().getDisplay() + " §7" + toRoman(entry.getValue()));
            }
        }
    }
    
    /**
     * Applique une rune sur l'armure en main
     */
    private boolean handleApply(Player player, String[] args) {
        if (!player.hasPermission("quantum.armor.apply")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage("§cUsage: /armor apply <rune> <niveau>");
            player.sendMessage("§7Runes disponibles: FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
            return true;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!dungeonArmor.isDungeonArmor(item)) {
            player.sendMessage("§cVous devez tenir une pièce d'armure de donjon.");
            return true;
        }
        
        RuneType rune;
        try {
            rune = RuneType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cRune invalide. Runes disponibles:");
            player.sendMessage("§7FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
            return true;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cNiveau invalide. Utilisez un nombre entre 1 et 3.");
            return true;
        }
        
        if (level < 1 || level > 3) {
            player.sendMessage("§cNiveau invalide. Utilisez un nombre entre 1 et 3.");
            return true;
        }
        
        boolean success = dungeonArmor.applyRune(item, rune, level);
        if (success) {
            player.sendMessage("§a§l✓ §aRune " + rune.getDisplay() + " §7" + toRoman(level) + "§a appliquée !");
        } else {
            player.sendMessage("§c§l✗ §cImpossible d'appliquer la rune (déjà appliquée ou limite atteinte).");
        }
        
        return true;
    }
    
    /**
     * Améliore une rune sur l'armure en main
     */
    private boolean handleUpgrade(Player player, String[] args) {
        if (!player.hasPermission("quantum.armor.upgrade")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /armor upgrade <rune>");
            return true;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!dungeonArmor.isDungeonArmor(item)) {
            player.sendMessage("§cVous devez tenir une pièce d'armure de donjon.");
            return true;
        }
        
        RuneType rune;
        try {
            rune = RuneType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cRune invalide.");
            return true;
        }
        
        boolean success = dungeonArmor.upgradeRune(item, rune);
        if (success) {
            int newLevel = dungeonArmor.getAppliedRunesWithLevels(item).get(rune);
            player.sendMessage("§a§l✓ §aRune " + rune.getDisplay() + " améliorée au niveau §7" + toRoman(newLevel) + "§a !");
        } else {
            player.sendMessage("§c§l✗ §cImpossible d'améliorer (rune absente ou déjà au maximum).");
        }
        
        return true;
    }
    
    /**
     * Affiche l'aide de la commande
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§6§lCOMMANDES ARMURE DE DONJON");
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§e/armor give <pièce> §7- Donne une pièce d'armure");
        player.sendMessage("§e/armor info §7- Affiche les infos de l'armure");
        player.sendMessage("§e/armor apply <rune> <niveau> §7- Applique une rune");
        player.sendMessage("§e/armor upgrade <rune> §7- Améliore une rune");
        player.sendMessage("§6§l─────────────────────────────");
    }
    
    /**
     * Convertit un nombre en chiffres romains
     */
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(number);
        }
    }
}
