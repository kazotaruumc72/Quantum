package com.wynvers.quantum.storage;

import com.wynvers.quantum.Quantum;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère les modes du storage (STORAGE / SELL)
 */
public class StorageMode {
    
    public enum Mode {
        STORAGE("§aMode: Stockage"),
        SELL("§eMode: Vente");
        
        private final String displayName;
        
        Mode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Stockage des modes par joueur (UUID -> Mode)
    private static final Map<UUID, Mode> playerModes = new HashMap<>();
    
    /**
     * Définir le mode d'un joueur
     */
    public static void setMode(Player player, Mode mode) {
        playerModes.put(player.getUniqueId(), mode);
    }
    
    /**
     * Récupérer le mode d'un joueur (STORAGE par défaut)
     */
    public static Mode getMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), Mode.STORAGE);
    }
    
    /**
     * Récupérer le mode sous forme de texte formaté
     */
    public static String getModeDisplay(Player player) {
        return getMode(player).getDisplayName();
    }
    
    /**
     * Basculer entre les modes
     * Rafraîchit automatiquement le menu si ouvert
     */
    public static void toggleMode(Player player) {
        Mode currentMode = getMode(player);
        Mode newMode = (currentMode == Mode.STORAGE) ? Mode.SELL : Mode.STORAGE;
        setMode(player, newMode);
        
        // Message de confirmation
        player.sendMessage(newMode.getDisplayName());
        
        // Vérifier si le joueur a le menu storage ouvert
        if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
            Quantum plugin = Quantum.getInstance();
            com.wynvers.quantum.menu.Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
            
            // Si le menu storage est ouvert, le rafraîchir
            if (activeMenu != null && activeMenu.getId().equals("storage")) {
                activeMenu.refresh(player, plugin);
            }
        }
    }
    
    /**
     * Vérifier si le joueur est en mode SELL
     */
    public static boolean isSellMode(Player player) {
        return getMode(player) == Mode.SELL;
    }
    
    /**
     * Vérifier si le joueur est en mode STORAGE
     */
    public static boolean isStorageMode(Player player) {
        return getMode(player) == Mode.STORAGE;
    }
    
    /**
     * Nettoyer le mode d'un joueur (quand il se déconnecte)
     */
    public static void clearMode(Player player) {
        playerModes.remove(player.getUniqueId());
    }
}
