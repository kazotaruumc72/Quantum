package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final Quantum plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messageCache;

    public MessagesManager(Quantum plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        loadMessages();
    }

    /**
     * Load messages from messages.yml
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();
        
        plugin.getQuantumLogger().info("Messages loaded successfully");
    }

    /**
     * Get a message from messages.yml with prefix
     * @param path The path in the YAML (e.g., "no-permission")
     * @return Formatted message with colors
     */
    public String get(String path) {
        return get(path, true);
    }

    /**
     * Get a message from messages.yml
     * @param path The path in the YAML
     * @param withPrefix Whether to include the prefix
     * @return Formatted message with colors
     */
    public String get(String path, boolean withPrefix) {
        // Check cache first
        String cacheKey = path + ":" + withPrefix;
        if (messageCache.containsKey(cacheKey)) {
            return messageCache.get(cacheKey);
        }

        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getQuantumLogger().warning("Message not found: " + path);
            return "&cMessage not found: " + path;
        }

        String prefix = "";
        if (withPrefix) {
            prefix = messagesConfig.getString("prefix", "");
        }

        String formatted = colorize(prefix + message);
        messageCache.put(cacheKey, formatted);
        return formatted;
    }

    /**
     * Get a message with placeholder replacements
     * @param path The path in the YAML
     * @param placeholders Map of placeholders to replace (e.g., {player}, {amount})
     * @return Formatted message with colors and placeholders replaced
     */
    public String get(String path, Map<String, String> placeholders) {
        return get(path, placeholders, true);
    }

    /**
     * Get a message with placeholder replacements
     * @param path The path in the YAML
     * @param placeholders Map of placeholders to replace
     * @param withPrefix Whether to include the prefix
     * @return Formatted message with colors and placeholders replaced
     */
    public String get(String path, Map<String, String> placeholders, boolean withPrefix) {
        String message = get(path, withPrefix);
        
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }

    /**
     * Colorize a message with color codes
     * @param message The message to colorize
     * @return Colorized message
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Reload messages from file
     */
    public void reload() {
        loadMessages();
    }
}
