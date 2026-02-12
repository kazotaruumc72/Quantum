package com.wynvers.quantum.database;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Quantum plugin;
    private Connection connection;

    public DatabaseManager(Quantum plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        FileConfiguration cfg = plugin.getConfig();
        
        // Support both old and new config formats
        ConfigurationSection mysql = cfg.getConfigurationSection("mysql");
        ConfigurationSection database = cfg.getConfigurationSection("database");
        
        // Check new format first
        if (database != null && "mysql".equalsIgnoreCase(database.getString("type"))) {
            ConfigurationSection mysqlConfig = database.getConfigurationSection("mysql");
            if (mysqlConfig != null) {
                connectMySQL(mysqlConfig);
                return;
            }
        }
        
        // Fall back to old format
        if (mysql != null && mysql.getBoolean("enabled", false)) {
            connectMySQL(mysql);
            return;
        }
        
        plugin.getQuantumLogger().warning("MySQL not configured in config.yml");
    }
    
    private void connectMySQL(ConfigurationSection config) {
        String host = config.getString("host");
        int port = config.getInt("port");
        String db = config.getString("database");
        String user = config.getString("username");
        String pass = config.getString("password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&autoReconnect=true&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, pass);
            plugin.getQuantumLogger().success("Connected to MySQL database '" + db + "'");
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to connect to MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    private void createTables() {
        if (connection == null) return;

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS quantum_player_levels (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "level INT NOT NULL DEFAULT 1," +
                    "exp INT NOT NULL DEFAULT 0," +
                    "last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP " +
                    "ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS quantum_tower_progress (" +
                    "uuid CHAR(36) NOT NULL," +
                    "tower_id VARCHAR(64) NOT NULL," +
                    "floor INT NOT NULL DEFAULT 0," +
                    "runs INT NOT NULL DEFAULT 0," +
                    "kills INT NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (uuid, tower_id)" +
                    ")");
            
            // Player storage table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_storage (" +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "material VARCHAR(255) NOT NULL DEFAULT '', " +
                    "nexo_id VARCHAR(255) NOT NULL DEFAULT '', " +
                    "amount INT NOT NULL, " +
                    "PRIMARY KEY (player_uuid, material, nexo_id)" +
                    ")");
            
            // Statistics table (trades par catégorie)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS statistics (" +
                    "category VARCHAR(255) PRIMARY KEY, " +
                    "items_stored BIGINT DEFAULT 0, " +
                    "trades_created BIGINT DEFAULT 0, " +
                    "trades_completed BIGINT DEFAULT 0, " +
                    "volume_traded BIGINT DEFAULT 0" +
                    ")");
            
            // Storage statistics table (tracking global du storage)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS storage_stats (" +
                    "stat_key VARCHAR(255) PRIMARY KEY, " +
                    "stat_value BIGINT DEFAULT 0" +
                    ")");
            
            // Initialiser les stats de storage si elles n'existent pas
            st.executeUpdate("INSERT IGNORE INTO storage_stats (stat_key, stat_value) " +
                    "VALUES ('total_items_stored', 0)");
            st.executeUpdate("INSERT IGNORE INTO storage_stats (stat_key, stat_value) " +
                    "VALUES ('total_items_sold', 0)");
            
            // Economy balances table for Quantum Economy
            st.executeUpdate("CREATE TABLE IF NOT EXISTS quantum_player_balances (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "balance DOUBLE NOT NULL DEFAULT 0.0," +
                    "last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP " +
                    "ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            
            plugin.getQuantumLogger().success("✓ Database tables verified");
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to create MySQL tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }
}
