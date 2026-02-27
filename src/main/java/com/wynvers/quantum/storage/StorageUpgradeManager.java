package com.wynvers.quantum.storage.upgrades;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère l'état des upgrades par stockage Quantum.
 * Capacité de base : 200 stacks.
 */
public class StorageUpgradeManager {

    public static final int BASE_STACK_CAPACITY = 200;
    public static final int MAX_PAGES = 5;

    public static class StorageState {
        // Autosell
        public boolean autoSellEnabled;
        public int autoSellLimit;            // 0 = tout vendre

        // Multiplicateur
        public boolean multiplierEnabled;
        public double multiplier = 1.0;

        // Storage+
        public boolean storagePlusEnabled;
        public int extraStacks;

        // Double storage
        public boolean storage1Enabled;
        public boolean storage2Enabled;

        // Nouveaux upgrades
        public int multiplierLevel = 0;  // chaque niveau ajoute x0.5 au multiplicateur de vente
        public int stackLevel = 0;       // chaque niveau ajoute 200 au stack max
        public int pageLevel = 0;        // chaque niveau ajoute 1 page (max MAX_PAGES)
    }

    // Ici on fait simple : 1 storage par joueur => key = UUID du joueur
    private final Map<UUID, StorageState> states = new HashMap<>();
    private final java.util.Set<UUID> loaded = new java.util.HashSet<>();
    private volatile com.wynvers.quantum.Quantum plugin;

    public void setPlugin(com.wynvers.quantum.Quantum plugin) {
        this.plugin = plugin;
    }

    public StorageState getState(Player player) {
        UUID uuid = player.getUniqueId();
        if (!loaded.contains(uuid) && plugin != null) {
            loaded.add(uuid);
            load(uuid, plugin);
        }
        return states.computeIfAbsent(uuid, id -> {
            StorageState s = new StorageState();
            s.autoSellLimit = 0;
            s.multiplier = 1.0;
            return s;
        });
    }

    public StorageState getState(UUID uuid) {
        if (!loaded.contains(uuid) && plugin != null) {
            loaded.add(uuid);
            load(uuid, plugin);
        }
        return states.computeIfAbsent(uuid, id -> {
            StorageState s = new StorageState();
            s.autoSellLimit = 0;
            s.multiplier = 1.0;
            return s;
        });
    }

    // ===== Capacité =====

    public int getMaxStacks(StorageState state) {
        int base = BASE_STACK_CAPACITY + state.extraStacks;

        int factor = 1;
        if (state.storage1Enabled) factor *= 2;
        if (state.storage2Enabled) factor *= 2;

        return base * factor;
    }

    // ===== Permissions =====

    public boolean canEnableAutoSell(Player p) {
        return p.hasPermission("quantum.storage.autosell");
    }

    public boolean canEnableMultiplier(Player p) {
        return p.hasPermission("quantum.storage.multiplier");
    }

    public boolean canUseAutoSellAndMultiplierTogether(Player p) {
        return p.hasPermission("quantum.storage.autosell.multiplier");
    }

    public void toggleAutoSell(Player p) {
        StorageState state = getState(p);
        boolean enabled = !state.autoSellEnabled;

        if (enabled && !canEnableAutoSell(p)) return;

        if (enabled && state.multiplierEnabled && !canUseAutoSellAndMultiplierTogether(p)) {
            p.sendMessage("§cTu ne peux pas activer l'autovente et le multiplicateur en même temps.");
            return;
        }
        state.autoSellEnabled = enabled;
    }

    public void setMultiplierEnabled(Player p, boolean enabled) {
        StorageState state = getState(p);

        if (enabled && !canEnableMultiplier(p)) return;

        if (enabled && state.autoSellEnabled && !canUseAutoSellAndMultiplierTogether(p)) {
            p.sendMessage("§cTu ne peux pas activer l'autovente et le multiplicateur en même temps.");
            return;
        }
        state.multiplierEnabled = enabled;
    }

    // ===== Autosell =====

    public void changeAutoSellLimit(Player p, int delta) {
        StorageState state = getState(p);
        int nw = Math.max(0, state.autoSellLimit + delta);
        state.autoSellLimit = nw;
    }

    /**
     * Exemple: limite=50, current=54 -> vend 4.
     */
    public int computeAutoSellAmount(int currentAmount, int limit) {
        if (limit <= 0) return currentAmount;
        if (currentAmount <= limit) return 0;
        return currentAmount - limit;
    }

    // ===== Nouveaux upgrades =====

    public void upgradeMultiplier(Player player, Quantum plugin) {
        StorageState state = getState(player);
        state.multiplierLevel++;
        save(player.getUniqueId(), plugin);
        player.sendMessage("§a§l✓ §aMultiplicateur de vente amélioré! §eNiveau §f" + state.multiplierLevel
                + " §7(x" + String.format("%.1f", getSellMultiplier(player)) + ")");
    }

    public void upgradeStack(Player player, Quantum plugin) {
        StorageState state = getState(player);
        state.stackLevel++;
        save(player.getUniqueId(), plugin);
        player.sendMessage("§a§l✓ §aStack amélioré! §eNiveau §f" + state.stackLevel
                + " §7(max §f" + getUpgradeStackMax(player) + " §7items)");
    }

    public void upgradePage(Player player, Quantum plugin) {
        StorageState state = getState(player);
        if (1 + state.pageLevel >= MAX_PAGES) {
            player.sendMessage("§c§l✗ §cNombre de pages maximum atteint! §7(max §f" + MAX_PAGES + " §7pages)");
            return;
        }
        state.pageLevel++;
        save(player.getUniqueId(), plugin);
        player.sendMessage("§a§l✓ §aPages améliorées! §eNiveau §f" + state.pageLevel
                + " §7(" + getPages(player) + " pages)");
    }

    /** Multiplicateur de vente (base 1.0, +0.5 par niveau) */
    public double getSellMultiplier(Player player) {
        return 1.0 + getState(player).multiplierLevel * 0.5;
    }

    /** Taille max du stack par upgrade (base 200, +200 par niveau) */
    public int getUpgradeStackMax(Player player) {
        return BASE_STACK_CAPACITY + getState(player).stackLevel * BASE_STACK_CAPACITY;
    }

    /** Nombre de pages (base 1, +1 par niveau, max MAX_PAGES) */
    public int getPages(Player player) {
        return Math.min(1 + getState(player).pageLevel, MAX_PAGES);
    }

    // ===== Persistence =====

    public void load(UUID uuid, Quantum plugin) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String query = "SELECT multiplier_level, stack_level, page_level FROM storage_upgrades WHERE player_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    StorageState state = getState(uuid);
                    state.multiplierLevel = rs.getInt("multiplier_level");
                    state.stackLevel = rs.getInt("stack_level");
                    state.pageLevel = rs.getInt("page_level");
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load storage upgrades for " + uuid + ": " + e.getMessage());
        }
    }

    public void save(UUID uuid, Quantum plugin) {
        StorageState state = states.get(uuid);
        if (state == null) return;
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String upsert = "INSERT INTO storage_upgrades (player_uuid, multiplier_level, stack_level, page_level) "
                    + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                    + "multiplier_level = VALUES(multiplier_level), "
                    + "stack_level = VALUES(stack_level), "
                    + "page_level = VALUES(page_level)";
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, state.multiplierLevel);
                stmt.setInt(3, state.stackLevel);
                stmt.setInt(4, state.pageLevel);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to save storage upgrades for " + uuid + ": " + e.getMessage());
        }
    }
}
