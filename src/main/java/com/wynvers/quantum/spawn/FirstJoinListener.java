package com.wynvers.quantum.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener to handle first join teleportation and custom join/leave/firstjoin messages
 * Supports per-rank messages via LuckPerms permissions
 */
public class FirstJoinListener implements Listener {

    private final SpawnManager spawnManager;
    private final FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public FirstJoinListener(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
        this.config = spawnManager.getPlugin().getConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // First join: teleport + custom firstjoin message
        if (!player.hasPlayedBefore()) {
            Location firstSpawn = spawnManager.getFirstSpawn();
            if (firstSpawn != null) {
                player.teleport(firstSpawn);
            }

            // Custom firstjoin message
            if (config.getBoolean("join-leave.firstjoin.enabled", false)) {
                String msg = config.getString("join-leave.firstjoin.message", "");
                if (!msg.isEmpty()) {
                    event.joinMessage(parsePlaceholders(msg, player));
                    return; // firstjoin message overrides join message
                }
            }
        }

        // Custom join message
        if (config.getBoolean("join-leave.join.enabled", false)) {
            String format = getFormatForPlayer(player, "join-leave.join");
            if (format != null && !format.isEmpty()) {
                event.joinMessage(parsePlaceholders(format, player));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Custom leave message
        if (config.getBoolean("join-leave.leave.enabled", false)) {
            String format = getFormatForPlayer(player, "join-leave.leave");
            if (format != null && !format.isEmpty()) {
                event.quitMessage(parsePlaceholders(format, player));
            }
        }
    }

    /**
     * Get the format string for a player based on their LuckPerms permissions.
     * Checks formats map (first match wins), falls back to default.
     */
    private String getFormatForPlayer(Player player, String configPath) {
        ConfigurationSection formatsSection = config.getConfigurationSection(configPath + ".formats");
        if (formatsSection != null) {
            // Use getKeys(true) to resolve dotted permission keys (e.g. quantum.join.elite)
            // that Bukkit YAML splits into nested sections. getString() returns null for
            // intermediate section nodes, so only leaf values (actual format strings) are used.
            for (String permission : formatsSection.getKeys(true)) {
                String format = formatsSection.getString(permission);
                if (format != null && player.hasPermission(permission)) {
                    return format;
                }
            }
        }
        return config.getString(configPath + ".default", "");
    }

    /**
     * Parse placeholders and convert MiniMessage to Component.
     * Player name is escaped to prevent MiniMessage injection.
     */
    private Component parsePlaceholders(String message, Player player) {
        // Escape player name to prevent MiniMessage tag injection
        String safeName = miniMessage.escapeTags(player.getName());
        String parsed = message
                .replace("%player%", safeName)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));
        return miniMessage.deserialize(parsed);
    }
}
