package com.wynvers.quantum.armor;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArmorGUI {
    
    private final DungeonArmor armorManager;
    private final RuneItem runeItemManager;
    
    public ArmorGUI(DungeonArmor armorManager, RuneItem runeItemManager) {
        this.armorManager = armorManager;
        this.runeItemManager = runeItemManager;
    }
    
    /**
     * Ouvre le GUI principal montrant les 4 pièces d'armure équipées
     */
    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6§l⚔ ARMURE DE DONJON ⚔");
        
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(i + 45, border);
        }
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
        
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        if (helmet != null && armorManager.isDungeonArmor(helmet)) {
            inv.setItem(20, createArmorDisplay(helmet, "CASQUE"));
        } else {
            inv.setItem(20, createEmptySlot(Material.IRON_HELMET, "§c§lCASQUE", "§7Aucun casque équipé"));
        }
        
        if (chestplate != null && armorManager.isDungeonArmor(chestplate)) {
            inv.setItem(22, createArmorDisplay(chestplate, "PLASTRON"));
        } else {
            inv.setItem(22, createEmptySlot(Material.IRON_CHESTPLATE, "§c§lPLASTRON", "§7Aucun plastron équipé"));
        }
        
        if (leggings != null && armorManager.isDungeonArmor(leggings)) {
            inv.setItem(24, createArmorDisplay(leggings, "JAMBIÈRES"));
        } else {
            inv.setItem(24, createEmptySlot(Material.IRON_LEGGINGS, "§c§lJAMBIÈRES", "§7Aucunes jambières équipées"));
        }
        
        if (boots != null && armorManager.isDungeonArmor(boots)) {
            inv.setItem(29, createArmorDisplay(boots, "BOTTES"));
        } else {
            inv.setItem(29, createEmptySlot(Material.IRON_BOOTS, "§c§lBOTTES", "§7Aucunes bottes équipées"));
        }
        
        ItemStack info = createItem(Material.BOOK, "§e§lINFORMATIONS", Arrays.asList(
            "",
            "§7Cliquez sur une pièce d'armure",
            "§7pour gérer ses runes.",
            "",
            "§c⚠ Les runes sont permanentes !",
            "§cUne fois appliquées, impossible",
            "§cde les retirer."
        ));
        inv.setItem(31, info);
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre le GUI de sélection de runes pour une pièce d'armure
     */
    public void openRuneSelectionGUI(Player player, String armorSlot) {
        Inventory inv = Bukkit.createInventory(null, 54, "§d§l⚔ RUNES - " + armorSlot.toUpperCase() + " ⚔");
        
        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(i + 45, border);
        }
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
        
        ItemStack armor = getArmorPiece(player, armorSlot);
        if (armor == null || !armorManager.isDungeonArmor(armor)) {
            player.sendMessage("§c✖ Vous devez équiper cette pièce d'armure !");
            return;
        }
        
        inv.setItem(4, armor.clone());
        
        Map<RuneType, Integer> appliedRunes = armorManager.getAppliedRunesWithLevels(armor);
        int maxSlots = armorManager.getMaxRuneSlots(armor);
        
        int slot = 19;
        for (RuneType rune : RuneType.values()) {
            if (slot == 26) slot = 28;
            if (slot >= 35) break;
            
            boolean isApplied = appliedRunes.containsKey(rune);
            int currentLevel = appliedRunes.getOrDefault(rune, 0);
            
            inv.setItem(slot, createRuneItem(player, rune, currentLevel, isApplied, maxSlots, appliedRunes.size()));
            slot++;
        }
        
        ItemStack back = createItem(Material.ARROW, "§c§l← RETOUR", Collections.singletonList("§7Retour au menu principal"));
        inv.setItem(49, back);
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre le GUI de sélection du niveau de rune
     */
    public void openRuneLevelGUI(Player player, String armorSlot, RuneType rune) {
        Inventory inv = Bukkit.createInventory(null, 27, "§d§l" + rune.getDisplay() + " - NIVEAU");
        
        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(i + 18, border);
        }
        
        int[] slots = {11, 13, 15};
        for (int level = 1; level <= rune.getMaxLevel(); level++) {
            inv.setItem(slots[level - 1], createRuneLevelItem(player, rune, level));
        }
        
        ItemStack back = createItem(Material.ARROW, "§c§l← RETOUR", Collections.singletonList("§7Retour aux runes"));
        inv.setItem(22, back);
        
        player.openInventory(inv);
    }
    
    /**
     * Tente d'appliquer une rune sur une pièce d'armure avec système de chance
     */
    public void applyRune(Player player, String armorSlot, RuneType rune, int level) {
        ItemStack armor = getArmorPiece(player, armorSlot);
        
        if (armor == null || !armorManager.isDungeonArmor(armor)) {
            player.sendMessage("§c✖ Cette pièce d'armure n'est pas valide !");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        
        String nexoId = rune.getNexoId(level);
        if (nexoId == null) {
            player.sendMessage("§c✖ Cette rune n'existe pas !");
            return;
        }
        
        ItemStack runeItem = findRuneInInventory(player, nexoId);
        if (runeItem == null) {
            player.sendMessage("§c✖ Vous n'avez pas cette rune dans votre inventaire !");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        
        // Récupérer le taux stocké en NBT
        int successChance = runeItemManager.getSuccessChance(runeItem);
        if (successChance == -1) {
            player.sendMessage("§c✖ Cette rune n'a pas de taux de réussite valide !");
            return;
        }
        
        Random random = new Random();
        int roll = random.nextInt(100) + 1;
        
        // Retirer la rune de l'inventaire
        runeItem.setAmount(runeItem.getAmount() - 1);
        
        if (roll <= successChance) {
            // SUCCÈS !
            boolean applied = armorManager.applyRune(armor, rune, level);
            
            if (applied) {
                player.sendMessage("");
                player.sendMessage("§a§l✔ SUCCÈS !");
                player.sendMessage("§7La rune " + rune.getDisplay() + " §7" + toRoman(level) + " a été appliquée !");
                player.sendMessage("§7Taux de réussite: §e" + successChance + "%");
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
            } else {
                player.sendMessage("§c✖ Impossible d'appliquer cette rune (slots pleins ou déjà appliquée)");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        } else {
            // ÉCHEC !
            player.sendMessage("");
            player.sendMessage("§c§l✖ ÉCHEC !");
            player.sendMessage("§7La rune " + rune.getDisplay() + " §7" + toRoman(level) + " a échoué...");
            player.sendMessage("§7Taux de réussite: §e" + successChance + "%");
            player.sendMessage("§c§l⚠ La rune a été détruite !");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
        }
        
        Bukkit.getScheduler().runTaskLater(armorManager.plugin, () -> {
            openRuneSelectionGUI(player, armorSlot);
        }, 20L);
    }
    
    // ==================== UTILITAIRES ====================
    
    private ItemStack createArmorDisplay(ItemStack armor, String name) {
        ItemStack display = armor.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§e§l» Cliquez pour gérer les runes");
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }
    
    private ItemStack createEmptySlot(Material material, String name, String description) {
        return createItem(material, name, Arrays.asList("", description));
    }
    
    private ItemStack createRuneItem(Player player, RuneType rune, int currentLevel, boolean isApplied, int maxSlots, int usedSlots) {
        Material material = isApplied ? Material.LIME_DYE : (usedSlots >= maxSlots ? Material.GRAY_DYE : Material.PAPER);
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        if (isApplied) {
            lore.add("§a§l✔ APPLIQUÉE - Niveau " + toRoman(currentLevel));
            lore.add("§7" + rune.getDescription(currentLevel));
            lore.add("");
            lore.add("§c⚠ Rune permanente");
        } else if (usedSlots >= maxSlots) {
            lore.add("§c§l✖ IMPOSSIBLE");
            lore.add("§7Tous les emplacements sont utilisés");
            lore.add("§7(" + usedSlots + "/" + maxSlots + ")");
        } else {
            lore.add("§e§l✦ DISPONIBLE");
            lore.add("");
            lore.add("§7Runes dans votre inventaire:");
            lore.add("");
            
            // Afficher les runes que le joueur possède
            for (int level = 1; level <= rune.getMaxLevel(); level++) {
                String nexoId = rune.getNexoId(level);
                ItemStack found = findRuneInInventory(player, nexoId);
                
                if (found != null) {
                    int chance = runeItemManager.getSuccessChance(found);
                    String color = getChanceColor(chance);
                    lore.add("§f• Niveau " + toRoman(level) + " " + color + "(" + chance + "% réussite)");
                    lore.add("  §7" + rune.getDescription(level));
                }
            }
            
            lore.add("");
            lore.add("§e§l» Cliquez pour choisir le niveau");
        }
        
        return createItem(material, rune.getDisplay(), lore);
    }
    
    private ItemStack createRuneLevelItem(Player player, RuneType rune, int level) {
        String nexoId = rune.getNexoId(level);
        ItemStack runeItem = findRuneInInventory(player, nexoId);
        
        if (runeItem == null) {
            return createItem(Material.BARRIER, "§c" + rune.getDisplay() + " §7" + toRoman(level), 
                Arrays.asList("", "§c✖ Vous ne possédez pas cette rune !"));
        }
        
        int chance = runeItemManager.getSuccessChance(runeItem);
        String color = getChanceColor(chance);
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Description:");
        lore.add("§f" + rune.getDescription(level));
        lore.add("");
        lore.add(color + "§lTaux de réussite: " + chance + "%");
        lore.add("");
        lore.add("§c⚠ Si la rune échoue, elle sera détruite");
        lore.add("§a✔ Si elle réussit, elle sera permanente");
        lore.add("");
        lore.add("§e§l» Cliquez pour appliquer");
        
        Material material = chance >= 70 ? Material.EMERALD : (chance >= 40 ? Material.GOLD_INGOT : Material.REDSTONE);
        
        return createItem(material, rune.getDisplay() + " §7" + toRoman(level), lore);
    }
    
    private String getChanceColor(int chance) {
        if (chance >= 80) return "§a";
        if (chance >= 60) return "§e";
        if (chance >= 40) return "§6";
        if (chance >= 20) return "§c";
        return "§4";
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack getArmorPiece(Player player, String slot) {
        switch (slot.toLowerCase()) {
            case "casque":
            case "helmet":
                return player.getInventory().getHelmet();
            case "plastron":
            case "chestplate":
                return player.getInventory().getChestplate();
            case "jambières":
            case "leggings":
                return player.getInventory().getLeggings();
            case "bottes":
            case "boots":
                return player.getInventory().getBoots();
            default:
                return null;
        }
    }
    
    private ItemStack findRuneInInventory(Player player, String nexoId) {
        if (nexoId == null) return null;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                String itemNexoId = NexoItems.idFromItem(item);
                if (nexoId.equals(itemNexoId)) {
                    return item;
                }
            }
        }
        return null;
    }
    
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(number);
        }
    }
    
    // ==================== GETTERS ====================
    
    public static String getArmorSlotFromPosition(int slot) {
        switch (slot) {
            case 20: return "helmet";
            case 22: return "chestplate";
            case 24: return "leggings";
            case 29: return "boots";
            default: return null;
        }
    }
    
    public static RuneType getRuneFromSlot(int slot) {
        RuneType[] runes = RuneType.values();
        int[] validSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29};
        
        for (int i = 0; i < validSlots.length && i < runes.length; i++) {
            if (validSlots[i] == slot) {
                return runes[i];
            }
        }
        return null;
    }
}
