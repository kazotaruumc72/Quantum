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

### Getting the Plugin Instance

```java
Quantum plugin = Quantum.getInstance();
```

### Accessing Managers

```java
// Storage Manager
StorageManager storageManager = plugin.getStorageManager();
PlayerStorage storage = storageManager.getPlayerStorage(uuid);

// Job Manager
JobManager jobManager = plugin.getJobManager();
PlayerJobData jobData = jobManager.getPlayerJob(uuid);

// Tower Manager
TowerManager towerManager = plugin.getTowerManager();
int floor = towerManager.getPlayerFloor(uuid);

// Integration Managers
TABManager tabManager = plugin.getTabManager();
PlaceholderAPIManager papiManager = plugin.getPlaceholderAPIManager();
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
