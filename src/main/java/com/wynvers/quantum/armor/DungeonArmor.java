package com.wynvers.quantum.armor;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestion de l'armure de donjon avec système de runes (niveaux 1-3) et raretés
 * Utilise l'API Nexo pour créer les items d'armure personnalisés
 */
public class DungeonArmor {
    
    public final JavaPlugin plugin;
    private final NamespacedKey armorKey;
    private final NamespacedKey levelKey;
    private final NamespacedKey runesKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey creationDateKey;
    private YamlConfiguration armorConfig;
    
    public DungeonArmor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.armorKey = new NamespacedKey(plugin, "dungeon_armor");
        this.levelKey = new NamespacedKey(plugin, "armor_level");
        this.runesKey = new NamespacedKey(plugin, "armor_runes");
        this.rarityKey = new NamespacedKey(plugin, "armor_rarity");
        this.creationDateKey = new NamespacedKey(plugin, "creation_date");
        loadConfig();
        
        // Initialiser les raretés depuis la config
        ArmorRarity.init(plugin);
    }
    
    private void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeon_armor.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeon_armor.yml", false);
            }
            armorConfig = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Érreur lors du chargement de dungeon_armor.yml: " + e.getMessage());
        }
    }
    
    public void reload() {
        loadConfig();
        ArmorRarity.init(plugin);
    }
    
    /**
     * Crée une pièce d'armure avec une rareté spécifique
     */
    public ItemStack createArmorPiece(String armorType, ArmorRarity rarity) {
        // Utiliser le Nexo ID de la rareté
        String nexoId = rarity.getNexoId(armorType);
        
        if (nexoId == null) {
            plugin.getLogger().warning("⚠️ Nexo ID non défini pour: " + armorType + " (rareté: " + rarity.name() + ")");
            return null;
        }
        
        ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) {
            plugin.getLogger().severe("❌ L'item Nexo '" + nexoId + "' n'existe pas !");
            return null;
        }
        
        ItemStack armor = builder.build();
        if (armor == null) {
            plugin.getLogger().warning("⚠️ Impossible de build l'item Nexo: " + nexoId);
            return null;
        }
        
        ItemMeta meta = armor.getItemMeta();
        if (meta != null) {
            // --- 1. Gestion du Tooltip Style (CORRECTIF ICI) ---
            try {
                // Applique le style défini dans ArmorRarity (ex: nexo:legendary)
                meta.setTooltipStyle(rarity.getTooltipKey());
            } catch (NoSuchMethodError e) {
                plugin.getLogger().warning("⚠️ Erreur Tooltip: Votre version de Spigot/Paper est trop ancienne (< 1.21.2).");
            } catch (Exception e) {
                plugin.getLogger().warning("⚠️ Erreur lors de l'application du tooltip: " + e.getMessage());
            }

            // --- 2. Display Name ---
            String displayName = armorConfig.getString("armor_pieces." + armorType + ".display_name");
            if (displayName != null) {
                meta.setDisplayName(rarity.getColor() + displayName.replace('&', '§'));
            }
            
            // --- 3. Enchantements ---
            Map<Enchantment, Integer> enchants = rarity.getEnchantments();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            
            // --- 4. Données Persistantes (PDC) ---
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(armorKey, PersistentDataType.BYTE, (byte) 1);
            data.set(levelKey, PersistentDataType.INTEGER, 0);
            data.set(rarityKey, PersistentDataType.STRING, rarity.name());
            data.set(creationDateKey, PersistentDataType.LONG, System.currentTimeMillis());
            
            armor.setItemMeta(meta);
            updateArmorLore(armor, 0);
        }
        
        return armor;
    }
    
    /**
     * Crée une pièce d'armure avec rareté COMMON par défaut
     */
    public ItemStack createArmorPiece(String armorType) {
        return createArmorPiece(armorType, ArmorRarity.COMMON);
    }
    
    /**
     * Récupère la rareté d'une armure
     */
    public ArmorRarity getArmorRarity(ItemStack armor) {
        if (!isDungeonArmor(armor)) return ArmorRarity.COMMON;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return ArmorRarity.COMMON;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String rarityStr = data.get(rarityKey, PersistentDataType.STRING);
        
        if (rarityStr == null) return ArmorRarity.COMMON;
        
        try {
            return ArmorRarity.valueOf(rarityStr);
        } catch (IllegalArgumentException e) {
            return ArmorRarity.COMMON;
        }
    }
    
    public boolean isDungeonArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        
        if (!data.has(armorKey, PersistentDataType.BYTE)) {
            return false;
        }
        
        String nexoId = NexoItems.idFromItem(item);
        return nexoId != null && isConfiguredArmorPiece(nexoId);
    }
    
    private boolean isConfiguredArmorPiece(String nexoId) {
        if (nexoId == null) return false;
        
        // Vérifier si le Nexo ID correspond à une rareté
        for (ArmorRarity rarity : ArmorRarity.values()) {
            for (String armorType : Arrays.asList("helmet", "chestplate", "leggings", "boots")) {
                String rarityNexoId = rarity.getNexoId(armorType);
                if (nexoId.equals(rarityNexoId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasCompleteArmor(org.bukkit.entity.Player player) {
        return isDungeonArmor(player.getInventory().getHelmet()) && 
               isDungeonArmor(player.getInventory().getChestplate()) && 
               isDungeonArmor(player.getInventory().getLeggings()) && 
               isDungeonArmor(player.getInventory().getBoots());
    }
    
    public void addKillExperience(ItemStack armor) {
        if (!isDungeonArmor(armor)) return;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int currentLevel = data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
        int newLevel = currentLevel + 1;
        
        data.set(levelKey, PersistentDataType.INTEGER, newLevel);
        armor.setItemMeta(meta);
        updateArmorLore(armor, newLevel);
    }
    
    public int getArmorLevel(ItemStack armor) {
        if (!isDungeonArmor(armor)) return 0;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return 0;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }
    
    public int getMaxRuneSlots(ItemStack armor) {
        if (!isDungeonArmor(armor)) return 0;
        return getArmorRarity(armor).getMaxRuneSlots();
    }
    
    public boolean applyRune(ItemStack armor, RuneType rune, int level) {
        if (!isDungeonArmor(armor)) return false;
        if (level < 1 || level > rune.getMaxLevel()) level = 1;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return false;
        
        Map<RuneType, Integer> appliedRunes = getAppliedRunesWithLevels(armor);
        
        if (appliedRunes.containsKey(rune)) return false;
        
        int maxSlots = getMaxRuneSlots(armor);
        if (appliedRunes.size() >= maxSlots) return false;
        
        appliedRunes.put(rune, level);
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String runesData = appliedRunes.entrySet().stream()
            .map(e -> e.getKey().name() + ":" + e.getValue())
            .collect(Collectors.joining(","));
        data.set(runesKey, PersistentDataType.STRING, runesData);
        
        armor.setItemMeta(meta);
        updateArmorLore(armor, getArmorLevel(armor));
        
        return true;
    }
    
    public boolean removeRune(ItemStack armor, RuneType rune) {
        if (!isDungeonArmor(armor)) return false;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return false;
        
        Map<RuneType, Integer> appliedRunes = getAppliedRunesWithLevels(armor);
        if (!appliedRunes.containsKey(rune)) return false;
        
        appliedRunes.remove(rune);
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (appliedRunes.isEmpty()) {
            data.remove(runesKey);
        } else {
            String runesData = appliedRunes.entrySet().stream()
                .map(e -> e.getKey().name() + ":" + e.getValue())
                .collect(Collectors.joining(","));
            data.set(runesKey, PersistentDataType.STRING, runesData);
        }
        
        armor.setItemMeta(meta);
        updateArmorLore(armor, getArmorLevel(armor));
        
        return true;
    }
    
    public Map<RuneType, Integer> getAppliedRunesWithLevels(ItemStack armor) {
        Map<RuneType, Integer> result = new LinkedHashMap<>();
        
        if (!isDungeonArmor(armor)) return result;
        
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return result;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String runesData = data.getOrDefault(runesKey, PersistentDataType.STRING, "");
        
        if (runesData.isEmpty()) return result;
        
        for (String runeEntry : runesData.split(",")) {
            String[] parts = runeEntry.split(":");
            if (parts.length == 2) {
                try {
                    RuneType rune = RuneType.valueOf(parts[0].trim());
                    int level = Integer.parseInt(parts[1].trim());
                    result.put(rune, level);
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
        }
        
        return result;
    }
    
    public List<RuneType> getAppliedRunes(ItemStack armor) {
        return new ArrayList<>(getAppliedRunesWithLevels(armor).keySet());
    }
    
    private void updateArmorLore(ItemStack armor, int level) {
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return;
        
        ArmorRarity rarity = getArmorRarity(armor);
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        int maxSlots = rarity.getMaxRuneSlots();
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§8╭────────────────────────╮");
        lore.add("§8│ " + rarity.getDisplayName() + " §8│");
        lore.add("§8│ §6§lNIVEAU §r§e" + level + " §8│ §b§lXP §r§3" + (level * 100) + "/" + ((level + 1) * 100) + " §8│");
        lore.add("§8╰────────────────────────╯");
        lore.add("");
        
        if (runes.isEmpty()) {
            lore.add("§7§o• Aucune rune appliquée");
            lore.add("§7§o• Emplacements disponibles: §e" + maxSlots);
        } else {
            lore.add("§d✦ §l§nRUNES ACTIVES§r §d✦");
            lore.add("");
            for (Map.Entry<RuneType, Integer> entry : runes.entrySet()) {
                RuneType rune = entry.getKey();
                int runeLevel = entry.getValue();
                String desc = rune.getDescription(runeLevel);
                lore.add("§f◆ " + rune.getDisplay() + " §7" + toRoman(runeLevel));
                lore.add("  §7" + desc);
            }
            lore.add("");
            lore.add("§7Emplacements: §e" + runes.size() + "/" + maxSlots);
        }
        
        lore.add("");
        lore.add("§8╭────────────────────────╮");
        lore.add("§8│ §6✪ §e§lARMURE DE DONJON §6✪ §8│");
        lore.add("§8╰────────────────────────╯");
        
        meta.setLore(lore);
        armor.setItemMeta(meta);
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
    
    public double getDamageBonus(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.FORCE)) {
            return RuneType.FORCE.getDamageBonus(runes.get(RuneType.FORCE));
        }
        return 1.0;
    }
    
    public double getCriticalChance(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.CRITICAL)) {
            return RuneType.CRITICAL.getCriticalChance(runes.get(RuneType.CRITICAL));
        }
        return 0.0;
    }
    
    public boolean hasVampirism(ItemStack armor) {
        return getAppliedRunesWithLevels(armor).containsKey(RuneType.VAMPIRISM);
    }
    
    public double getVampirismPercentage(ItemStack armor) {
        Map<RuneType, Integer> runes = getAppliedRunesWithLevels(armor);
        if (runes.containsKey(RuneType.VAMPIRISM)) {
            return RuneType.VAMPIRISM.getVampirismPercent(runes.get(RuneType.VAMPIRISM));
        }
        return 0.0;
    }
}
