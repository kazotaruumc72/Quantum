# BetterHud Integration for Quantum Plugin

## Overview

This document describes the BetterHud integration in the Quantum plugin for Minecraft 1.21.11.

## Supported Versions

- **Minecraft**: 1.21.11 (Paper)
- **BetterHud API**: 1.14.1
- **BetterCommand**: 1.4.3
- **Kotlin stdlib**: 2.1.0

## Dependencies

The following dependencies are configured in `pom.xml`:

```xml
<!-- BetterHud Integration - Standard API -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterHud-standard-api</artifactId>
    <version>1.14.1</version>
    <scope>provided</scope>
</dependency>

<!-- BetterHud Integration - Bukkit API -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterHud-bukkit-api</artifactId>
    <version>1.14.1</version>
    <scope>provided</scope>
</dependency>

<!-- BetterCommand Library (required by BetterHud) -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterCommand</artifactId>
    <version>1.4.3</version>
    <scope>provided</scope>
</dependency>

<!-- Kotlin Standard Library (required by BetterHud) -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>2.1.0</version>
    <scope>provided</scope>
</dependency>
```

BetterHud is declared as a **soft dependency** in `plugin.yml`, meaning Quantum will work without it but will enable additional features when BetterHud is present.

## Integration Components

### 1. QuantumBetterHudManager
**Location**: `src/main/java/com/wynvers/quantum/betterhud/QuantumBetterHudManager.java`

The main manager class that handles:
- Initialization of BetterHud API
- Player HUD management with caching
- Popup display with cooldown optimization
- HUD updates

**Key Features**:
- **Player Cache**: Caches `HudPlayer` instances to avoid repeated lookups
- **Popup Cooldown**: Prevents spam with 100ms minimum cooldown between identical popups
- **Thread-Safe**: Uses `ConcurrentHashMap` for safe concurrent access

**Example Usage**:
```java
QuantumBetterHudManager hudManager = plugin.getBetterHudManager();

// Show a simple popup
hudManager.showPopup(player, "welcome_popup");

// Show popup with variables
Map<String, String> vars = BetterHudUtil.createVariables(
    "player", player.getName(),
    "balance", String.valueOf(economy.getBalance(player))
);
hudManager.showPopup(player, "balance_popup", vars);

// Update player's HUD
hudManager.updateHud(player, null, UpdateEvent.EMPTY);
```

### 2. QuantumCompassManager
**Location**: `src/main/java/com/wynvers/quantum/betterhud/QuantumCompassManager.java`

Manages compass waypoints and markers for players.

**Important Note**: The BetterHud 1.14.1 API has changed significantly from earlier versions. The compass/waypoint functionality is implemented with caching but has limited display capabilities due to API changes. For full compass functionality, consider using BetterHud's built-in configuration files.

**Example Usage**:
```java
QuantumCompassManager compassManager = plugin.getCompassManager();

// Add a waypoint
Location target = new Location(world, x, y, z);
compassManager.addWaypoint(player, "home", target);

// Add waypoint with icon
compassManager.addWaypoint(player, "shop", target, "shop_icon");

// Remove a waypoint
compassManager.removeWaypoint(player, "home");

// Clear all waypoints
compassManager.clearWaypoints(player);

// Get all waypoints
Map<String, CompassPoint> waypoints = compassManager.getWaypoints(player);
```

### 3. BetterHudUtil
**Location**: `src/main/java/com/wynvers/quantum/betterhud/BetterHudUtil.java`

Utility class providing convenience methods for BetterHud operations.

**Available Methods**:

**Variable Creation**:
```java
// Create variable map from key-value pairs
Map<String, String> vars = BetterHudUtil.createVariables(
    "key1", "value1",
    "key2", "value2"
);

// Single variable
Map<String, String> var = BetterHudUtil.singleVariable("player", playerName);
```

**Number Formatting**:
```java
// Format with suffixes (K, M, B)
String formatted = BetterHudUtil.formatNumber(1500); // "1.5K"
String formatted = BetterHudUtil.formatNumber(1500000); // "1.5M"
```

**Percentage Formatting**:
```java
String percent = BetterHudUtil.formatPercentage(75, 100); // "75%"
```

**Health Bars**:
```java
// Simple health bar
String bar = BetterHudUtil.getHealthBar(75, 100, 10); // "████████░░"

// Colored health bar
String coloredBar = BetterHudUtil.getColoredHealthBar(50, 100, 10);
// Returns colored bar based on health percentage
```

**Text Utilities**:
```java
// Strip colors
String clean = BetterHudUtil.stripColors("§aGreen text");

// Truncate text
String short = BetterHudUtil.truncate("Very long text here", 10); // "Very lo..."
String short = BetterHudUtil.truncate("Text", 10, ">>"); // If too long: "Text>>"
```

### 4. BetterHudListener
**Location**: `src/main/java/com/wynvers/quantum/betterhud/BetterHudListener.java`

Event listener that handles cleanup when players quit.

**Features**:
- Removes players from HUD manager cache
- Removes players from compass manager cache
- Optimizes memory usage

## Commands

### /huddemo
Demonstrates BetterHud integration features.

**Permission**: `quantum.betterhud.use` (default: true)

**Subcommands**:

1. **Show Popup**:
   ```
   /huddemo popup <popup_name> [key:value key:value ...]
   ```
   Example: `/huddemo popup welcome_msg player:Steve coins:100`

2. **Add Waypoint**:
   ```
   /huddemo waypoint add <name> [icon]
   ```
   Example: `/huddemo waypoint add spawn`

3. **Remove Waypoint**:
   ```
   /huddemo waypoint remove <name>
   ```
   Example: `/huddemo waypoint remove spawn`

4. **Clear Waypoints**:
   ```
   /huddemo waypoint clear
   ```

5. **List Waypoints**:
   ```
   /huddemo waypoint list
   ```

6. **Test Popup**:
   ```
   /huddemo test
   ```
   Shows a test popup with example variables.

## Integration in Main Plugin

The BetterHud integration is initialized in the `Quantum` main class:

```java
// Check if BetterHud is available
if (Bukkit.getPluginManager().getPlugin("BetterHud") != null) {
    this.betterHudManager = new QuantumBetterHudManager(this);
    this.compassManager = new QuantumCompassManager(betterHudManager, this.getLogger());
    
    // Initialize with delay to ensure BetterHud is fully loaded
    Bukkit.getScheduler().runTaskLater(this, () -> {
        betterHudManager.initialize();
    }, 20L); // 1 second delay
    
    // Register listener for cleanup
    getServer().getPluginManager().registerEvents(
        new BetterHudListener(betterHudManager, compassManager), 
        this
    );
    
    logger.success("✓ BetterHud Integration initialized!");
} else {
    logger.warning("⚠ BetterHud not found - HUD features disabled");
}
```

## Accessing the Managers

From other parts of the Quantum plugin:

```java
// Get the managers
QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
QuantumCompassManager compassManager = plugin.getCompassManager();

// Check if available
if (hudManager != null && hudManager.isAvailable()) {
    // Use BetterHud features
}
```

## Configuration

BetterHud popups and HUD elements must be configured in BetterHud's own configuration files, typically located in `plugins/BetterHud/`.

Refer to [BetterHud documentation](https://github.com/toxicity188/BetterHud) for:
- Creating custom popups
- Configuring HUD elements
- Setting up compass markers
- Using PlaceholderAPI integration

## Performance Optimizations

The integration includes several optimizations:

1. **Caching**: Player HUD instances are cached to avoid repeated API calls
2. **Cooldowns**: Popup spam prevention with 100ms minimum cooldown
3. **Thread-Safety**: Uses concurrent data structures for safe multi-threaded access
4. **Lazy Initialization**: BetterHud API is initialized after plugin load with a delay
5. **Memory Management**: Automatic cleanup when players disconnect

## API Changes in BetterHud 1.14.1

**Important**: BetterHud 1.14.1 introduced significant API changes:

- **Compass API**: The builder-based compass API is no longer available
- **PointedLocation**: Now requires implementation-specific details not in public API
- **Update Method**: `hudPlayer.update()` no longer takes parameters

These changes are accounted for in the current integration.

## Troubleshooting

### BetterHud not found warning
**Solution**: Install BetterHud plugin version 1.14.1 or later from:
- [GitHub Releases](https://github.com/toxicity188/BetterHud/releases)
- [Modrinth](https://modrinth.com/plugin/betterhud2)
- [Hangar](https://hangar.papermc.io/toxicity188/BetterHud)

### Popups not showing
**Possible causes**:
1. Popup name doesn't exist in BetterHud configuration
2. Player is on cooldown (100ms between same popup)
3. BetterHud resource pack not loaded by client

**Solution**: Check BetterHud configuration and ensure resource pack is applied.

### Compass/Waypoint not displaying
**Note**: Due to API limitations in 1.14.1, waypoint display functionality is limited. Waypoints are cached internally but may not display visually. Consider using BetterHud's built-in compass configuration instead of the API.

## Links

- **BetterHud GitHub**: https://github.com/toxicity188/BetterHud
- **BetterHud Documentation**: https://deepwiki.com/toxicity188/BetterHud
- **Example Plugin**: https://github.com/toxicity188/BetterHud-MMOCore

## License

This integration follows the same license as the Quantum plugin.
