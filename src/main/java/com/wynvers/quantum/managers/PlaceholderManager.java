package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageMode;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderManager {

    private final Quantum plugin;
    private final boolean enabled;

    public PlaceholderManager(Quantum plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Parse placeholders in string
     */
    public String parse(Player player, String text) {
        if (text == null) {
            return text;
        }

        // Remplacer le placeholder %mode% (AVANT PlaceholderAPI)
        if (text.contains("%mode%")) {
            String modeDisplay = StorageMode.getModeDisplay(player);
            text = text.replace("%mode%", ChatColor.translateAlternateColorCodes('&', modeDisplay));
        }
        
        // Ensuite utiliser PlaceholderAPI si disponible
        if (enabled) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        return text;
    }

    /**
     * Parse placeholders in list
     */
    public List<String> parse(Player player, List<String> texts) {
        if (texts == null) {
            return texts;
        }

        List<String> parsed = new ArrayList<>();
        for (String text : texts) {
            parsed.add(parse(player, text));
        }
        return parsed;
    }

    /**
     * Check if PlaceholderAPI is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
