package com.wynvers.quantum;

import com.wynvers.quantum.commands.MenuCommand;
import com.wynvers.quantum.commands.QuantumCommand;
import com.wynvers.quantum.commands.QuantumStorageCommand;
import com.wynvers.quantum.commands.QuantumStorageTabCompleter;
import com.wynvers.quantum.commands.StorageCommand;
import com.wynvers.quantum.listeners.MenuListener;
import com.wynvers.quantum.tabcompleters.MenuTabCompleter;
import com.wynvers.quantum.tabcompleters.QuantumTabCompleter;
import com.wynvers.quantum.tabcompleters.StorageTabCompleter;
import com.wynvers.quantum.managers.*;
import com.wynvers.quantum.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
    private MessagesManager messagesManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize logger
        this.logger = new Logger("Quantum");
        logger.info("┌───────────────────────────────────┐");
        logger.info("│  §6§lQUANTUM §f- Advanced Storage │");
        logger.info("│       §7v1.0.0 by Kazotaruu_      │");
        logger.info("└───────────────────────────────────┘");
        
        // Extract default resources
        extractDefaultResources();
        
        // Save default config
        saveDefaultConfig();

        // Initialize MessagesManager first
        this.messagesManager = new MessagesManager(this);
        
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
    
    /**
     * Extract all default resources from JAR to plugin folder
     */
    private void extractDefaultResources() {
        logger.info("Extracting default resources...");
        
        // Create directories
        createDirectory("menus");
        createDirectory("messages");
        
        // Extract menu files
        extractResource("menus/example.yml");
        extractResource("menus/example_advanced.yml");
        extractResource("menus/storage.yml");
        
        // Extract message files
        extractResource("messages/messages_en.yml");
        extractResource("messages/messages_fr.yml");
        
        logger.success("✓ Default resources extracted");
    }
    
    /**
     * Create directory if it doesn't exist
     */
    private void createDirectory(String path) {
        File dir = new File(getDataFolder(), path);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Created directory: " + path);
        }
    }
    
    /**
     * Extract resource from JAR if it doesn't exist
     */
    private void extractResource(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        
        // Only extract if file doesn't exist
        if (file.exists()) {
            return;
        }
        
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Resource not found in JAR: " + resourcePath);
                return;
            }
            
            // Create parent directories if needed
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            // Copy resource
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Extracted: " + resourcePath);
            
        } catch (IOException e) {
            logger.error("Failed to extract resource: " + resourcePath);
            e.printStackTrace();
        }
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
        getCommand("qstorage").setExecutor(new QuantumStorageCommand(this));

        // Register TabCompleters
        getCommand("quantum").setTabCompleter(new QuantumTabCompleter());
        getCommand("storage").setTabCompleter(new StorageTabCompleter());
        getCommand("menu").setTabCompleter(new MenuTabCompleter(this));
        getCommand("qstorage").setTabCompleter(new QuantumStorageTabCompleter(this));
        
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

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}


