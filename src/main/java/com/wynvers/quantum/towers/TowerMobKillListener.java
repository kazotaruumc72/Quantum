package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens for MythicMobs death events inside tower floor regions.
 *
 * <p>When a player kills a MythicMobs mob while in a tower floor:
 * <ol>
 *   <li>The kill count for that floor is incremented in {@link TowerProgress}.</li>
 *   <li>If the total reaches the {@code mob_kills_required} threshold configured in
 *       towers.yml, the corresponding floor door is opened (blocks disappear for 30 s).</li>
 * </ol>
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        // We only care about kills made by a player.
        Entity killer = event.getKiller();
        if (!(killer instanceof Player player)) return;

        TowerProgress progress = towerManager.getProgress(player.getUniqueId());
        String towerId = progress.getCurrentTower();
        int floor = progress.getCurrentFloor();

        if (towerId == null || floor <= 0) return;

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) return;

        int required = tower.getFloorMobKillsRequired(floor);
        if (required <= 0) return; // no kill requirement on this floor

        // Increment and check
        progress.incrementFloorMobKills(towerId, floor);
        int kills = progress.getFloorMobKills(towerId, floor);

        if (kills >= required) {
            plugin.getQuantumLogger().info(
                    "[Tower] " + player.getName() + " reached kill goal on " + towerId
                    + " floor " + floor + " (" + kills + "/" + required + ") – opening door");
            // Reset counter so the door can be triggered again after it closes
            progress.resetFloorMobKills(towerId, floor);
            doorManager.openDoor(towerId, floor, player);
        }
    }
}
