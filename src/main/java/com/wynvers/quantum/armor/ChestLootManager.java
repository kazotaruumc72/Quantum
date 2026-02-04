package com.wynvers.quantum.armor;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * Gestionnaire des loots de coffres de donjon
 * Lit la config tower_loots.yml et génère les items
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChestLootManager {
    
    private final Quantum plugin;
    private FileConfiguration lootConfig;
    private final Random random;
    
    public ChestLootManager(Quantum plugin) {
        this.plugin = plugin;
        this.random = new Random();
        loadConfig();
    }
    
    /**
     * Charge la configuration des loots
     */
    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "tower_loots.yml");
        if (!file.exists()) {
            plugin.saveResource("tower_loots.yml", false);
        }
        this.lootConfig = YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Génère un inventaire de coffre avec les loots pour un étage
     * @param towerId ID de la tour
     * @param floor Numéro d'étage
     * @return Inventaire rempli avec les loots
     */
    public Inventory generateChestInventory(String towerId, int floor) {
        String title = lootConfig.getString("settings.chest_title", "§6§lCoffre de Donjon");
        int size = lootConfig.getInt("settings.chest_size", 27);
        
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        // Récupérer les items pour cet étage
        List<ItemStack> items = generateLootItems(towerId, floor);
        
        // Placer les items dans le coffre
        boolean randomPlacement = lootConfig.getBoolean("settings.random_slot_placement", true);
        
        if (randomPlacement) {
            placeItemsRandomly(inv, items);
        } else {
            placeItemsSequentially(inv, items);
        }
        
        return inv;
    }
    
    /**
     * Génère la liste des items de loot pour un étage
     */
    private List<ItemStack> generateLootItems(String towerId, int floor) {
        List<ItemStack> items = new ArrayList<>();
        
        // Vérifier si configuration spécifique pour cet étage
        ConfigurationSection floorSection = lootConfig.getConfigurationSection(
            "towers." + towerId + ".floors." + floor + ".chest.items");
        
        // Sinon utiliser la config par défaut
        if (floorSection == null) {
            floorSection = lootConfig.getConfigurationSection(
                "towers." + towerId + ".default_chest.items");
        }
        
        if (floorSection == null) {
            plugin.getQuantumLogger().warning("Aucune config de loot trouvée pour " + towerId + " étage " + floor);
            return items;
        }
        
        // Parcourir chaque item configuré
        List<?> itemsList = lootConfig.getList("towers." + towerId + ".floors." + floor + ".chest.items");
        if (itemsList == null) {
            itemsList = lootConfig.getList("towers." + towerId + ".default_chest.items");
        }
        
        if (itemsList == null) return items;
        
        for (Object obj : itemsList) {
            if (!(obj instanceof Map)) continue;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> itemConfig = (Map<String, Object>) obj;
            
            double chance = getDouble(itemConfig.get("chance"), 1.0);
            
            // Roll de chance
            if (random.nextDouble() > chance) continue;
            
            String type = (String) itemConfig.get("type");
            int amount = parseAmount((String) itemConfig.get("amount"));
            
            ItemStack item = createLootItem(type, itemConfig, amount);
            if (item != null) {
                items.add(item);
            }
        }
        
        return items;
    }
    
    /**
     * Crée un item de loot selon sa configuration
     */
    private ItemStack createLootItem(String type, Map<String, Object> config, int amount) {
        ItemStack item = null;
        
        switch (type.toUpperCase()) {
            case "RUNE":
                item = createRuneItem(config, amount);
                break;
            
            case "NEXO":
                item = createNexoItem(config, amount);
                break;
            
            case "MINECRAFT":
                item = createMinecraftItem(config, amount);
                break;
        }
        
        return item;
    }
    
    /**
     * Crée un item de rune physique
     */
    private ItemStack createRuneItem(Map<String, Object> config, int amount) {
        String runeType = (String) config.get("rune_type");
        int level = getInt(config.get("level"), 1);
        
        try {
            RuneType rune = RuneType.valueOf(runeType.toUpperCase());
            
            // Essayer de créer via Nexo d'abord
            String nexoId = plugin.getConfig().getString(
                "runes." + runeType + ".item_nexo_ids." + level);
            
            if (nexoId != null && Bukkit.getPluginManager().getPlugin("Nexo") != null) {
                // TODO: Intégration Nexo
                // ItemStack nexoItem = NexoItems.itemFromId(nexoId);
                // if (nexoItem != null) return nexoItem;
            }
            
            // Fallback: créer un item vanilla avec lore
            Material material = Material.getMaterial(
                plugin.getConfig().getString("runes." + runeType + ".icon", "PAPER"));
            
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(rune.getDisplay() + " §7" + toRoman(level));
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Clic droit sur une armure");
                lore.add("§7pour appliquer cette rune");
                lore.add("");
                lore.addAll(plugin.getConfig().getStringList("runes." + runeType + ".lore." + level));
                meta.setLore(lore);
                
                item.setItemMeta(meta);
            }
            
            return item;
            
        } catch (IllegalArgumentException e) {
            plugin.getQuantumLogger().warning("Type de rune invalide: " + runeType);
            return null;
        }
    }
    
    /**
     * Crée un item Nexo
     */
    private ItemStack createNexoItem(Map<String, Object> config, int amount) {
        String nexoId = (String) config.get("id");
        
        if (Bukkit.getPluginManager().getPlugin("Nexo") != null) {
            // TODO: Intégration Nexo
            // ItemStack item = NexoItems.itemFromId(nexoId);
            // if (item != null) {
            //     item.setAmount(amount);
            //     return item;
            // }
        }
        
        // Fallback si Nexo pas disponible
        plugin.getQuantumLogger().warning("Nexo non disponible pour l'item: " + nexoId);
        ItemStack item = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = (String) config.get("display_name");
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Crée un item Minecraft
     */
    private ItemStack createMinecraftItem(Map<String, Object> config, int amount) {
        String materialName = (String) config.get("id");
        
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return new ItemStack(material, amount);
        } catch (IllegalArgumentException e) {
            plugin.getQuantumLogger().warning("Matériau invalide: " + materialName);
            return null;
        }
    }
    
    /**
     * Place les items aléatoirement dans l'inventaire
     */
    private void placeItemsRandomly(Inventory inv, List<ItemStack> items) {
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            availableSlots.add(i);
        }
        
        Collections.shuffle(availableSlots);
        
        for (int i = 0; i < items.size() && i < availableSlots.size(); i++) {
            inv.setItem(availableSlots.get(i), items.get(i));
        }
    }
    
    /**
     * Place les items séquentiellement
     */
    private void placeItemsSequentially(Inventory inv, List<ItemStack> items) {
        for (int i = 0; i < items.size() && i < inv.getSize(); i++) {
            inv.setItem(i, items.get(i));
        }
    }
    
    /**
     * Parse un montant (ex: "3-5" ou "10")
     */
    private int parseAmount(Object amountObj) {
        if (amountObj == null) return 1;
        
        String amountStr = amountObj.toString();
        
        if (amountStr.contains("-")) {
            String[] parts = amountStr.split("-");
            try {
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return min + random.nextInt(max - min + 1);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        
        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    
    /**
     * Convertit un Object en double
     */
    private double getDouble(Object obj, double defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Convertit un Object en int
     */
    private int getInt(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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
