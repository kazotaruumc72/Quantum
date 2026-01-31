package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
// import com.wynvers.quantum.storage.StorageModeManager;
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
        if (!enabled || text == null) {
            return text;
        }

                // Remplacer le placeholder %mode%
        if (text.contains("%mode%")) {
            // StorageModeManager functionality temporarily disabled            text = text.replace("%mode%", mode.getDisplayName());
        //             }
        
        return PlaceholderAPI.setPlaceholders(player, text);
    }
    
    /**
     * Parse placeholders in list
     */
    public List<String> parse(Player player, List<String> texts) {
        if (!enabled || texts == null) {
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


