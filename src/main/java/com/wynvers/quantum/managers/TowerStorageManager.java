package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.storage.PlayerTowerStorage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TowerStorageManager {

    private final Quantum plugin;
    private final Map<UUID, PlayerTowerStorage> storages;

    public TowerStorageManager(Quantum plugin) {
        this.plugin = plugin;
        this.storages = new ConcurrentHashMap<>();
    }

    /**
     * Get player tower storage (creates if doesn't exist)
     */
    public PlayerTowerStorage getStorage(Player player) {
        return storages.computeIfAbsent(player.getUniqueId(), uuid -> {
            PlayerTowerStorage storage = new PlayerTowerStorage(uuid);
            storage.load(plugin);
            return storage;
        });
    }

    /**
     * Get tower storage by UUID
     */
    public PlayerTowerStorage getStorage(UUID uuid) {
        return storages.computeIfAbsent(uuid, uuid2 -> {
            PlayerTowerStorage storage = new PlayerTowerStorage(uuid2);
            storage.load(plugin);
            return storage;
        });
    }

    /**
     * Alias for getStorage(UUID) for compatibility
     */
    public PlayerTowerStorage getPlayerStorage(UUID uuid) {
        return getStorage(uuid);
    }

    /**
     * Save specific tower storage
     */
    public void saveStorage(UUID uuid) {
        save(uuid);
    }

    public void save(UUID uuid) {
        PlayerTowerStorage storage = storages.get(uuid);
        if (storage != null) {
            storage.save(plugin);
        }
    }

    /**
     * Save all tower storages
     */
    public void saveAll() {
        plugin.getQuantumLogger().info("Saving all player tower storages...");
        int saved = 0;

        for (PlayerTowerStorage storage : storages.values()) {
            storage.save(plugin);
            saved++;
        }

        plugin.getQuantumLogger().success("Saved " + saved + " player tower storages!");
    }

    /**
     * Unload player tower storage (saves first)
     */
    public void unload(UUID uuid) {
        save(uuid);
        storages.remove(uuid);
    }

    /**
     * Reload all tower storages
     */
    public void reload() {
        saveAll();
        storages.clear();
    }
}
