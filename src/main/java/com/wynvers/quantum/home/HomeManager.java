package com.wynvers.quantum.home;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

/**
 * Manager for player home locations
 * Supports permission-based home limits (quantum.home.limit.X)
 */
public class HomeManager {

    private final Quantum plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, List<Home>> homeCache;

    public HomeManager(Quantum plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.homeCache = new HashMap<>();
        createTable();
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS quantum_homes (
                    uuid VARCHAR(36) NOT NULL,
                    name VARCHAR(32) NOT NULL,
                    world VARCHAR(64) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    yaw FLOAT NOT NULL,
                    pitch FLOAT NOT NULL,
                    PRIMARY KEY (uuid, name),
                    INDEX idx_uuid (uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create homes table: " + e.getMessage());
        }
    }

    /**
     * Get the maximum number of homes a player can have based on permissions
     */
    public int getMaxHomes(Player player) {
        if (player.hasPermission("quantum.home.limit.unlimited")) {
            return Integer.MAX_VALUE;
        }

        // Check for quantum.home.limit.X permissions, find highest number
        int maxHomes = 1; // Default to 1 home
        for (int i = 1; i <= 100; i++) {
            if (player.hasPermission("quantum.home.limit." + i)) {
                maxHomes = Math.max(maxHomes, i);
            }
        }

        return maxHomes;
    }

    /**
     * Get all homes for a player
     */
    public List<Home> getHomes(UUID playerUuid) {
        // Check cache first
        if (homeCache.containsKey(playerUuid)) {
            return new ArrayList<>(homeCache.get(playerUuid));
        }

        List<Home> homes = new ArrayList<>();
        String sql = "SELECT * FROM quantum_homes WHERE uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Home home = new Home(
                    playerUuid,
                    rs.getString("name"),
                    rs.getString("world"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    rs.getFloat("yaw"),
                    rs.getFloat("pitch")
                );
                homes.add(home);
            }

            homeCache.put(playerUuid, new ArrayList<>(homes));
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load homes for " + playerUuid + ": " + e.getMessage());
        }

        return homes;
    }

    /**
     * Get a specific home by name
     */
    public Home getHome(UUID playerUuid, String name) {
        return getHomes(playerUuid).stream()
            .filter(h -> h.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Set a home location
     */
    public boolean setHome(Player player, String name, Location location) {
        UUID uuid = player.getUniqueId();
        
        // Validate name
        if (!name.matches("[a-zA-Z0-9_-]+")) {
            return false;
        }

        // Check home limit
        List<Home> existingHomes = getHomes(uuid);
        Home existing = existingHomes.stream()
            .filter(h -> h.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);

        if (existing == null && existingHomes.size() >= getMaxHomes(player)) {
            return false; // Reached limit
        }

        String sql = """
                INSERT INTO quantum_homes (uuid, name, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    world = VALUES(world),
                    x = VALUES(x),
                    y = VALUES(y),
                    z = VALUES(z),
                    yaw = VALUES(yaw),
                    pitch = VALUES(pitch)
                """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name.toLowerCase());
            stmt.setString(3, location.getWorld().getName());
            stmt.setDouble(4, location.getX());
            stmt.setDouble(5, location.getY());
            stmt.setDouble(6, location.getZ());
            stmt.setFloat(7, location.getYaw());
            stmt.setFloat(8, location.getPitch());
            stmt.executeUpdate();

            // Update cache
            invalidateCache(uuid);
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set home for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a home
     */
    public boolean deleteHome(UUID playerUuid, String name) {
        String sql = "DELETE FROM quantum_homes WHERE uuid = ? AND name = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, name.toLowerCase());
            int deleted = stmt.executeUpdate();

            // Update cache
            invalidateCache(playerUuid);
            
            return deleted > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete home: " + e.getMessage());
            return false;
        }
    }

    /**
     * Teleport player to a home
     */
    public boolean teleportToHome(Player player, String name) {
        Home home = getHome(player.getUniqueId(), name);
        if (home == null) {
            return false;
        }

        World world = Bukkit.getWorld(home.getWorldName());
        if (world == null) {
            return false;
        }

        Location location = home.toLocation(world);
        player.teleport(location);
        return true;
    }

    /**
     * Invalidate cache for a player
     */
    public void invalidateCache(UUID playerUuid) {
        homeCache.remove(playerUuid);
    }

    /**
     * Clear all cache
     */
    public void clearCache() {
        homeCache.clear();
    }
}
