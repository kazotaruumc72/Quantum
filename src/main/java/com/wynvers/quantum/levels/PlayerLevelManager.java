package com.wynvers.quantum.levels;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLevelManager {

    private final Quantum plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerLevelData> cache = new HashMap<>();

    public PlayerLevelManager(Quantum plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public PlayerLevelData getData(UUID uuid) {
        return cache.get(uuid);
    }

    public int getLevel(UUID uuid) {
        PlayerLevelData data = cache.get(uuid);
        return data != null ? data.getLevel() : 1;
    }

    public int getExp(UUID uuid) {
        PlayerLevelData data = cache.get(uuid);
        return data != null ? data.getExp() : 0;
    }

    /**
     * Charge (ou crée) les données dans le cache depuis MySQL.
     * À appeler en ASYNC au join.
     */
    public void loadPlayer(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT level, exp FROM quantum_player_levels WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int level = rs.getInt("level");
                        int exp = rs.getInt("exp");
                        cache.put(uuid, new PlayerLevelData(uuid, level, exp));
                        return;
                    }
                }
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO quantum_player_levels (uuid, level, exp) VALUES (?, ?, ?)")) {
                insert.setString(1, uuid.toString());
                insert.setInt(2, 1);
                insert.setInt(3, 0);
                insert.executeUpdate();
            }

            cache.put(uuid, new PlayerLevelData(uuid, 1, 0));

        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load player level for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applique le niveau/XP sur la barre d'XP vanilla.
     */
    public void applyToBar(Player player) {
        PlayerLevelData data = cache.get(player.getUniqueId());
        if (data == null) return;

        int level = data.getLevel();
        int exp = data.getExp();
        int needed = getRequiredExp(level);

        player.setLevel(level);

        float progress = 0f;
        if (needed > 0) {
            progress = Math.min(1f, (float) exp / (float) needed);
        }
        player.setExp(progress);
    }

    /**
     * Sauvegarde les données en DB (si en cache).
     * À appeler en ASYNC au quit.
     */
    public void savePlayer(UUID uuid) {
        PlayerLevelData data = cache.get(uuid);
        if (data == null) return;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE quantum_player_levels SET level = ?, exp = ? WHERE uuid = ?")) {

            ps.setInt(1, data.getLevel());
            ps.setInt(2, data.getExp());
            ps.setString(3, uuid.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to save player level for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprime le joueur du cache après sauvegarde.
     */
    public void unloadPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Ajoute de l'XP et gère un level-up simple,
     * puis met à jour la barre d'XP si le joueur est en ligne.
     */
    public void addExp(UUID uuid, int amount) {
        PlayerLevelData data = cache.get(uuid);
        if (data == null) return;

        int exp = data.getExp() + amount;
        int level = data.getLevel();

        int needed = getRequiredExp(level);
        while (exp >= needed) {
            exp -= needed;
            level++;
            needed = getRequiredExp(level);
        }

        data.setLevel(level);
        data.setExp(exp);

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> applyToBar(player));
        }
    }

    /**
     * Courbe d'XP : quadratique (arc de cercle).
     */
    private int getRequiredExp(int level) {
        return 25 * level * level + 25 * level;
    }
}
