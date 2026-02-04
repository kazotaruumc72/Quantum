package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.Bukkit;
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
 * Commande pour donner des runes physiques (items)
 * /rune give <joueur> <type> <niveau> [quantité]
 * /rune list - Liste toutes les runes disponibles
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class RuneCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public RuneCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                return handleGive(sender, args);
            
            case "list":
                return handleList(sender);
            
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Donne une rune physique à un joueur
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.rune.give")) {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /rune give <joueur> <type> <niveau> [quantité]");
            sender.sendMessage("§7Types: FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
            sender.sendMessage(§7Niveaux: 1, 2, 3");
            return true;
        }
        
        // Récupérer le joueur cible
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[1]);
            return true;
        }
        
        // Récupérer le type de rune
        RuneType runeType;
        try {
            runeType = RuneType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cType de rune invalide: " + args[2]);
            sender.sendMessage("§7Types disponibles: FORCE, SPEED, RESISTANCE, CRITICAL, REGENERATION, VAMPIRISM, THORNS, WISDOM, LUCK");
            return true;
        }
        
        // Récupérer le niveau
        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNiveau invalide. Utilisez 1, 2 ou 3.");
            return true;
        }
        
        if (level < 1 || level > 3) {
            sender.sendMessage("§cNiveau invalide. Utilisez 1, 2 ou 3.");
            return true;
        }
        
        // Récupérer la quantité
        int amount = 1;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage("§cQuantité invalide (1-64).");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cQuantité invalide.");
                return true;
            }
        }
        
        // Créer l'item de rune
        ItemStack runeItem = createRuneItem(runeType, level, amount);
        
        // Donner au joueur
        target.getInventory().addItem(runeItem);
        
        // Messages
        sender.sendMessage("§a§l✓ §aVous avez donné " + amount + "x " + runeType.getDisplay() + " §7" + toRoman(level) + " §aà " + target.getName());
        target.sendMessage("§a§l✓ §aVous avez reçu " + amount + "x " + runeType.getDisplay() + " §7" + toRoman(level));
        
        return true;
    }
    
    /**
     * Liste toutes les runes disponibles
     */
    private boolean handleList(CommandSender sender) {
        sender.sendMessage("§6§l─────────────────────────────");
        sender.sendMessage("§6§lRUNES DISPONIBLES");
        sender.sendMessage("§6§l─────────────────────────────");
        
        for (RuneType rune : RuneType.values()) {
            sender.sendMessage("§7• " + rune.getDisplay() + " §8(I, II, III)");
            sender.sendMessage(§7  " + plugin.getConfig().getString("runes." + rune.name() + ".description", ""));
        }
        
        sender.sendMessage("§6§l─────────────────────────────");
        sender.sendMessage("§7Utilisez §e/rune give <joueur> <type> <niveau>");
        
        return true;
    }
    
    /**
     * Crée un item de rune physique
     */
    private ItemStack createRuneItem(RuneType runeType, int level, int amount) {
        // Essayer de récupérer l'ID Nexo depuis la config
        String nexoId = plugin.getConfig().getString("runes." + runeType.name() + ".item_nexo_ids." + level);
        
        if (nexoId != null && Bukkit.getPluginManager().getPlugin("Nexo") != null) {
            // TODO: Intégration Nexo
            // ItemStack nexoItem = NexoItems.itemFromId(nexoId);
            // if (nexoItem != null) {
            //     nexoItem.setAmount(amount);
            //     return nexoItem;
            // }
        }
        
        // Fallback: créer un item vanilla avec NBT et lore
        Material material = Material.getMaterial(
            plugin.getConfig().getString("runes." + runeType.name() + ".icon", "PAPER"));
        
        if (material == null) {
            material = Material.PAPER;
        }
        
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Nom de l'item
            meta.setDisplayName(runeType.getDisplay() + " §7" + toRoman(level));
            
            // Lore
            List<String> lore = new ArrayList<>();
            lore.add("§8────────────────────");
            lore.add("§7Clic droit sur une pièce");
            lore.add("§7d'armure pour appliquer");
            lore.add("");
            
            // Ajouter les bonus depuis la config
            List<String> bonusLore = plugin.getConfig().getStringList("runes." + runeType.name() + ".lore." + level);
            lore.addAll(bonusLore);
            
            lore.add("");
            lore.add("§8Rune de niveau " + toRoman(level));
            lore.add("§8────────────────────");
            
            meta.setLore(lore);
            
            // NBT pour identifier la rune
            // TODO: Ajouter PDC (PersistentDataContainer) pour stocker type et niveau
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Affiche l'aide de la commande
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l─────────────────────────────");
        sender.sendMessage("§6§lCOMMANDES RUNES");
        sender.sendMessage("§6§l─────────────────────────────");
        sender.sendMessage("§e/rune give <joueur> <type> <niveau> [qte] §7- Donne une rune");
        sender.sendMessage("§e/rune list §7- Liste toutes les runes");
        sender.sendMessage("§6§l─────────────────────────────");
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
