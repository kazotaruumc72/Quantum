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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Gère l'entrée / sortie des régions WorldGuard liées aux tours.
 *
 * - Détecte quand un joueur entre / sort d'un étage de tour (region WorldGuard)
 * - Vérifie les niveaux min/max de la tour (PlayerLevelManager)
 * - Met à jour TowerManager (currentTower/currentFloor)
 * - Active / désactive le TowerScoreboardHandler
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

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String previousRegion = currentRegion.get(uuid);
        String newRegion = getWorldGuardRegionAt(to);

        if (Objects.equals(previousRegion, newRegion)) {
            return; // pas de changement
        }

        // Debug log
        if (newRegion != null) {
            plugin.getQuantumLogger().info("Player " + player.getName() + " moved to region: " + newRegion);
        }

        // Sortie d'une région de tour
        if (previousRegion != null && (newRegion == null || !isTowerRegion(newRegion))) {
            handleLeaveTower(player);
            currentRegion.remove(uuid);
        }

        // Entrée dans une nouvelle région de tour
        if (newRegion != null && isTowerRegion(newRegion)) {
            boolean ok = handleEnterTowerRegion(player, newRegion, from);
            if (!ok) {
                // Si refus (niveau insuffisant), on le renvoie à l'ancienne position
                event.setTo(from);
                return;
            }
            currentRegion.put(uuid, newRegion);
        }
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
     * Retourne le nom d'une région WorldGuard à cette location
     * (première trouvée), ou null s'il n'y en a pas.
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
            return region.getId(); // on prend la première
        }
        return null;
    }

    /**
     * Vérifie si le nom de région correspond à une région de tour
     * via TowerManager.getTowerByRegion(...)
     */
    private boolean isTowerRegion(String regionName) {
        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);
        boolean isTower = towerId != null && floor > 0;
        
        if (isTower) {
            plugin.getQuantumLogger().info("Region " + regionName + " is tower " + towerId + " floor " + floor);
        }
        
        return isTower;
    }

    /**
     * Gère l'entrée dans une région de tour (avec check niveau)
     * @return true si le joueur est autorisé à entrer
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
            player.sendMessage("§cTu dois être niveau §f" + min + " §cà §f" + max +
                    " §cpour entrer dans §f" + tower.getName() + "§c.");
            return false;
        }

        // OK : enregistrer la position, spawners + scoreboard
        towerManager.updateCurrentLocation(player, towerId, floor);
        if (!scoreboardHandler.hasTowerScoreboard(player)) {
            plugin.getQuantumLogger().info("Enabling tower scoreboard for " + player.getName());
            scoreboardHandler.enableTowerScoreboard(player, towerId);
        }

        player.sendMessage("§aTu entres dans §f" + tower.getName() +
                " §7(Étage §f" + floor + "§7)");
        return true;
    }

    /**
     * Entrée sans check de niveau (bypass)
     */
    private boolean enterWithoutLevelCheck(Player player, String regionName) {
        String towerId = towerManager.getTowerByRegion(regionName);
        int floor = towerManager.getFloorByRegion(regionName);
        if (towerId == null || floor <= 0) return true;

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) return true;

        towerManager.updateCurrentLocation(player, towerId, floor);
        if (!scoreboardHandler.hasTowerScoreboard(player)) {
            plugin.getQuantumLogger().info("Enabling tower scoreboard for " + player.getName() + " (bypass)");
            scoreboardHandler.enableTowerScoreboard(player, towerId);
        }

        player.sendMessage("§e[Bypass] §aTu entres dans §f" + tower.getName() +
                " §7(Étage §f" + floor + "§7)");
        return true;
    }

    /**
     * Gère la sortie de tour (clear location + scoreboard)
     */
    private void handleLeaveTower(Player player) {
        towerManager.clearCurrentLocation(player);
        if (scoreboardHandler.hasTowerScoreboard(player)) {
            plugin.getQuantumLogger().info("Disabling tower scoreboard for " + player.getName());
            scoreboardHandler.disableTowerScoreboard(player);
        }
        player.sendMessage("§7Tu quittes la tour.");
    }
}
