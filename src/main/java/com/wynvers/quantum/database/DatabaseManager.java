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
        ConfigurationSection mysql = cfg.getConfigurationSection("mysql");
        if (mysql == null || !mysql.getBoolean("enabled", false)) {
            plugin.getQuantumLogger().warning("MySQL disabled in config.yml");
            return;
        }

        String host = mysql.getString("host");
        int port = mysql.getInt("port");
        String db = mysql.getString("database");
        String user = mysql.getString("username");
        String pass = mysql.getString("password");

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
