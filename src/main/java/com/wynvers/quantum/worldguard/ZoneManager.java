package com.wynvers.quantum.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Gere l'entree / sortie des regions WorldGuard liees aux tours.
 *
 * - Detecte quand un joueur entre / sort d'un etage de tour (region WorldGuard)
 * - Verifie les niveaux min/max de la tour (PlayerLevelManager)
 * - Met a jour TowerManager (currentTower/currentFloor)
 * - Active / desactive le TowerScoreboardHandler
 */
public class ZoneManager implements Listener {

    private final Quantum plugin;
    private final TowerManager towerManager;
    private final PlayerLevelManager levelManager;
    private final TowerScoreboardHandler scoreboardHandler;

    private final Map<UUID, String> currentRegion = new HashMap<>();
    private static final String BYPASS_PERMISSION = "quantum.tower.bypass";

    public ZoneManager(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
        this.levelManager = plugin.getPlayerLevelManager();
        this.scoreboardHandler = plugin.getTowerScoreboardHandler();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getQuantumLogger().success("ZoneManager (tours) initialized");
        
        // DEBUG: Log initialization
        plugin.getQuantumLogger().info("[DEBUG] ZoneManager created - towerManager: " + (towerManager != null));
        plugin.getQuantumLogger().info("[DEBUG] ZoneManager created - levelManager: " + (levelManager != null));
        plugin.getQuantumLogger().info("[DEBUG] ZoneManager created - scoreboardHandler: " + (scoreboardHandler != null));
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
     * Verifie si le joueur a change de region et gere entree/sortie
     */
    private void checkRegionChange(Player player, Location from, Location to, PlayerMoveEvent moveEvent) {
        UUID uuid = player.getUniqueId();
        String previousRegion = currentRegion.get(uuid);
        String newRegion = getWorldGuardRegionAt(to);
        
        // DEBUG: Log region change check
        plugin.getQuantumLogger().info("[DEBUG] checkRegionChange for " + player.getName() + 
                ": previousRegion=" + previousRegion + ", newRegion=" + newRegion);

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
     * Retourne le nom d'une region WorldGuard a cette location
     * (premiere trouvee), ou null s'il n'y en a pas.
     */
    private String getWorldGuardRegionAt(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        RegionManager regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));

        if (regions == null) return null;

        ApplicableRegionSet set = regions.getApplicableRegions(
                BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
        );

        for (ProtectedRegion region : set) {
            // Retirer ou commenter cette ligne :
            // plugin.getQuantumLogger().info("[DEBUG] Found WorldGuard region: " + region.getId());
            return region.getId();
        }
        return null;
    }

    /**
     * Verifie si le nom de region correspond a une region de tour
     * via TowerManager.getTowerByRegion(...)
     */
    private boolean isTowerRegion(String regionName) {
        if (regionName == null) return false;
        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);
    
        return towerId != null && floor >= 0;  // ← Note: >= 0 au lieu de > 0
    }

    /**
     * Gere l'entree dans une region de tour (avec check niveau)
     * @return true si le joueur est autorise a entrer
     */
    private boolean handleEnterTowerRegion(Player player, String regionName, Location from) {
        // DEBUG: Log enter attempt
        plugin.getQuantumLogger().info("[DEBUG] handleEnterTowerRegion for " + player.getName() + 
                " in region " + regionName);
        plugin.getQuantumLogger().info("[DEBUG] Player has bypass: " + player.hasPermission(BYPASS_PERMISSION));
        
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
