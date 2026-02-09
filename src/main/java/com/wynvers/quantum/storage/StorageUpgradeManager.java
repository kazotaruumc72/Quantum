package com.wynvers.quantum.storage.upgrades;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère l'état des upgrades par stockage Quantum.
 * Capacité de base : 200 stacks.
 */
public class StorageUpgradeManager {

    public static final int BASE_STACK_CAPACITY = 200;

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
    }

    // Ici on fait simple : 1 storage par joueur => key = UUID du joueur
    private final Map<UUID, StorageState> states = new HashMap<>();

    public StorageState getState(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), id -> {
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
}
