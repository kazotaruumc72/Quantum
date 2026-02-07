package com.wynvers.quantum.armor;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestion des items runes avec taux de réussite aléatoire stocké en NBT
 */
public class RuneItem {
    
    private final JavaPlugin plugin;
    private final NamespacedKey successChanceKey;
    private static final Random RANDOM = new Random();
    
    public RuneItem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.successChanceKey = new NamespacedKey(plugin, "rune_success_chance");
    }
    
    /**
     * Crée une rune avec un taux de réussite aléatoire (0-100%)
     * @param rune Type de rune
     * @param level Niveau (1-3)
     * @return ItemStack de la rune avec son taux en NBT
     */
    public ItemStack createRune(RuneType rune, int level) {
        String nexoId = rune.getNexoId(level);
        if (nexoId == null) {
            plugin.getLogger().warning("⚠️ Nexo ID non trouvé pour rune: " + rune.name() + " niveau " + level);
            return null;
        }
        
       if (item == null) {
        com.nexomc.nexo.items.ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) {
            plugin.getLogger().severe("⚠️ ERREUR: Nexo ne trouve pas l'item '" + nexoId + "'");
            plugin.getLogger().severe("⚠️ Vérifiez que cet item existe dans votre configuration Nexo !");
            return null;
        }
        
        ItemStack item = builder.build();
        if (item == null) {
            plugin.getLogger().warning("⚠️ Impossible de créer la rune Nexo: " + nexoId);
            return null;
        }
        
        // Générer un taux aléatoire entre 0 et 100
        // Stocker le taux en NBT
                   int successChance = RANDOM.nextInt(101);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(successChanceKey, PersistentDataType.INTEGER, successChance);
            
            // Ajouter le taux dans le lore
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            
            String color = getChanceColor(successChance);
            lore.add(color + "§l✦ Taux de réussite: " + successChance + "%");
            lore.add("");
            lore.add("§7" + rune.getDescription(level));
            lore.add("");
            lore.add("§c⚠ Si la rune échoue, elle sera détruite");
            lore.add("§a✔ Si elle réussit, elle sera permanente");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Récupère le taux de réussite stocké dans une rune
     * @param item ItemStack de la rune
     * @return Taux de réussite (0-100), ou -1 si non trouvé
     */
    public int getSuccessChance(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(successChanceKey, PersistentDataType.INTEGER)) {
            return -1;
        }
        
        return meta.getPersistentDataContainer().get(successChanceKey, PersistentDataType.INTEGER);
    }
    
    /**
     * Vérifie si un item est une rune valide
     */
    public boolean isRune(ItemStack item) {
        return getSuccessChance(item) != -1;
    }
    
    /**
     * Retourne la couleur selon le taux de réussite
     */
    private String getChanceColor(int chance) {
        if (chance >= 80) return "§a"; // Vert
        if (chance >= 60) return "§e"; // Jaune
        if (chance >= 40) return "§6"; // Orange
        if (chance >= 20) return "§c"; // Rouge
        return "§4"; // Rouge foncé
    }
}
