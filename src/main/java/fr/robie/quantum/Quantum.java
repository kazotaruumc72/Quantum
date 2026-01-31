package fr.robie.quantum;

import fr.robie.quantum.commands.MenuCommand;
import fr.robie.quantum.commands.QuantumCommand;
import fr.robie.quantum.commands.StorageCommand;
import fr.robie.quantum.listeners.MenuListener;
import fr.robie.quantum.managers.*;
import fr.robie.quantum.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Quantum - Advanced Virtual Storage & Dynamic GUI Builder
 * 
 * Features:
 * - Virtual storage system with unlimited capacity
 * - Dynamic GUI builder (DeluxeMenus-style)
 * - Nexo integration for custom items
 * - PlaceholderAPI support
 * - Animated titles
 * - Requirements system
 * - Action system (click handlers)
 * - Database storage (MySQL/SQLite)
 */
public final class Quantum extends JavaPlugin {

    private static Quantum instance;
    private Logger logger;
    
    // Managers
    private DatabaseManager databaseManager;
    private StorageManager storageManager;
    private MenuManager menuManager;
    private PlaceholderManager placeholderManager;
    private AnimationManager animationManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize logger
        this.logger = new Logger("Quantum");
        logger.info("┌───────────────────────────────────┐");
        logger.info("│    §6§lQUANTUM §f- Advanced Storage    │");
        logger.info("│         §7v1.0.0 by Robie          │");
        logger.info("└───────────────────────────────────┘");
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        initializeManagers();
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        logger.success("✓ Quantum enabled successfully!");
        logger.info("Dynamic GUI system loaded!");
        logger.info("Storage system ready!");
    }
    
    private void initializeManagers() {
        logger.info("Initializing managers...");
        
        // Database
        this.databaseManager = new DatabaseManager(this);
        logger.success("✓ Database Manager");
        
        // Storage
        this.storageManager = new StorageManager(this);
        logger.success("✓ Storage Manager");
        
        // Animation
        this.animationManager = new AnimationManager(this);
        logger.success("✓ Animation Manager");
        
        // Placeholder (if available)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderManager = new PlaceholderManager(this);
            logger.success("✓ Placeholder Manager (PlaceholderAPI found)");
        } else {
            logger.warning("⚠ PlaceholderAPI not found - placeholder features disabled");
        }
        
        // Menu (must be last - depends on other managers)
        this.menuManager = new MenuManager(this);
        logger.success("✓ Menu Manager (" + menuManager.getMenuCount() + " menus loaded)");
    }
    
    private void registerListeners() {
        logger.info("Registering listeners...");
        
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        logger.success("✓ Menu Listener");
    }
    
    private void registerCommands() {
        logger.info("Registering commands...");
        
        getCommand("quantum").setExecutor(new QuantumCommand(this));
        getCommand("storage").setExecutor(new StorageCommand(this));
        getCommand("menu").setExecutor(new MenuCommand(this));
        
        logger.success("✓ Commands registered");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling Quantum...");
        
        // Stop animations
        if (animationManager != null) {
            animationManager.stopAll();
        }
        
        // Save all storage data
        if (storageManager != null) {
            storageManager.saveAll();
        }
        
        // Close database
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        logger.info("Quantum disabled. Goodbye!");
    }
    
    /**
     * Reload configuration and managers
     */
    public void reloadPlugin() {
        logger.info("Reloading Quantum...");
        
        reloadConfig();
        
        if (storageManager != null) storageManager.reload();
        if (menuManager != null) menuManager.reload();
        if (animationManager != null) animationManager.reload();
        
        logger.success("Quantum reloaded successfully!");
    }

    // === GETTERS ===
    
    public static Quantum getInstance() {
        return instance;
    }
    
    public Logger getQuantumLogger() {
        return logger;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public AnimationManager getAnimationManager() {
        return animationManager;
    }
}
