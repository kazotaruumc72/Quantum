# BetterHud Integration for Quantum Plugin

## Overview

Quantum plugin provides full integration with BetterHud plugin for Minecraft 1.21.11. This integration allows server administrators and developers to display custom HUD elements, popups, and waypoints to players.

## Requirements

- **Minecraft Version**: 1.21.11
- **BetterHud Plugin**: Version 1.14.1 or higher
- **Quantum Plugin**: Version 1.0.1 or higher

## Features

### 1. Popup Management
Display temporary popup messages to players with custom variables.

### 2. Waypoint/Compass System
Add navigation waypoints that players can see in their HUD.

### 3. HUD Updates
Trigger HUD refreshes to show updated player data.

### 4. Optimized Performance
- Player caching to reduce API calls
- Popup cooldown system to prevent spam
- Automatic cleanup on player disconnect

## API Usage

### Getting the Managers

```java
Quantum plugin = Quantum.getInstance();
QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
QuantumCompassManager compassManager = plugin.getCompassManager();
```

### Showing Popups

```java
// Simple popup without variables
hudManager.showPopup(player, "popup_name");

// Popup with variables
Map<String, String> variables = BetterHudUtil.createVariables(
    "player", player.getName(),
    "health", String.valueOf(player.getHealth())
);
hudManager.showPopup(player, "popup_name", variables);
```

### Managing Waypoints

```java
// Add a waypoint
Location location = player.getLocation();
compassManager.addWaypoint(player, "waypoint_name", location);

// Remove a waypoint
compassManager.removeWaypoint(player, "waypoint_name");

// Clear all waypoints
compassManager.clearWaypoints(player);

// Get all waypoints
Map<String, CompassPoint> waypoints = compassManager.getWaypoints(player);
```

### Updating HUD

```java
// Trigger a HUD update for a player
hudManager.updateHud(player, null, UpdateEvent.EMPTY);
```

## Utility Methods

The `BetterHudUtil` class provides helpful utility methods:

```java
// Format numbers with suffixes (K, M, B)
String formatted = BetterHudUtil.formatNumber(1500); // "1.5K"

// Format percentages
String percent = BetterHudUtil.formatPercentage(75, 100); // "75%"

// Create health bars
String healthBar = BetterHudUtil.getHealthBar(15.0, 20.0, 10);

// Colored health bar
String coloredBar = BetterHudUtil.getColoredHealthBar(15.0, 20.0, 10);

// Truncate text
String truncated = BetterHudUtil.truncate("Long text here", 10); // "Long te..."
```

## Demo Commands

Players with permission can use the `/huddemo` command to test the integration:

```
/huddemo popup <popup_name> [key:value...] - Show a popup
/huddemo waypoint add <name> [icon] - Add waypoint at current location
/huddemo waypoint remove <name> - Remove a waypoint
/huddemo waypoint clear - Clear all waypoints
/huddemo waypoint list - List all waypoints
/huddemo test - Show test popup with example variables
```

**Permission**: `quantum.betterhud.use`

## Configuration

### plugin.yml

BetterHud is configured as a soft dependency, meaning Quantum will work with or without it:

```yaml
softdepend:
  - BetterHud
```

### pom.xml Dependencies

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

## Architecture

### Class Structure

```
com.wynvers.quantum.betterhud/
├── QuantumBetterHudManager.java    - Main HUD operations manager
├── QuantumCompassManager.java       - Waypoint/compass management
├── BetterHudListener.java           - Event handling & cleanup
└── BetterHudUtil.java               - Utility methods

com.wynvers.quantum.commands/
└── HudDemoCommand.java              - Demo command implementation
```

### Initialization Flow

1. Quantum plugin starts
2. Checks if BetterHud plugin is installed
3. Creates `QuantumBetterHudManager` and `QuantumCompassManager`
4. Delays 1 second, then initializes BetterHud API
5. Registers `BetterHudListener` for player cleanup
6. Registers `/huddemo` command if available

## Performance Optimizations

### Player Caching
HudPlayer instances are cached to avoid repeated API lookups:
- Cached on first access
- Cleared on player disconnect
- Thread-safe with ConcurrentHashMap

### Popup Cooldown
Prevents popup spam with a 100ms cooldown per popup type per player.

### Automatic Cleanup
`BetterHudListener` automatically removes player data when they disconnect, preventing memory leaks.

## API Compatibility

This integration is compatible with:
- **BetterHud API**: 1.14.1+
- **Minecraft**: 1.21.11
- **Paper API**: 1.21.11-R0.1-SNAPSHOT

### API Version Notes

BetterHud 1.14.1 introduced breaking changes from earlier versions:
- `HudPlayer.update()` no longer takes parameters
- Compass system uses `PointedLocationProvider` interface
- `BetterHudAPI.inst()` is the new way to access the API

## Limitations

### Compass/Waypoint System
The current compass implementation has limited functionality due to API changes in BetterHud 1.14.1:
- Waypoints are cached internally
- Full compass display requires BetterHud configuration files
- Consider using BetterHud's built-in compass configuration instead of the API

For full compass/waypoint functionality, create waypoint configurations in BetterHud's config files rather than using the API.

## Troubleshooting

### BetterHud Not Loading
**Problem**: Console shows "BetterHud not found"
**Solution**: Ensure BetterHud plugin is installed and loads before Quantum

### Popups Not Showing
**Problem**: Popup calls succeed but nothing displays
**Solution**: 
1. Verify popup exists in BetterHud configuration
2. Check popup cooldown hasn't been triggered
3. Ensure player has HudPlayer instance

### API Errors
**Problem**: NullPointerException when calling API
**Solution**:
1. Check `hudManager.isAvailable()` before API calls
2. Verify BetterHud loaded successfully
3. Check console for initialization errors

## Example Integration

```java
public class MyFeature {
    private final Quantum plugin;
    
    public void onPlayerLevelUp(Player player, int newLevel) {
        QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
        
        if (hudManager != null && hudManager.isAvailable()) {
            Map<String, String> vars = BetterHudUtil.createVariables(
                "level", String.valueOf(newLevel),
                "player", player.getName()
            );
            
            hudManager.showPopup(player, "level_up", vars);
        }
    }
}
```

## Support

For issues related to:
- **Quantum Integration**: Create an issue on Quantum's GitHub
- **BetterHud Plugin**: Visit BetterHud's GitHub or SpigotMC page
- **API Usage**: Consult BetterHud's API documentation

## Credits

- **BetterHud Plugin**: toxicity188
- **Quantum Integration**: kazotaruumc72/Quantum development team

## License

This integration follows Quantum plugin's license. BetterHud API usage follows BetterHud's license terms.
