package com.wynvers.quantum.towers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stocke les sélections temporaires de zones de spawn faites avec la hache en netherite.
 */
public class SpawnSelectionManager {

    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public void setPos1(UUID uuid, Location loc) {
        pos1.put(uuid, loc.clone());
    }

    public void setPos2(UUID uuid, Location loc) {
        pos2.put(uuid, loc.clone());
    }

    public Location getPos1(UUID uuid) {
        return pos1.get(uuid);
    }

    public Location getPos2(UUID uuid) {
        return pos2.get(uuid);
    }

    public SpawnRegion toRegion(UUID uuid) {
        Location l1 = pos1.get(uuid);
        Location l2 = pos2.get(uuid);
        if (l1 == null || l2 == null) return null;
        if (l1.getWorld() == null || l2.getWorld() == null) return null;
        if (!l1.getWorld().getName().equals(l2.getWorld().getName())) return null;
        return new SpawnRegion(l1.getWorld().getName(),
                l1.getX(), l1.getY(), l1.getZ(),
                l2.getX(), l2.getY(), l2.getZ());
    }
}
