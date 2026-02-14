package com.wynvers.quantum.spawn;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;

/**
 * Manager for spawn locations (spawn and firstspawn)
 */
public class SpawnManager {

    private final Quantum plugin;
    private final DatabaseManager databaseManager;
    private Spawn spawn;
    private Spawn firstSpawn;

    public SpawnManager(Quantum plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        createTable();
        loadSpawns();
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS quantum_spawns (
                    type VARCHAR(32) NOT NULL PRIMARY KEY,
                    world VARCHAR(64) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    yaw FLOAT NOT NULL,
                    pitch FLOAT NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create spawns table: " + e.getMessage());
        }
    }

    /**
     * Load spawn locations from database
     */
    private void loadSpawns() {
        String sql = "SELECT * FROM quantum_spawns WHERE type IN ('spawn', 'firstspawn')";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String type = rs.getString("type");
                Spawn spawn = new Spawn(
                    rs.getString("world"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    rs.getFloat("yaw"),
                    rs.getFloat("pitch")
                );

                if ("spawn".equals(type)) {
                    this.spawn = spawn;
                } else if ("firstspawn".equals(type)) {
                    this.firstSpawn = spawn;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load spawns: " + e.getMessage());
        }
    }

    /**
     * Set the spawn location
     */
    public boolean setSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Spawn newSpawn = new Spawn(location);
        String sql = """
                INSERT INTO quantum_spawns (type, world, x, y, z, yaw, pitch)
                VALUES ('spawn', ?, ?, ?, ?, ?, ?)
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

            stmt.setString(1, newSpawn.getWorld());
            stmt.setDouble(2, newSpawn.getX());
            stmt.setDouble(3, newSpawn.getY());
            stmt.setDouble(4, newSpawn.getZ());
            stmt.setFloat(5, newSpawn.getYaw());
            stmt.setFloat(6, newSpawn.getPitch());
            stmt.executeUpdate();

            this.spawn = newSpawn;
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set spawn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set the first spawn location
     */
    public boolean setFirstSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Spawn newFirstSpawn = new Spawn(location);
        String sql = """
                INSERT INTO quantum_spawns (type, world, x, y, z, yaw, pitch)
                VALUES ('firstspawn', ?, ?, ?, ?, ?, ?)
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

            stmt.setString(1, newFirstSpawn.getWorld());
            stmt.setDouble(2, newFirstSpawn.getX());
            stmt.setDouble(3, newFirstSpawn.getY());
            stmt.setDouble(4, newFirstSpawn.getZ());
            stmt.setFloat(5, newFirstSpawn.getYaw());
            stmt.setFloat(6, newFirstSpawn.getPitch());
            stmt.executeUpdate();

            this.firstSpawn = newFirstSpawn;
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set first spawn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the spawn location
     */
    public Location getSpawn() {
        if (spawn == null) {
            return null;
        }
        return spawn.toLocation();
    }

    /**
     * Get the first spawn location
     */
    public Location getFirstSpawn() {
        if (firstSpawn == null) {
            return null;
        }
        return firstSpawn.toLocation();
    }

    /**
     * Check if spawn is set
     */
    public boolean hasSpawn() {
        return spawn != null;
    }

    /**
     * Check if first spawn is set
     */
    public boolean hasFirstSpawn() {
        return firstSpawn != null;
    }

    /**
     * Get the plugin instance
     */
    public Quantum getPlugin() {
        return plugin;
    }
}
