package com.wynvers.quantum.tab;

import com.wynvers.quantum.Quantum;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * TAB Plugin Integration Manager
 * Provides MiniMessage support, custom placeholders, and permission-based header/footer for TAB
 * Reference: https://github.com/NEZNAMY/TAB/
 * Version: 5.5.0 (Compatible with Minecraft 1.21.11)
 */
public class TABManager {

    private final Quantum plugin;
    private TabAPI tabAPI;
    private boolean enabled = false;
    private YamlConfiguration tabConfig;
    private final Map<String, TabGroup> groups = new LinkedHashMap<>();
    private List<String> priorityOrder = new ArrayList<>();
    private BukkitRunnable refreshTask;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TABManager(Quantum plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Check if TAB is loaded
        if (Bukkit.getPluginManager().getPlugin("TAB") == null) {
            plugin.getLogger().warning("TAB plugin not found - TAB integration disabled");
            return;
        }

        try {
            // Load TAB configuration
            loadConfig();
            
            // Get TAB API instance
            this.tabAPI = TabAPI.getInstance();
            
            // Register custom Quantum placeholders
            registerPlaceholders();
            
            // Register server placeholders
            registerServerPlaceholders();
            
            // Start refresh task
            startRefreshTask();
            
            this.enabled = true;
            plugin.getLogger().info("✓ TAB integration enabled! (v5.5.0, Header/Footer system active)");
            plugin.getLogger().info("✓ Loaded " + groups.size() + " header/footer groups");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize TAB integration: " + e.getMessage());
            e.printStackTrace();
            this.enabled = false;
        }
    }

    /**
     * Load TAB configuration file
     */
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "tab_config.yml");
        
        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                InputStream defaultConfig = plugin.getResource("tab_config.yml");
                if (defaultConfig != null) {
                    Files.copy(defaultConfig, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Created default tab_config.yml");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create tab_config.yml: " + e.getMessage());
                return;
            }
        }
        
        // Load configuration
        tabConfig = YamlConfiguration.loadConfiguration(configFile);
        
        // Load priority order
        priorityOrder = tabConfig.getStringList("settings.priority-order");
        if (priorityOrder.isEmpty()) {
            priorityOrder = Arrays.asList("elite", "mvp+", "mvp", "vip+", "vip", "default");
        }
        
        // Load groups
        groups.clear();
        ConfigurationSection groupsSection = tabConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                if (groupSection != null) {
                    TabGroup group = new TabGroup(
                        groupName,
                        groupSection.getString("permission", ""),
                        groupSection.getStringList("header"),
                        groupSection.getStringList("footer")
                    );
                    groups.put(groupName, group);
                }
            }
        }
    }

    /**
     * Register custom placeholders for TAB
     * These can be used in TAB's configuration files
     */
    private void registerPlaceholders() {
        PlaceholderManager placeholderManager = tabAPI.getPlaceholderManager();

        // Quantum Level Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_level%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "0";
            return String.valueOf(plugin.getPlayerLevelManager().getLevel(bukkitPlayer.getUniqueId()));
        });

        // Quantum Job Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_job%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "Aucun";
            
            if (plugin.getJobManager() == null) return "Aucun";
            
            var jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
            if (jobData == null) return "Aucun";
            
            var job = plugin.getJobManager().getJob(jobData.getJobId());
            return job != null ? job.getDisplayName() : "Inconnu";
        });

        // Quantum Job Level Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_job_level%", 1000, player -> {
            if (plugin.getJobManager() == null) return "0";
            
            var jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
            return jobData != null ? String.valueOf(jobData.getLevel()) : "0";
        });

        // Quantum Tower Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_tower%", 1000, player -> {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (bukkitPlayer == null) return "Aucune";
            
            if (plugin.getTowerManager() == null) return "Aucune";
            
            var tower = plugin.getTowerManager().getPlayerTower(bukkitPlayer);
            return tower != null ? tower.getId() : "Aucune";
        });

        // Quantum Tower Floor Placeholder
        placeholderManager.registerPlayerPlaceholder("%quantum_tower_floor%", 1000, player -> {
            if (plugin.getTowerManager() == null) return "0";
            
            var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
            return progress != null ? String.valueOf(progress.getCurrentFloor()) : "0";
        });

        plugin.getLogger().info("✓ Registered TAB placeholders: %quantum_level%, %quantum_job%, %quantum_job_level%, %quantum_tower%, %quantum_tower_floor%");
    }

    /**
     * Register server-wide placeholders (TPS, online players, etc.)
     */
    private void registerServerPlaceholders() {
        PlaceholderManager placeholderManager = tabAPI.getPlaceholderManager();
        
        // Server online players
        placeholderManager.registerServerPlaceholder("%server_online%", 1000, () -> 
            String.valueOf(Bukkit.getOnlinePlayers().size())
        );
        
        // Server max players
        placeholderManager.registerServerPlaceholder("%server_max_players%", 1000, () -> 
            String.valueOf(Bukkit.getMaxPlayers())
        );
        
        // Server TPS
        placeholderManager.registerServerPlaceholder("%server_tps%", 1000, () -> {
            try {
                double tps = Bukkit.getTPS()[0];
                return String.format("%.1f", tps);
            } catch (Exception e) {
                return "20.0";
            }
        });
        
        plugin.getLogger().info("✓ Registered server placeholders: %server_online%, %server_max_players%, %server_tps%");
    }

    /**
     * Update a player's TAB display
     * Can be called when player data changes
     * Note: TAB 5.x+ automatically updates when placeholders change
     */
    public void updatePlayer(Player player) {
        if (!enabled) return;
        
        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) {
                // Set header and footer based on player's permissions
                updatePlayerHeaderFooter(player);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update TAB for player " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Update all online players' TAB displays
     */
    public void updateAllPlayers() {
        if (!enabled) return;
        
        Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
    }
    
    /**
     * Update a player's header and footer based on their permissions
     */
    public void updatePlayerHeaderFooter(Player player) {
        if (!enabled) return;
        
        TabGroup group = getPlayerGroup(player);
        if (group == null) {
            return;
        }
        
        // Process header
        Component header = Component.empty();
        for (String line : group.getHeader()) {
            String processedLine = processPlaceholders(player, line);
            Component lineComponent = miniMessage.deserialize(processedLine);
            header = header.append(lineComponent).append(Component.newline());
        }
        
        // Process footer
        Component footer = Component.empty();
        for (String line : group.getFooter()) {
            String processedLine = processPlaceholders(player, line);
            Component lineComponent = miniMessage.deserialize(processedLine);
            footer = footer.append(lineComponent).append(Component.newline());
        }
        
        // Set player list header and footer
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
    
    /**
     * Get the appropriate TAB group for a player based on their permissions
     */
    private TabGroup getPlayerGroup(Player player) {
        // Check groups in priority order
        for (String groupName : priorityOrder) {
            TabGroup group = groups.get(groupName);
            if (group != null) {
                // If no permission required (default group) or player has permission
                if (group.getPermission().isEmpty() || player.hasPermission(group.getPermission())) {
                    return group;
                }
            }
        }
        
        // Fallback to default group
        return groups.get("default");
    }
    
    /**
     * Process placeholders in a string
     */
    private String processPlaceholders(Player player, String text) {
        // Replace basic player placeholders
        text = text.replace("%player_name%", player.getName())
                   .replace("%player_displayname%", player.getDisplayName())
                   .replace("%player_ping%", String.valueOf(player.getPing()));
        
        // Replace server placeholders
        text = text.replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                   .replace("%server_max_players%", String.valueOf(Bukkit.getMaxPlayers()));
        
        try {
            double tps = Bukkit.getTPS()[0];
            text = text.replace("%server_tps%", String.format("%.1f", tps));
        } catch (Exception e) {
            text = text.replace("%server_tps%", "20.0");
        }
        
        // Replace Quantum placeholders
        text = text.replace("%quantum_level%", 
            String.valueOf(plugin.getPlayerLevelManager().getLevel(player.getUniqueId())));
        
        if (plugin.getJobManager() != null) {
            var jobData = plugin.getJobManager().getPlayerJob(player.getUniqueId());
            if (jobData != null) {
                var job = plugin.getJobManager().getJob(jobData.getJobId());
                text = text.replace("%quantum_job%", job != null ? job.getDisplayName() : "Aucun");
                text = text.replace("%quantum_job_level%", String.valueOf(jobData.getLevel()));
            } else {
                text = text.replace("%quantum_job%", "Aucun");
                text = text.replace("%quantum_job_level%", "0");
            }
        } else {
            text = text.replace("%quantum_job%", "Aucun");
            text = text.replace("%quantum_job_level%", "0");
        }
        
        if (plugin.getTowerManager() != null) {
            var tower = plugin.getTowerManager().getPlayerTower(player);
            text = text.replace("%quantum_tower%", tower != null ? tower.getId() : "Aucune");
            
            var progress = plugin.getTowerManager().getProgress(player.getUniqueId());
            text = text.replace("%quantum_tower_floor%", 
                progress != null ? String.valueOf(progress.getCurrentFloor()) : "0");
        } else {
            text = text.replace("%quantum_tower%", "Aucune");
            text = text.replace("%quantum_tower_floor%", "0");
        }
        
        // Try PlaceholderAPI if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        return text;
    }
    
    /**
     * Start automatic refresh task
     */
    private void startRefreshTask() {
        int interval = tabConfig.getInt("settings.refresh-interval", 5) * 20; // Convert to ticks
        
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (enabled) {
                    updateAllPlayers();
                }
            }
        };
        
        refreshTask.runTaskTimer(plugin, 20L, interval);
    }
    
    /**
     * Reload TAB configuration
     */
    public void reload() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        
        loadConfig();
        
        if (enabled) {
            startRefreshTask();
            updateAllPlayers();
            plugin.getLogger().info("✓ TAB configuration reloaded");
        }
    }
    
    /**
     * Shutdown TAB manager
     */
    public void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    /**
     * Check if TAB integration is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the TAB API instance
     */
    public TabAPI getAPI() {
        return tabAPI;
    }
    
    /**
     * Get all loaded groups
     */
    public Map<String, TabGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }
    
    /**
     * Get the TAB configuration
     */
    public YamlConfiguration getConfig() {
        return tabConfig;
    }
    
    /**
     * Save TAB configuration
     */
    public void saveConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "tab_config.yml");
            tabConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save tab_config.yml: " + e.getMessage());
        }
    }
    
    /**
     * Inner class representing a TAB group
     */
    public static class TabGroup {
        private final String name;
        private final String permission;
        private final List<String> header;
        private final List<String> footer;
        
        public TabGroup(String name, String permission, List<String> header, List<String> footer) {
            this.name = name;
            this.permission = permission;
            this.header = new ArrayList<>(header);
            this.footer = new ArrayList<>(footer);
        }
        
        public String getName() {
            return name;
        }
        
        public String getPermission() {
            return permission;
        }
        
        public List<String> getHeader() {
            return Collections.unmodifiableList(header);
        }
        
        public List<String> getFooter() {
            return Collections.unmodifiableList(footer);
        }
        
        public void setHeader(List<String> header) {
            this.header.clear();
            this.header.addAll(header);
        }
        
        public void setFooter(List<String> footer) {
            this.footer.clear();
            this.footer.addAll(footer);
        }
    }
}
