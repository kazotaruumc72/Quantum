package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    
    private final Quantum plugin;
    private Connection connection;
    private final String type;
    
    public DatabaseManager(Quantum plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.type = config.getString("database.type", "sqlite");
        
        connect();
        createTables();
    }
    
    /**
     * Connect to database
     */
    private void connect() {
        try {
            if (type.equalsIgnoreCase("mysql")) {
                connectMySQL();
            } else {
                connectSQLite();
            }
            plugin.getQuantumLogger().success("✓ Connected to " + type.toUpperCase() + " database");
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Connect to MySQL
     */
    private void connectMySQL() throws SQLException {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.mysql.host");
        int port = config.getInt("database.mysql.port");
        String database = config.getString("database.mysql.database");
        String username = config.getString("database.mysql.username");
        String password = config.getString("database.mysql.password");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        connection = DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Connect to SQLite
     */
    private void connectSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String filename = plugin.getConfig().getString("database.sqlite.file", "quantum.db");
        File dbFile = new File(dataFolder, filename);
        
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }
    
    /**
     * Create necessary tables
     */
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Player storage table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_storage (" +
                "player_uuid TEXT NOT NULL, " +
                "material TEXT, " +
                "nexo_id TEXT, " +
                "amount INTEGER NOT NULL, " +
                "PRIMARY KEY (player_uuid, material, nexo_id))"
            );
            
            plugin.getQuantumLogger().success("✓ Database tables verified");
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to create tables: " + e.getMessage());
        }
    }
    
    /**
     * Get connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Connection check failed: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Close connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getQuantumLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to close database: " + e.getMessage());
        }
    }
}
