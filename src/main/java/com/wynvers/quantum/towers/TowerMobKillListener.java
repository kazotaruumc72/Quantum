package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

/**
 * Listens for MythicMobs death events inside tower floor regions.
 *
 * <p>Config format in towers.yml:
 * <pre>
 * mob_kills_required:
 *   - 'mm:SkeletonKing:5'   # MythicMobs mob
 * </pre>
 *
 * <p>Only MythicMobs count for tower progression. Vanilla Minecraft mobs are ignored.
 * <p>When all requirements for the current floor are met the door opens (blocks
 * disappear for 1m30s via {@link TowerDoorManager}).
 */
public class TowerMobKillListener implements Listener {

    private final Quantum plugin;
    private final TowerManager towerManager;
    private final TowerDoorManager doorManager;

    public TowerMobKillListener(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
        this.doorManager = plugin.getDoorManager();
    }

    // ------------------------------------------------------------------ //
    // MythicMobs kills
    // ------------------------------------------------------------------ //

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Player player = resolvePlayer(event.getKiller());
        if (player == null) return;

        String mythicId = event.getMobType().getInternalName();
        String mobKey = "mm:" + mythicId;

        handleKill(player, mobKey, event.getEntity().getLocation());
    }

    /**
     * Resolves the player responsible for a kill.
     * Handles both direct kills (killer is a Player) and indirect kills
     * via projectiles (bow, trident, etc.) where the killer entity is a
     * Projectile whose shooter is the player.
     *
     * @param killer the entity returned by the death event (may be null)
     * @return the responsible Player, or null if no player was involved
     */
    private Player resolvePlayer(Entity killer) {
        if (killer instanceof Player player) return player;
        if (killer instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) return player;
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    // Shared logic
    // ------------------------------------------------------------------ //

    private void handleKill(Player player, String mobKey, Location entityLoc) {
        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String towerId = progress.getCurrentTower();
        int floor = progress.getCurrentFloor();

        if (towerId == null || floor <= 0) return;

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) return;

        // Verify the mob died within the floor's configured region (if one is set)
        String floorRegion = tower.getFloorRegion(floor);
        if (floorRegion != null && entityLoc != null) {
            com.wynvers.quantum.worldguard.ZoneManager zoneMan = plugin.getZoneManager();
            String entityRegion = zoneMan != null ? zoneMan.getRegionAt(entityLoc) : null;
            if (entityRegion != null && !floorRegion.equalsIgnoreCase(entityRegion)) return;
            if (entityRegion == null) {
                plugin.getQuantumLogger().debug(
                        "[Tower] Could not determine region for entity death at " + entityLoc
                        + " - counting kill anyway (expected floor region: " + floorRegion + ")");
            }
        }

        List<FloorMobRequirement> requirements = tower.getFloorMobRequirements(floor);
        if (requirements.isEmpty()) return;

        // Check if this mob key is part of any requirement on this floor
        boolean relevant = false;
        for (FloorMobRequirement req : requirements) {
            if (req.getKey().equals(mobKey)) {
                relevant = true;
                break;
            }
        }
        if (!relevant) return;

        // Start the floor timer on the first relevant kill
        if (progress.getFloorElapsedTime(towerId, floor) < 0) {
            progress.startFloorTimer(towerId, floor);
        }

        // Increment this mob's counter
        progress.incrementFloorMobKills(towerId, floor, mobKey);

        // Check whether ALL requirements are now satisfied
        if (areAllRequirementsMet(progress, towerId, floor, requirements)) {
            // Record clear time for the leaderboard
            long elapsed = progress.getFloorElapsedTime(towerId, floor);
            if (elapsed > 0 && plugin.getFloorClearTimeManager() != null) {
                plugin.getFloorClearTimeManager().recordClearTime(
                        player.getUniqueId(), player.getName(), towerId, floor, elapsed);
            }
            progress.clearFloorTimer(towerId, floor);

            plugin.getQuantumLogger().info(
                    "[Tower] " + player.getName() + " met all kill requirements on "
                    + towerId + " floor " + floor + " - opening door");
            progress.resetFloorMobKills(towerId, floor);
            doorManager.openDoor(towerId, floor, player);

            // Open the reward-selection menu for the killer
            plugin.getFloorRewardMenuManager().openForPlayer(player, towerId, floor);
        }
    }

    private boolean areAllRequirementsMet(TowerProgress progress, String towerId, int floor,
                                           List<FloorMobRequirement> requirements) {
        for (FloorMobRequirement req : requirements) {
            if (progress.getFloorMobKills(towerId, floor, req.getKey()) < req.getAmount()) {
                return false;
            }
        }
        return true;
    }
}
