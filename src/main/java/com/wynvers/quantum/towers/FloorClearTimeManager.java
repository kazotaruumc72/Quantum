package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages floor clear time records (best time per player per tower per floor).
 * Provides a leaderboard (podium) of the top 30 fastest players for each room.
 *
 * <p>Placeholders:
 * <ul>
 *   <li>{@code %quantum_floor_top_<towerId>_<floor>_name_<pos>%} – player name at position</li>
 *   <li>{@code %quantum_floor_top_<towerId>_<floor>_time_<pos>%} – formatted time at position</li>
 * </ul>
 */
public class FloorClearTimeManager {

    private final Quantum plugin;

    /** Cache: "towerId:floor" → sorted list of entries (ascending by time). */
    private final Map<String, List<LeaderboardEntry>> leaderboardCache = new ConcurrentHashMap<>();

    private static final int MAX_LEADERBOARD_SIZE = 30;

    public FloorClearTimeManager(Quantum plugin) {
        this.plugin = plugin;
        loadAll();
    }

    // ------------------------------------------------------------------
    // Data class
    // ------------------------------------------------------------------

    public record LeaderboardEntry(UUID uuid, String playerName, long clearTimeMs) {}

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Record a floor clear time. Only keeps the personal best per player.
     * If the new time is better than the previous record, it updates the DB and cache.
     *
     * @param uuid     player UUID
     * @param name     player display name
     * @param towerId  tower identifier
     * @param floor    floor number
     * @param timeMs   clear time in milliseconds
     */
    public void recordClearTime(UUID uuid, String name, String towerId, int floor, long timeMs) {
        if (timeMs <= 0) return;

        String cacheKey = cacheKey(towerId, floor);

        // Check if this beats the player's current record
        List<LeaderboardEntry> board = leaderboardCache.computeIfAbsent(cacheKey, k -> new ArrayList<>());
        synchronized (board) {
            LeaderboardEntry existing = null;
            for (LeaderboardEntry e : board) {
                if (e.uuid().equals(uuid)) {
                    existing = e;
                    break;
                }
            }

            if (existing != null && existing.clearTimeMs() <= timeMs) {
                // Not a personal best – nothing to do
                return;
            }

            // Remove old entry if present
            if (existing != null) {
                board.remove(existing);
            }

            // Insert new entry and re-sort
            board.add(new LeaderboardEntry(uuid, name, timeMs));
            board.sort(Comparator.comparingLong(LeaderboardEntry::clearTimeMs));

            // Trim to max size
            while (board.size() > MAX_LEADERBOARD_SIZE) {
                board.remove(board.size() - 1);
            }
        }

        // Persist asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveRecord(uuid, name, towerId, floor, timeMs));
    }

    /**
     * Get the leaderboard for a specific tower floor.
     *
     * @param towerId tower identifier
     * @param floor   floor number
     * @param limit   max entries (capped at {@value MAX_LEADERBOARD_SIZE})
     * @return unmodifiable sorted list (fastest first)
     */
    public List<LeaderboardEntry> getTopPlayers(String towerId, int floor, int limit) {
        String key = cacheKey(towerId, floor);
        List<LeaderboardEntry> board = leaderboardCache.get(key);
        if (board == null || board.isEmpty()) return Collections.emptyList();

        int size = Math.min(limit, board.size());
        synchronized (board) {
            return List.copyOf(board.subList(0, size));
        }
    }

    /**
     * Format a time in milliseconds to a human-readable string (e.g. "1m 23s 456ms" or "45s 120ms").
     */
    public static String formatTime(long ms) {
        if (ms < 0) return "N/A";
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis = ms % 1000;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s " + millis + "ms";
        }
        return seconds + "s " + millis + "ms";
    }

    // ------------------------------------------------------------------
    // Database persistence
    // ------------------------------------------------------------------

    private void loadAll() {
        try {
            Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn == null) return;

            String sql = "SELECT uuid, tower_id, floor, clear_time_ms, player_name FROM quantum_floor_clear_times ORDER BY clear_time_ms ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String towerId = rs.getString("tower_id");
                    int floor = rs.getInt("floor");
                    long timeMs = rs.getLong("clear_time_ms");
                    String playerName = rs.getString("player_name");
                    if (playerName == null || playerName.isEmpty()) playerName = "Unknown";

                    String key = cacheKey(towerId, floor);
                    leaderboardCache.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new LeaderboardEntry(uuid, playerName, timeMs));
                }
            }

            // Sort and trim each board
            for (List<LeaderboardEntry> board : leaderboardCache.values()) {
                board.sort(Comparator.comparingLong(LeaderboardEntry::clearTimeMs));
                while (board.size() > MAX_LEADERBOARD_SIZE) {
                    board.remove(board.size() - 1);
                }
            }

            plugin.getQuantumLogger().success("✓ Floor clear time leaderboards loaded (" + leaderboardCache.size() + " boards)");
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load floor clear times: " + e.getMessage());
        }
    }

    private void saveRecord(UUID uuid, String playerName, String towerId, int floor, long timeMs) {
        try {
            Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn == null) return;

            // INSERT or UPDATE only if new time is better
            String sql = "INSERT INTO quantum_floor_clear_times (uuid, tower_id, floor, clear_time_ms, player_name) "
                    + "VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE clear_time_ms = LEAST(clear_time_ms, VALUES(clear_time_ms)), "
                    + "player_name = IF(VALUES(clear_time_ms) < clear_time_ms, VALUES(player_name), player_name)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, towerId);
                ps.setInt(3, floor);
                ps.setLong(4, timeMs);
                ps.setString(5, playerName);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to save floor clear time: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String cacheKey(String towerId, int floor) {
        return towerId + ":" + floor;
    }
}
