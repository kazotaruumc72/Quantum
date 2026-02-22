package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

/**
 * Listens for mob death events (MythicMobs and vanilla) inside tower floor regions.
 *
 * <p>Config format in towers.yml:
 * <pre>
 * mob_kills_required:
 *   - 'mm:SkeletonKing:5'   # MythicMobs mob
 *   - 'zombie:3'             # vanilla Minecraft mob
 * </pre>
 *
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
        Entity killer = event.getKiller();
        if (!(killer instanceof Player player)) return;

        String mythicId = event.getMobType().getInternalName();
        String mobKey = "mm:" + mythicId;

        handleKill(player, mobKey, event.getEntity().getLocation());
    }

    // ------------------------------------------------------------------ //
    // Vanilla Minecraft kills
    // ------------------------------------------------------------------ //

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        // Skip MythicMobs mobs to avoid double-counting
        try {
            if (MythicBukkit.inst().getMobManager().isActiveMob(entity.getUniqueId())) return;
        } catch (NoClassDefFoundError | NullPointerException ignored) {
            // MythicBukkit not available at runtime – treat as vanilla
        }

        String mobKey = "mc:" + entity.getType().name();
        handleKill(killer, mobKey, entity.getLocation());
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
            if (!floorRegion.equalsIgnoreCase(entityRegion)) return;
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

        // Increment this mob's counter
        progress.incrementFloorMobKills(towerId, floor, mobKey);

        // Immediately refresh the scoreboard to show the updated kill count
        TowerScoreboardHandler scoreboardHandler = plugin.getTowerScoreboardHandler();
        if (scoreboardHandler != null) {
            scoreboardHandler.forceUpdate(player);
        }

        // Check whether ALL requirements are now satisfied
        if (areAllRequirementsMet(progress, towerId, floor, requirements)) {
            plugin.getQuantumLogger().info(
                    "[Tower] " + player.getName() + " met all kill requirements on "
                    + towerId + " floor " + floor + " - opening door");
            progress.resetFloorMobKills(towerId, floor);
            doorManager.openDoor(towerId, floor, player);
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
