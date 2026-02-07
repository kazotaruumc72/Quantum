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
import com.wynvers.quantum.listeners.TowerKillListener;
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

        // Configuration des tours (TowerManager)
        extractResource("towers.yml");

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

    // ... (reste du fichier inchangé)

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
