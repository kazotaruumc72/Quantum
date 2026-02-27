package com.wynvers.quantum.listeners;

import com.wynvers.quantum.apartment.Apartment;
import com.wynvers.quantum.apartment.ApartmentDoorManager;
import com.wynvers.quantum.apartment.ApartmentDoorManager.ApartmentDoorConfig;
import com.wynvers.quantum.apartment.ApartmentManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles two responsibilities for the apartment front-door system:
 * <ol>
 *   <li>Wand clicks (left/right click on blocks) to set selection positions.</li>
 *   <li>Proximity detection: when a player walks within {@value #TRIGGER_RADIUS} blocks of
 *       a door centre the door is opened if they are authorised.</li>
 * </ol>
 */
public class ApartmentDoorListener implements Listener {

    /** Distance (blocks) at which a door triggers for an approaching player. */
    private static final double TRIGGER_RADIUS = 3.0;

    private final ApartmentDoorManager doorManager;
    private final ApartmentManager apartmentManager;

    public ApartmentDoorListener(ApartmentDoorManager doorManager, ApartmentManager apartmentManager) {
        this.doorManager = doorManager;
        this.apartmentManager = apartmentManager;
    }

    // ──────── WAND ────────

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ApartmentDoorManager.isWand(item)) return;

        // Permission check
        if (!player.hasPermission("quantum.apartment.door.admin")) {
            player.sendMessage("§c§l[AptDoor] §cVous n'avez pas la permission !");
            event.setCancelled(true);
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            doorManager.setPos1(player, event.getClickedBlock().getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            doorManager.setPos2(player, event.getClickedBlock().getLocation());
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            player.sendMessage("§b§l[AptDoor] §7Cliquez sur un bloc pour sélectionner !");
        }
    }

    // ──────── PROXIMITY ────────

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Only process block-level changes to reduce overhead
        if (from.getWorld() == to.getWorld()
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        for (int aptId : doorManager.getAllApartmentDoorIds()) {
            ApartmentDoorConfig config = doorManager.getDoorConfig(aptId);
            if (config == null) continue;

            // Only evaluate doors in the same world as the player
            Location center = config.getCenter();
            if (center.getWorld() == null || !center.getWorld().equals(to.getWorld())) continue;

            double distance = to.distance(center);
            if (distance > TRIGGER_RADIUS) continue;

            // Door is already open – nothing to do
            if (doorManager.isDoorOpen(aptId)) continue;

            // Get the apartment
            Apartment apt = apartmentManager.getApartment(aptId);
            if (apt == null) continue;

            // Check authorisation
            if (!doorManager.isAuthorized(player, apt)) {
                if (apt.isLocked()) {
                    player.sendMessage("§c🔒 Cet appartement est verrouillé !");
                }
                continue;
            }

            // Open the door for this player
            doorManager.openDoor(aptId, player);
        }
    }
}
