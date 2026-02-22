package com.wynvers.quantum.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.regions.InternalRegionManager;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerInventoryManager;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import com.wynvers.quantum.towers.events.TowerEnterEvent;
import com.wynvers.quantum.towers.events.TowerLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Gere l'entree / sortie des regions liees aux tours.
 * Utilise le systeme de regions interne.
 *
 * - Detecte quand un joueur entre / sort d'un etage de tour (region)
 * - Verifie les niveaux min/max de la tour (PlayerLevelManager)
 * - Met a jour TowerManager (currentTower/currentFloor)
 * - Active / desactive le TowerScoreboardHandler
 */
public class ZoneManager implements Listener {

    private final Quantum plugin;
    private final TowerManager towerManager;
    private final PlayerLevelManager levelManager;
    private final TowerScoreboardHandler scoreboardHandler;
    private final InternalRegionManager regionManager;
    private final TowerInventoryManager towerInventoryManager;

    private final Map<UUID, String> currentRegion = new HashMap<>();
    private static final int REGION_CACHE_SIZE = 100;
    private final Map<String, Boolean> regionIsTowerCache = new LinkedHashMap<String, Boolean>(REGION_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > REGION_CACHE_SIZE;
        }
    };
    private static final String BYPASS_PERMISSION = "quantum.tower.bypass";
    // true when WorldGuard is detected and has not thrown an error yet
    private boolean worldGuardWorking;

    public ZoneManager(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
        this.levelManager = plugin.getPlayerLevelManager();
        this.scoreboardHandler = plugin.getTowerScoreboardHandler();
        this.regionManager = plugin.getInternalRegionManager();
        this.towerInventoryManager = plugin.getTowerInventoryManager();
        this.worldGuardWorking = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        String regionBackend = worldGuardWorking ? "WorldGuard" : "internal region system";
        plugin.getQuantumLogger().success("ZoneManager (tours) initialized - using " + regionBackend);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // On ne traite que les changements de bloc (pour limiter le spam)
        if (from.getWorld() == to.getWorld()
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        checkRegionChange(event.getPlayer(), from, to, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Verifier 1 tick apres la teleportation pour etre sur que le joueur est arrive
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    checkRegionChange(event.getPlayer(), from, to, null);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Verifier 1 tick apres le join pour etre sur que le joueur est spawne
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Location loc = player.getLocation();
                    checkRegionChange(player, loc, loc, null);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (currentRegion.containsKey(uuid)) {
            handleLeaveTower(player);
            currentRegion.remove(uuid);
        }
        // Cleanup tower inventory data
        if (towerInventoryManager != null) {
            towerInventoryManager.cleanup(uuid);
        }
    }

    /**
     * Handle player respawn - if they respawn outside tower, clean up
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Check 1 tick after respawn to see if they're still in a tower
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Location currentLoc = player.getLocation();
                    String regionName = getRegionAt(currentLoc);
                    UUID uuid = player.getUniqueId();
                    String previousRegion = currentRegion.get(uuid);
                    
                    // If they were in a tower but respawned outside, clean up
                    if (previousRegion != null && isTowerRegion(previousRegion) &&
                        (regionName == null || !isTowerRegion(regionName))) {
                        handleLeaveTower(player);
                        currentRegion.remove(uuid);
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Verifie si le joueur a change de region et gere entree/sortie
     */
    private void checkRegionChange(Player player, Location from, Location to, PlayerMoveEvent moveEvent) {
        UUID uuid = player.getUniqueId();
        String previousRegion = currentRegion.get(uuid);
        String newRegion = getRegionAt(to);

        // Pas de changement de region
        if (Objects.equals(previousRegion, newRegion)) {
            return;
        }

        // Determiner si les regions sont des tours
        boolean wasTower = previousRegion != null && isTowerRegion(previousRegion);
        boolean isTower = newRegion != null && isTowerRegion(newRegion);

        // Sortie d'une region de tour
        if (wasTower && !isTower) {
            plugin.getQuantumLogger().info("Player " + player.getName() + " left tower region: " + previousRegion);
            handleLeaveTower(player);
            currentRegion.remove(uuid);
        }

        // Entree dans une nouvelle region de tour
        if (isTower && !wasTower) {
            plugin.getQuantumLogger().info("Player " + player.getName() + " entered tower region: " + newRegion);
            boolean ok = handleEnterTowerRegion(player, newRegion, from);
            if (!ok) {
                // Si refus (niveau insuffisant), on le renvoie a l'ancienne position
                if (moveEvent != null) {
                    moveEvent.setTo(from);
                } else {
                    // Teleportation : on ejecte le joueur
                    player.teleport(from);
                }
                currentRegion.remove(uuid);
                return;
            }
            currentRegion.put(uuid, newRegion);
        }

        // Changement d'etage dans la meme tour
        if (wasTower && isTower && !Objects.equals(previousRegion, newRegion)) {
            plugin.getQuantumLogger().info("Player " + player.getName() + " changed floor: " + 
                    previousRegion + " -> " + newRegion);
            boolean ok = handleEnterTowerRegion(player, newRegion, from);
            if (!ok) {
                if (moveEvent != null) {
                    moveEvent.setTo(from);
                } else {
                    player.teleport(from);
                }
                currentRegion.put(uuid, previousRegion); // garder l'ancien
                return;
            }
            currentRegion.put(uuid, newRegion);
        }
    }

    /**
     * Retourne le nom d'une région de tour a cette location.
     * Uses WorldGuard when available, otherwise falls back to the internal system.
     * @return nom de la région ou null s'il n'y en a pas
     */
    public String getRegionAt(Location loc) {
        if (worldGuardWorking) {
            return getWorldGuardRegionAt(loc);
        }
        return regionManager.getRegionAt(loc);
    }

    /**
     * Query WorldGuard for the highest-priority region that matches a tower floor.
     * Returns null when the location is not inside any configured tower region.
     */
    private String getWorldGuardRegionAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        try {
            RegionManager wgManager =
                    WorldGuard.getInstance().getPlatform().getRegionContainer()
                              .get(BukkitAdapter.adapt(loc.getWorld()));
            if (wgManager == null) return null;

            BlockVector3 pos = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            com.sk89q.worldguard.protection.ApplicableRegionSet regions =
                    wgManager.getApplicableRegions(pos);

            // Iterate regions and return the first one that belongs to a tower floor
            for (ProtectedRegion region : regions) {
                String id = region.getId();
                if (towerManager.getTowerByRegion(id) != null) {
                    return id;
                }
            }
        } catch (Exception e) {
            // WorldGuard threw an unexpected error – switch to internal system and log once
            worldGuardWorking = false;
            plugin.getQuantumLogger().warning("[ZoneManager] WorldGuard lookup failed, switching to internal regions: " + e.getMessage());
            return regionManager.getRegionAt(loc);
        }
        return null;
    }

    /**
     * Verifie si le nom de region correspond a une region de tour
     * via TowerManager.getTowerByRegion(...)
     * Uses caching to avoid repeated lookups
     */
    private boolean isTowerRegion(String regionName) {
        if (regionName == null) return false;
        
        // Check cache first
        Boolean cached = regionIsTowerCache.get(regionName);
        if (cached != null) {
            return cached;
        }
        
        // Not in cache, perform lookup
        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);
        boolean result = towerId != null && floor >= 0;
        
        // Cache the result
        regionIsTowerCache.put(regionName, result);
        
        return result;
    }

    /**
     * Gere l'entree dans une region de tour (avec check niveau)
     * @return true si le joueur est autorise a entrer
     */
    private boolean handleEnterTowerRegion(Player player, String regionName, Location from) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return enterWithoutLevelCheck(player, regionName);
        }

        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);

        if (towerId == null || floor <= 0) return true; // pas une tour

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) return true;

        int level = levelManager.getLevel(player.getUniqueId());
        int min = tower.getMinLevel();
        int max = tower.getMaxLevel();

        if (level < min || level > max) {
            player.sendMessage("\u00a7cTu dois etre niveau \u00a7f" + min +
                    " \u00a7ca \u00a7f" + max +
                    " \u00a7cpour entrer dans \u00a7f" + tower.getName() + "\u00a7c.");
            return false;
        }

        // Fire enter event – allow external plugins to cancel
        TowerEnterEvent enterEvent = new TowerEnterEvent(player, tower, floor);
        plugin.getServer().getPluginManager().callEvent(enterEvent);
        if (enterEvent.isCancelled()) return false;

        // OK : enregistrer la position + scoreboard
        towerManager.updateCurrentLocation(player, towerId, floor);
        // Swap to tower inventory
        if (towerInventoryManager != null && !towerInventoryManager.isInTower(player.getUniqueId())) {
            towerInventoryManager.onEnterTower(player, towerId);
        }
        if (!scoreboardHandler.hasTowerScoreboard(player)) {
            scoreboardHandler.enableTowerScoreboard(player, towerId);
        }
        player.sendMessage("\u00a7aTu entres dans \u00a7f" + tower.getName() +
                " \u00a77(Etage \u00a7f" + floor + "\u00a77)");
        return true;
    }

    /**
     * Entree sans check de niveau (bypass)
     */
    private boolean enterWithoutLevelCheck(Player player, String regionName) {
        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);

        if (towerId == null || floor <= 0) return true;

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) return true;

        // Fire enter event – allow external plugins to cancel even for bypass
        TowerEnterEvent enterEvent = new TowerEnterEvent(player, tower, floor);
        plugin.getServer().getPluginManager().callEvent(enterEvent);
        if (enterEvent.isCancelled()) return false;

        towerManager.updateCurrentLocation(player, towerId, floor);
        if (towerInventoryManager != null && !towerInventoryManager.isInTower(player.getUniqueId())) {
            towerInventoryManager.onEnterTower(player, towerId);
        }
        if (!scoreboardHandler.hasTowerScoreboard(player)) {
            scoreboardHandler.enableTowerScoreboard(player, towerId);
        }
        player.sendMessage("\u00a7e[Bypass] \u00a7aTu entres dans \u00a7f" + tower.getName() +
                " \u00a77(Etage \u00a7f" + floor + "\u00a77)");
        return true;
    }

    /**
     * Gere la sortie de tour (clear location + scoreboard)
     */
    private void handleLeaveTower(Player player) {
        // Restore main inventory and unequip dungeon armor
        String regionName = currentRegion.get(player.getUniqueId());
        String towerId = regionName != null ? towerManager.getTowerByRegion(regionName) : null;
        int floor = regionName != null ? towerManager.getFloorByRegion(regionName) : 0;

        if (towerInventoryManager != null) {
            towerInventoryManager.onLeaveTower(player, towerId);
        }
        towerManager.clearCurrentLocation(player);
        if (scoreboardHandler.hasTowerScoreboard(player)) {
            scoreboardHandler.disableTowerScoreboard(player);
        }
        player.sendMessage("\u00a77Tu quittes la tour.");

        // Fire leave event after cleanup so listeners see clean state
        if (towerId != null) {
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower != null) {
                TowerLeaveEvent leaveEvent = new TowerLeaveEvent(player, tower, floor);
                plugin.getServer().getPluginManager().callEvent(leaveEvent);
            }
        }
    }
}
