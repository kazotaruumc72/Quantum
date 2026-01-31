package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Gère les messages depuis messages.yml
 */
public class MessageManager {
    
    private final Quantum plugin;
    private FileConfiguration messages;
    
    public MessageManager(Quantum plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * Recharge messages.yml
     */
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.messages = plugin.getConfig();
        
        // Load messages.yml
        plugin.saveResource("messages.yml", false);
        this.messages = plugin.getConfig();
    }
    
    /**
     * Récupère un message avec placeholders
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null) {
            return ChatColor.RED + "Message not found: " + path;
        }
        
        // Replace placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Récupère un message sans placeholders
     */
    public String getMessage(String path) {
        return getMessage(path, null);
    }
    
    /**
     * Récupère le préfixe
     */
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    /**
     * Builder pour les placeholders
     */
    public static class PlaceholderBuilder {
        private final Map<String, String> placeholders = new HashMap<>();
        
        public PlaceholderBuilder add(String key, String value) {
            placeholders.put(key, value);
            return this;
        }
        
        public PlaceholderBuilder add(String key, int value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }
        
        public PlaceholderBuilder add(String key, long value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }
        
        public PlaceholderBuilder add(String key, double value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }
        
        public Map<String, String> build() {
            return placeholders;
        }
    }
    
    /**
     * Crée un builder de placeholders
     */
    public static PlaceholderBuilder placeholder() {
        return new PlaceholderBuilder();
    }
}
