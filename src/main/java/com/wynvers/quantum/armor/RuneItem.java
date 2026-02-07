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
    private final NamespacedKey appliedRuneKey; // Rune appliquée sur l’armure
    private static final Random RANDOM = new Random();

    public RuneItem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.successChanceKey = new NamespacedKey(plugin, "rune_success_chance");
        this.appliedRuneKey = new NamespacedKey(plugin, "armor_applied_rune");
    }

    /**
     * Crée une rune avec un taux de réussite aléatoire (0-100%)
     * @param rune  Type de rune
     * @param level Niveau (1-3)
     * @return ItemStack de la rune avec son taux en NBT
     */
    public ItemStack createRune(RuneType rune, int level) {
        String nexoId = rune.getNexoId(level);
        if (nexoId == null) {
            plugin.getLogger().warning("⚠️ Nexo ID non trouvé pour rune: " + rune.name() + " niveau " + level);
            return null;
        }

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
        int successChance = RANDOM.nextInt(101);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Stocker le taux en NBT
            meta.getPersistentDataContainer().set(successChanceKey, PersistentDataType.INTEGER, successChance);

            // Construire le lore
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
    public ItemStack createRuneWithChance(RuneType rune, int level, int forcedChance) {
        String nexoId = rune.getNexoId(level);
        if (nexoId == null) return null;
    
        com.nexomc.nexo.items.ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) return null;
    
        ItemStack item = builder.build();
        if (item == null) return null;
    
        // Utiliser le pourcentage forcé
        int successChance = Math.max(0, Math.min(100, forcedChance));
    
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(successChanceKey, PersistentDataType.INTEGER, successChance);
            
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

        Integer value = meta.getPersistentDataContainer().get(successChanceKey, PersistentDataType.INTEGER);
        return value == null ? -1 : value;
    }

    /**
     * Vérifie si un item est une rune valide
     */
    public boolean isRune(ItemStack item) {
        return getSuccessChance(item) != -1;
    }

    /**
     * Vérifie si une armure possède déjà une rune (simple : 1 rune max).
     */
    public boolean hasRuneOnArmor(ItemStack armor) {
        if (armor == null || !armor.hasItemMeta()) return false;
        ItemMeta meta = armor.getItemMeta();
        return meta.getPersistentDataContainer().has(appliedRuneKey, PersistentDataType.STRING);
    }

    /**
     * Applique une rune sur une armure.
     *
     * @param runeItem  l'ItemStack de la rune (consommé par le caller)
     * @param armorItem l'ItemStack de l'armure cible
     * @param rune      le type de rune
     * @param level     le niveau de la rune
     * @return -1 si rune invalide ou armure non compatible, 0 si échec, 1 si réussite
     */
    public int applyRuneOnArmor(ItemStack runeItem, ItemStack armorItem, RuneType rune, int level) {
        int successChance = getSuccessChance(runeItem);
        if (successChance < 0) {
            return -1; // pas une rune valide
        }

        // Exemple simple : 1 seule rune max par armure
        if (hasRuneOnArmor(armorItem)) {
            return -1;
        }

        int roll = RANDOM.nextInt(100) + 1; // 1–100
        boolean success = roll <= successChance;

        if (!success) {
            return 0; // échec
        }

        // Réussite : marquer l’armure comme runée + ajouter une ligne de lore
        ItemMeta armorMeta = armorItem.getItemMeta();
        if (armorMeta != null) {
            armorMeta.getPersistentDataContainer().set(
                    appliedRuneKey,
                    PersistentDataType.STRING,
                    rune.name() + ":" + level
            );

            List<String> lore = armorMeta.hasLore() ? new ArrayList<>(armorMeta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§b✦ Rune appliquée: §f" + rune.name() + " " + level);
            armorMeta.setLore(lore);

            armorItem.setItemMeta(armorMeta);
        }

        return 1;
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
