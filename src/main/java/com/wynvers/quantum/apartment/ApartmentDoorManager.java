package com.wynvers.quantum.apartment;

import com.wynvers.quantum.Quantum;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages apartment front doors with a WorldEdit-style selection system.
 * Doors disappear for a configurable duration then reappear.
 * Access is controlled by apartment lock status and owner/visitor lists — no LuckPerms required.
 */
public class ApartmentDoorManager {

    /** Duration (seconds) a door stays open once opened. */
    private static final int DOOR_OPEN_SECONDS = 30;

    // Wand item
    public static final String WAND_NAME = "§b§lHache de Porte";
    public static final Material WAND_MATERIAL = Material.COPPER_AXE;

    private final Quantum plugin;
    private final ApartmentManager apartmentManager;
    private final File doorsFile;
    private FileConfiguration doorsConfig;

    // Per-player selection
    private final Map<UUID, Location> pos1Selection = new HashMap<>();
    private final Map<UUID, Location> pos2Selection = new HashMap<>();

    // Saved door configurations
    private final Map<Integer, ApartmentDoorConfig> doorConfigs = new HashMap<>();

    // Currently open doors
    private final Map<Integer, Set<BlockSnapshot>> openedDoors = new HashMap<>();
    private final Map<Integer, BukkitTask> doorCloseTasks = new HashMap<>();

    // Countdown tasks per player
    private final Map<UUID, BukkitTask> playerCountdownTasks = new HashMap<>();

    public ApartmentDoorManager(Quantum plugin, ApartmentManager apartmentManager) {
        this.plugin = plugin;
        this.apartmentManager = apartmentManager;
        this.doorsFile = new File(plugin.getDataFolder(), "apartment_doors.yml");
        loadDoors();
    }

    // ──────── WAND ────────

    /**
     * Creates the wand item used to select door corners.
     */
    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            meta.setLore(Arrays.asList(
                    "§7Clic gauche: §fPosition 1",
                    "§7Clic droit: §fPosition 2",
                    "",
                    "§bPour créer une porte d'appartement"
            ));
            meta.setUnbreakable(true);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    /**
     * Returns true if the given item is the apartment door wand.
     */
    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && WAND_NAME.equals(meta.getDisplayName());
    }

    // ──────── SELECTION ────────

    public void setPos1(Player player, Location loc) {
        pos1Selection.put(player.getUniqueId(), loc.clone());
        player.sendMessage("§b§l[AptDoor] §7Position 1: §f"
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10);
    }

    public void setPos2(Player player, Location loc) {
        pos2Selection.put(player.getUniqueId(), loc.clone());
        player.sendMessage("§b§l[AptDoor] §7Position 2: §f"
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10);
    }

    // ──────── DOOR CRUD ────────

    /**
     * Creates and saves a door config from the player's current selection.
     *
     * @return true if successful
     */
    public boolean createDoor(Player player, int apartmentId) {
        UUID uuid = player.getUniqueId();
        if (!pos1Selection.containsKey(uuid) || !pos2Selection.containsKey(uuid)) {
            player.sendMessage("§c§l[AptDoor] §cSélectionnez d'abord les deux positions avec la hache !");
            return false;
        }

        Location pos1 = pos1Selection.get(uuid);
        Location pos2 = pos2Selection.get(uuid);

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c§l[AptDoor] §cLes deux positions doivent être dans le même monde !");
            return false;
        }

        ApartmentDoorConfig config = new ApartmentDoorConfig(apartmentId, pos1, pos2);
        doorConfigs.put(apartmentId, config);
        config.saveToConfig(doorsConfig, "doors." + apartmentId);
        saveDoors();

        pos1Selection.remove(uuid);
        pos2Selection.remove(uuid);

        player.sendMessage("§a§l[AptDoor] §aPorte créée pour l'appartement §f" + apartmentId
                + " §a(§f" + config.getBlockCount() + " §ablocks)");
        return true;
    }

    /**
     * Deletes the door configuration for an apartment.
     *
     * @return true if a door was found and removed
     */
    public boolean deleteDoor(int apartmentId) {
        if (!doorConfigs.containsKey(apartmentId)) return false;

        if (openedDoors.containsKey(apartmentId)) {
            closeDoor(apartmentId);
        }

        doorConfigs.remove(apartmentId);
        doorsConfig.set("doors." + apartmentId, null);
        saveDoors();
        return true;
    }

    /**
     * Returns the door config for the given apartment, or null.
     */
    public ApartmentDoorConfig getDoorConfig(int apartmentId) {
        return doorConfigs.get(apartmentId);
    }

    /**
     * Returns all configured apartment IDs.
     */
    public Set<Integer> getAllApartmentDoorIds() {
        return new HashSet<>(doorConfigs.keySet());
    }

    // ──────── DOOR OPEN / CLOSE ────────

    /**
     * Physically opens the door (removes blocks) if the player is authorized.
     * The door closes automatically after {@value #DOOR_OPEN_SECONDS} seconds.
     *
     * @param apartmentId the apartment whose door to open
     * @param player      the player requesting access (may be null for admin force-open)
     */
    public void openDoor(int apartmentId, Player player) {
        ApartmentDoorConfig config = doorConfigs.get(apartmentId);
        if (config == null) {
            if (player != null) {
                plugin.getQuantumLogger().warning("No door config for apartment " + apartmentId);
            }
            return;
        }

        // Cancel any existing close task
        BukkitTask existingTask = doorCloseTasks.remove(apartmentId);
        if (existingTask != null) existingTask.cancel();

        // Cancel countdown for previous opener
        if (openedDoors.containsKey(apartmentId) && player != null) {
            BukkitTask prevCountdown = playerCountdownTasks.remove(player.getUniqueId());
            if (prevCountdown != null) prevCountdown.cancel();
        }

        // Remove blocks and save snapshots
        Set<BlockSnapshot> snapshots = new HashSet<>();
        World world = config.getPos1().getWorld();

        for (int x = config.getMinX(); x <= config.getMaxX(); x++) {
            for (int y = config.getMinY(); y <= config.getMaxY(); y++) {
                for (int z = config.getMinZ(); z <= config.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) continue;
                    snapshots.add(new BlockSnapshot(block));
                    block.setType(Material.AIR, false);
                }
            }
        }

        openedDoors.put(apartmentId, snapshots);

        // Player feedback
        if (player != null) {
            player.sendTitle("§a§lPorte ouverte !", "§7Vous avez §e" + DOOR_OPEN_SECONDS + " secondes §7pour entrer.", 10, 60, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 0.8f);
            startCountdown(player, DOOR_OPEN_SECONDS);
        }

        // Schedule auto-close
        BukkitTask closeTask = Bukkit.getScheduler().runTaskLater(plugin,
                () -> closeDoor(apartmentId), DOOR_OPEN_SECONDS * 20L);
        doorCloseTasks.put(apartmentId, closeTask);

        plugin.getQuantumLogger().info("Apartment door opened: apt#" + apartmentId
                + " (" + snapshots.size() + " blocks removed)");
    }

    /**
     * Restores the door blocks.
     */
    public void closeDoor(int apartmentId) {
        Set<BlockSnapshot> snapshots = openedDoors.remove(apartmentId);
        if (snapshots != null) {
            for (BlockSnapshot snap : snapshots) snap.restore();
        }

        BukkitTask task = doorCloseTasks.remove(apartmentId);
        if (task != null) task.cancel();

        plugin.getQuantumLogger().info("Apartment door closed: apt#" + apartmentId);
    }

    /**
     * Returns true if the door for the given apartment is currently open.
     */
    public boolean isDoorOpen(int apartmentId) {
        return openedDoors.containsKey(apartmentId);
    }

    // ──────── ACCESS CHECK ────────

    /**
     * Returns true if the player is allowed to open the apartment door.
     * <ul>
     *   <li>The apartment owner can always open it.</li>
     *   <li>Visitors can always open it (even when locked).</li>
     *   <li>Any other player can open it only if the apartment is not locked.</li>
     * </ul>
     */
    public boolean isAuthorized(Player player, Apartment apt) {
        UUID uuid = player.getUniqueId();
        if (uuid.equals(apt.getOwnerId())) return true;
        if (apartmentManager.isVisitor(apt.getApartmentId(), uuid)) return true;
        return !apt.isLocked();
    }

    // ──────── PERSISTENCE ────────

    private void loadDoors() {
        if (!doorsFile.exists()) {
            try {
                doorsFile.createNewFile();
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create apartment_doors.yml: " + e.getMessage());
                return;
            }
        }

        doorsConfig = YamlConfiguration.loadConfiguration(doorsFile);
        doorConfigs.clear();

        ConfigurationSection doorsSection = doorsConfig.getConfigurationSection("doors");
        if (doorsSection == null) return;

        for (String key : doorsSection.getKeys(false)) {
            try {
                int aptId = Integer.parseInt(key);
                ConfigurationSection section = doorsSection.getConfigurationSection(key);
                if (section == null) continue;

                ApartmentDoorConfig config = ApartmentDoorConfig.fromConfig(aptId, section);
                if (config == null) {
                    plugin.getQuantumLogger().warning("Could not load apartment door: " + key + " (world not loaded?)");
                    continue;
                }
                doorConfigs.put(aptId, config);
            } catch (NumberFormatException e) {
                plugin.getQuantumLogger().warning("Invalid apartment door key: " + key);
            }
        }

        plugin.getQuantumLogger().success("Loaded " + doorConfigs.size() + " apartment door configs");
    }

    private void saveDoors() {
        try {
            doorsConfig.save(doorsFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save apartment_doors.yml: " + e.getMessage());
        }
    }

    // ──────── COUNTDOWN ────────

    private void startCountdown(Player player, int seconds) {
        final UUID playerId = player.getUniqueId();
        final int[] left = {seconds};

        BukkitTask existing = playerCountdownTasks.remove(playerId);
        if (existing != null) existing.cancel();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (left[0] <= 0) return;
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("§b⏱ §7Temps restant pour entrer: §b" + formatCountdown(left[0])));
            }
            left[0]--;
            if (left[0] <= 0) {
                BukkitTask t = playerCountdownTasks.remove(playerId);
                if (t != null) t.cancel();
                Player p2 = Bukkit.getPlayer(playerId);
                if (p2 != null) {
                    p2.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                }
            }
        }, 0L, 20L);

        playerCountdownTasks.put(playerId, task);
    }

    private String formatCountdown(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return mins > 0 ? mins + "m" + String.format("%02d", secs) + "s" : secs + "s";
    }

    // ──────── INNER CLASSES ────────

    /**
     * Stores the block coordinates that define an apartment front door zone.
     */
    public static class ApartmentDoorConfig {
        private final int apartmentId;
        private final Location pos1;
        private final Location pos2;

        public ApartmentDoorConfig(int apartmentId, Location pos1, Location pos2) {
            this.apartmentId = apartmentId;
            this.pos1 = pos1.clone();
            this.pos2 = pos2.clone();
        }

        public int getApartmentId() { return apartmentId; }
        public Location getPos1()   { return pos1.clone(); }
        public Location getPos2()   { return pos2.clone(); }

        public int getMinX() { return Math.min(pos1.getBlockX(), pos2.getBlockX()); }
        public int getMaxX() { return Math.max(pos1.getBlockX(), pos2.getBlockX()); }
        public int getMinY() { return Math.min(pos1.getBlockY(), pos2.getBlockY()); }
        public int getMaxY() { return Math.max(pos1.getBlockY(), pos2.getBlockY()); }
        public int getMinZ() { return Math.min(pos1.getBlockZ(), pos2.getBlockZ()); }
        public int getMaxZ() { return Math.max(pos1.getBlockZ(), pos2.getBlockZ()); }

        public int getBlockCount() {
            return (getMaxX() - getMinX() + 1)
                    * (getMaxY() - getMinY() + 1)
                    * (getMaxZ() - getMinZ() + 1);
        }

        /**
         * Returns the centre of the door bounding box (used for proximity checks).
         */
        public Location getCenter() {
            World world = pos1.getWorld();
            double cx = (getMinX() + getMaxX()) / 2.0 + 0.5;
            double cy = (getMinY() + getMaxY()) / 2.0 + 0.5;
            double cz = (getMinZ() + getMaxZ()) / 2.0 + 0.5;
            return new Location(world, cx, cy, cz);
        }

        public void saveToConfig(FileConfiguration config, String path) {
            config.set(path + ".world", pos1.getWorld().getName());
            config.set(path + ".pos1.x", pos1.getBlockX());
            config.set(path + ".pos1.y", pos1.getBlockY());
            config.set(path + ".pos1.z", pos1.getBlockZ());
            config.set(path + ".pos2.x", pos2.getBlockX());
            config.set(path + ".pos2.y", pos2.getBlockY());
            config.set(path + ".pos2.z", pos2.getBlockZ());
        }

        public static ApartmentDoorConfig fromConfig(int apartmentId, ConfigurationSection section) {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;

            Location p1 = new Location(world,
                    section.getInt("pos1.x"), section.getInt("pos1.y"), section.getInt("pos1.z"));
            Location p2 = new Location(world,
                    section.getInt("pos2.x"), section.getInt("pos2.y"), section.getInt("pos2.z"));
            return new ApartmentDoorConfig(apartmentId, p1, p2);
        }
    }

    private static class BlockSnapshot {
        private final Location location;
        private final org.bukkit.block.data.BlockData blockData;

        public BlockSnapshot(Block block) {
            this.location = block.getLocation();
            this.blockData = block.getBlockData().clone();
        }

        public void restore() {
            location.getBlock().setBlockData(blockData, false);
        }
    }
}
