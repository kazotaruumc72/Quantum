package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
    
    private final Quantum plugin;
    private final Map<UUID, PlayerStorage> storages;
    
    public StorageManager(Quantum plugin) {
        this.plugin = plugin;
        this.storages = new ConcurrentHashMap<>();
    }
    
    /**
     * Get player storage (creates if doesn't exist)
     */
    public PlayerStorage getStorage(Player player) {
        return storages.computeIfAbsent(player.getUniqueId(), uuid -> {
            PlayerStorage storage = new PlayerStorage(uuid);
            storage.load(plugin);
            return storage;
        });
    }
    
    /**
     * Get storage by UUID
     */
    public PlayerStorage getStorage(UUID uuid) {
        return storages.computeIfAbsent(uuid, uuid2 -> {
            PlayerStorage storage = new PlayerStorage(uuid2);
            storage.load(plugin);
            return storage;
        });
    }
    
    /**
     * Alias for getStorage(UUID) for PlaceholderAPI compatibility
     */
    public PlayerStorage getPlayerStorage(UUID uuid) {
        return getStorage(uuid);
    }
    
    /**
     * Save specific storage (alias for save)
     */
    public void saveStorage(UUID uuid) {
        save(uuid);
    }
    
    /**
     * Save specific storage
     */
    public void save(UUID uuid) {
        PlayerStorage storage = storages.get(uuid);
        if (storage != null) {
            storage.save(plugin);
        }
    }
    
    /**
     * Save all storages
     */
    public void saveAll() {
        plugin.getQuantumLogger().info("Saving all player storages...");
        int saved = 0;
        
        for (PlayerStorage storage : storages.values()) {
            storage.save(plugin);
            saved++;
        }
        
        plugin.getQuantumLogger().success("Saved " + saved + " player storages!");
    }
    
    /**
     * Unload player storage (saves first)
     */
    public void unload(UUID uuid) {
        save(uuid);
        storages.remove(uuid);
    }
    
    /**
     * Reload all storages
     */
    public void reload() {
        saveAll();
        storages.clear();
    }
}
