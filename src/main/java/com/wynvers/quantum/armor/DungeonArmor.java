package com.wynvers.quantum.armor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestion de l'armure de donjon avec système de runes (niveaux 1-3)
 * 
 * L'armure:
 * - S'améliore au fil du temps (niveau = nombre de kills * 10)
 * - Peut être améliorée avec des runes (max 9 par pièce)
 * - Chaque rune a un niveau 1-3
 * - Gagne des enchantements automatiques à partir du niveau 50
 * - Est stockable via NBT persistent data et lore
 * 
 * Format de stockage des runes: "FORCE:2,SPEED:3,RESISTANCE:1"
 */
public class DungeonArmor {
    
    private final JavaPlugin plugin;
    private final NamespacedKey armorKey;
    private final NamespacedKey levelKey;
    private final NamespacedKey runesKey;
    private final NamespacedKey creationDateKey;
    
    public DungeonArmor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.armorKey = new NamespacedKey(plugin, "dungeon_armor");
        this.levelKey = new NamespacedKey(plugin, "armor_level");
        this.runesKey = new NamespacedKey(plugin, "armor_runes");
        this.creationDateKey = new NamespacedKey(plugin, "creation_date");
    }
    
    /**
     * Crée une pièce d'armure de donjon vierge
     */
    public ItemStack createArmorPiece(Material material) {
        ItemStack armor = new ItemStack(material);
        ItemMeta meta = armor.getItemMeta();
        
        if (meta == null) return armor;
        
        meta.setDisplayName("§6§lArmure de Donjon");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Niveau: §f0");
        lore.add("§7Progression: §c0%");
        lore.add("§7Runes appliquées: §c0/9");
        lore.add("");
        lore.add("§8[Armure de Donjon]");
        
        meta.setLore(lore);
        armor.setItemMeta(meta);
        
        // Store armor data
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(armorKey, PersistentDataType.BYTE, (byte) 1);
        data.set(levelKey, PersistentDataType.INTEGER, 0);
        data.set(creationDateKey, PersistentDataType.LONG, System.currentTimeMillis());
        
        armor.setItemMeta(meta);
        return armor;
    }
    
    /**
     * Vérifie si un ItemStack est une armure de donjon
     */
    public boolean isDungeonArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        
        if (!meta.getDisplayName().contains("Armure de Donjon")) return false;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(armorKey, PersistentDataType.BYTE);
    }
    
    /**
     * Vérifie si un joueur porte une armure de donjon complète
     */
    public boolean hasCompleteArmor(org.bukkit.entity.Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        return isDungeonArmor(helmet) && 
               isDungeonArmor(chestplate) && 
               isDungeonArmor(leggings) && 
               isDungeonArmor(boots);
    }
    
    /**
     * Augmente le niveau de l'armure (1 kill = +1 XP)
     */
    public void addKillExperience(ItemStack armor) {
        if (!isDungeonArmor(armor)) return;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int currentLevel = data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
        int newLevel = currentLevel + 1;
        
        data.set(levelKey, PersistentDataType.INTEGER, newLevel);
        updateArmorLore(armor, newLevel);
        
        armor.setItemMeta(meta);
    }
    
    /**
     * Récupère le niveau de l'armure
     */
    public int getArmorLevel(ItemStack armor) {
        if (!isDungeonArmor(armor)) return 0;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return 0;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }
    
    /**
     * Applique une rune sur l'armure avec un niveau spécifique (1-3)
     * Format: "FORCE:2,SPEED:3"
     */
    public boolean applyRune(ItemStack armor, RuneType rune, int level) {
        if (!isDungeonArmor(armor)) return false;
        if (level < 1 || level > rune.getMaxLevel()) level = 1;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        
        // Récupérer les runes existantes
        Map<RuneType, Integer> appliedRunes = getAppliedRunesWithLevels(armor);
        
        // Vérifier si la rune est déjà appliquée
        if (appliedRunes.containsKey(rune)) {
            return false; // Rune déjà appliquée
        }
        
        // Vérifier la limite de 9 runes
        if (appliedRunes.size() >= 9) {
            return false; // Limite atteinte
        }
        
        // Ajouter la rune avec son niveau
        appliedRunes.put(rune, level);
        
        // Stocker les runes au format "FORCE:2,SPEED:3"
        String runesData = appliedRunes.entrySet().stream()
            .map(e -> e.getKey().name() + ":" + e.getValue())
            .collect(Collectors.joining(","));
        data.set(runesKey, PersistentDataType.STRING, runesData);
        
        // Mettre à jour la lore
        updateArmorLore(armor, getArmorLevel(armor));
        
        armor.setItemMeta(meta);
        return true;
    }
    
    /**
     * Améliore une rune existante au niveau suivant (max: max_level de la rune)
     */
    public boolean upgradeRune(ItemStack armor, RuneType rune) {
        if (!isDungeonArmor(armor)) return false;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return false;
        
        Map<RuneType, Integer> appliedRunes = getAppliedRunesWithLevels(armor);
        
        // Vérifier que la rune existe
        if (!appliedRunes.containsKey(rune)) {
            return false;
        }
        
        int currentLevel = appliedRunes.get(rune);
        int maxLevel = rune.getMaxLevel();
        
        // Vérifier que la rune n'est pas au max
        if (currentLevel >= maxLevel) {
            return false;
        }
        
        // Augmenter le niveau
        appliedRunes.put(rune, currentLevel + 1);
        
        // Stocker les runes
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String runesData = appliedRunes.entrySet().stream()
            .map(e -> e.getKey().name() + ":" + e.getValue())
            .collect(Collectors.joining(","));
        data.set(runesKey, PersistentDataType.STRING, runesData);
        
        // Mettre à jour la lore
        updateArmorLore(armor, getArmorLevel(armor));
        
        armor.setItemMeta(meta);
        return true;
    }
    
    /**
     * Récupère les runes appliquées avec leurs niveaux
     * @return Map<RuneType, Integer> avec le niveau de chaque rune
     */
    public Map<RuneType, Integer> getAppliedRunesWithLevels(ItemStack armor) {
        Map<RuneType, Integer> result = new LinkedHashMap<>();
        
        if (!isDungeonArmor(armor)) return result;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return result;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String runesData = data.getOrDefault(runesKey, PersistentDataType.STRING, "");
        
        if (runesData.isEmpty()) return result;
        
        // Parser: "FORCE:2,SPEED:3"
        for (String runeEntry : runesData.split(",")) {
            String[] parts = runeEntry.split(":");
            if (parts.length == 2) {
                try {
                    RuneType rune = RuneType.valueOf(parts[0].trim());
                    int level = Integer.parseInt(parts[1].trim());
                    result.put(rune, level);
                } catch (IllegalArgumentException e) {
                    // Ignorer les entrées invalides (inclut NumberFormatException)
                }
            }
        }
        
        return result;
    }
    
    /**
     * Récupère SEULEMENT les types de runes appliquées (sans niveaux)
     * Pour compatibilité avec le code existant
     */
    public List<RuneType> getAppliedRunes(ItemStack armor) {
        return new ArrayList<>(getAppliedRunesWithLevels(armor).keySet());
    }
    
    /**
     * Met à jour la lore de l'armure
     */
    private void updateArmorLore(ItemStack armor, int level) {
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = new ArrayList<>();
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        
        lore.add("");
        lore.add("§7Niveau: §f" + level);
        
        int progression = (level % 50) * 2; // 0-100%
        lore.add("§7Progression: §c" + progression + "%");
        
        lore.add("§7Runes appliquées: §e" + runes.size() + "/9");
        
        if (!runes.isEmpty()) {
            lore.add("");
            lore.add("§7Runes actives:");
            for (Map.Entry<RuneType, Integer> entry : runes.entrySet()) {
                RuneType rune = entry.getKey();
                int runeLevel = entry.getValue();
                lore.add("§f  • " + rune.getDisplay() + " §7" + toRoman(runeLevel));
            }
        }
        
        lore.add("");
        
        // Vérifier si l'armure est haute niveau
        if (level >= 50) {
            lore.add("§6★ Armure avancée déverouillée !");
        }
        
        lore.add("§8[Armure de Donjon]");
        
        meta.setLore(lore);
        armor.setItemMeta(meta);
    }
    
    /**
     * Convertit un nombre en chiffres romains
     */
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
    
    /**
     * Obtient le bonus de dégâts basé sur les runes (FORCE rune avec niveau)
     */
    public double getDamageBonus(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.FORCE)) {
            int level = runes.get(RuneType.FORCE);
            switch (level) {
                case 1: return 1.1;  // +10%
                case 2: return 1.2;  // +20%
                case 3: return 1.3;  // +30%
                default: return 1.0;
            }
        }
        return 1.0;
    }
    
    /**
     * Obtient le bonus de crité basé sur les runes
     */
    public double getCriticalChance(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.CRITICAL)) {
            int level = runes.get(RuneType.CRITICAL);
            switch (level) {
                case 1: return 0.15;  // +15%
                case 2: return 0.30;  // +30%
                case 3: return 0.45;  // +45%
                default: return 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * Vérifie si l'armure a le vampirisme
     */
    public boolean hasVampirism(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        return runes.containsKey(RuneType.VAMPIRISM);
    }
    
    /**
     * Obtient le pourcentage de vol de vie (VAMPIRISM rune avec niveau)
     */
    public double getVampirismPercentage(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.VAMPIRISM)) {
            int level = runes.get(RuneType.VAMPIRISM);
            switch (level) {
                case 1: return 0.10;  // Vol 10%
                case 2: return 0.20;  // Vol 20%
                case 3: return 0.30;  // Vol 30%
                default: return 0.0;
            }
        }
        return 0.0;
    }
}
