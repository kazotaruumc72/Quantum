package com.wynvers.quantum.towers;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Zone cuboïde pour limiter le spawn des mobs d'un spawner.
 */
public class SpawnRegion {

    private final String worldName;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public SpawnRegion(String worldName, double x1, double y1, double z1,
                       double x2, double y2, double z2) {
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isInRegion(Location loc) {
        World world = loc.getWorld();
        if (world == null || !world.getName().equals(worldName)) return false;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    public Location getRandomLocation(World world) {
        double x = minX + Math.random() * (maxX - minX);
        double y = minY + Math.random() * (maxY - minY);
        double z = minZ + Math.random() * (maxZ - minZ);
        return new Location(world, x, y, z);
    }
}
