package com.wynvers.quantum.storage;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire des modes de storage pour chaque joueur
 * Permet de basculer entre STORAGE et VENTE
 */
public class StorageModeManager {

    private final Map<UUID, StorageMode> playerModes;

    public StorageModeManager() {
        this.playerModes = new HashMap<>();
    }

    /**
     * Récupère le mode actuel d'un joueur
     * @param player Le joueur
     * @return Le mode actuel (STORAGE par défaut)
     */
    public StorageMode getMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), StorageMode.STORAGE);
    }

    /**
     * Définit le mode d'un joueur
     * @param player Le joueur
     * @param mode Le nouveau mode
     */
    public void setMode(Player player, StorageMode mode) {
        playerModes.put(player.getUniqueId(), mode);
    }

    /**
     * Bascule le mode d'un joueur (STORAGE <-> VENTE)
     * @param player Le joueur
     * @return Le nouveau mode après basculement
     */
    public StorageMode toggleMode(Player player) {
        StorageMode currentMode = getMode(player);
        StorageMode newMode = currentMode == StorageMode.STORAGE ? StorageMode.VENTE : StorageMode.STORAGE;
        setMode(player, newMode);
        return newMode;
    }

    /**
     * Efface le mode d'un joueur (utilisé à la déconnexion)
     * @param player Le joueur
     */
    public void clearMode(Player player) {
        playerModes.remove(player.getUniqueId());
    }

    /**
     * Enumération des modes de storage
     */
    public enum StorageMode {
        STORAGE("STORAGE"),
        VENTE("VENTE");

        private final String displayName;

        StorageMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static StorageMode fromString(String name) {
            for (StorageMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return STORAGE; // Par défaut
        }
    }
}
