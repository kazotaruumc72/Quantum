package com.wynvers.quantum;

import com.wynvers.quantum.armor.ArmorListener;
import com.wynvers.quantum.armor.ArmorManager;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneApplyListener;
import com.wynvers.quantum.armor.RuneItem;
import com.wynvers.quantum.armor.RuneType;
import com.wynvers.quantum.betterhud.BetterHudListener;
import com.wynvers.quantum.betterhud.QuantumBetterHudManager;
import com.wynvers.quantum.betterhud.QuantumCompassManager;
import com.wynvers.quantum.furniture.FurnitureManager;
import com.wynvers.quantum.furniture.FurnitureListener;
import com.wynvers.quantum.crops.CustomCropManager;
import com.wynvers.quantum.crops.CustomCropListener;
import com.wynvers.quantum.tools.ToolManager;
import com.wynvers.quantum.tools.ToolListener;
import com.wynvers.quantum.tools.StructureManager;
import com.wynvers.quantum.tools.StructureSelectionManager;
import com.wynvers.quantum.tools.StructureSelectionListener;
import com.wynvers.quantum.weapon.DungeonWeapon;
import com.wynvers.quantum.weapon.DungeonWeaponListener;
import com.wynvers.quantum.jobs.JobManager;
import com.wynvers.quantum.jobs.JobListener;
import com.wynvers.quantum.jobs.JobActionListener;
import com.wynvers.quantum.jobs.JobCommand;
import com.wynvers.quantum.jobs.JobAdminCommand;
import com.wynvers.quantum.jobs.JobTabCompleter;
import com.wynvers.quantum.jobs.JobAdminTabCompleter;
import com.wynvers.quantum.home.HomeManager;
import com.wynvers.quantum.tab.TABManager;
import com.wynvers.quantum.placeholderapi.PlaceholderAPIManager;
import com.wynvers.quantum.worldguard.gui.ZoneGUIManager;
import com.wynvers.quantum.worldguard.gui.ZoneSettingsGUI;
import com.wynvers.quantum.apartment.ApartmentManager;
import com.wynvers.quantum.commands.*;
import com.wynvers.quantum.database.DatabaseManager;
import com.wynvers.quantum.healthbar.HealthBarListener;
import com.wynvers.quantum.healthbar.HealthBarManager;
import com.wynvers.quantum.levels.PlayerLevelListener;
import com.wynvers.quantum.levels.PlayerLevelManager;
import com.wynvers.quantum.listeners.DoorSelectionListener;
import com.wynvers.quantum.listeners.MenuListener;
import com.wynvers.quantum.listeners.ScoreboardListener;
import com.wynvers.quantum.listeners.SpawnSelectionListener;
import com.wynvers.quantum.listeners.StorageListener;
import com.wynvers.quantum.listeners.TowerKillListener;
import com.wynvers.quantum.managers.*;
import com.wynvers.quantum.menu.StorageSettingsMenuListener;
import com.wynvers.quantum.orders.OrderAcceptanceHandler;
import com.wynvers.quantum.orders.OrderButtonHandler;
import com.wynvers.quantum.orders.OrderCreationManager;
import com.wynvers.quantum.sell.SellManager;
import com.wynvers.quantum.statistics.StatisticsManager;
import com.wynvers.quantum.statistics.StorageStatsManager;
import com.wynvers.quantum.statistics.TradingStatisticsManager;
import com.wynvers.quantum.storage.upgrades.StorageUpgradeManager;
import com.wynvers.quantum.tabcompleters.*;
import com.wynvers.quantum.towers.*;
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.utils.ActionExecutor;
import com.wynvers.quantum.utils.Logger;
import com.wynvers.quantum.worldguard.KillTracker;
import com.wynvers.quantum.worldguard.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private ScoreboardListener scoreboardListener;

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
    private ZoneManager zoneManager;        // Tower zone management (supports WorldGuard or internal)
    private KillTracker killTracker;       // Kill tracking for (anciens) systèmes
    private TowerManager towerManager;     // Tower progression system
    private TowerScoreboardHandler scoreboardHandler; // Tower scoreboard
    private com.wynvers.quantum.regions.InternalRegionManager internalRegionManager; // Internal region system
    private MobSkillManager mobSkillManager;          // NEW: Mob skills titles/subtitles
    private MobSkillExecutor mobSkillExecutor;        // NEW: Mob skills execution
    private TowerDoorManager doorManager;
    private TowerNPCManager npcManager;
    private TowerLootManager lootManager;
    private MobAnimationManager mobAnimationManager;  // NEW: Mob animations
    private SpawnSelectionManager spawnSelectionManager; // NEW: spawn zone selection
    private HealthBarManager healthBarManager;       // NEW: Health bar display system
    private StorageUpgradeManager storageUpgradeManager;


    private DungeonArmor dungeonArmor;     // Dungeon armor system
    private ArmorManager armorManager;
    private RuneItem runeItem;
    
    // NEW: Furniture, Crops, Tools, and Weapon systems
    private FurnitureManager furnitureManager;
    private CustomCropManager customCropManager;
    private ToolManager toolManager;
    private StructureManager structureManager;
    private StructureSelectionManager structureSelectionManager;
    private DungeonWeapon dungeonWeapon;
    
    // Jobs System
    private JobManager jobManager;
    
    // Home System
    private HomeManager homeManager;
    
    // Spawn System
    private com.wynvers.quantum.spawn.SpawnManager spawnManager;
    
    // TAB Integration
    private TABManager tabManager;
    
    // PlaceholderAPI Integration
    private PlaceholderAPIManager placeholderAPIManager;
    
    // WorldGuard Zone GUI
    private ZoneGUIManager zoneGUIManager;
    private ZoneSettingsGUI zoneSettingsGUI;
    
    // Apartment System (preparation phase)
    private ApartmentManager apartmentManager;
    // BetterHud Integration
    private com.wynvers.quantum.betterhud.QuantumBetterHudManager betterHudManager;
    private com.wynvers.quantum.betterhud.QuantumCompassManager compassManager;

    // Utils
    private ActionExecutor actionExecutor;

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

        // HealthBar System
        this.healthBarManager = new HealthBarManager(this);
        logger.success("✓ HealthBar system initialized! (Mob health display)");

        // Tours
        this.towerManager = new TowerManager(this);
        getServer().getPluginManager().registerEvents(new TowerDamageListener(this), this);

        this.doorManager = new TowerDoorManager(this);
        this.npcManager = new TowerNPCManager(this);
        this.lootManager = new TowerLootManager(this);
        lootManager.loadFromConfig(YamlConfiguration.loadConfiguration(new File(getDataFolder(), "towers.yml")));
        
        // Mob Animation Manager
        this.mobAnimationManager = new MobAnimationManager(this);
        logger.success("✓ Mob Animation Manager initialized!");
        
        // Spawn Selection Manager + Listener (hache netherite pour définir zones de spawn)
        this.spawnSelectionManager = new SpawnSelectionManager();
        Bukkit.getPluginManager().registerEvents(
                new SpawnSelectionListener(this, spawnSelectionManager),
                this
        );
        logger.success("✓ Spawn Selection Manager + Listener initialized!");
        
        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new DoorSelectionListener(this, doorManager), this);

        // Mob Skills System
        this.mobSkillManager = new MobSkillManager(this);
        this.mobSkillExecutor = new MobSkillExecutor(this);

        // Internal Region Manager (works with or without WorldGuard)
        this.internalRegionManager = new com.wynvers.quantum.regions.InternalRegionManager(this);
        loadInternalRegions();
        
        // Tower zone management (supports both WorldGuard and internal regions)
        this.killTracker = new KillTracker(this);
        this.scoreboardHandler = new TowerScoreboardHandler(this);
        this.zoneManager = new ZoneManager(this); // s'enregistre lui-même en listener
        
        // WorldGuard GUI (only if WorldGuard is available)
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            // Zone GUI System
            this.zoneGUIManager = new ZoneGUIManager(this);
            this.zoneSettingsGUI = new ZoneSettingsGUI(this, zoneGUIManager);
            logger.success("✓ WorldGuard integration enabled!");
            logger.success("✓ Zone GUI system initialized!");
        }
        
        logger.success("✓ Tower system loaded! (" + towerManager.getTowerCount() + " tours)");
        logger.success("✓ Integrated tower scoreboard ready!");

        // Managers généraux
        initializeManagers();
        
        // NEW: Furniture, Crops, Tools, and Weapon systems
        initializeNewSystems();
        
        // TAB Integration
        this.tabManager = new TABManager(this);
        
        // PlaceholderAPI Integration
        this.placeholderAPIManager = new PlaceholderAPIManager(this);

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

        storageUpgradeManager = new StorageUpgradeManager();
        getServer().getPluginManager().registerEvents(
                new StorageSettingsMenuListener(this, storageUpgradeManager),
                this
        );
    }

    // ───────────────────── Ressources ─────────────────────

    private void extractDefaultResources() {
        logger.info("Extracting default resources...");

        createDirectory("menus");

        // Menus
        extractResource("menus/example.yml");
        extractResource("menus/example_advanced.yml");
        extractResource("menus/storage.yml");
        extractResource("menus/storage_settings.yml");
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
        
        // NEW: Furniture, Crops, Tools, and Weapon configs
        extractResource("furniture.yml");
        extractResource("custom_crops.yml");
        extractResource("tools.yml");
        extractResource("structures.yml");
        extractResource("dungeon_weapon.yml");
        extractResource("jobs.yml");
        extractResource("zone_configs.yml");

        // Ancien zones.yml (optionnel, plus utilisé par les tours)
        extractResource("zones.yml");

        extractResource("scoreboard.yml");
        extractResource("dungeon.yml");
        extractResource("dungeon_armor.yml");

        // Configuration des tours (TowerManager)
        extractResource("towers.yml");
        
        // Configuration des skills de mobs (MobSkillManager)
        extractResource("mob_skills.yml");

        // Plugin integration examples
        createDirectory("betterhud-examples");
        extractResource("betterhud-examples/compass.yml");
        extractResource("betterhud-examples/config.yml");
        extractResource("betterhud-examples/huds.yml");
        extractResource("betterhud-examples/popups.yml");

        createDirectory("placeholderapi-examples");
        extractResource("placeholderapi-examples/chat_example.yml");
        extractResource("placeholderapi-examples/hologram_example.yml");
        extractResource("placeholderapi-examples/scoreboard_example.yml");
        extractResource("placeholderapi-examples/tab_example.yml");

        createDirectory("tab-examples");
        extractResource("tab-examples/config_example.yml");
        extractResource("tab-examples/scoreboard_example.yml");

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
            scoreboardListener = new ScoreboardListener(this);
            Bukkit.getPluginManager().registerEvents(scoreboardListener, this);
            logger.success("✓ Scoreboard Listener (auto-enable on join)");
        }

        // ZoneListener supprimé : le nouveau ZoneManager gère lui-même ses events

        Bukkit.getPluginManager().registerEvents(new ArmorListener(this), this);
        logger.success("✓ Armor Listener (bonus system)");

        Bukkit.getPluginManager().registerEvents(new RuneApplyListener(runeItem, dungeonArmor), this);
        logger.success("✓ Rune Apply Listener (drag & drop runes)");

        if (towerManager != null) {
            Bukkit.getPluginManager().registerEvents(
                    new TowerKillListener(this, towerManager, playerLevelManager, doorManager, lootManager), this
            );
            logger.success("✓ Tower Kill Listener (XP on mob kill)");
        }
        
        // HealthBar Listener
        if (healthBarManager != null) {
            Bukkit.getPluginManager().registerEvents(new HealthBarListener(this, healthBarManager), this);
            logger.success("✓ HealthBar Listener (mob health display)");
        }
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

        // Internal placeholder manager (no external dependency)
        this.placeholderManager = new PlaceholderManager(this);
        logger.success("✓ Internal Placeholder Manager initialized");

        this.actionExecutor = new ActionExecutor(this);
        logger.success("✓ Action Executor");
        
        this.homeManager = new HomeManager(this, databaseManager);
        logger.success("✓ Home Manager");
        
        this.spawnManager = new com.wynvers.quantum.spawn.SpawnManager(this, databaseManager);
        getServer().getPluginManager().registerEvents(new com.wynvers.quantum.spawn.FirstJoinListener(spawnManager), this);
        logger.success("✓ Spawn Manager");
        
        this.apartmentManager = new ApartmentManager(this, databaseManager);
        logger.success("✓ Apartment Manager (preparation phase)");

        this.menuManager = new MenuManager(this);
        logger.success("✓ Menu Manager (" + menuManager.getMenuCount() + " menus loaded)");
    }
    
    // ───────────────────── NEW Systems Initialization ─────────────────────
    
    private void initializeNewSystems() {
        logger.info("Initializing new systems (Furniture, Crops, Tools, Weapon)...");
        
        // Furniture System
        this.furnitureManager = new FurnitureManager(this);
        getServer().getPluginManager().registerEvents(new FurnitureListener(this, furnitureManager), this);
        logger.success("✓ Furniture System initialized!");
        
        // Custom Crops System
        this.customCropManager = new CustomCropManager(this);
        getServer().getPluginManager().registerEvents(new CustomCropListener(this, customCropManager), this);
        logger.success("✓ Custom Crops System initialized!");
        
        // Tools System
        this.toolManager = new ToolManager(this);
        this.structureManager = new StructureManager(this);
        this.structureSelectionManager = new StructureSelectionManager();
        getServer().getPluginManager().registerEvents(new ToolListener(this, toolManager), this);
        getServer().getPluginManager().registerEvents(new StructureSelectionListener(this, structureSelectionManager), this);
        logger.success("✓ Tools System initialized! (Pickaxe, Axe, Hoe)");
        logger.success("✓ Structure Selection System initialized!");
        
        // Dungeon Weapon System
        this.dungeonWeapon = new DungeonWeapon(this);
        getServer().getPluginManager().registerEvents(new DungeonWeaponListener(this, dungeonWeapon), this);
        logger.success("✓ Dungeon Weapon System initialized!");
        
        // Jobs System
        this.jobManager = new JobManager(this, databaseManager);
        getServer().getPluginManager().registerEvents(new JobListener(this, jobManager), this);
        getServer().getPluginManager().registerEvents(new JobActionListener(this, jobManager), this);
        logger.success("✓ Jobs System initialized! (" + jobManager.getAllJobs().size() + " jobs available)");
        
        // BetterHud Integration System
        if (Bukkit.getPluginManager().getPlugin("BetterHud") != null) {
            this.betterHudManager = new QuantumBetterHudManager(this);
            this.compassManager = new QuantumCompassManager(betterHudManager, this.getLogger());
            
            // Initialize BetterHud API after a short delay to ensure plugin is fully loaded
            Bukkit.getScheduler().runTaskLater(this, () -> {
                betterHudManager.initialize();
            }, 20L); // 1 second delay
            
            // Register listener for cleanup
            getServer().getPluginManager().registerEvents(
                new BetterHudListener(betterHudManager, compassManager), 
                this
            );
            
            logger.success("✓ BetterHud Integration initialized! (Optimized HUD & Compass)");
        } else {
            logger.warning("⚠ BetterHud not found - HUD features disabled");
        }
    }
    
    // ───────────────────── Internal Regions Loading ─────────────────────
    
    /**
     * Load internal regions from towers.yml configuration
     * Regions are loaded from the tower configuration and registered with InternalRegionManager
     */
    private void loadInternalRegions() {
        File towersFile = new File(getDataFolder(), "towers.yml");
        if (!towersFile.exists()) {
            logger.warning("towers.yml not found - internal regions not loaded");
            return;
        }
        
        org.bukkit.configuration.file.FileConfiguration config = 
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(towersFile);
        
        org.bukkit.configuration.ConfigurationSection towersSection = config.getConfigurationSection("towers");
        if (towersSection == null) {
            logger.warning("No 'towers' section in towers.yml");
            return;
        }
        
        int regionsLoaded = 0;
        
        for (String towerId : towersSection.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection towerSection = towersSection.getConfigurationSection(towerId);
            if (towerSection == null) continue;
            
            // Load floor regions
            org.bukkit.configuration.ConfigurationSection floorsSection = towerSection.getConfigurationSection("floors");
            if (floorsSection == null) continue;
            
            for (String floorKey : floorsSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
                if (floorSection == null) continue;
                
                // Check if this floor has region coordinates (for internal system)
                String regionName = floorSection.getString("worldguard_region");
                if (regionName == null || regionName.isEmpty()) continue;
                
                // Try to load region from internal format
                org.bukkit.configuration.ConfigurationSection regionSection = floorSection.getConfigurationSection("region");
                if (regionSection != null) {
                    if (internalRegionManager.loadRegionFromConfig(regionName, regionSection)) {
                        regionsLoaded++;
                    }
                }
            }
        }
        
        // Also load from separate regions.yml if it exists
        File regionsFile = new File(getDataFolder(), "regions.yml");
        if (regionsFile.exists()) {
            org.bukkit.configuration.file.FileConfiguration regionsConfig = 
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(regionsFile);
            
            org.bukkit.configuration.ConfigurationSection regionsSection = regionsConfig.getConfigurationSection("regions");
            if (regionsSection != null) {
                for (String regionId : regionsSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionId);
                    if (regionSection != null) {
                        if (internalRegionManager.loadRegionFromConfig(regionId, regionSection)) {
                            regionsLoaded++;
                        }
                    }
                }
            }
        }
        
        logger.success("✓ Internal Regions loaded! (" + regionsLoaded + " regions registered)");
    }

    // ───────────────────── Commandes ─────────────────────

    private void registerCommands() {
        logger.info("Registering commands...");

        // /quantum command with QuantumCommand executor and QuantumTabCompleter
        PluginCommand quantumCmd = getCommand("quantum");
        QuantumCommand quantumExecutor = new QuantumCommand(this);
        quantumCmd.setExecutor(quantumExecutor);
        quantumCmd.setTabCompleter(new QuantumTabCompleter(this));
        logger.success("✓ Quantum Command + TabCompleter");

        getCommand("storage").setExecutor(new StorageCommand(this));
        getCommand("storage").setTabCompleter(new StorageTabCompleter());
        
        getCommand("menu").setExecutor(new MenuCommand(this));
        getCommand("menu").setTabCompleter(new MenuTabCompleter(this));
        
        getCommand("qstorage").setExecutor(new QuantumStorageCommand(this));
        getCommand("qstorage").setTabCompleter(new QuantumStorageTabCompleter(this));

        getCommand("qscoreboard").setExecutor(new QScoreboardCommand(this));
        getCommand("qscoreboard").setTabCompleter(new QScoreboardTabCompleter(this));
        logger.success("✓ Scoreboard Command + TabCompleter");

        getCommand("rechercher").setExecutor(new RechercherCommand(this));
        getCommand("rechercher").setTabCompleter(new RechercherTabCompleter());
        
        getCommand("recherche").setExecutor(new RechercheCommand(this));
        getCommand("recherche").setTabCompleter(new RechercheTabCompleter(this));
        
        getCommand("offre").setExecutor(new OffreCommand(this));
        getCommand("offre").setTabCompleter(new OffreTabCompleter(this));

        // zoneexit retiré : l'ancien système de zones à kills est désactivé

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

        getCommand("qexp").setExecutor(new QexpCommand(this, playerLevelManager));
        getCommand("qexp").setTabCompleter(new QexpTabCompleter());
        
        // HealthBar Command
        if (healthBarManager != null) {
            getCommand("healthbar").setExecutor(new HealthBarCommand(this, healthBarManager));
            getCommand("healthbar").setTabCompleter(new HealthBarTabCompleter());
            logger.success("✓ HealthBar Command + TabCompleter");
        }
        
        // NEW: Tool and Weapon Commands
        if (toolManager != null) {
            getCommand("tool").setExecutor(new ToolCommand(this));
            getCommand("tool").setTabCompleter(new ToolTabCompleter());
            logger.success("✓ Tool Command + TabCompleter");
        }
        
        if (dungeonWeapon != null) {
            getCommand("weapon").setExecutor(new WeaponCommand(this));
            getCommand("weapon").setTabCompleter(new WeaponTabCompleter());
            logger.success("✓ Weapon Command + TabCompleter");
        }
        
        // Jobs Commands
        if (jobManager != null) {
            getCommand("job").setExecutor(new JobCommand(this, jobManager));
            getCommand("job").setTabCompleter(new JobTabCompleter(jobManager));
            getCommand("jobadmin").setExecutor(new JobAdminCommand(this, jobManager));
            getCommand("jobadmin").setTabCompleter(new JobAdminTabCompleter(jobManager));
            logger.success("✓ Job Commands + TabCompleters");
        }
        
        // Gamemode Shortcuts
        GamemodeCommand gamemodeCommand = new GamemodeCommand();
        getCommand("gmc").setExecutor(gamemodeCommand);
        getCommand("gms").setExecutor(gamemodeCommand);
        getCommand("gmsp").setExecutor(gamemodeCommand);
        getCommand("gma").setExecutor(gamemodeCommand);
        logger.success("✓ Gamemode Shortcuts (gmc, gms, gmsp, gma)");
        
        // Home Commands
        if (homeManager != null) {
            HomeCommand homeCommand = new HomeCommand(homeManager);
            HomeTabCompleter homeTabCompleter = new HomeTabCompleter(homeManager);
            getCommand("home").setExecutor(homeCommand);
            getCommand("home").setTabCompleter(homeTabCompleter);
            getCommand("sethome").setExecutor(homeCommand);
            getCommand("sethome").setTabCompleter(homeTabCompleter);
            getCommand("delhome").setExecutor(homeCommand);
            getCommand("delhome").setTabCompleter(homeTabCompleter);
            logger.success("✓ Home Commands + TabCompleters");
        }
        
        // Spawn Command
        if (spawnManager != null) {
            com.wynvers.quantum.commands.SpawnCommand spawnCommand = new com.wynvers.quantum.commands.SpawnCommand(spawnManager);
            com.wynvers.quantum.tabcompleters.SpawnTabCompleter spawnTabCompleter = new com.wynvers.quantum.tabcompleters.SpawnTabCompleter();
            getCommand("spawn").setExecutor(spawnCommand);
            getCommand("spawn").setTabCompleter(spawnTabCompleter);
            logger.success("✓ Spawn Command + TabCompleter");
        }
        
        // Zone GUI Command
        if (zoneGUIManager != null && zoneSettingsGUI != null) {
            getCommand("zonegui").setExecutor(new ZoneGUICommand(this, zoneGUIManager, zoneSettingsGUI));
            logger.success("✓ Zone GUI Command");
        }
        
        // Apartment Command (preparation phase)
        if (apartmentManager != null) {
            getCommand("apartment").setExecutor(new ApartmentCommand(this, apartmentManager));
            logger.success("✓ Apartment Command (preparation phase)");
        }
        // BetterHud Demo Command
        if (betterHudManager != null && betterHudManager.isAvailable()) {
            getCommand("huddemo").setExecutor(new HudDemoCommand(this));
            logger.success("✓ BetterHud Demo Command registered");
        }

        logger.success("✓ Commands registered");
    }

    // ───────────────────── onDisable / reload ─────────────────────

    @Override
    public void onDisable() {
        logger.info("Disabling Quantum...");

        if (animationManager != null) animationManager.stopAll();
        if (sellManager != null) sellManager.clearAllSessions();

        if (scoreboardListener != null) {
            scoreboardListener.shutdown();
            logger.success("✓ Scoreboard listener stopped");
        }
        
        if (scoreboardHandler != null) {
            scoreboardHandler.shutdown();
            logger.success("✓ Tower scoreboards cleared");
        }
        
        if (mobSkillExecutor != null) {
            mobSkillExecutor.shutdown();
            logger.success("✓ Mob skills stopped");
        }
        
        if (mobAnimationManager != null) {
            mobAnimationManager.shutdown();
            logger.success("✓ Mob animations stopped");
        }
        
        if (healthBarManager != null) {
            healthBarManager.shutdown();
            logger.success("✓ HealthBar displays cleaned up");
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
        
        if (placeholderAPIManager != null) {
            placeholderAPIManager.disable();
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

        // zoneManager.reloadConfig() supprimé (nouveau ZoneManager n'a plus cette méthode)
        if (towerManager != null) towerManager.reload();
        
        if (mobSkillManager != null) mobSkillManager.reload();
        
        if (mobAnimationManager != null) mobAnimationManager.reload();

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

    public StorageUpgradeManager getStorageUpgradeManager() {
        return storageUpgradeManager;
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
    
    public com.wynvers.quantum.regions.InternalRegionManager getInternalRegionManager() {
        return internalRegionManager;
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

    // alias si tu veux garder l'ancien nom
    public TowerScoreboardHandler getScoreboardHandler() {
        return scoreboardHandler;
    }
    
    public MobSkillManager getMobSkillManager() {
        return mobSkillManager;
    }
    
    public MobSkillExecutor getMobSkillExecutor() {
        return mobSkillExecutor;
    }
    
    public MobAnimationManager getMobAnimationManager() {
        return mobAnimationManager;
    }
    
    public SpawnSelectionManager getSpawnSelectionManager() {
        return spawnSelectionManager;
    }
    
    public HealthBarManager getHealthBarManager() {
        return healthBarManager;
    }
    
    // NEW: Getters pour les managers des tours
    public TowerDoorManager getDoorManager() {
        return doorManager;
    }
    
    public TowerNPCManager getNPCManager() {
        return npcManager;
    }
    
    public TowerLootManager getLootManager() {
        return lootManager;
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
    
    // NEW: Getters for Furniture, Crops, Tools, and Weapon systems
    
    public FurnitureManager getFurnitureManager() {
        return furnitureManager;
    }
    
    public CustomCropManager getCustomCropManager() {
        return customCropManager;
    }
    
    public ToolManager getToolManager() {
        return toolManager;
    }
    
    public StructureManager getStructureManager() {
        return structureManager;
    }
    
    public JobManager getJobManager() {
        return jobManager;
    }
    
    public HomeManager getHomeManager() {
        return homeManager;
    }
    
    public com.wynvers.quantum.spawn.SpawnManager getSpawnManager() {
        return spawnManager;
    }
    
    public TABManager getTabManager() {
        return tabManager;
    }
    
    public PlaceholderAPIManager getPlaceholderAPIManager() {
        return placeholderAPIManager;
    }
    
    public ZoneGUIManager getZoneGUIManager() {
        return zoneGUIManager;
    }
    
    public ZoneSettingsGUI getZoneSettingsGUI() {
        return zoneSettingsGUI;
    }
    
    public ApartmentManager getApartmentManager() {
        return apartmentManager;
    }

    public StructureSelectionManager getStructureSelectionManager() {
        return structureSelectionManager;
    }
    
    public DungeonWeapon getDungeonWeapon() {
        return dungeonWeapon;
    }
    
    // BetterHud Integration Getters
    
    /**
     * Get the BetterHud manager for HUD and popup operations.
     * @return QuantumBetterHudManager instance or null if BetterHud is not available
     */
    public QuantumBetterHudManager getBetterHudManager() {
        return betterHudManager;
    }
    
    /**
     * Get the Compass manager for waypoint operations.
     * @return QuantumCompassManager instance or null if BetterHud is not available
     */
    public QuantumCompassManager getCompassManager() {
        return compassManager;
    }
}
