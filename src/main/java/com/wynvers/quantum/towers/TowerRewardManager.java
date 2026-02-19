package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Distributes floor rewards defined in towers.yml.
 *
 * <p>Reward types supported per floor:
 * <ul>
 *   <li><b>nexo</b>   – custom items via the Nexo API (optional dependency)</li>
 *   <li><b>mythic</b> – items via the MythicMobs API (optional dependency)</li>
 *   <li><b>commands</b> – console commands; entries may be plain strings (100% chance)
 *       or maps with {@code cmd} + {@code chance} keys.</li>
 * </ul>
 */
public class TowerRewardManager {

    private final Quantum plugin;
    private final Random random = new Random();

    public TowerRewardManager(Quantum plugin) {
        this.plugin = plugin;
    }

    /**
     * Give all configured rewards for completing a tower floor.
     *
     * @param player  the player to reward
     * @param towerId the tower ID
     * @param floor   the floor number that was completed
     */
    public void giveFloorRewards(Player player, String towerId, int floor) {
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        String rewardPath = "towers." + towerId + ".floors." + floor + ".rewards";
        ConfigurationSection rewardsSection = config.getConfigurationSection(rewardPath);
        if (rewardsSection == null) return;

        giveNexoRewards(player, rewardsSection.getConfigurationSection("nexo"));
        giveMythicRewards(player, rewardsSection.getConfigurationSection("mythic"));
        executeCommandRewards(player, rewardsSection);

        String message = rewardsSection.getString("message");
        if (message != null && !message.isEmpty()) {
            player.sendMessage(message.replace("&", "§"));
        }
    }

    // ==================== NEXO ====================

    private void giveNexoRewards(Player player, ConfigurationSection nexoSection) {
        if (nexoSection == null) return;

        for (String key : nexoSection.getKeys(false)) {
            ConfigurationSection itemSection = nexoSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            String itemId = itemSection.getString("id");
            int amount = itemSection.getInt("amount", 1);
            double chance = itemSection.getDouble("chance", 100.0);

            if (itemId == null || itemId.isEmpty()) continue;

            if (rollChance(chance)) {
                try {
                    com.nexomc.nexo.items.ItemBuilder builder = com.nexomc.nexo.api.NexoItems.itemFromId(itemId);
                    if (builder != null) {
                        ItemStack item = builder.build();
                        item.setAmount(amount);
                        player.getInventory().addItem(item);

                        String msg = itemSection.getString("message");
                        if (msg != null && !msg.isEmpty()) {
                            player.sendMessage(msg.replace("&", "§"));
                        }
                    } else {
                        plugin.getQuantumLogger().warning("[TowerRewards] Nexo item not found: " + itemId);
                    }
                } catch (Exception e) {
                    plugin.getQuantumLogger().warning("[TowerRewards] Failed to give Nexo item " + itemId + ": " + e.getMessage());
                }
            }
        }
    }

    // ==================== MYTHICMOBS ====================

    private void giveMythicRewards(Player player, ConfigurationSection mythicSection) {
        if (mythicSection == null) return;

        for (String key : mythicSection.getKeys(false)) {
            ConfigurationSection dropSection = mythicSection.getConfigurationSection(key);
            if (dropSection == null) continue;

            String dropId = dropSection.getString("id");
            int amount = dropSection.getInt("amount", 1);
            double chance = dropSection.getDouble("chance", 100.0);

            if (dropId == null || dropId.isEmpty()) continue;

            if (rollChance(chance)) {
                try {
                    ItemStack item = io.lumine.mythic.bukkit.MythicBukkit.inst()
                            .getItemManager()
                            .getItemStack(dropId);
                    if (item != null) {
                        item = item.clone();
                        item.setAmount(amount);
                        player.getInventory().addItem(item);
                    } else {
                        plugin.getQuantumLogger().warning("[TowerRewards] MythicMobs item not found: " + dropId);
                    }
                } catch (Exception e) {
                    plugin.getQuantumLogger().warning("[TowerRewards] Failed to give MythicMobs item " + dropId + ": " + e.getMessage());
                }
            }
        }
    }

    // ==================== COMMANDS ====================

    /**
     * Execute command rewards.
     *
     * <p>YAML list entries may be:
     * <ul>
     *   <li>A plain {@link String} → executed with 100% probability.</li>
     *   <li>A {@link Map} with keys {@code cmd} (String) and {@code chance} (double) →
     *       executed with the given probability.</li>
     * </ul>
     */
    private void executeCommandRewards(Player player, ConfigurationSection rewardsSection) {
        List<?> commandsList = rewardsSection.getList("commands");
        if (commandsList == null) return;

        for (Object obj : commandsList) {
            if (obj instanceof String) {
                executeCommand(player, (String) obj);
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cmdMap = (Map<String, Object>) obj;
                Object cmdObj = cmdMap.get("cmd");
                if (!(cmdObj instanceof String)) continue;

                String cmd = (String) cmdObj;
                double chance = 100.0;
                Object chanceObj = cmdMap.get("chance");
                if (chanceObj instanceof Number) {
                    chance = ((Number) chanceObj).doubleValue();
                }

                if (rollChance(chance)) {
                    executeCommand(player, cmd);
                }
            }
        }
    }

    private void executeCommand(Player player, String command) {
        String processed = command
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
    }

    // ==================== UTIL ====================

    /** @return true if a random roll falls within the given percentage [0, 100]; values > 100 always succeed */
    private boolean rollChance(double chance) {
        return chance >= 100.0 || random.nextDouble() * 100.0 < chance;
    }
}
