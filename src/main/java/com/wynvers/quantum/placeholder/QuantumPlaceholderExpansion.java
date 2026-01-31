package com.wynvers.quantum.placeholder;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuantumPlaceholderExpansion extends PlaceholderExpansion {

    private final Quantum plugin;

    public QuantumPlaceholderExpansion(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "quantum";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Wynvers";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
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

        PlayerStorage storage = plugin.getStorageManager().getStorage(player);

        // %quantum_amt_nexo-custom_sword%
        // %quantum_amt_minecraft-diamond%
        if (params.startsWith("amt_")) {
            String itemId = params.substring(4); // Remove "amt_"

            // Check if it's a Nexo item (nexo-id format)
            if (itemId.startsWith("nexo-")) {
                String nexoId = itemId.substring(5).replace("-", ":"); // nexo-custom_sword -> custom_sword, then replace - with :
                int amount = storage.getNexoAmount(nexoId);
                return String.valueOf(amount);
            }

            // Check if it's a Minecraft item (minecraft-id format)
            if (itemId.startsWith("minecraft-")) {
                String materialName = itemId.substring(10).toUpperCase().replace("-", "_");
                try {
                    Material material = Material.valueOf(materialName);
                    int amount = storage.getAmount(material);
                    return String.valueOf(amount);
                } catch (IllegalArgumentException e) {
                    return "0";
                }
            }

            // No prefix - try both (priority to Nexo)
            String normalizedId = itemId.replace("-", ":");
            
            // Try Nexo first
            if (storage.getNexoItems().containsKey(normalizedId)) {
                return String.valueOf(storage.getNexoAmount(normalizedId));
            }

            // Try Minecraft
            try {
                Material material = Material.valueOf(itemId.toUpperCase().replace("-", "_"));
                return String.valueOf(storage.getAmount(material));
            } catch (IllegalArgumentException e) {
                return "0";
            }
        }

        return null;
    }
}
