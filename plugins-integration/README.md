# Quantum Plugin Integrations

This directory contains all files, configurations, and resources needed to integrate BetterHud, TAB, and PlaceholderAPI with the Quantum plugin.

## 📦 Included Plugins

### 1. BetterHud (@toxicity188/BetterHud)
- **Version**: 1.14.1+
- **Purpose**: Custom HUD elements, popups, and waypoints
- **Folder**: `betterhud/`
- **GitHub**: https://github.com/toxicity188/BetterHud
- **SpigotMC**: https://www.spigotmc.org/resources/115559/

### 2. TAB (NEZNAMY/TAB)
- **Version**: 5.5.0+
- **Purpose**: Advanced tablist, nametags, and scoreboard
- **Folder**: `tab/`
- **GitHub**: https://github.com/NEZNAMY/TAB
- **SpigotMC**: https://www.spigotmc.org/resources/57806/

### 3. PlaceholderAPI (PlaceholderAPI/PlaceholderAPI)
- **Version**: 2.11.6+
- **Purpose**: Universal placeholder system
- **Folder**: `placeholderapi/`
- **GitHub**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **SpigotMC**: https://www.spigotmc.org/resources/6245/

## 🚀 Quick Installation Guide

### Step 1: Download Plugins

Download the latest compatible versions of each plugin:

1. **BetterHud** - https://www.spigotmc.org/resources/115559/
2. **TAB** - https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
3. **PlaceholderAPI** - https://www.spigotmc.org/resources/6245/

### Step 2: Install Plugins

Place the downloaded JAR files in your server's plugins folder:

```
server/
└── plugins/
    ├── Quantum-1.0.1.jar
    ├── BetterHud-1.14.1.jar (optional)
    ├── TAB-v5.5.0.jar (optional)
    └── PlaceholderAPI-2.11.6.jar (optional)
```

### Step 3: Start Server

Start your Minecraft server. The plugins will create their configuration folders:

```
server/
└── plugins/
    ├── Quantum/
    ├── BetterHud/
    ├── TAB/
    └── PlaceholderAPI/
```

### Step 4: Configure Integrations

Copy the example configurations from this directory to your server:

#### For BetterHud:
```bash
cp plugins-integration/betterhud/examples/* server/plugins/BetterHud/
```

#### For TAB:
```bash
cp plugins-integration/tab/examples/* server/plugins/TAB/
```

#### For PlaceholderAPI:
No configuration needed - Quantum expansion registers automatically!

### Step 5: Restart Server

Restart your server to apply the configurations.

## 📁 Directory Structure

```
plugins-integration/
├── README.md (this file)
├── betterhud/
│   ├── README.md
│   └── examples/
│       ├── config.yml
│       ├── huds.yml
│       ├── popups.yml
│       └── compass.yml
├── tab/
│   ├── README.md
│   └── examples/
│       └── (TAB configuration examples)
└── placeholderapi/
    ├── README.md
    └── examples/
        ├── chat_example.yml
        ├── hologram_example.yml
        ├── scoreboard_example.yml
        └── tab_example.yml
```

## 📖 Documentation

Each plugin has detailed integration documentation:

- **Main Overview**: [../PLUGIN_INTEGRATIONS.md](../PLUGIN_INTEGRATIONS.md)
- **BetterHud Integration**: [../BETTERHUD_INTEGRATION.md](../BETTERHUD_INTEGRATION.md)
- **TAB Integration**: [../TAB_INTEGRATION.md](../TAB_INTEGRATION.md)
- **PlaceholderAPI Integration**: [../PLACEHOLDERAPI_INTEGRATION.md](../PLACEHOLDERAPI_INTEGRATION.md)

## 🔖 Available Placeholders

All three plugins can use Quantum's custom placeholders:

### Player Level & Experience
- `%quantum_level%` - Player's level
- `%quantum_exp%` - Current experience
- `%quantum_exp_required%` - EXP needed for next level
- `%quantum_exp_progress%` - Progress percentage

### Job System
- `%quantum_job%` - Current job name
- `%quantum_job_level%` - Job level
- `%quantum_job_exp%` - Job experience

### Tower System
- `%quantum_tower%` - Current tower ID
- `%quantum_tower_name%` - Tower display name
- `%quantum_tower_floor%` - Current floor

### Storage System
- `%quantum_storage_items%` - Items in storage
- `%quantum_storage_capacity%` - Total capacity
- `%quantum_storage_used_percent%` - Usage percentage

### Statistics
- `%quantum_orders_created%` - Orders created
- `%quantum_orders_filled%` - Orders filled
- `%quantum_items_sold%` - Total items sold
- `%quantum_items_bought%` - Total items bought

### Home System
- `%quantum_homes%` - Number of homes set
- `%quantum_homes_max%` - Maximum homes allowed

## ⚙️ Requirements

### Minecraft Server
- **Version**: 1.21.11 (Paper recommended)
- **Java**: 21 or higher

### Dependencies
- **Required**: Quantum plugin, Nexo
- **Optional**: BetterHud, TAB, PlaceholderAPI (any or all)

## 🔧 Integration Features

### BetterHud Features
- ✅ Custom HUD elements configured via YAML
- ✅ Popup messages with variable support
- ✅ Waypoint/compass markers
- ✅ Level-up notifications
- ✅ Job change popups
- ✅ Tower progress indicators

### TAB Features
- ✅ Custom placeholders in tablist
- ✅ Player nametags with Quantum data
- ✅ Scoreboard integration
- ✅ MiniMessage support
- ✅ Player sorting by level/job/tower

### PlaceholderAPI Features
- ✅ 20+ Quantum placeholders
- ✅ Universal compatibility with other plugins
- ✅ Offline player support
- ✅ Works with chat, holograms, scoreboards, etc.

## 🐛 Troubleshooting

### Plugin Not Detected
1. Verify plugin JAR is in `/plugins/` folder
2. Check server startup logs for errors
3. Use `/plugins` command to see loaded plugins
4. Ensure compatible versions are installed

### Placeholders Not Working
1. Verify placeholder syntax: `%quantum_level%`
2. Check console for Quantum integration messages
3. Test with `/papi parse me %quantum_level%` (PlaceholderAPI)
4. Ensure target plugin supports placeholders

### Configuration Issues
1. Verify YAML syntax is correct
2. Check file encoding is UTF-8
3. Review example configurations in this directory
4. Check plugin-specific documentation

## 📞 Support

### Getting Help
1. Check the [documentation](../PLUGIN_INTEGRATIONS.md)
2. Review example configurations in subdirectories
3. Check console logs for error messages
4. Create an [issue](https://github.com/kazotaruumc72/Quantum/issues)

### Useful Links
- **Quantum Repository**: https://github.com/kazotaruumc72/Quantum
- **BetterHud Wiki**: Check SpigotMC resource page
- **TAB Wiki**: https://github.com/NEZNAMY/TAB/wiki
- **PlaceholderAPI Wiki**: https://github.com/PlaceholderAPI/PlaceholderAPI/wiki

## 📝 License

- **Quantum Plugin**: Custom License (see repository)
- **BetterHud**: See plugin's license
- **TAB**: See plugin's license
- **PlaceholderAPI**: See plugin's license

## 👥 Credits

### Plugin Authors
- **BetterHud**: toxicity188
- **TAB**: NEZNAMY
- **PlaceholderAPI**: Clip, extended_clip, and contributors
- **Quantum**: kazotaruumc72

### Integration Development
- **Quantum Team**: Full integration implementation

---

**Last Updated**: 2026-02-12  
**Quantum Version**: 1.0.1  
**Minecraft Version**: 1.21.11
