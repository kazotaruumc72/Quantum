package com.wynvers.quantum.worldguard;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.regions.InternalRegionManager;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
 * Supporte a la fois WorldGuard (si disponible) et le systeme de regions interne.
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
    private final boolean useWorldGuard;
    
    // Cache reflection classes for WorldGuard to avoid repeated lookups
    private static Class<?> worldGuardClass;
    private static Class<?> bukkitAdapterClass;
    private static Class<?> blockVector3Class;
    
    static {
        try {
            worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
        } catch (ClassNotFoundException e) {
            // WorldGuard not available, reflection classes will be null
        }
    }

    private final Map<UUID, String> currentRegion = new HashMap<>();
    private static final int REGION_CACHE_SIZE = 100;
    private final Map<String, Boolean> regionIsTowerCache = new LinkedHashMap<String, Boolean>(REGION_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > REGION_CACHE_SIZE;
        }
    };
    private static final String BYPASS_PERMISSION = "quantum.tower.bypass";

    public ZoneManager(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
        this.levelManager = plugin.getPlayerLevelManager();
        this.scoreboardHandler = plugin.getTowerScoreboardHandler();
        this.regionManager = plugin.getInternalRegionManager();
        
        // Check if WorldGuard is available
        this.useWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
        
        if (useWorldGuard) {
            plugin.getQuantumLogger().info("WorldGuard detected - using WorldGuard for region detection");
        } else {
            plugin.getQuantumLogger().info("WorldGuard not found - using internal region system");
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getQuantumLogger().success("ZoneManager (tours) initialized");
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
    }

    /**
     * Handle player death in tower - reset kill counter
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        // If player is in a tower when they die, reset their kill counter
        if (currentRegion.containsKey(uuid)) {
            String regionName = currentRegion.get(uuid);
            if (isTowerRegion(regionName)) {
                // Reset kill counter
                towerManager.getProgress(uuid).resetKills();
                plugin.getQuantumLogger().info("Player " + player.getName() + " died in tower - kill counter reset");
            }
        }
    }

    /**
     * Handle player respawn - if they respawn outside tower, clean up
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location respawnLoc = event.getRespawnLocation();
        
        // Check 1 tick after respawn to see if they're still in a tower
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    Location currentLoc = player.getLocation();
                    String regionName = getWorldGuardRegionAt(currentLoc);
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
        String newRegion = getWorldGuardRegionAt(to);

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
     * Retourne le nom d'une region a cette location.
     * Utilise WorldGuard si disponible, sinon le systeme interne.
     * @return nom de la region ou null s'il n'y en a pas
     */
    private String getWorldGuardRegionAt(Location loc) {
        if (useWorldGuard) {
            return getWorldGuardRegionAtWG(loc);
        } else {
            return regionManager.getRegionAt(loc);
        }
    }
    
    /**
     * Retourne le nom d'une region WorldGuard a cette location
     * (premiere trouvee), ou null s'il n'y en a pas.
     */
    private String getWorldGuardRegionAtWG(Location loc) {
        try {
            // Check if reflection classes are available
            if (worldGuardClass == null || bukkitAdapterClass == null || blockVector3Class == null) {
                return null;
            }
            
            World world = loc.getWorld();
            if (world == null) return null;

            Object worldGuardInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuardClass.getMethod("getPlatform").invoke(worldGuardInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            
            Object adaptedWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);
            Object regionManager = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World")).invoke(regionContainer, adaptedWorld);
            
            if (regionManager == null) return null;
            
            Object blockVector = blockVector3Class.getMethod("at", int.class, int.class, int.class)
                .invoke(null, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            
            Object applicableRegionSet = regionManager.getClass()
                .getMethod("getApplicableRegions", blockVector3Class)
                .invoke(regionManager, blockVector);
            
            Iterable<?> regions = (Iterable<?>) applicableRegionSet;
            for (Object region : regions) {
                return (String) region.getClass().getMethod("getId").invoke(region);
            }
            
            return null;
        } catch (Exception e) {
            // WorldGuard not available or reflection failed
            plugin.getQuantumLogger().warning(
                "Failed to query WorldGuard region at location " + 
                loc.getWorld().getName() + " " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + 
                ": " + e.getClass().getSimpleName() + " - " + e.getMessage()
            );
            plugin.getQuantumLogger().error("Stack trace:", e);
            return null;
        }
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
        boolean result = towerId != null && floor >= 0;  // ← Note: >= 0 au lieu de > 0
        
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

        // OK : enregistrer la position, spawners + scoreboard
        towerManager.updateCurrentLocation(player, towerId, floor);
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

        towerManager.updateCurrentLocation(player, towerId, floor);
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
        towerManager.clearCurrentLocation(player);
        if (scoreboardHandler.hasTowerScoreboard(player)) {
            scoreboardHandler.disableTowerScoreboard(player);
        }
        player.sendMessage("\u00a77Tu quittes la tour.");
    }
}
