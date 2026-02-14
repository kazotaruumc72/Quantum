package com.wynvers.quantum.apartment;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manager for the apartment system.
 * Handles creation, contracts, furniture, and title display.
 */
public class ApartmentManager {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Quantum plugin;
    private final DatabaseManager databaseManager;
    private final Map<Integer, Apartment> apartments;
    private final Map<UUID, Integer> playerApartments; // UUID -> apartment ID

    public ApartmentManager(Quantum plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.apartments = new HashMap<>();
        this.playerApartments = new HashMap<>();

        createTables();
        loadApartments();
        startContractCheckTask();
        plugin.getLogger().info("✓ Apartment system initialized (" + apartments.size() + " apartments loaded)");
    }

    // ──────── DATABASE TABLES ────────

    private void createTables() {
        String apartmentsTable = """
                CREATE TABLE IF NOT EXISTS quantum_apartments (
                    apartment_id INT AUTO_INCREMENT PRIMARY KEY,
                    owner_uuid VARCHAR(36),
                    apartment_name VARCHAR(64) NOT NULL,
                    world_name VARCHAR(64),
                    x DOUBLE,
                    y DOUBLE,
                    z DOUBLE,
                    yaw FLOAT,
                    pitch FLOAT,
                    tier INT DEFAULT 1,
                    is_locked BOOLEAN DEFAULT FALSE,
                    size VARCHAR(16) DEFAULT 'SMALL',
                    zone_name VARCHAR(64) DEFAULT '',
                    contract_deadline DATETIME NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_owner (owner_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String visitorsTable = """
                CREATE TABLE IF NOT EXISTS quantum_apartment_visitors (
                    apartment_id INT NOT NULL,
                    visitor_uuid VARCHAR(36) NOT NULL,
                    can_edit BOOLEAN DEFAULT FALSE,
                    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (apartment_id, visitor_uuid),
                    FOREIGN KEY (apartment_id) REFERENCES quantum_apartments(apartment_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String furnitureTable = """
                CREATE TABLE IF NOT EXISTS quantum_apartment_furniture (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    apartment_id INT NOT NULL,
                    furniture_id VARCHAR(128) NOT NULL,
                    placed BOOLEAN DEFAULT FALSE,
                    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (apartment_id) REFERENCES quantum_apartments(apartment_id) ON DELETE CASCADE,
                    INDEX idx_apartment (apartment_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(apartmentsTable);
            stmt.execute(visitorsTable);
            stmt.execute(furnitureTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create apartment tables: " + e.getMessage());
        }
    }

    // ──────── LOADING ────────

    private void loadApartments() {
        String sql = "SELECT * FROM quantum_apartments";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("apartment_id");
                String ownerStr = rs.getString("owner_uuid");
                UUID ownerId = ownerStr != null && !ownerStr.isEmpty() ? UUID.fromString(ownerStr) : null;
                String name = rs.getString("apartment_name");

                Apartment apt = new Apartment(id, ownerId, name);
                apt.setLocation(
                        rs.getString("world_name"),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")
                );
                apt.setTier(rs.getInt("tier"));
                apt.setLocked(rs.getBoolean("is_locked"));

                String sizeStr = rs.getString("size");
                try {
                    apt.setSize(Apartment.Size.valueOf(sizeStr));
                } catch (Exception e) {
                    apt.setSize(Apartment.Size.SMALL);
                }

                apt.setZoneName(rs.getString("zone_name"));

                Timestamp deadline = rs.getTimestamp("contract_deadline");
                if (deadline != null) {
                    apt.setContractDeadline(deadline.toLocalDateTime());
                }

                apartments.put(id, apt);
                if (ownerId != null) {
                    playerApartments.put(ownerId, id);
                }
            }

            // Load furniture for each apartment
            loadFurniture();

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load apartments: " + e.getMessage());
        }
    }

    private void loadFurniture() {
        String sql = "SELECT apartment_id, furniture_id FROM quantum_apartment_furniture";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int aptId = rs.getInt("apartment_id");
                String furnitureId = rs.getString("furniture_id");
                Apartment apt = apartments.get(aptId);
                if (apt != null) {
                    apt.addFurniture(furnitureId);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load apartment furniture: " + e.getMessage());
        }
    }

    // ──────── SAVING ────────

    public void saveApartment(Apartment apt) {
        String sql = """
                UPDATE quantum_apartments SET
                    owner_uuid = ?, apartment_name = ?, world_name = ?,
                    x = ?, y = ?, z = ?, yaw = ?, pitch = ?,
                    tier = ?, is_locked = ?, size = ?, zone_name = ?,
                    contract_deadline = ?
                WHERE apartment_id = ?
                """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apt.getOwnerId() != null ? apt.getOwnerId().toString() : null);
            ps.setString(2, apt.getApartmentName());
            ps.setString(3, apt.getWorldName());
            ps.setDouble(4, apt.getX());
            ps.setDouble(5, apt.getY());
            ps.setDouble(6, apt.getZ());
            ps.setFloat(7, apt.getYaw());
            ps.setFloat(8, apt.getPitch());
            ps.setInt(9, apt.getTier());
            ps.setBoolean(10, apt.isLocked());
            ps.setString(11, apt.getSize().name());
            ps.setString(12, apt.getZoneName());
            ps.setTimestamp(13, apt.getContractDeadline() != null
                    ? Timestamp.valueOf(apt.getContractDeadline()) : null);
            ps.setInt(14, apt.getApartmentId());
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save apartment " + apt.getApartmentId() + ": " + e.getMessage());
        }
    }

    // ──────── CRUD ────────

    public Apartment getApartment(int apartmentId) {
        return apartments.get(apartmentId);
    }

    public Apartment getPlayerApartment(UUID playerUuid) {
        Integer apartmentId = playerApartments.get(playerUuid);
        return apartmentId != null ? apartments.get(apartmentId) : null;
    }

    public boolean hasApartment(UUID playerUuid) {
        return playerApartments.containsKey(playerUuid);
    }

    public Apartment createApartment(UUID ownerId, String apartmentName, Apartment.Size size, String zoneName) {
        String sql = """
                INSERT INTO quantum_apartments (owner_uuid, apartment_name, size, zone_name, contract_deadline)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime deadline = LocalDateTime.now().plusDays(30); // 30 days initial
            ps.setString(1, ownerId.toString());
            ps.setString(2, apartmentName);
            ps.setString(3, size.name());
            ps.setString(4, zoneName);
            ps.setTimestamp(5, Timestamp.valueOf(deadline));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    Apartment apt = new Apartment(id, ownerId, apartmentName);
                    apt.setSize(size);
                    apt.setZoneName(zoneName);
                    apt.setContractDeadline(deadline);
                    apartments.put(id, apt);
                    playerApartments.put(ownerId, id);
                    return apt;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create apartment: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteApartment(int apartmentId) {
        Apartment apt = apartments.get(apartmentId);
        if (apt == null) return false;

        String sql = "DELETE FROM quantum_apartments WHERE apartment_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.executeUpdate();

            if (apt.getOwnerId() != null) {
                playerApartments.remove(apt.getOwnerId());
            }
            apartments.remove(apartmentId);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete apartment " + apartmentId + ": " + e.getMessage());
            return false;
        }
    }

    // ──────── VISITORS ────────

    public boolean addVisitor(int apartmentId, UUID visitorUuid, boolean canEdit) {
        String sql = "INSERT INTO quantum_apartment_visitors (apartment_id, visitor_uuid, can_edit) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE can_edit = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, visitorUuid.toString());
            ps.setBoolean(3, canEdit);
            ps.setBoolean(4, canEdit);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add visitor: " + e.getMessage());
            return false;
        }
    }

    public boolean removeVisitor(int apartmentId, UUID visitorUuid) {
        String sql = "DELETE FROM quantum_apartment_visitors WHERE apartment_id = ? AND visitor_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, visitorUuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove visitor: " + e.getMessage());
            return false;
        }
    }

    // ──────── CONTRACT ────────

    /**
     * Add days to a player's apartment contract
     * @return true if successful
     */
    public boolean addContractDeadline(UUID playerUuid, int days) {
        Apartment apt = getPlayerApartment(playerUuid);
        if (apt == null) return false;

        apt.addContractDays(days);
        saveApartment(apt);
        return true;
    }

    /**
     * Get the formatted contract deadline string
     */
    public String getFormattedDeadline(Apartment apt) {
        if (apt.getContractDeadline() == null) return "Aucun contrat";
        if (apt.isContractExpired()) return "§cExpiré";
        return apt.getContractDeadline().format(DATE_FORMAT);
    }

    // ──────── FURNITURE ────────

    /**
     * Add furniture to a player's apartment
     */
    public boolean addFurnitureToApartment(int apartmentId, String furnitureId) {
        String sql = "INSERT INTO quantum_apartment_furniture (apartment_id, furniture_id) VALUES (?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, furnitureId);
            ps.executeUpdate();

            Apartment apt = apartments.get(apartmentId);
            if (apt != null) {
                apt.addFurniture(furnitureId);
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add furniture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove furniture from an apartment
     */
    public boolean removeFurnitureFromApartment(int apartmentId, String furnitureId) {
        String sql = "DELETE FROM quantum_apartment_furniture WHERE apartment_id = ? AND furniture_id = ? LIMIT 1";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, furnitureId);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                Apartment apt = apartments.get(apartmentId);
                if (apt != null) {
                    apt.removeFurniture(furnitureId);
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove furniture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle contract expiry: store all furniture, clear owner
     */
    private void handleContractExpiry(Apartment apt) {
        plugin.getLogger().info("Contract expired for apartment " + apt.getApartmentId()
                + " owned by " + apt.getOwnerId());

        // Furniture stays in database (stored in personal catalogue)
        // Mark all furniture as not placed
        String sql = "UPDATE quantum_apartment_furniture SET placed = FALSE WHERE apartment_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apt.getApartmentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to store furniture on expiry: " + e.getMessage());
        }

        // Notify player if online
        if (apt.getOwnerId() != null) {
            Player player = Bukkit.getPlayer(apt.getOwnerId());
            if (player != null && player.isOnline()) {
                player.sendMessage("§c§l⚠ Votre contrat d'appartement a expiré !");
                player.sendMessage("§7Votre mobilier a été stocké dans votre catalogue personnel.");
                player.sendMessage("§7Utilisez §f/appart contrat adddeadline §7pour renouveler.");
            }
        }
    }

    // ──────── TITLE DISPLAY ────────

    /**
     * Send title to a player showing apartment owner and deadline
     */
    public void showApartmentTitle(Player player, Apartment apt) {
        if (apt.getOwnerId() == null) return;

        String ownerName = Bukkit.getOfflinePlayer(apt.getOwnerId()).getName();
        if (ownerName == null) ownerName = "Inconnu";

        String title = "§6" + ownerName;
        String subtitle;

        if (apt.isContractExpired()) {
            subtitle = "§cContrat expiré";
        } else if (apt.getContractDeadline() != null) {
            subtitle = "§7Expire: §f" + apt.getContractDeadline().format(DATE_FORMAT);
        } else {
            subtitle = "§7Aucun contrat";
        }

        player.sendTitle(title, subtitle, 10, 60, 20);
    }

    // ──────── SCHEDULED TASKS ────────

    private void startContractCheckTask() {
        // Check contracts every 5 minutes (6000 ticks)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Apartment apt : apartments.values()) {
                if (apt.getOwnerId() != null && apt.isContractExpired()) {
                    Bukkit.getScheduler().runTask(plugin, () -> handleContractExpiry(apt));
                }
            }
        }, 6000L, 6000L);
    }

    // ──────── GETTERS ────────

    public Collection<Apartment> getAllApartments() {
        return new ArrayList<>(apartments.values());
    }

    /**
     * Get the date formatter used for deadlines
     */
    public static DateTimeFormatter getDateFormat() {
        return DATE_FORMAT;
    }
}
