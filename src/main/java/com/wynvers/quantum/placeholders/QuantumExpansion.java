package com.wynvers.quantum.placeholders;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.storage.PlayerStorage;
import com.wynvers.quantum.storage.StorageMode;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * PlaceholderAPI Expansion pour Quantum
 * Supporte les placeholders de session de création d'ordres
 */
public class QuantumExpansion extends PlaceholderExpansion {
    
    private final Quantum plugin;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
    
    public QuantumExpansion(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "quantum";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "Wynvers";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        // === STORAGE MODE ===
        if (params.equals("mode")) {
            return StorageMode.getMode(player).name();
        }
        
        if (params.equals("mode_display")) {
            StorageMode.Mode mode = StorageMode.getMode(player);
            switch (mode) {
                case STORAGE: return "§aSTOCKAGE";
                case SELL: return "§6VENTE";
                case RECHERCHE: return "§bRECHERCHE";
                default: return mode.name();
            }
        }
        
        // === STORAGE STATS ===
        if (params.equals("storage_items")) {
            PlayerStorage storage = plugin.getStorageManager().getStorage(player);
            return String.valueOf(storage.getUniqueItemCount());
        }
        
        if (params.equals("storage_total")) {
            PlayerStorage storage = plugin.getStorageManager().getStorage(player);
            return String.valueOf(storage.getTotalItemCount());
        }
        
        // === ORDER CREATION SESSION ===
        // Tous les placeholders %quantum_order_*%
        if (params.startsWith("order_")) {
            OrderCreationSession session = plugin.getOrderCreationManager().getSession(player);
            if (session == null) {
                return "";
            }
            
            Map<String, String> placeholders = session.getPlaceholders();
            String key = "quantum_" + params;
            return placeholders.getOrDefault(key, "");
        }
        
        return null;
    }
}
