package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * Manages the floor reward selection GUI.
 *
 * <p>When a player completes all mob-kill requirements on a tower floor, this manager
 * opens a custom inventory listing the available Nexo / MythicMobs rewards. The player
 * picks up to {@code choices} items (configurable in {@code menus/floor_rewards.yml}).
 * Command rewards are always auto-executed and are never shown in the selection menu.
 *
 * <p>Menu layout (background, info item, close button, reward slots) is fully editable
 * via {@code menus/floor_rewards.yml}.
 */
public class FloorRewardMenuManager implements Listener {

    private final Quantum plugin;

    /** Active reward sessions keyed by player UUID. */
    private final Map<UUID, RewardSession> sessions = new HashMap<>();

    // ------------------------------------------------------------------ //
    // Session data
    // ------------------------------------------------------------------ //

    private static final class RewardSession {
        final Inventory inventory;
        final List<FloorRewardEntry> entries;
        /** Inventory slots that contain reward items (parallel to entries list). */
        final int[] rewardSlots;
        final int maxChoices;
        final String towerId;
        final int floor;
        /** Tracks how many choices the player has left. */
        int remainingChoices;
        /** Guards against duplicate command-reward execution. */
        boolean commandsExecuted = false;

        RewardSession(Inventory inventory, List<FloorRewardEntry> entries,
                      int[] rewardSlots, int maxChoices, String towerId, int floor) {
            this.inventory = inventory;
            this.entries = entries;
            this.rewardSlots = rewardSlots;
            this.maxChoices = maxChoices;
            this.towerId = towerId;
            this.floor = floor;
            this.remainingChoices = maxChoices;
        }
    }

    // ------------------------------------------------------------------ //
    // Constructor
    // ------------------------------------------------------------------ //

    public FloorRewardMenuManager(Quantum plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ //
    // Public API
    // ------------------------------------------------------------------ //

    /**
     * Opens the reward-selection menu for {@code player} for the given tower floor.
     *
     * <p>If the floor has no selectable (Nexo / Mythic) rewards, command rewards are
     * executed immediately and no menu is shown.
     *
     * @param player  the player who completed the kill quota
     * @param towerId the tower identifier
     * @param floor   the floor number
     */
    public void openForPlayer(Player player, String towerId, int floor) {
        TowerRewardManager rewardManager = plugin.getTowerManager().getRewardManager();
        List<FloorRewardEntry> entries = rewardManager.buildRewardEntries(towerId, floor);

        if (entries.isEmpty()) {
            // No visual rewards – execute command rewards immediately and return
            rewardManager.executeCommandRewardsOnly(player, towerId, floor);
            sendRewardMessage(player, towerId, floor);
            return;
        }

        // ---- Load menu layout config ----
        File configFile = new File(plugin.getDataFolder(), "menus/floor_rewards.yml");
        FileConfiguration cfg = configFile.exists()
                ? YamlConfiguration.loadConfiguration(configFile)
                : new YamlConfiguration();

        String rawTitle = cfg.getString("title",
                "&6\u2756 R\u00e9compenses &e- Choisissez &6{choices} r\u00e9compenses");
        int size = cfg.getInt("size", 54);
        int maxChoices = cfg.getInt("choices", 3);

        List<Integer> rewardSlotsList = cfg.getIntegerList("reward_slots");
        if (rewardSlotsList.isEmpty()) {
            rewardSlotsList = Arrays.asList(11, 13, 15, 20, 22, 24, 29, 31, 33);
        }

        int actualChoices = Math.min(maxChoices, entries.size());
        // Replace {choices} placeholder before translating color codes
        String title = ChatColor.translateAlternateColorCodes('&',
                rawTitle.replace("{choices}", String.valueOf(actualChoices)));

        Inventory inv = Bukkit.createInventory(null, size, title);

        // ---- Background ----
        String bgMat = cfg.getString("background.material", "BLACK_STAINED_GLASS_PANE");
        String bgName = cfg.getString("background.display_name", " ");
        ItemStack bgItem = makeSimpleItem(Material.valueOf(bgMat.toUpperCase()),
                ChatColor.translateAlternateColorCodes('&', bgName), null);
        for (int i = 0; i < size; i++) {
            inv.setItem(i, bgItem.clone());
        }

        // ---- Info item ----
        ConfigurationSection infoSec = cfg.getConfigurationSection("info");
        int infoSlot = 4;
        if (infoSec != null) {
            infoSlot = infoSec.getInt("slot", 4);
            String infoMat = infoSec.getString("material", "NETHER_STAR");
            String infoName = infoSec.getString("display_name", "&6\u2756 R\u00e9compenses d'\u00c9tage");
            List<String> infoLore = infoSec.getStringList("lore");
            List<String> parsedLore = parseLore(infoLore, actualChoices, actualChoices);
            inv.setItem(infoSlot, makeSimpleItem(Material.valueOf(infoMat.toUpperCase()),
                    ChatColor.translateAlternateColorCodes('&', infoName), parsedLore));
        }

        // ---- Close button ----
        int closeSlot = cfg.getInt("close_button.slot", 49);
        ConfigurationSection closeSec = cfg.getConfigurationSection("close_button");
        if (closeSec != null) {
            String closeMat = closeSec.getString("material", "BARRIER");
            String closeName = closeSec.getString("display_name", "&cFermer");
            List<String> closeLore = new ArrayList<>();
            for (String line : closeSec.getStringList("lore")) {
                closeLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            inv.setItem(closeSlot, makeSimpleItem(Material.valueOf(closeMat.toUpperCase()),
                    ChatColor.translateAlternateColorCodes('&', closeName), closeLore));
        } else {
            inv.setItem(closeSlot, makeSimpleItem(Material.BARRIER, ChatColor.RED + "Fermer", null));
        }

        // ---- Reward items ----
        int[] slotsArray = new int[Math.min(entries.size(), rewardSlotsList.size())];
        for (int i = 0; i < slotsArray.length; i++) {
            slotsArray[i] = rewardSlotsList.get(i);
            ItemStack display = buildDisplayItem(entries.get(i));
            if (display != null) {
                inv.setItem(slotsArray[i], display);
            }
        }

        // ---- Open inventory and create session ----
        RewardSession session = new RewardSession(inv, entries, slotsArray, actualChoices, towerId, floor);
        sessions.put(player.getUniqueId(), session);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    /**
     * Returns {@code true} if the given inventory belongs to an active reward-selection session.
     * Used by {@link com.wynvers.quantum.listeners.MenuListener} to avoid intercepting clicks.
     */
    public boolean isRewardMenu(Inventory inventory) {
        if (inventory == null) return false;
        for (RewardSession s : sessions.values()) {
            if (s.inventory.equals(inventory)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ //
    // Event handlers
    // ------------------------------------------------------------------ //

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        RewardSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        Inventory topInv = event.getView().getTopInventory();
        if (!topInv.equals(session.inventory)) return;

        // Always cancel interaction with this inventory
        event.setCancelled(true);

        // Only handle clicks inside the reward inventory (not player inventory)
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInv)) return;

        int slot = event.getSlot();

        // Find which reward entry matches this slot
        int rewardIndex = -1;
        for (int i = 0; i < session.rewardSlots.length; i++) {
            if (session.rewardSlots[i] == slot) {
                rewardIndex = i;
                break;
            }
        }

        if (rewardIndex < 0 || rewardIndex >= session.entries.size()) return;

        if (session.remainingChoices <= 0) {
            player.sendMessage(ChatColor.RED + "Vous avez d\u00e9j\u00e0 s\u00e9lectionn\u00e9 toutes vos r\u00e9compenses!");
            return;
        }

        // Give the reward
        FloorRewardEntry entry = session.entries.get(rewardIndex);
        giveReward(player, entry);

        // Replace slot with "claimed" indicator
        session.inventory.setItem(slot, makeSimpleItem(Material.GREEN_STAINED_GLASS_PANE,
                ChatColor.GREEN + "\u2713 R\u00e9compense r\u00e9clam\u00e9e!", null));

        session.remainingChoices--;
        updateInfoSlot(session, plugin.getDataFolder());

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        if (session.remainingChoices <= 0) {
            // Auto-close after a short delay so the player sees the green slots
            Bukkit.getScheduler().runTaskLater(plugin, (Runnable) player::closeInventory, 10L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        RewardSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        // Validate this close event is for our inventory
        if (!event.getInventory().equals(session.inventory)) {
            sessions.put(player.getUniqueId(), session); // restore – different inventory
            return;
        }

        // Execute command rewards exactly once when the menu is closed
        if (!session.commandsExecuted) {
            session.commandsExecuted = true;
            plugin.getTowerManager().getRewardManager()
                    .executeCommandRewardsOnly(player, session.towerId, session.floor);
            sendRewardMessage(player, session.towerId, session.floor);
        }
    }

    // ------------------------------------------------------------------ //
    // Private helpers
    // ------------------------------------------------------------------ //

    /** Give a single reward item to the player. */
    private void giveReward(Player player, FloorRewardEntry entry) {
        try {
            if (entry.getType() == FloorRewardEntry.Type.NEXO) {
                com.nexomc.nexo.items.ItemBuilder builder =
                        com.nexomc.nexo.api.NexoItems.itemFromId(entry.getId());
                if (builder != null) {
                    ItemStack item = builder.build();
                    item.setAmount(entry.getAmount());
                    player.getInventory().addItem(item);
                } else {
                    plugin.getQuantumLogger().warning("[FloorReward] Nexo item not found: " + entry.getId());
                }
            } else if (entry.getType() == FloorRewardEntry.Type.MYTHIC) {
                ItemStack item = io.lumine.mythic.bukkit.MythicBukkit.inst()
                        .getItemManager().getItemStack(entry.getId());
                if (item != null) {
                    item = item.clone();
                    item.setAmount(entry.getAmount());
                    player.getInventory().addItem(item);
                } else {
                    plugin.getQuantumLogger().warning("[FloorReward] MythicMobs item not found: " + entry.getId());
                }
            }
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("[FloorReward] Failed to give reward '"
                    + entry.getId() + "': " + e.getMessage());
        }

        if (entry.getMessage() != null && !entry.getMessage().isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', entry.getMessage()));
        }
    }

    /** Build the ItemStack displayed in the selection GUI for a reward entry. */
    private ItemStack buildDisplayItem(FloorRewardEntry entry) {
        ItemStack item = null;

        try {
            if (entry.getType() == FloorRewardEntry.Type.NEXO) {
                com.nexomc.nexo.items.ItemBuilder builder =
                        com.nexomc.nexo.api.NexoItems.itemFromId(entry.getId());
                if (builder != null) {
                    item = builder.build();
                    item.setAmount(entry.getAmount());
                }
            } else if (entry.getType() == FloorRewardEntry.Type.MYTHIC) {
                ItemStack mythicItem = io.lumine.mythic.bukkit.MythicBukkit.inst()
                        .getItemManager().getItemStack(entry.getId());
                if (mythicItem != null) {
                    item = mythicItem.clone();
                    item.setAmount(entry.getAmount());
                }
            }
        } catch (Exception ignored) {
            // API not available – fall through to fallback
        }

        if (item == null) {
            item = new ItemStack(Material.CHEST);
        }

        // Append chance info + click prompt to existing lore
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            if (entry.getChance() < 100.0) {
                lore.add(ChatColor.GRAY + "Chance: " + ChatColor.YELLOW
                        + String.format("%.0f", entry.getChance()) + "%");
            }
            lore.add(ChatColor.YELLOW + "\u25ba Cliquez pour r\u00e9clamer!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /** Refresh the info item in the inventory to reflect remaining choices. */
    private void updateInfoSlot(RewardSession session, File dataFolder) {
        File configFile = new File(dataFolder, "menus/floor_rewards.yml");
        FileConfiguration cfg = configFile.exists()
                ? YamlConfiguration.loadConfiguration(configFile)
                : new YamlConfiguration();

        ConfigurationSection infoSec = cfg.getConfigurationSection("info");
        if (infoSec == null) return;

        int infoSlot = infoSec.getInt("slot", 4);
        String infoMat = infoSec.getString("material", "NETHER_STAR");
        String infoName = infoSec.getString("display_name", "&6\u2756 R\u00e9compenses d'\u00c9tage");
        List<String> parsedLore = parseLore(infoSec.getStringList("lore"),
                session.maxChoices, session.remainingChoices);

        session.inventory.setItem(infoSlot,
                makeSimpleItem(Material.valueOf(infoMat.toUpperCase()),
                        ChatColor.translateAlternateColorCodes('&', infoName), parsedLore));
    }

    /** Parse lore lines, replacing {choices} and {remaining} placeholders. */
    private List<String> parseLore(List<String> raw, int choices, int remaining) {
        List<String> result = new ArrayList<>();
        for (String line : raw) {
            result.add(ChatColor.translateAlternateColorCodes('&',
                    line.replace("{choices}", String.valueOf(choices))
                        .replace("{remaining}", String.valueOf(remaining))));
        }
        return result;
    }

    /** Create a simple ItemStack with display name, lore, and hidden attributes. */
    private ItemStack makeSimpleItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Send the floor's completion message to the player if one is configured. */
    private void sendRewardMessage(Player player, String towerId, int floor) {
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(towersFile);
        String msg = cfg.getString("towers." + towerId + ".floors." + floor + ".rewards.message");
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
