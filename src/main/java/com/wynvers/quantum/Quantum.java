package com.wynvers.quantum;

import com.wynvers.quantum.armor.ArmorListener;
import com.wynvers.quantum.armor.ArmorManager;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneApplyListener;
import com.wynvers.quantum.armor.RuneItem;
import com.wynvers.quantum.armor.RuneType;
import com.wynvers.quantum.commands.*;
import com.wynvers.quantum.database.DatabaseManager;
import com.wynvers.quantum.levels.PlayerLevelListener;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.listeners.MenuListener;
import com.wynvers.quantum.listeners.ScoreboardListener;
import com.wynvers.quantum.listeners.StorageListener;
import com.wynvers.quantum.managers.*;
import com.wynvers.quantum.orders.OrderAcceptanceHandler;
import com.wynvers.quantum.orders.OrderButtonHandler;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.placeholder.QuantumPlaceholderExpansion;
import com.wynvers.quantum.placeholders.QuantumExpansion;
import com.wynvers.quantum.sell.SellManager;
import com.wynvers.quantum.statistics.StatisticsManager;
import com.wynvers.quantum.statistics.StorageStatsManager;
import com.wynvers.quantum.statistics.TradingStatisticsManager;
import com.wynvers.quantum.tabcompleters.*;
import com.wynvers.quantum.towers.TowerDamageListener;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.utils.ActionExecutor;
import com.wynvers.quantum.utils.Logger;
import com.wynvers.quantum.worldguard.KillTracker;
import com.wynvers.quantum.worldguard.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Quantum - Advanced Virtual Storage & Dynamic GUI Builder
 */
public final class Quantum extends JavaPlugin {

    private static Quantum instance;
    private Logger logger;
    private ScoreboardManager scoreboardManager;
    private ScoreboardConfig scoreboardConfig;

    // Managers
    private DatabaseManager databaseManager;
    private PlayerLevelManager playerLevelManager;

    private StorageManager storageManager;
    private MenuManager menuManager;
    private PlaceholderManager placeholderManager;
    private AnimationManager animationManager;
    private MessageManager messageManager;        // NEW: System messages
    private GuiMessageManager guiMessageManager;  // NEW: GUI messages
    private MessagesManager messagesManager;      // Legacy messages
    private EscrowManager escrowManager;          // NEW: Escrow system
    private PriceManager priceManager;
    private VaultManager vaultManager;
    private SellManager sellManager;
    private OrderManager orderManager;
    private OrderCreationManager orderCreationManager;
    private OrderButtonHandler orderButtonHandler;
    private OrderAcceptanceHandler orderAcceptanceHandler; // NEW
    private StatisticsManager statisticsManager;
    private StorageStatsManager storageStatsManager;
    private TransactionHistoryManager transactionHistoryManager;   // NEW
    private TradingStatisticsManager tradingStatisticsManager;     // NEW
    private ZoneManager zoneManager;        // NEW: WorldGuard tower zones
    private KillTracker killTracker;       // Kill tracking for (anciens) systèmes
    private TowerManager towerManager;     // Tower progression system
    private TowerScoreboardHandler scoreboardHandler; // Tower scoreboard

    private DungeonArmor dungeonArmor;     // Dungeon armor system
    private ArmorManager armorManager;
    private RuneItem runeItem;

    // Utils
    private ActionExecutor actionExecutor;

    // PlaceholderAPI expansions
    private QuantumPlaceholderExpansion placeholderExpansion;
    private QuantumExpansion quantumExpansion;

    @Override
    public void onEnable() {
        instance = this;

        // Logger
        this.logger = new Logger("Quantum");
        logger.info("┌───────────────────────────────────┐");
        logger.info("│  §6§lQUANTUM §f- Advanced Storage │");
        logger.info("│       §7v1.0.1 by Kazotaruu_      │");
        logger.info("└───────────────────────────────────┘");

        // Ressources + config
        extractDefaultResources();
        saveDefaultConfig();

        // Database + niveaux joueurs
        this.databaseManager = new DatabaseManager(this);
        this.playerLevelManager = new PlayerLevelManager(this, databaseManager);
        getServer().getPluginManager().registerEvents(
                new PlayerLevelListener(this, playerLevelManager), this
        );

        // Messages
        this.messageManager = new MessageManager(this);
        this.guiMessageManager = new GuiMessageManager(this);
        this.messagesManager = new MessagesManager(this);

        // Scoreboard global
        this.scoreboardConfig = new ScoreboardConfig(this);
        this.scoreboardManager = new ScoreboardManager(this);
        logger.success("✓ Scoreboard Config & Manager initialized!");

        // Dungeon Armor & Runes
        this.dungeonArmor = new DungeonArmor(this);
        RuneType.init(this);
        this.armorManager = new ArmorManager(this, dungeonArmor);
        this.runeItem = new RuneItem(this);
        logger.success("✓ Dungeon Armor & Rune system initialized! (9 runes with 3 levels)");

        // Tours
        this.towerManager = new TowerManager(this);
        getServer().getPluginManager().registerEvents(new TowerDamageListener(this), this);

        // WorldGuard / zones de tours
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            this.killTracker = new KillTracker(this);
            this.scoreboardHandler = new TowerScoreboardHandler(this);
            this.zoneManager = new ZoneManager(this); // s’enregistre lui-même en listener

            logger.success("✓ WorldGuard integration enabled!");
            logger.success("✓ Tower system loaded! (" + towerManager.getTowerCount() + " tours)");
            logger.success("✓ Integrated tower scoreboard ready!");
        } else {
            logger.warning("⚠ WorldGuard not found - zone restriction and tower features disabled");
        }

        // Managers généraux
        initializeManagers();

        // PlaceholderAPI
        registerPlaceholderExpansion();

        // Listeners globaux (hors tours / niveaux)
        registerListeners();

        // Commandes
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
    }

    // ───────────────────── Ressources ─────────────────────

    private void extractDefaultResources() {
        logger.info("Extracting default resources...");

        createDirectory("menus");

        // Menus
        extractResource("menus/example.yml");
        extractResource("menus/example_advanced.yml");
        extractResource("menus/storage.yml");
        extractResource("menus/sell.yml");

        extractResource("menus/order_quantity.yml");
        extractResource("menus/order_price.yml");
        extractResource("menus/order_confirm.yml");

        extractResource("menus/orders_categories.yml");
        extractResource("menus/orders_cultures.yml");
        extractResource("menus/orders_loots.yml");
        extractResource("menus/orders_items.yml");
        extractResource("menus/orders_potions.yml");
        extractResource("menus/orders_armures.yml");
        extractResource("menus/orders_outils.yml");
        extractResource("menus/orders_autre.yml");

        extractResource("menus/history.yml");
        extractResource("menus/statistics.yml");
        extractResource("menus/rune_equipment.yml");

        // Templates / messages
        extractResource("orders_template.yml");
        extractResource("messages.yml");
        extractResource("messages_gui.yml");

        // Ancien zones.yml (optionnel, plus utilisé par les tours)
        extractResource("zones.yml");

        extractResource("scoreboard.yml");
        extractResource("dungeon.yml");
        extractResource("dungeon_armor.yml");

        logger.success("✓ Default resources extracted");
    }

    private void createDirectory(String path) {
        File dir = new File(getDataFolder(), path);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Created directory: " + path);
        }
    }

    private void extractResource(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (file.exists()) return;

        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Resource not found in JAR: " + resourcePath);
                return;
            }

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Extracted: " + resourcePath);

        } catch (IOException e) {
            logger.error("Failed to extract resource: " + resourcePath);
            e.printStackTrace();
        }
    }

    // ───────────────────── Listeners globaux ─────────────────────

    private void registerListeners() {
        logger.info("Registering listeners...");

        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        logger.success("✓ Menu Listener");

        Bukkit.getPluginManager().registerEvents(new StorageListener(this), this);
        logger.success("✓ Storage Listener");

        if (scoreboardManager != null) {
            Bukkit.getPluginManager().registerEvents(new ScoreboardListener(this), this);
            logger.success("✓ Scoreboard Listener (auto-enable on join)");
        }

        // ZoneListener supprimé : le nouveau ZoneManager gère lui-même ses events

        Bukkit.getPluginManager().registerEvents(new ArmorListener(this), this);
        logger.success("✓ Armor Listener (bonus system)");

        Bukkit.getPluginManager().registerEvents(new RuneApplyListener(runeItem, dungeonArmor), this);
        logger.success("✓ Rune Apply Listener (drag & drop runes)");
    }

    // ───────────────────── Managers ─────────────────────

    private void initializeManagers() {
        logger.info("Initializing managers...");

        // Database déjà initialisée dans onEnable
        logger.success("✓ Database Manager");

        this.storageManager = new StorageManager(this);
        logger.success("✓ Storage Manager");

        this.priceManager = new PriceManager(this);
        logger.success("✓ Price Manager");

        this.vaultManager = new VaultManager(this);
        logger.success("✓ Vault Manager");

        this.sellManager = new SellManager(this);
        logger.success("✓ Sell Manager");

        this.orderManager = new OrderManager(this);
        logger.success("✓ Order Manager");

        this.orderCreationManager = new OrderCreationManager(this);
        logger.success("✓ Order Creation Manager");

        this.orderButtonHandler = new OrderButtonHandler(this);
        logger.success("✓ Order Button Handler");

        this.orderAcceptanceHandler = new OrderAcceptanceHandler(this);
        logger.success("✓ Order Acceptance Handler");

        this.escrowManager = new EscrowManager(this);
        logger.success("✓ Escrow Manager");

        this.transactionHistoryManager = new TransactionHistoryManager(this);
        logger.success("✓ Transaction History Manager");

        this.tradingStatisticsManager = new TradingStatisticsManager(this);
        logger.success("✓ Trading Statistics Manager");

        this.statisticsManager = new StatisticsManager(this);
        logger.success("✓ Statistics Manager");

        this.storageStatsManager = new StorageStatsManager(this);
        logger.success("✓ Storage Stats Manager");

        this.animationManager = new AnimationManager(this);
        logger.success("✓ Animation Manager");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderManager = new PlaceholderManager(this);
            logger.success("✓ Placeholder Manager (PlaceholderAPI found)");
        } else {
            logger.warning("⚠ PlaceholderAPI not found - placeholder features disabled");
        }

        this.actionExecutor = new ActionExecutor(this);
        logger.success("✓ Action Executor");

        this.menuManager = new MenuManager(this);
        logger.success("✓ Menu Manager (" + menuManager.getMenuCount() + " menus loaded)");
    }

    // ───────────────────── PlaceholderAPI ─────────────────────

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        logger.info("Registering PlaceholderAPI expansions...");

        this.placeholderExpansion = new QuantumPlaceholderExpansion(this);
        if (placeholderExpansion.register()) {
            logger.success("✓ QuantumPlaceholderExpansion registered");
        }

        this.quantumExpansion = new QuantumExpansion(this);
        if (quantumExpansion.register()) {
            logger.success("✓ QuantumExpansion registered");
        }
    }

    // ───────────────────── Commandes ─────────────────────

    private void registerCommands() {
        logger.info("Registering commands...");

        getCommand("quantum").setExecutor(new QuantumCommand(this));
        getCommand("storage").setExecutor(new StorageCommand(this));
        getCommand("menu").setExecutor(new MenuCommand(this));
        getCommand("qstorage").setExecutor(new QuantumStorageCommand(this));

        getCommand("qscoreboard").setExecutor(new QScoreboardCommand(this));
        getCommand("qscoreboard").setTabCompleter(new QScoreboardTabCompleter(this));
        logger.success("✓ Scoreboard Command + TabCompleter");

        getCommand("rechercher").setExecutor(new RechercherCommand(this));
        getCommand("recherche").setExecutor(new RechercheCommand(this));
        getCommand("offre").setExecutor(new OffreCommand(this));

        // zoneexit retiré : l’ancien système de zones à kills est désactivé

        if (towerManager != null) {
            getCommand("tower").setExecutor(new TowerCommand(this));
            getCommand("tower").setTabCompleter(new TowerTabCompleter(this));
            logger.success("✓ Tower Command + TabCompleter");
        }

        getCommand("armor").setExecutor(new ArmorCommand(this));
        getCommand("armor").setTabCompleter(new ArmorTabCompleter());
        logger.success("✓ Armor Command + TabCompleter");

        if (getCommand("armure") != null) {
            getCommand("armure").setExecutor(new ArmorCommand(this));
            getCommand("armure").setTabCompleter(new ArmorTabCompleter());
        }

        getCommand("rune").setExecutor(new RuneCommand(this));
        getCommand("rune").setTabCompleter(new QuantumArmorRuneTabCompleter());

        getCommand("quantum").setTabCompleter(new QuantumTabCompleter(this));
        getCommand("storage").setTabCompleter(new StorageTabCompleter());
        getCommand("menu").setTabCompleter(new MenuTabCompleter(this));
        getCommand("qstorage").setTabCompleter(new QuantumStorageTabCompleter(this));
        getCommand("rechercher").setTabCompleter(new RechercherTabCompleter());
        getCommand("recherche").setTabCompleter(new RechercheTabCompleter(this));
        getCommand("offre").setTabCompleter(new OffreTabCompleter(this));

        logger.success("✓ Commands registered");
    }

    // ───────────────────── onDisable / reload ─────────────────────

    @Override
    public void onDisable() {
        logger.info("Disabling Quantum...");

        if (placeholderExpansion != null) placeholderExpansion.unregister();
        if (quantumExpansion != null) quantumExpansion.unregister();

        if (animationManager != null) animationManager.stopAll();
        if (sellManager != null) sellManager.clearAllSessions();

        if (scoreboardHandler != null) {
            scoreboardHandler.shutdown();
            logger.success("✓ Tower scoreboards cleared");
        }

        if (escrowManager != null) {
            escrowManager.saveEscrow();
            logger.success("✓ Escrow data saved (" + escrowManager.getTotalEscrow() + "€)");
        }

        if (towerManager != null) {
            towerManager.saveProgress();
            logger.success("✓ Tower progress saved");
        }

        if (statisticsManager != null) {
            statisticsManager.saveStatistics();
            logger.success("✓ Statistics saved");
        }

        if (storageManager != null) {
            storageManager.saveAll();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        logger.info("Quantum disabled. Goodbye!");
    }

    public void reloadPlugin() {
        logger.info("Reloading Quantum...");

        reloadConfig();

        if (storageManager != null) storageManager.reload();
        if (menuManager != null) menuManager.reload();
        if (animationManager != null) animationManager.reload();

        if (messagesManager != null) messagesManager.reload();
        if (messageManager != null) messageManager.reload();
        if (guiMessageManager != null) guiMessageManager.reload();

        if (scoreboardConfig != null) scoreboardConfig.reload();

        if (escrowManager != null) escrowManager.reload();
        if (priceManager != null) priceManager.reload();
        if (orderManager != null) orderManager.loadItems();

        if (statisticsManager != null) statisticsManager.loadStatistics();

        // zoneManager.reloadConfig() supprimé (nouveau ZoneManager n’a plus cette méthode)
        if (towerManager != null) towerManager.reload();

        RuneType.init(this);
        logger.success("✓ Dungeon armor & rune configs reloaded");

        logger.success("Quantum reloaded successfully!");
    }

    // ───────────────────── GETTERS ─────────────────────

    public static Quantum getInstance() {
        return instance;
    }

    public Logger getQuantumLogger() {
        return logger;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerLevelManager getPlayerLevelManager() {
        return playerLevelManager;
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

    public TowerScoreboardHandler getTowerScoreboardHandler() {
        return scoreboardHandler;
    }

    // alias si tu veux garder l’ancien nom
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
