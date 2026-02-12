package com.wynvers.quantum.placeholderapi;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;

/**
 * PlaceholderAPI Integration Manager
 * Manages the registration and lifecycle of the Quantum PlaceholderAPI expansion
 * Reference: https://github.com/PlaceholderAPI/PlaceholderAPI
 * Version: 2.11.6 (Compatible with Minecraft 1.21.11)
 */
public class PlaceholderAPIManager {

    private final Quantum plugin;
    private QuantumExpansion expansion;
    private boolean enabled = false;

    public PlaceholderAPIManager(Quantum plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Check if PlaceholderAPI is loaded
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().warning("PlaceholderAPI not found - PlaceholderAPI integration disabled");
            return;
        }

        try {
            // Create and register the expansion
            this.expansion = new QuantumExpansion(plugin);
            
            if (expansion.register()) {
                this.enabled = true;
                plugin.getLogger().info("✓ PlaceholderAPI integration enabled! (v2.11.6)");
                plugin.getLogger().info("  Available placeholders: %quantum_level%, %quantum_job%, %quantum_tower%, and more");
            } else {
                plugin.getLogger().warning("Failed to register PlaceholderAPI expansion");
                this.enabled = false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize PlaceholderAPI integration: " + e.getMessage());
            this.enabled = false;
        }
    }

    /**
     * Check if PlaceholderAPI integration is enabled
     * @return true if enabled and working
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the expansion instance
     * @return the QuantumExpansion instance, or null if not initialized
     */
    public QuantumExpansion getExpansion() {
        return expansion;
    }

    /**
     * Unregister the expansion (called on plugin disable)
     */
    public void disable() {
        if (expansion != null && enabled) {
            expansion.unregister();
            plugin.getLogger().info("✓ PlaceholderAPI expansion unregistered");
        }
        enabled = false;
    }
}
