package com.wynvers.quantum.armor;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestion des runes physiques (items) avec API Nexo
 * Chaque rune peut avoir un niveau 1-3
 */
public class RuneItem {
    
    private final JavaPlugin plugin;
    private final NamespacedKey runeTypeKey;
    private final NamespacedKey runeLevelKey;
    
    public RuneItem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runeTypeKey = new NamespacedKey(plugin, "rune_type");
        this.runeLevelKey = new NamespacedKey(plugin, "rune_level");
    }
    
    /**
     * Crée une rune depuis Nexo
     */
    public ItemStack createRune(RuneType runeType, int level) {
        if (level < 1 || level > runeType.getMaxLevel()) {
            level = 1;
        }
        
        String nexoId = runeType.getNexoId(level);
        
        if (nexoId == null) {
            plugin.getLogger().warning("Nexo ID non trouvé pour rune: " + runeType.name() + " niveau " + level);
            return null;
        }
        
        ItemStack rune = NexoItems.itemFromId(nexoId).build();
        
        if (rune == null) {
            plugin.getLogger().warning("Impossible de créer la rune Nexo: " + nexoId);
            return null;
        }
        
        ItemMeta meta = rune.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(runeTypeKey, PersistentDataType.STRING, runeType.name());
            data.set(runeLevelKey, PersistentDataType.INTEGER, level);
            
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            
            lore.add("");
            lore.add("§7Niveau: §e" + toRoman(level));
            lore.add("");
            lore.add("§7Effets:");
            lore.add(runeType.getDescription(level));
            lore.add("");
            lore.add("§8[Rune de Donjon]");
            
            meta.setLore(lore);
            rune.setItemMeta(meta);
        }
        
        return rune;
    }
    
    /**
     * Vérifie si un item est une rune
     */
    public boolean isRune(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        
        if (!data.has(runeTypeKey, PersistentDataType.STRING)) {
            return false;
        }
        
        String nexoId = NexoItems.idFromItem(item);
        return nexoId != null;
    }
    
    /**
     * Récupère le type de rune
     */
    public RuneType getRuneType(ItemStack item) {
        if (!isRune(item)) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String typeStr = data.get(runeTypeKey, PersistentDataType.STRING);
        
        if (typeStr == null) return null;
        
        try {
            return RuneType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Récupère le niveau de la rune
     */
    public int getRuneLevel(ItemStack item) {
        if (!isRune(item)) return 0;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(runeLevelKey, PersistentDataType.INTEGER, 1);
    }
    
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number);
        }
    }
}
