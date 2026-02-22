package com.wynvers.quantum.dungeonutis;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.wynvers.quantum.Quantum;
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
    private YamlConfiguration config;
    private final Random random;

    public DungeonUtils(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonUtilKey = new NamespacedKey(plugin, "dungeon_util");
        this.rarityKey = new NamespacedKey(plugin, "util_rarity");
        this.typeKey = new NamespacedKey(plugin, "util_type");
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

            // Set lore
            updateLore(meta, type, rarity);

            item.setItemMeta(meta);
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

        String playerJob = plugin.getJobManager().getPlayerJob(player.getUniqueId());
        if (playerJob == null) return false;

        // Check if job is compatible
        return type.getCompatibleJob().equalsIgnoreCase(playerJob);
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
     * Updates item lore with rarity and bonus information
     */
    private void updateLore(ItemMeta meta, DungeonUtilsType type, DungeonUtilsRarity rarity) {
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
