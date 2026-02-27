package com.wynvers.quantum.towers.storage;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestion des modes de stockage par joueur pour le Tower Storage
 * 3 modes possibles: STORAGE, SELL, RECHERCHE
 */
public class TowerStorageMode {

    /**
     * Modes disponibles
     */
    public enum Mode {
        /**
         * Mode stockage normal (par défaut)
         */
        STORAGE,

        /**
         * Mode vente - items ajoutés au storage sont vendus
         */
        SELL,

        /**
         * Mode recherche - items ramassés complètent les ordres de recherche
         */
        RECHERCHE;

        /**
         * Obtenir l'affichage formaté du mode
         * @return Nom formaté avec couleur
         */
        public String getDisplayName() {
            switch (this) {
                case STORAGE:
                    return "§aSTOCKAGE";
                case SELL:
                    return "§6VENTE";
                case RECHERCHE:
                    return "§bRECHERCHE";
                default:
                    return this.name();
            }
        }
    }

    /**
     * Stockage des modes par joueur (séparé du StorageMode régulier)
     * Key: UUID du joueur
     * Value: Mode actuel
     */
    private static final Map<UUID, Mode> playerModes = new HashMap<>();

    /**
     * Obtenir le mode actuel d'un joueur
     * @param player Le joueur
     * @return Le mode actuel (STORAGE par défaut)
     */
    public static Mode getMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), Mode.STORAGE);
    }

    /**
     * Définir le mode d'un joueur
     * @param player Le joueur
     * @param mode Le nouveau mode
     */
    public static void setMode(Player player, Mode mode) {
        if (mode == Mode.STORAGE) {
            playerModes.remove(player.getUniqueId());
        } else {
            playerModes.put(player.getUniqueId(), mode);
        }
    }

    /**
     * Vérifier si un joueur est en mode vente
     */
    public static boolean isSellMode(Player player) {
        return getMode(player) == Mode.SELL;
    }

    /**
     * Vérifier si un joueur est en mode recherche
     */
    public static boolean isRechercheMode(Player player) {
        return getMode(player) == Mode.RECHERCHE;
    }

    /**
     * Vérifier si un joueur est en mode stockage normal
     */
    public static boolean isStorageMode(Player player) {
        return getMode(player) == Mode.STORAGE;
    }

    /**
     * Toggle entre les modes (cycle: STORAGE -> SELL -> RECHERCHE -> STORAGE)
     * @param player Le joueur
     * @return Le nouveau mode
     */
    public static Mode toggleMode(Player player) {
        Mode current = getMode(player);
        Mode next;

        switch (current) {
            case STORAGE:
                next = Mode.SELL;
                break;
            case SELL:
                next = Mode.RECHERCHE;
                break;
            case RECHERCHE:
                next = Mode.STORAGE;
                break;
            default:
                next = Mode.STORAGE;
        }

        setMode(player, next);
        return next;
    }

    /**
     * Réinitialiser tous les modes (cleanup au déchargement du plugin)
     */
    public static void resetAll() {
        playerModes.clear();
    }

    public static String getDisplayName(Mode mode) {
        return mode.getDisplayName();
    }

    public static String getDisplayName(Player player) {
        return getMode(player).getDisplayName();
    }

    public static String getModeDisplay(Player player) {
        Mode mode = getMode(player);
        return "§7Mode: " + mode.getDisplayName();
    }

    public static String getSimpleModeDisplay(Player player) {
        return getMode(player).getDisplayName();
    }
}
