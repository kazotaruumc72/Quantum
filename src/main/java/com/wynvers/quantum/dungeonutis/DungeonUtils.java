package com.wynvers.quantum.dungeonutis;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.jobs.JobData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manager for dungeon tools and weapons with rarity system
 * These items provide bonuses for jobs
 */
public class DungeonUtils {

    private final Quantum plugin;
    private final NamespacedKey dungeonUtilKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey levelKey;
    private final NamespacedKey expKey;
    private YamlConfiguration config;
    private final Random random;

    public DungeonUtils(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonUtilKey = new NamespacedKey(plugin, "dungeon_util");
        this.rarityKey = new NamespacedKey(plugin, "util_rarity");
        this.typeKey = new NamespacedKey(plugin, "util_type");
        this.levelKey = new NamespacedKey(plugin, "util_level");
        this.expKey = new NamespacedKey(plugin, "util_exp");
        this.random = new Random();
        loadConfig();
    }

    private void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "dungeons_utils.yml");
            if (!configFile.exists()) {
                plugin.saveResource("dungeons_utils.yml", false);
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("✓ Dungeon Utils system loaded!");
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading dungeons_utils.yml: " + e.getMessage());
        }
    }

    public void reload() {
        loadConfig();
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * Creates a dungeon tool or weapon with specified type and rarity
     */
    public ItemStack createItem(DungeonUtilsType type, DungeonUtilsRarity rarity) {
        String nexoId = getNexoId(type, rarity);
        if (nexoId == null) {
            plugin.getLogger().warning("⚠️ Nexo ID not defined for: " + type.name() + " (rarity: " + rarity.name() + ")");
            return null;
        }

        ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) {
            plugin.getLogger().severe("❌ Nexo item '" + nexoId + "' does not exist!");
            return null;
        }

        ItemStack item = builder.build();
        if (item == null) {
            plugin.getLogger().warning("⚠️ Could not build Nexo item: " + nexoId);
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Set display name
            String displayName = config.getString(type.getConfigKey() + ".display_name");
            if (displayName != null) {
                meta.setDisplayName(rarity.getColor() + displayName.replace('&', '§'));
            }

            // Add enchantments
            addEnchantments(meta, type, rarity);

            // Set persistent data
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(dungeonUtilKey, PersistentDataType.BYTE, (byte) 1);
            data.set(rarityKey, PersistentDataType.STRING, rarity.name());
            data.set(typeKey, PersistentDataType.STRING, type.name());
            data.set(levelKey, PersistentDataType.INTEGER, 0);
            data.set(expKey, PersistentDataType.INTEGER, 0);

            item.setItemMeta(meta);

            // Set lore (must be called after setItemMeta)
            updateLore(item, type, rarity, 0, 0);
        }

        return item;
    }

    /**
     * Checks if an item is a dungeon util
     */
    public boolean isDungeonUtil(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(dungeonUtilKey, PersistentDataType.BYTE);
    }

    /**
     * Gets the rarity of a dungeon util
     */
    public DungeonUtilsRarity getRarity(ItemStack item) {
        if (!isDungeonUtil(item)) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String rarityStr = data.get(rarityKey, PersistentDataType.STRING);

        if (rarityStr == null) return DungeonUtilsRarity.COMMON;

        try {
            return DungeonUtilsRarity.valueOf(rarityStr);
        } catch (IllegalArgumentException e) {
            return DungeonUtilsRarity.COMMON;
        }
    }

    /**
     * Gets the type of a dungeon util
     */
    public DungeonUtilsType getType(ItemStack item) {
        if (!isDungeonUtil(item)) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String typeStr = data.get(typeKey, PersistentDataType.STRING);

        if (typeStr == null) return null;

        try {
            return DungeonUtilsType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if a player can use this dungeon util for their current job
     */
    public boolean canUseForJob(Player player, ItemStack item) {
        DungeonUtilsType type = getType(item);
        if (type == null) return false;

        // Get player's current job
        if (plugin.getJobManager() == null) return false;

        JobData jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
        if (jobData == null) return false;

        // Check if job is compatible
        return type.getCompatibleJob().equalsIgnoreCase(jobData.getJobId());
    }

    /**
     * Gets the job exp bonus multiplier for this item
     */
    public double getJobExpBonus(ItemStack item) {
        DungeonUtilsRarity rarity = getRarity(item);
        return rarity != null ? rarity.getJobExpBonus() : 1.0;
    }

    /**
     * Gets the job money bonus multiplier for this item
     */
    public double getJobMoneyBonus(ItemStack item) {
        DungeonUtilsRarity rarity = getRarity(item);
        return rarity != null ? rarity.getJobMoneyBonus() : 1.0;
    }

    /**
     * Gets the level of a dungeon util
     */
    public int getLevel(ItemStack item) {
        if (!isDungeonUtil(item)) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    /**
     * Gets the experience of a dungeon util
     */
    public int getExp(ItemStack item) {
        if (!isDungeonUtil(item)) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(expKey, PersistentDataType.INTEGER, 0);
    }

    /**
     * Adds kill experience to a dungeon util (weapon or tool)
     * Uses exponential curve with XP reset on level up
     */
    public void addKillExperience(ItemStack item) {
        if (!isDungeonUtil(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        int currentExp = data.getOrDefault(expKey, PersistentDataType.INTEGER, 0);
        int currentLevel = data.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
        int newExp = currentExp + 1;

        // Check if we leveled up with the exponential curve
        int expForNextLevel = getExpForLevel(currentLevel + 1);

        if (newExp >= expForNextLevel) {
            // Level up! Reset XP to 0 and increment level
            int newLevel = currentLevel + 1;
            data.set(levelKey, PersistentDataType.INTEGER, newLevel);
            data.set(expKey, PersistentDataType.INTEGER, 0);
            item.setItemMeta(meta);

            // Update lore
            DungeonUtilsType type = getType(item);
            DungeonUtilsRarity rarity = getRarity(item);
            if (type != null && rarity != null) {
                updateLore(item, type, rarity, newLevel, 0);
            }
        } else {
            // No level up yet, just add the XP
            data.set(expKey, PersistentDataType.INTEGER, newExp);
            item.setItemMeta(meta);

            // Update lore
            DungeonUtilsType type = getType(item);
            DungeonUtilsRarity rarity = getRarity(item);
            if (type != null && rarity != null) {
                updateLore(item, type, rarity, currentLevel, newExp);
            }
        }
    }

    /**
     * Calculates the XP required to reach a given level (exponential curve)
     * Formula: 25 * level * level + 25 * level
     * Level 1: 50 XP, Level 2: 150 XP, Level 3: 300 XP, Level 5: 750 XP, etc.
     */
    private int getExpForLevel(int level) {
        if (level <= 0) return 0;
        return 25 * level * level + 25 * level;
    }

    /**
     * Calculates the XP required to reach the next level from the current level
     */
    private int getExpForNextLevel(int currentLevel) {
        return getExpForLevel(currentLevel + 1);
    }

    /**
     * Gets Nexo ID for type and rarity
     */
    private String getNexoId(DungeonUtilsType type, DungeonUtilsRarity rarity) {
        String path = type.getConfigKey() + ".nexo_ids." + rarity.name();
        return config.getString(path);
    }

    /**
     * Adds enchantments based on type and rarity
     */
    private void addEnchantments(ItemMeta meta, DungeonUtilsType type, DungeonUtilsRarity rarity) {
        String path = type.getConfigKey() + ".enchantments." + rarity.name();
        ConfigurationSection enchantSection = config.getConfigurationSection(path);

        if (enchantSection == null) return;

        for (String key : enchantSection.getKeys(false)) {
            String enchantName = config.getString(path + "." + key + ".enchant");
            int minLevel = config.getInt(path + "." + key + ".min_level", 1);
            int maxLevel = config.getInt(path + "." + key + ".max_level", minLevel);

            if (enchantName == null) continue;

            try {
                NamespacedKey enchantKey = NamespacedKey.minecraft(enchantName.toLowerCase());
                Enchantment enchantment = Enchantment.getByKey(enchantKey);

                if (enchantment != null) {
                    int level = minLevel + random.nextInt(maxLevel - minLevel + 1);
                    meta.addEnchant(enchantment, level, true);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error applying enchantment " + enchantName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Updates item lore with rarity, level, XP, and bonus information
     */
    private void updateLore(ItemStack item, DungeonUtilsType type, DungeonUtilsRarity rarity, int level, int currentExp) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();

        // Get description from config
        List<String> description = config.getStringList(type.getConfigKey() + ".description");
        if (!description.isEmpty()) {
            for (String line : description) {
                lore.add(line.replace('&', '§'));
            }
            lore.add("");
        }

        // Separator
        String separator = config.getString("messages.lore.separator", "&7&m-------------------");
        lore.add(separator.replace('&', '§'));

        // Level and XP progress
        int expForNextLevel = getExpForNextLevel(level);
        lore.add("§8│ §6§lNIVEAU §r§e" + level + " §8│ §b§lXP §r§3" + currentExp + "/" + expForNextLevel + " §8│");

        // Rarity
        String rarityHeader = config.getString("messages.lore.rarity_header", "&6&lRareté:");
        lore.add(rarityHeader.replace('&', '§'));
        lore.add("§8▪ " + rarity.getFormattedName());

        lore.add("");

        // Job bonuses
        String jobBonusHeader = config.getString("messages.lore.job_bonus_header", "&e&lBonus de Job:");
        lore.add(jobBonusHeader.replace('&', '§'));

        String expBonusFormat = config.getString("messages.lore.job_exp_bonus", "&8▪ &7XP: &a+{bonus}%");
        lore.add(expBonusFormat.replace("{bonus}", String.valueOf(rarity.getExpBonusPercent())).replace('&', '§'));

        String moneyBonusFormat = config.getString("messages.lore.job_money_bonus", "&8▪ &7Argent: &a+{bonus}%");
        lore.add(moneyBonusFormat.replace("{bonus}", String.valueOf(rarity.getMoneyBonusPercent())).replace('&', '§'));

        String durabilityBonusFormat = config.getString("messages.lore.durability_bonus", "&8▪ &7Durabilité: &a+{bonus}%");
        lore.add(durabilityBonusFormat.replace("{bonus}", String.valueOf(rarity.getDurabilityBonusPercent())).replace('&', '§'));

        lore.add("");

        // Compatible job
        String compatibleJobsFormat = config.getString("messages.lore.compatible_jobs", "&7Compatible avec: &e{jobs}");
        lore.add(compatibleJobsFormat.replace("{jobs}", type.getCompatibleJob()).replace('&', '§'));

        lore.add("");

        // Footer
        String footer = config.getString("messages.lore.footer", "&7Outil/Arme de Donjon");
        lore.add(separator.replace('&', '§'));
        lore.add(footer.replace('&', '§'));
        lore.add(separator.replace('&', '§'));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Sends a message to player when job bonus is active
     */
    public void notifyJobBonus(Player player, ItemStack item) {
        DungeonUtilsRarity rarity = getRarity(item);
        if (rarity == null || rarity == DungeonUtilsRarity.COMMON) return;

        String message = config.getString("messages.job_bonus_active", "&e⚡ Bonus de job actif: &a+{exp_bonus}% XP &7| &a+{money_bonus}% $");
        message = message.replace("{exp_bonus}", String.valueOf(rarity.getExpBonusPercent()));
        message = message.replace("{money_bonus}", String.valueOf(rarity.getMoneyBonusPercent()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
