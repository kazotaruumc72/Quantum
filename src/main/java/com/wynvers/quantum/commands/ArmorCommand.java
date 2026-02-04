package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Commande pour gérer l'armure de donjon
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

            // ✅ AJOUT DE LA CASE DEBUG ICI (au bon endroit)
            case "debug":
                return handleDebug(player);
            
            default:
                sendHelp(player);
                return true;
        }
    }

    /**
     * ✅ Nouvelle méthode dédiée au debug pour trouver l'ID
     */
    private boolean handleDebug(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) {
            player.sendMessage("§cPrends un item dans ta main !");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        
        // Vérification du tooltip style (Paper 1.21.2+)
        try {
            if (meta.hasTooltipStyle()) {
                String idTrouve = meta.getTooltipStyle().getKey().toString();
                plugin.getLogger().info(">>> ID TOOLTIP TROUVÉ : " + idTrouve);
                player.sendMessage("§a§lID TROUVÉ ! §r§aRegarde ta console serveur (fenêtre noire) !");
                player.sendMessage("§7Valeur: " + idTrouve); // Tente de l'afficher aussi dans le chat
            } else {
                plugin.getLogger().info(">>> CET ITEM N'A PAS DE TOOLTIP STYLE DÉFINI.");
                player.sendMessage("§cCet item n'a pas de style de tooltip actif.");
            }
        } catch (NoSuchMethodError e) {
            player.sendMessage("§cErreur: Ton serveur n'est pas en 1.21.2+ ou Spigot/Paper n'est pas à jour.");
        }
        return true;
    }
    
    private boolean handleGive(Player player, String[] args) {
        if (!player.hasPermission("quantum.armor.give")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /armor give <helmet|chestplate|leggings|boots>");
            return true;
        }
        
        String armorType = args[1].toLowerCase();
        if (!armorType.matches("helmet|chestplate|leggings|boots")) {
            player.sendMessage("§cType invalide. Utilisez: helmet, chestplate, leggings, boots");
            return true;
        }
        
        ItemStack armor = dungeonArmor.createArmorPiece(armorType);
        if (armor == null) {
            player.sendMessage("§cErreur lors de la création de l'armure.");
            return true;
        }
        
        player.getInventory().addItem(armor);
        player.sendMessage("§a§l✓ §aVous avez reçu une pièce d'armure de donjon !");
        return true;
    }
    
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
    
    private void displayArmorInfo(Player player, String name, ItemStack armor) {
        if (!dungeonArmor.isDungeonArmor(armor)) {
            player.sendMessage("§7" + name + ": §c✗ Aucune");
            return;
        }
        
        int level = dungeonArmor.getArmorLevel(armor);
        Map<RuneType, Integer> runes = dungeonArmor.getAppliedRunesWithLevels(armor);
        int maxSlots = dungeonArmor.getMaxRuneSlots(armor);
        
        player.sendMessage("§7" + name + ": §aNiveau " + level + " §8(§e" + runes.size() + "/" + maxSlots + " runes§8)");
        
        if (!runes.isEmpty()) {
            for (Map.Entry<RuneType, Integer> entry : runes.entrySet()) {
                player.sendMessage("§7  • " + entry.getKey().getDisplay() + " §7" + toRoman(entry.getValue()));
            }
        }
    }
    
    private boolean handleApply(Player player, String[] args) {
        if (!player.hasPermission("quantum.armor.apply")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage("§cUsage: /armor apply <rune> <niveau>");
            player.sendMessage("§7Runes disponibles: FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM");
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
        
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cNiveau invalide.");
            return true;
        }
        
        if (level < 1 || level > 3) {
            player.sendMessage("§cNiveau invalide (1-3).");
            return true;
        }
        
        boolean success = dungeonArmor.applyRune(item, rune, level);
        if (success) {
            player.sendMessage("§a§l✓ §aRune " + rune.getDisplay() + " §7" + toRoman(level) + "§a appliquée !");
        } else {
            player.sendMessage("§c§l✗ §cImpossible d'appliquer la rune.");
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§6§lCOMMANDES ARMURE DE DONJON");
        player.sendMessage("§6§l─────────────────────────────");
        player.sendMessage("§e/armor give <pièce> §7- Donne une pièce d'armure");
        player.sendMessage("§e/armor info §7- Affiche les infos de l'armure");
        player.sendMessage("§e/armor apply <rune> <niveau> §7- Applique une rune");
        player.sendMessage("§e/armor debug §7- Affiche l'ID du tooltip (pour dev)");
        player.sendMessage("§6§l─────────────────────────────");
    }
    
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(number);
        }
    }
}
