package com.wynvers.quantum;

import com.wynvers.quantum.armor.ArmorListener;
import com.wynvers.quantum.armor.ArmorManager;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneItem;
import com.wynvers.quantum.armor.RuneApplyListener;
import com.wynvers.quantum.armor.RuneType;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.levels.PlayerLevelListener;
import com.wynvers.quantum.towers.TowerDamageListener;
import com.wynvers.quantum.commands.*;
import com.wynvers.quantum.listeners.MenuListener;
import com.wynvers.quantum.listeners.ScoreboardListener;
import com.wynvers.quantum.listeners.StorageListener;
import com.wynvers.quantum.placeholder.QuantumPlaceholderExpansion;
import com.wynvers.quantum.placeholders.QuantumExpansion;
import com.wynvers.quantum.statistics.StatisticsManager;
import com.wynvers.quantum.statistics.StorageStatsManager;
import com.wynvers.quantum.statistics.TradingStatisticsManager;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.worldguard.KillTracker;
import com.wynvers.quantum.worldguard.ZoneListener;
import com.wynvers.quantum.worldguard.ZoneManager;
import com.wynvers.quantum.tabcompleters.*;
import com.wynvers.quantum.tabcompleters.QuantumArmorRuneTabCompleter;
import com.wynvers.quantum.managers.*;
import com.wynvers.quantum.orders.OrderAcceptanceHandler;
import com.wynvers.quantum.orders.OrderButtonHandler;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.sell.SellManager;
import com.wynvers.quantum.utils.ActionExecutor;
import com.wynvers.quantum.utils.Logger;
import com.wynvers.quantum.commands.ArmorTabCompleter;
import com.wynvers.quantum.commands.ArmorCommand;
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
 * - Vault economy integration
 * - Selling system
 * - Orders system (buy/sell orders)
 * - Escrow system (secure money storage)
 * - Statistics tracking (items stored, trades, storage stats)
 * - Transaction history (all trades recorded)
 * - Trading statistics (performance analysis)
 * - Centralized message system (MiniMessage + Legacy support)
 * - WorldGuard zone restrictions with mob kill requirements
 * - Tower progression system with 4 towers (25 floors + final boss each)
 * - Integrated tower scoreboard system (auto-disable Oreo Essentials)
 * - Dungeon armor system with runes (9 types, 3 levels each)
 * - Configurable dungeon armor IDs (Nexo items)
 * - Interactive rune equipment menu (drag & drop)
 */
public final class Quantum extends JavaPlugin {

    private static Quantum instance;
    private Logger logger;
    private ScoreboardManager scoreboardManager;
    private ScoreboardConfig scoreboardConfig;
    
    // Managers
    private PlayerLevelManager playerLevelManager;
    private DatabaseManager databaseManager;
    private StorageManager storageManager;
    private MenuManager menuManager;
    private PlaceholderManager placeholderManager;
    private AnimationManager animationManager;
    private MessagesManager messagesManager; // Legacy
    private MessageManager messageManager; // NEW: System messages
    private GuiMessageManager guiMessageManager; // NEW: GUI messages
    private EscrowManager escrowManager; // NEW: Escrow system
    private PriceManager priceManager;
    private VaultManager vaultManager;
    private SellManager sellManager;
    private OrderManager orderManager;
    private OrderCreationManager orderCreationManager;
    private OrderButtonHandler orderButtonHandler;
    private OrderAcceptanceHandler orderAcceptanceHandler; // NEW: Order acceptance
    private StatisticsManager statisticsManager;
    private StorageStatsManager storageStatsManager;
    private TransactionHistoryManager transactionHistoryManager; // NEW: Transaction history
    private TradingStatisticsManager tradingStatisticsManager; // NEW: Trading statistics
    private ZoneManager zoneManager; // NEW: WorldGuard zone manager
    private KillTracker killTracker; // NEW: Kill tracking for zones
    private TowerManager towerManager; // NEW: Tower progression system
    private TowerScoreboardHandler scoreboardHandler; // NEW: Integrated tower scoreboard
    private DungeonArmor dungeonArmor; // NEW: Dungeon armor system
    private ArmorManager armorManager; // NEW: Armor manager
    private RuneItem runeItem; // NEW: Rune item utility
    private DatabaseManager databaseManager;
    // Utils
    private ActionExecutor actionExecutor;
    
    // PlaceholderAPI expansions
    private QuantumPlaceholderExpansion placeholderExpansion;
    private QuantumExpansion quantumExpansion;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize logger
        this.logger = new Logger("Quantum");
        logger.info("┌───────────────────────────────────┐");
        logger.info("│  §6§lQUANTUM §f- Advanced Storage │");
        logger.info("│       §7v1.0.1 by Kazotaruu_      │");
        logger.info("└───────────────────────────────────┘");
        
        // Extract default resources
        extractDefaultResources();
        
        // Save default config
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.playerLevelManager = new PlayerLevelManager(this, databaseManager);
    
        getServer().getPluginManager().registerEvents(
                new PlayerLevelListener(this, playerLevelManager), this
        );
        // Initialize NEW message managers FIRST
        this.messageManager = new MessageManager(this);
        this.guiMessageManager = new GuiMessageManager(this);
        
        // Initialize escrow manager BEFORE order managers
        this.escrowManager = new EscrowManager(this);
        
        // Initialize legacy MessagesManager for compatibility
        this.messagesManager = new MessagesManager(this);
        
        // Initialize scoreboard config and manager (always available)
        this.scoreboardConfig = new ScoreboardConfig(this);
        this.scoreboardManager = new ScoreboardManager(this);
        logger.success("✓ Scoreboard Config & Manager initialized!");
        
        // Initialize Dungeon Armor & Rune system
        this.dungeonArmor = new DungeonArmor(this);
        RuneType.init(this); // Initialize rune config
        this.armorManager = new ArmorManager(this, dungeonArmor);
        this.runeItem = new RuneItem(this);
        logger.success("✓ Dungeon Armor & Rune system initialized! (9 runes with 3 levels)");

        this.towerManager = new TowerManager(this);
        getServer().getPluginManager().registerEvents(new TowerDamageListener(this), this);
        
        // Initialize WorldGuard zone system
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            this.killTracker = new KillTracker(this);
            this.zoneManager = new ZoneManager(this);
            this.scoreboardHandler = new TowerScoreboardHandler(this);
            logger.success("✓ WorldGuard integration enabled!");
            logger.success("✓ Tower system loaded! (" + towerManager.getTowerCount() + " tours)");
            logger.success("✓ Integrated scoreboard system ready!");
        } else {
            logger.warning("⚠ WorldGuard not found - zone restriction and tower features disabled");
        }
        
        // Initialize managers
        initializeManagers();
        
        // Register PlaceholderAPI expansion
        registerPlaceholderExpansion();
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        logger.success("✓ Quantum enabled successfully!");
        logger.info("Dynamic GUI system loaded!");
        logger.info("Storage system ready!");
        logger.success("✓ Message system ready! (MiniMessage + Legacy)");
        logger.success("✓ Escrow system ready! (" + escrowManager.getEscrowCount() + " deposits loaded)");
        if (vaultManager.isEnabled()) {
            logger.success("✓ Economy system ready!");
        }
        logger.success("✓ Orders system ready!");
        logger.success("✓ Statistics tracking enabled!");
        logger.success("✓ Transaction history enabled!");
        if (zoneManager != null) {
            logger.success("✓ Zone restriction system ready! (" + zoneManager.getZoneCount() + " zones)");
        }
    }
    
    /**
     * Extract all default resources from JAR to plugin folder
     */
    private void extractDefaultResources() {
        logger.info("Extracting default resources...");
        
        // Create menus directory
        createDirectory("menus");
        
        // Extract menu files
        extractResource("menus/example.yml");
        extractResource("menus/example_advanced.yml");
        extractResource("menus/storage.yml");
        extractResource("menus/sell.yml");
        
        // Extract order creation menus
        extractResource("menus/order_quantity.yml");
        extractResource("menus/order_price.yml");
        extractResource("menus/order_confirm.yml");
        
        // Extract orders menu files
        extractResource("menus/orders_categories.yml");
        extractResource("menus/orders_cultures.yml");
        extractResource("menus/orders_loots.yml");
        extractResource("menus/orders_items.yml");
        extractResource("menus/orders_potions.yml");
        extractResource("menus/orders_armures.yml");
        extractResource("menus/orders_outils.yml");
        extractResource("menus/orders_autre.yml");
        
        // Extract history and statistics menus (NEW)
        extractResource("menus/history.yml");
        extractResource("menus/statistics.yml");
        
        // Extract rune equipment menu (NEW)
        extractResource("menus/rune_equipment.yml");
        
        // Extract orders template
        extractResource("orders_template.yml");
        
        // Extract message files
        extractResource("messages.yml");
        extractResource("messages_gui.yml");
        
        // Extract zones config
        extractResource("zones.yml");
        
        // Extract scoreboard config
        extractResource("scoreboard.yml");
        
        // Extract dungeon config
        extractResource("dungeon.yml");
        
        // Extract dungeon armor config (NEW)
        extractResource("dungeon_armor.yml");
        
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
    
    private void registerListeners() {
        logger.info("Registering listeners...");
        
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        logger.success("✓ Menu Listener");
        
        Bukkit.getPluginManager().registerEvents(new StorageListener(this), this);
        logger.success("✓ Storage Listener");
        
        // Register ScoreboardListener for auto-scoreboard on join
        if (scoreboardManager != null) {
            Bukkit.getPluginManager().registerEvents(new ScoreboardListener(this), this);
            logger.success("✓ Scoreboard Listener (auto-enable on join)");
        }
        
        // Register ZoneListener if WorldGuard is available
        if (zoneManager != null) {
            Bukkit.getPluginManager().registerEvents(new ZoneListener(this), this);
            logger.success("✓ Zone Listener");
        }
        
        // Register ArmorListener
        Bukkit.getPluginManager().registerEvents(new ArmorListener(this), this);
        logger.success("✓ Armor Listener (bonus system)");

        Bukkit.getPluginManager().registerEvents(new RuneApplyListener(runeItem, dungeonArmor), this);
        logger.success("✓ Rune Apply Listener (drag & drop runes)");
    }
   
    private void initializeManagers() {
        logger.info("Initializing managers...");
        
        // Database
        this.databaseManager = new DatabaseManager(this);
        logger.success("✓ Database Manager");
        
        // Storage
        this.storageManager = new StorageManager(this);
        logger.success("✓ Storage Manager");
        
        // Price Manager
        this.priceManager = new PriceManager(this);
        logger.success("✓ Price Manager");
        
        // Vault Manager
        this.vaultManager = new VaultManager(this);
        logger.success("✓ Vault Manager");
        
        // Sell Manager
        this.sellManager = new SellManager(this);
        logger.success("✓ Sell Manager");
        
        // Order Manager
        this.orderManager = new OrderManager(this);
        logger.success("✓ Order Manager");
        
        // Order Creation Manager
        this.orderCreationManager = new OrderCreationManager(this);
        logger.success("✓ Order Creation Manager");
        
        // Order Button Handler
        this.orderButtonHandler = new OrderButtonHandler(this);
        logger.success("✓ Order Button Handler");
        
        // Order Acceptance Handler (NEW)
        this.orderAcceptanceHandler = new OrderAcceptanceHandler(this);
        logger.success("✓ Order Acceptance Handler");
        
        // Transaction History Manager (NEW)
        this.transactionHistoryManager = new TransactionHistoryManager(this);
        logger.success("✓ Transaction History Manager");
        
        // Trading Statistics Manager (NEW)
        this.tradingStatisticsManager = new TradingStatisticsManager(this);
        logger.success("✓ Trading Statistics Manager");
        
        // Statistics Manager
        this.statisticsManager = new StatisticsManager(this);
        logger.success("✓ Statistics Manager");
        
        // Storage Stats Manager
        this.storageStatsManager = new StorageStatsManager(this);
        logger.success("✓ Storage Stats Manager");
        
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
        
        // Action Executor
        this.actionExecutor = new ActionExecutor(this);
        logger.success("✓ Action Executor");
        
        // Menu (must be last - depends on other managers)
        this.menuManager = new MenuManager(this);
        logger.success("✓ Menu Manager (" + menuManager.getMenuCount() + " menus loaded)");
    }
    
    /**
     * Register PlaceholderAPI expansion for storage amount placeholders
     */
    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("Registering PlaceholderAPI expansions...");
            
            // Register legacy expansion (storage amounts)
            this.placeholderExpansion = new QuantumPlaceholderExpansion(this);
            if (placeholderExpansion.register()) {
                logger.success("✓ QuantumPlaceholderExpansion registered");
                logger.info("  - %quantum_amt_nexo-<id>%");
                logger.info("  - %quantum_amt_minecraft-<id>%");
            }
            
            // Register new expansion (order creation + mode + stats + kills + towers)
            this.quantumExpansion = new QuantumExpansion(this);
            if (quantumExpansion.register()) {
                logger.success("✓ QuantumExpansion registered");
                logger.info("  - %quantum_mode%");
                logger.info("  - %quantum_mode_display%");
                logger.info("  - %quantum_order_*% (order placeholders)");
                logger.info("  - %quantum_stats_*% (statistics placeholders)");
                logger.info("  - %quantum_history_*% (history placeholders)");
                logger.info("  - %quantum_top_*% (top rankings placeholders)");
                logger.info("  - %quantum_killed_<mob>_<amount>% (kill tracking)");
                logger.info("  - %quantum_tower_*% (tower progression placeholders)");
            }
        }
    }
    
    private void registerCommands() {
        logger.info("Registering commands...");
        
        // Existing commands
        getCommand("quantum").setExecutor(new QuantumCommand(this));
        getCommand("storage").setExecutor(new StorageCommand(this));
        getCommand("menu").setExecutor(new MenuCommand(this));
        getCommand("qstorage").setExecutor(new QuantumStorageCommand(this));
        
        // Scoreboard toggle command
        getCommand("qscoreboard").setExecutor(new QScoreboardCommand(this));
        getCommand("qscoreboard").setTabCompleter(new QScoreboardTabCompleter(this));
        logger.success("✓ Scoreboard Command + TabCompleter");
        
        // Orders system commands
        getCommand("rechercher").setExecutor(new RechercherCommand(this));
        getCommand("recherche").setExecutor(new RechercheCommand(this));
        getCommand("offre").setExecutor(new OffreCommand(this));
        
        // Zone exit command (console only)
        if (zoneManager != null) {
            getCommand("zoneexit").setExecutor(new ZoneExitCommand(this));
            logger.success("✓ Zone Exit Command");
        }
        
        // Tower command
        if (towerManager != null) {
            getCommand("tower").setExecutor(new TowerCommand(this));
            getCommand("tower").setTabCompleter(new TowerTabCompleter(this));
            logger.success("✓ Tower Command + TabCompleter");
        }
        
        // Armor command
        getCommand("armor").setExecutor(new ArmorCommand(this));
        getCommand("armor").setTabCompleter(new ArmorTabCompleter());
        logger.success("✓ Armor Command + TabCompleter (dungeon system)");
        
        // (Optionnel) alias /armure si tu le veux comme commande séparée
        if (getCommand("armure") != null) {
            getCommand("armure").setExecutor(new ArmorCommand(this));
            getCommand("armure").setTabCompleter(new ArmorTabCompleter());
            logger.success("✓ Armure Command + TabCompleter (alias FR)");
        }
        
        // Rune command
        getCommand("rune").setExecutor(new RuneCommand(this));
        getCommand("rune").setTabCompleter(new QuantumArmorRuneTabCompleter());
        logger.success("✓ Rune Command + TabCompleter");

        // Register TabCompleters
        getCommand("quantum").setTabCompleter(new QuantumTabCompleter(this));
        getCommand("storage").setTabCompleter(new StorageTabCompleter());
        getCommand("menu").setTabCompleter(new MenuTabCompleter(this));
        getCommand("qstorage").setTabCompleter(new QuantumStorageTabCompleter(this));
        getCommand("rechercher").setTabCompleter(new RechercherTabCompleter());
        getCommand("recherche").setTabCompleter(new RechercheTabCompleter(this));
        getCommand("offre").setTabCompleter(new OffreTabCompleter(this));
        
        logger.success("✓ Commands registered");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling Quantum...");
        
        // Unregister PlaceholderAPI expansions
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        if (quantumExpansion != null) {
            quantumExpansion.unregister();
        }
        
        // Stop animations
        if (animationManager != null) {
            animationManager.stopAll();
        }
        
        // Clear sell sessions
        if (sellManager != null) {
            sellManager.clearAllSessions();
        }
        
        // Shutdown scoreboard handler
        if (scoreboardHandler != null) {
            scoreboardHandler.shutdown();
            logger.success("✓ Tower scoreboards cleared");
        }
        
        // Save escrow data
        if (escrowManager != null) {
            escrowManager.saveEscrow();
            logger.success("✓ Escrow data saved (" + escrowManager.getTotalEscrow() + "€)");
        }
        
        // Save tower progress
        if (towerManager != null) {
            towerManager.saveProgress();
            logger.success("✓ Tower progress saved");
        }
        
        // Save transaction history
        if (transactionHistoryManager != null) {
            logger.success("✓ Transaction history persisted (real-time saves)");
        }
        
        // Save statistics
        if (statisticsManager != null) {
            statisticsManager.saveStatistics();
            logger.success("✓ Statistics saved");
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
        
        // Reload core managers
        if (storageManager != null) storageManager.reload();
        if (menuManager != null) menuManager.reload();
        if (animationManager != null) animationManager.reload();
        
        // Reload message managers
        if (messagesManager != null) messagesManager.reload();
        if (messageManager != null) messageManager.reload();
        if (guiMessageManager != null) guiMessageManager.reload();
        
        // Reload scoreboard config
        if (scoreboardConfig != null) scoreboardConfig.reload();
        
        // Reload economy & orders
        if (escrowManager != null) escrowManager.reload();
        if (priceManager != null) priceManager.reload();
        if (orderManager != null) orderManager.loadItems();
        
        // Reload statistics
        if (statisticsManager != null) statisticsManager.loadStatistics();
        
        // Reload WorldGuard features
        if (zoneManager != null) zoneManager.reloadConfig();
        if (towerManager != null) towerManager.reload();
        
        // Reload dungeon armor & rune system
        RuneType.init(this); // Recharge dungeon.yml
        logger.success("✓ Dungeon armor & rune configs reloaded");
        
        logger.success("Quantum reloaded successfully!");
    }

    // === GETTERS ===

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerLevelManager getPlayerLevelManager() {
        return playerLevelManager;
    }
    
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

    @Deprecated
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public GuiMessageManager getGuiMessageManager() {
        return guiMessageManager;
    }
    
    public EscrowManager getEscrowManager() {
        return escrowManager;
    }

    public PriceManager getPriceManager() {
        return priceManager;
    }
    
    public VaultManager getVaultManager() {
        return vaultManager;
    }
    
    public SellManager getSellManager() {
        return sellManager;
    }
    
    public OrderManager getOrderManager() {
        return orderManager;
    }
    
    public OrderCreationManager getOrderCreationManager() {
        return orderCreationManager;
    }
    
    public OrderButtonHandler getOrderButtonHandler() {
        return orderButtonHandler;
    }
    
    public OrderAcceptanceHandler getOrderAcceptanceHandler() {
        return orderAcceptanceHandler;
    }
    
    public TransactionHistoryManager getTransactionHistoryManager() {
        return transactionHistoryManager;
    }
    
    public TradingStatisticsManager getTradingStatisticsManager() {
        return tradingStatisticsManager;
    }
    
    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
    
    public StorageStatsManager getStorageStatsManager() {
        return storageStatsManager;
    }
    
    public ActionExecutor getActionExecutor() {
        return actionExecutor;
    }
    
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
    
    public KillTracker getKillTracker() {
        return killTracker;
    }
    
    public TowerManager getTowerManager() {
        return towerManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public ScoreboardConfig getScoreboardConfig() {
        return scoreboardConfig;
    }
    
    public TowerScoreboardHandler getScoreboardHandler() {
        return scoreboardHandler;
    }
    
    public DungeonArmor getDungeonArmor() {
        return dungeonArmor;
    }
    
    public ArmorManager getArmorManager() {
        return armorManager;
    }
    
    public RuneItem getRuneItem() {
        return runeItem;
    }
}
