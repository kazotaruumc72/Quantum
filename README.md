# Quantum - Advanced Minecraft Plugin

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

Quantum is an advanced Minecraft plugin featuring virtual storage, dynamic GUI builder, orders system, tower progression, jobs, and comprehensive integrations with popular plugins.

## ✨ Features

### Core Systems
- 🗄️ **Virtual Storage** - Advanced virtual storage with dynamic capacity
- 🎨 **Dynamic GUI Builder** - Create custom menus via YAML configuration
- 💰 **Orders System** - Player-to-player trading orders
- 🏢 **Job System** - Multiple jobs with levels and rewards
- 🗼 **Tower Progression** - Tower-based advancement system
- 🏠 **Home System** - Multiple home locations per player
- 📊 **Statistics & History** - Comprehensive tracking of player actions
- 💬 **Chat System** - Complete formatted chat with permissions and placeholders

### Advanced Features
- 🌾 **Custom Crops** - Custom farming system
- 🪑 **Furniture System** - Placeable furniture items
- ⚔️ **Dungeon Equipment** - Weapons, armor, and runes
- 🔧 **Upgradeable Tools** - Progressive tool enhancement
- 🎯 **Mob Skills** - Custom mob abilities
- 💚 **Health Bar System** - Configurable mob health displays

### Plugin Integrations
- 📋 **TAB** - Enhanced tablist with permission-based headers/footers
- 🔖 **PlaceholderAPI** - Universal placeholder support
- 🏛️ **WorldGuard** - Zone management and restrictions (optional - internal system available)
- 💎 **Vault** - Economy integration
- 🔐 **LuckPerms** - Permission groups
- ⛏️ **Nexo** - Custom items

See [PLUGIN_INTEGRATIONS.md](PLUGIN_INTEGRATIONS.md) for complete integration documentation.

### Tower System
- **Internal Region System**: Towers now work with or without WorldGuard
- **Automatic Detection**: Plugin automatically uses WorldGuard if available, otherwise uses internal regions
- See [INTERNAL_REGIONS.md](INTERNAL_REGIONS.md) for details on configuring regions without WorldGuard

## 📥 Installation

### Requirements
- **Minecraft Server**: 1.21.11 (Paper/Spigot)
- **Java**: 21 or higher
- **Required Dependencies**: Nexo
- **Optional Dependencies**: Vault, LuckPerms, WorldGuard, TAB, PlaceholderAPI

### Quick Install

1. **Download the latest release** from the [Releases](https://github.com/kazotaruumc72/Quantum/releases) page

2. **Place in plugins folder:**
   ```bash
   server/plugins/Quantum-1.0.1.jar
   ```

3. **Install dependencies:**
   - Required: Nexo plugin
   - Optional: TAB, PlaceholderAPI
   - See [plugins-integration/](plugins-integration/) for download links and setup guides

4. **Start your server**

5. **Configure** the plugin by editing files in `plugins/Quantum/`

## 🔨 Building from Source

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/kazotaruumc72/Quantum.git
cd Quantum

# Build with Maven
mvn clean package

# The compiled JAR will be in: target/Quantum-1.0.1.jar
```

For detailed build instructions, see [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md).

## 📚 Documentation

### Quick Links
- **[Plugin Integrations Directory](plugins-integration/)** - Complete integration package with all files
- **[Installation Guide](plugins-integration/INSTALLATION_GUIDE.md)** - Step-by-step setup instructions
- **[Plugin Integrations Overview](PLUGIN_INTEGRATIONS.md)** - Overview of all integrations
- **[TAB System](TAB_SYSTEM.md)** - Complete tab system with permission-based headers/footers
- **[TAB Integration](TAB_INTEGRATION.md)** - TAB API and placeholders
- **[PlaceholderAPI Integration](PLACEHOLDERAPI_INTEGRATION.md)** - Universal placeholders
- **[Chat System](CHAT_SYSTEM.md)** - Complete formatted chat system
- **[Tab Completion Summary](TAB_COMPLETION_SUMMARY.md)** - Complete tab completion for all commands
- **[Deployment Guide](DEPLOYMENT_GUIDE.md)** - Building and deployment

### Configuration Files
After first run, configuration files are located in `plugins/Quantum/`:

#### Core Configuration
- `config.yml` - Main plugin configuration
- `messages.yml` - System messages
- `messages_gui.yml` - GUI messages

#### System Configurations
- `jobs.yml` - Job system configuration
- `towers.yml` - Tower progression configuration
- `dungeon.yml` - Dungeon settings
- `tools.yml` - Tool upgrade configuration
- `custom_crops.yml` - Custom farming configuration
- `furniture.yml` - Furniture system
- `price.yml` - Item pricing
- `zone_configs.yml` - Zone configurations
- `tab_config.yml` - TAB headers and footers (NEW)

#### Menu Configurations
Located in `plugins/Quantum/menus/`:
- `storage.yml` - Virtual storage GUI
- `sell.yml` - Selling interface
- `orders_categories.yml` - Order categories
- `history.yml` - Transaction history
- `statistics.yml` - Player statistics
- And many more...

### Example Configurations
The plugin includes comprehensive example configurations for integrated plugins:
- `plugins-integration/tab/` - TAB configuration examples
- `plugins-integration/placeholderapi/` - PlaceholderAPI usage examples with other plugins
- `src/main/resources/placeholderapi-examples/` - PlaceholderAPI examples (legacy location)

## 🎮 Commands

### Player Commands
- `/storage` - Open virtual storage
- `/menu <menu_name>` - Open custom menu
- `/job [select|list|info]` - Manage your job
- `/tower [progress|tp]` - Tower progression
- `/home [name]` - Teleport to home
- `/sethome [name]` - Set home location
- `/rechercher` - Browse orders
- `/offre <item> <qty> <price>` - Create buy order

### Admin Commands
- `/quantum reload` - Reload configuration
- `/qstorage <transfer|remove>` - Manage storage
- `/jobadmin <set|addexp|reset>` - Manage player jobs
- `/armor <give|upgrade>` - Dungeon armor management
- `/weapon <give|upgrade>` - Weapon management
- `/tool <give|upgrade>` - Tool management
- `/rune <give|list>` - Rune management
- `/zonegui` - Zone configuration GUI
- `/chat reload` - Reload chat configuration
- `/tabedit <header|footer|reload|list>` - Edit TAB headers/footers (NEW)

For complete command list, see `src/main/resources/plugin.yml`.

## 🔖 Placeholders

Quantum provides 20+ custom placeholders compatible with TAB and PlaceholderAPI.

### Examples
- `%quantum_level%` - Player level
- `%quantum_job%` - Current job
- `%quantum_tower_floor%` - Tower progress
- `%quantum_storage_items%` - Items in storage
- `%quantum_orders_created%` - Orders created

For complete list, see [PLACEHOLDERAPI_INTEGRATION.md](PLACEHOLDERAPI_INTEGRATION.md#available-placeholders).

## 🔧 API Usage

This section documents all public API calls exposed by the Quantum plugin. Add Quantum as a dependency in your `plugin.yml` (`depend: [Quantum]` or `softdepend: [Quantum]`) and shade or reference the API accordingly.

### Getting the Plugin Instance

```java
Quantum plugin = Quantum.getInstance();
```

### Accessing Managers

All managers are accessible via the main `Quantum` plugin instance:

```java
Quantum plugin = Quantum.getInstance();

StorageManager          storageManager       = plugin.getStorageManager();
TowerStorageManager     towerStorageManager  = plugin.getTowerStorageManager();
StorageUpgradeManager   storageUpgradeMgr    = plugin.getStorageUpgradeManager();
StorageUpgradeManager   towerUpgradeMgr      = plugin.getTowerStorageUpgradeManager();
MenuManager             menuManager          = plugin.getMenuManager();
MessageManager          messageManager       = plugin.getMessageManager();    // Rich (Component) message manager
MessagesManager         messagesManager      = plugin.getMessagesManager();   // Lightweight string message manager
GuiMessageManager       guiMessageManager    = plugin.getGuiMessageManager();
PlaceholderManager      placeholderManager   = plugin.getPlaceholderManager();
AnimationManager        animationManager     = plugin.getAnimationManager();
VaultManager            vaultManager         = plugin.getVaultManager();
EscrowManager           escrowManager        = plugin.getEscrowManager();
PriceManager            priceManager         = plugin.getPriceManager();
OrderManager            orderManager         = plugin.getOrderManager();
SellManager             sellManager          = plugin.getSellManager();
TransactionHistoryManager txHistoryManager   = plugin.getTransactionHistoryManager();
TradingStatisticsManager tradingStatsMgr     = plugin.getTradingStatisticsManager();
StatisticsManager       statisticsManager    = plugin.getStatisticsManager();
StorageStatsManager     storageStatsMgr      = plugin.getStorageStatsManager();
JobManager              jobManager           = plugin.getJobManager();
HomeManager             homeManager          = plugin.getHomeManager();
TowerManager            towerManager         = plugin.getTowerManager();
ZoneManager             zoneManager          = plugin.getZoneManager();
InternalRegionManager   regionManager        = plugin.getInternalRegionManager();
ArmorManager            armorManager         = plugin.getArmorManager();
DungeonArmor            dungeonArmor         = plugin.getDungeonArmor();
RuneItem                runeItem             = plugin.getRuneItem();
FurnitureManager        furnitureManager     = plugin.getFurnitureManager();
CustomCropManager       cropManager          = plugin.getCustomCropManager();
PlayerLevelManager      levelManager         = plugin.getPlayerLevelManager();
SpawnManager            spawnManager         = plugin.getSpawnManager();
DatabaseManager         databaseManager      = plugin.getDatabaseManager();
PlaceholderAPIManager   papiManager          = plugin.getPlaceholderAPIManager();
ChatManager             chatManager          = plugin.getChatManager();
ActionExecutor          actionExecutor       = plugin.getActionExecutor();
DungeonUtils            dungeonUtils         = plugin.getDungeonUtils();
MobConfig               mobConfig            = plugin.getMobConfig();
QuantumItemAttributeManager itemAttrManager  = plugin.getQuantumItemAttributeManager();
```

---

### StorageManager — Virtual Storage

```java
StorageManager storageManager = plugin.getStorageManager();

// Get (or create) a player's virtual storage
PlayerStorage storage = storageManager.getStorage(Player player);
PlayerStorage storage = storageManager.getStorage(UUID uuid);
PlayerStorage storage = storageManager.getPlayerStorage(UUID uuid); // alias

// Persist storage
storageManager.saveStorage(UUID uuid);   // save one player
storageManager.save(UUID uuid);          // alias
storageManager.saveAll();                // save everyone

// Unload a player's storage from memory (saves first)
storageManager.unload(UUID uuid);

// Reload all storage data from disk
storageManager.reload();
```

#### PlayerStorage — Working with Items

```java
PlayerStorage storage = plugin.getStorageManager().getStorage(uuid);

// --- Vanilla items ---
storage.addItem(Quantum plugin, Player player, Material material, int amount); // returns boolean (capacity check)
storage.addItem(Material material, int amount);                                 // direct add (no capacity check)
storage.removeItem(Material material, int amount);
int qty = storage.getAmount(Material material);
boolean ok = storage.hasItem(Material material, int amount);
Map<Material, Integer> vanilla = storage.getVanillaItems();

// --- Nexo custom items ---
storage.addNexoItem(Quantum plugin, Player player, String nexoId, int amount); // returns boolean
storage.addNexoItem(String nexoId, int amount);
storage.removeNexoItem(String nexoId, int amount);
int qty = storage.getNexoAmount(String nexoId);
boolean ok = storage.hasNexoItem(String nexoId, int amount);
Map<String, Integer> nexo = storage.getNexoItems();

// --- Aggregated views ---
Map<String, Integer> all  = storage.getAllStorageItems();  // merged vanilla + nexo (by itemId string)
int unique = storage.getUniqueItemCount();
int total  = storage.getTotalItemCount();
int amount = storage.getAmountByItemId(String itemId);
```

---

### TowerStorageManager — Tower Storage

Same interface as `StorageManager` but operates on `PlayerTowerStorage` objects:

```java
TowerStorageManager mgr = plugin.getTowerStorageManager();

PlayerTowerStorage ts = mgr.getStorage(Player player);
PlayerTowerStorage ts = mgr.getStorage(UUID uuid);
PlayerTowerStorage ts = mgr.getPlayerStorage(UUID uuid); // alias

mgr.saveStorage(UUID uuid);
mgr.save(UUID uuid);        // alias
mgr.saveAll();
mgr.unload(UUID uuid);
mgr.reload();
```

---

### JobManager — Job & Profession System

```java
JobManager jobManager = plugin.getJobManager();

// Job configuration
Job job = jobManager.getJob(String jobId);
Collection<Job> all = jobManager.getAllJobs();
YamlConfiguration cfg = jobManager.getConfig();

// Player job data
JobData data = jobManager.getPlayerJob(UUID uuid);
boolean changed = jobManager.setJob(UUID uuid, String jobId);
boolean removed = jobManager.removeJob(UUID uuid);

// Experience
jobManager.addExp(UUID uuid, int amount);
int requiredExp = jobManager.getRequiredExp(int level);

// Boosters
jobManager.activateBooster(UUID uuid, String boosterType, double multiplier,
                            long durationMillis, boolean dungeonOnly);
List<ActiveBooster> boosters = jobManager.getActiveBoosters(UUID uuid);
double expMult   = jobManager.getExpMultiplier(UUID uuid, boolean inDungeon);
double expMult   = jobManager.getExpMultiplier(Player player, boolean inDungeon);
double moneyMult = jobManager.getMoneyMultiplier(UUID uuid, boolean inDungeon);
double moneyMult = jobManager.getMoneyMultiplier(Player player, boolean inDungeon);

// Actions (triggers job rewards for a given action + item)
jobManager.handleAction(Player player, String actionType, String itemId);
ActionPreview preview = jobManager.getActionPreview();

// Rankings
int rank = jobManager.getPlayerRank(UUID uuid);
List<JobData> top       = jobManager.getTopPlayers(String jobId, int limit);
Map<UUID, Integer> gTop = jobManager.getGlobalTopPlayers(int limit);

// Persistence
jobManager.loadPlayer(UUID uuid);
jobManager.savePlayer(UUID uuid);
jobManager.unloadPlayer(UUID uuid);
jobManager.reload();
```

---

### TowerManager — Tower Progression

```java
TowerManager towerManager = plugin.getTowerManager();

// Tower configuration
TowerConfig tower = towerManager.getTower(String towerId);
Map<String, TowerConfig> all = towerManager.getAllTowers();
List<String> ids = towerManager.getTowerIds();
int count = towerManager.getTowerCount();

// Region lookups
String towerId  = towerManager.getTowerByRegion(String regionName);
int floor       = towerManager.getFloorByRegion(String regionName);

// Player progress
TowerProgress progress = towerManager.getProgress(UUID uuid);
int currentFloor       = towerManager.getPlayerFloor(UUID uuid);
TowerConfig current    = towerManager.getPlayerTower(Player player);

// Progression actions
towerManager.completeFloor(Player player, String towerId, int floor);
towerManager.updateCurrentLocation(Player player, String towerId, int floor);
towerManager.clearCurrentLocation(Player player);
towerManager.resetProgress(UUID uuid);
towerManager.resetTowerProgress(UUID uuid, String towerId);

// Leaderboard
List<Map.Entry<UUID, Integer>> top = towerManager.getTopPlayers(int limit);

// Auxiliary managers
TowerRewardManager rewardMgr = towerManager.getRewardManager();

// Persistence
towerManager.saveProgress();
towerManager.reload();
```

---

### HomeManager — Player Homes

```java
HomeManager homeManager = plugin.getHomeManager();

// Home CRUD
List<Home> homes = homeManager.getHomes(UUID playerUuid);
Home home        = homeManager.getHome(UUID playerUuid, String name);
boolean ok       = homeManager.setHome(Player player, String name, Location location);
boolean ok       = homeManager.deleteHome(UUID playerUuid, String name);
int count        = homeManager.getHomeCount(UUID playerUuid);

// Limits
int max = homeManager.getMaxHomes(Player player);

// Teleportation
boolean ok = homeManager.teleportToHome(Player player, String name);

// Cache management
homeManager.invalidateCache(UUID playerUuid);
homeManager.clearCache();
```

---

### OrderManager — Trading Orders

```java
OrderManager orderManager = plugin.getOrderManager();

// Item catalogue
boolean allowed        = orderManager.isItemAllowed(String itemId);
Set<String> categories = orderManager.getAllCategories();
Set<String> itemIds    = orderManager.getAllItemIds();
Map<String, String> displayNames = orderManager.getAllItemsWithDisplayNames();
List<String> catItems  = orderManager.getItemsForCategory(String category);
String cat             = orderManager.getCategoryForItem(String itemId);

// Player limits
long durationMs  = orderManager.getOrderDurationForPlayer(Player player);
int maxOrders    = orderManager.getMaxActiveOrdersForPlayer(Player player);
int activeCount  = orderManager.countActiveOrdersForPlayer(UUID playerId);

// Order lifecycle
Order order = orderManager.createOrder(Player player, String itemId,
                                       int quantity, double pricePerUnit);
Order order = orderManager.getOrder(UUID orderId);
boolean cancelled = orderManager.cancelOrder(UUID orderId);
orderManager.cleanExpiredOrders();

// Queries
List<Order> byCategory = orderManager.getActiveOrdersForCategory(String category);
List<Order> byItem     = orderManager.getActiveOrdersForItem(String itemId);
List<Order> byPlayer   = orderManager.getActiveOrdersForPlayer(UUID playerId);

// Reload
orderManager.loadItems();
```

---

### VaultManager — Economy & Multi-Currency

```java
VaultManager vaultManager = plugin.getVaultManager();

boolean enabled = vaultManager.isEnabled();

// Default currency (Vault)
double bal  = vaultManager.getBalance(OfflinePlayer player);
boolean dep = vaultManager.deposit(OfflinePlayer player, double amount);
boolean wd  = vaultManager.withdraw(OfflinePlayer player, double amount);
boolean has = vaultManager.has(OfflinePlayer player, double amount);
String fmt  = vaultManager.format(double amount);
String name = vaultManager.getCurrencyName();
String namePlural = vaultManager.getCurrencyNamePlural();
String sym  = vaultManager.getSymbol();

// Multi-currency (by currencyId)
double bal  = vaultManager.getBalance(OfflinePlayer player, String currencyId);
boolean dep = vaultManager.deposit(OfflinePlayer player, double amount, String currencyId);
boolean wd  = vaultManager.withdraw(OfflinePlayer player, double amount, String currencyId);
boolean has = vaultManager.has(OfflinePlayer player, double amount, String currencyId);
String fmt  = vaultManager.format(double amount, String currencyId);
String sym  = vaultManager.getSymbol(String currencyId);

// Currency objects
Economy           economy = vaultManager.getEconomy();
QuantumEconomy    qEco    = vaultManager.getQuantumEconomy();
QuantumEconomy    qEco    = vaultManager.getCurrency(String currencyId);
Map<String, QuantumEconomy> all = vaultManager.getCurrencies();
Set<String> ids = vaultManager.getCurrencyIds();
```

---

### EscrowManager — Order Escrow

```java
EscrowManager escrowManager = plugin.getEscrowManager();

boolean deposited = escrowManager.deposit(UUID orderId, double amount);
double released   = escrowManager.withdraw(UUID orderId);
double refunded   = escrowManager.refund(UUID orderId);
boolean has       = escrowManager.hasDeposit(UUID orderId);
double held       = escrowManager.getAmount(UUID orderId);
double totalHeld  = escrowManager.getTotalEscrow();
int count         = escrowManager.getEscrowCount();
double cleared    = escrowManager.clearAll();

escrowManager.saveEscrow();
escrowManager.reload();
```

---

### PriceManager — Item Pricing

```java
PriceManager priceManager = plugin.getPriceManager();

double price = priceManager.getPrice(String itemId);
String fmt   = priceManager.getFormattedPrice(String itemId);
String fmt   = priceManager.formatPrice(double price);
boolean has  = priceManager.hasPrice(String itemId);
priceManager.setPrice(String itemId, double price);

priceManager.loadPrices();
priceManager.reload();
```

---

### MenuManager — Dynamic GUI

```java
MenuManager menuManager = plugin.getMenuManager();

Menu menu = menuManager.getMenu(String id);
Menu menu = menuManager.getMenuByCommand(String command);
Menu menu = menuManager.getMenuByTitle(String title);
Menu open = menuManager.getActiveMenu(Player player);

menuManager.setActiveMenu(Player player, Menu menu);
menuManager.clearActiveMenu(Player player);
menuManager.openMenu(Player player, String menuId);
menuManager.openMenuWithSession(Player player, String menuId,
                                OrderCreationSession session,
                                ItemStack displayItem);

Collection<Menu> all = menuManager.getAllMenus();
int count = menuManager.getMenuCount();
menuManager.reload();
```

---

### MessageManager — Player Messages

```java
MessageManager msgManager = plugin.getMessageManager();

String raw     = msgManager.getRawMessage(String path);
String msg     = msgManager.getMessage(String path);
String msg     = msgManager.getMessage(String path, Map<String, String> placeholders);
Component comp = msgManager.toComponent(String message);

msgManager.sendMessage(Player player, String path);
msgManager.sendMessage(Player player, String path, Map<String, String> placeholders);
msgManager.sendPrefixedMessage(Player player, String path);
msgManager.sendPrefixedMessage(Player player, String path, Map<String, String> placeholders);
msgManager.sendPrefixedMessage(CommandSender sender, String path);
msgManager.sendPrefixedMessage(CommandSender sender, String path, Map<String, String> placeholders);

// Convenience senders
msgManager.sendNoPermission(Player player);
msgManager.sendPlayerOnly(Player player);
msgManager.sendReloadSuccess(Player player);
msgManager.sendReloadError(Player player);
msgManager.sendSellSuccess(Player player, String item, int quantity, double price);
msgManager.sendInsufficientStock(Player player, int current);
msgManager.sendOrderCreated(Player player, String item, int quantity, double totalPrice);
msgManager.sendOrderAccepted(Player seller, String item, int quantity,
                              String buyer, double totalPrice);
msgManager.sendOrderCompleted(Player buyer, String item, int quantity,
                               String seller, double totalPrice);

List<String> list     = msgManager.getMessageList(String path);
List<Component> comps = msgManager.getComponentList(String path);

msgManager.loadMessages();
msgManager.reload();
```

---

### GuiMessageManager — GUI Labels & Lore

```java
GuiMessageManager guiMgr = plugin.getGuiMessageManager();

String raw  = guiMgr.getRawMessage(String path);
String msg  = guiMgr.getMessage(String path);
String msg  = guiMgr.getMessage(String path, Map<String, String> placeholders);
Component c = guiMgr.toComponent(String message);

List<String> raw   = guiMgr.getRawMessageList(String path);
List<String> list  = guiMgr.getMessageList(String path);
List<String> list  = guiMgr.getMessageList(String path, Map<String, String> placeholders);
List<Component> cs = guiMgr.toComponentList(List<String> messages);

// Specific GUI titles
String title = guiMgr.getStorageTitle(String mode);
String title = guiMgr.getSellTitle(String itemName);
String title = guiMgr.getOrderQuantityTitle();
String title = guiMgr.getOrderPriceTitle();
String title = guiMgr.getOrderConfirmTitle();
String title = guiMgr.getOrdersCategoriesTitle();
String title = guiMgr.getOrdersCategoryTitle(String category);

// Lore builders
List<String> lore = guiMgr.getModeStorageLore(String currentMode);
List<String> lore = guiMgr.getModeRechercheLore(String currentMode);  // "Recherche" = search/order-browse mode (French naming)
List<String> lore = guiMgr.getModeSellLore(String currentMode);
List<String> lore = guiMgr.getStorageSlotLore(int quantity, double price, double totalPrice);
List<String> lore = guiMgr.getSellItemDisplayLore(String itemName, int maxQty,
                                                    int qty, double pricePerUnit,
                                                    double totalPrice);
List<String> lore = guiMgr.getOrderItemLore(String orderer, int quantity,
                                              double price, double totalPrice);

// Button labels
String name       = guiMgr.getButtonName(String section, String button);
String name       = guiMgr.getButtonName(String section, String button,
                                          Map<String, String> placeholders);
List<String> lore = guiMgr.getButtonLore(String section, String button);
List<String> lore = guiMgr.getButtonLore(String section, String button,
                                           Map<String, String> placeholders);

guiMgr.loadGuiMessages();
guiMgr.reload();
```

---

### PlaceholderManager — Internal Placeholder Parser

```java
PlaceholderManager placeholderManager = plugin.getPlaceholderManager();

String result          = placeholderManager.parse(Player player, String text);
String result          = placeholderManager.parse(Player player, String text,
                                                   Map<String, String> custom);
List<String> results   = placeholderManager.parse(Player player, List<String> lines);
```

---

### PlaceholderAPIManager — PlaceholderAPI Bridge

```java
PlaceholderAPIManager papi = plugin.getPlaceholderAPIManager();

boolean enabled        = papi.isEnabled();
QuantumExpansion exp   = papi.getExpansion();
papi.disable();
```

For the full list of `%quantum_*%` placeholders, see [PLACEHOLDERAPI_INTEGRATION.md](PLACEHOLDERAPI_INTEGRATION.md#available-placeholders).

---

### AnimationManager — Item Animations

```java
AnimationManager animMgr = plugin.getAnimationManager();

animMgr.startAnimation(Player player, List<String> frames, int speedTicks);
animMgr.stopAnimation(Player player);
animMgr.stopAll();
animMgr.reload();
```

---

### QuantumItemAttributeManager — Item Attribute Modifiers

```java
QuantumItemAttributeManager attrMgr = plugin.getQuantumItemAttributeManager();

attrMgr.setActiveSlot(Player player, int slot);
int slot      = attrMgr.getActiveSlot(Player player);
ItemStack item = attrMgr.getActiveItem(Player player);
attrMgr.clearActiveSlot(Player player);

attrMgr.applyModifiers(Player player, List<Map<String, Object>> attrList);
attrMgr.applyModifier(Player player, String attributeName,
                       double amount, int operationInt, String slotStr);
attrMgr.resetModifiers(Player player);
```

---

### ArmorManager — Dungeon Armor & Runes

```java
ArmorManager armorManager = plugin.getArmorManager();

armorManager.applyArmorBonuses(Player player);
armorManager.clearArmorBonuses(Player player);
Map<RuneType, Integer> runes = armorManager.getPlayerRunes(Player player);
```

---

### ActionExecutor — YAML Action Runner

```java
ActionExecutor executor = plugin.getActionExecutor();

// Execute a list of action strings (as defined in YAML configs) for a player
executor.execute(Player player, List<String> actions);
executor.execute(Player player, List<String> actions,
                 Map<String, String> placeholders);
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or create issues for bugs and feature requests.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Commit: `git commit -m 'Add amazing feature'`
5. Push: `git push origin feature/amazing-feature`
6. Open a Pull Request

## 📝 Version History

### Version 1.0.1 (Current)
- ✅ Complete TAB integration with permission-based headers/footers
- ✅ Complete PlaceholderAPI integration
- ✅ Virtual storage system
- ✅ Dynamic GUI builder
- ✅ Orders system
- ✅ Job system
- ✅ Tower progression
- ✅ Home system
- ✅ Dungeon equipment
- ✅ Custom crops and furniture

## 🐛 Known Issues

- Network issues may occasionally prevent Maven build (temporary)
- See individual integration docs for plugin-specific issues

## 📜 License

This project is licensed under a custom license. See the LICENSE file for details.

## 👥 Credits

### Plugin Development
- **Author**: Kazotaruu_
- **Repository**: https://github.com/kazotaruumc72/Quantum

### Integrated Plugins
- **TAB**: NEZNAMY
- **PlaceholderAPI**: Clip, extended_clip, and contributors

## 📞 Support

### Getting Help
1. Check the [documentation](PLUGIN_INTEGRATIONS.md)
2. Review [example configurations](src/main/resources/)
3. Create an [issue](https://github.com/kazotaruumc72/Quantum/issues)

### Useful Links
- **SpigotMC Resources**: Check SpigotMC for compatible versions
- **Paper Documentation**: https://docs.papermc.io/
- **Plugin Wikis**: See individual plugin documentation

---

**Made with ❤️ for the Minecraft community**

*Quantum - Advanced virtual storage with dynamic GUI builder and comprehensive plugin integrations*
