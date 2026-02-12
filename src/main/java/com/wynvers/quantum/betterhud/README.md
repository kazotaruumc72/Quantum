# Quantum BetterHud Integration

This module integrates [BetterHud](https://github.com/toxicity188/BetterHud) into the Quantum plugin with optimized performance and additional utilities.

## Features

### Core BetterHud Functionality
- ✅ Server-side HUD implementation (no client mods required)
- ✅ Auto-generating resource packs
- ✅ Display images (PNG sequences), text, and player heads
- ✅ Animation support
- ✅ Popup system with variables
- ✅ Compass/waypoint tracking
- ✅ Placeholder integration
- ✅ Hot reload without server restart

### Quantum Optimizations
- 🚀 **Player caching** - Reduced API lookups through intelligent caching
- 🚀 **Popup cooldown management** - Prevents popup spam with configurable cooldowns
- 🚀 **Waypoint tracking** - Efficient compass point management
- 🚀 **Automatic cleanup** - Memory optimization through event-based cache clearing
- 🚀 **Utility methods** - Common operations simplified for better code reuse
- 🚀 **Thread-safe operations** - ConcurrentHashMap usage for multi-threaded safety

## Architecture

### Classes

1. **QuantumBetterHudManager** - Main manager class
   - Player HUD management with caching
   - Popup display with cooldown protection
   - HUD updates and event handling

2. **QuantumCompassManager** - Compass/waypoint manager
   - Add/remove waypoints
   - Custom icon support
   - Player-specific waypoint tracking

3. **BetterHudListener** - Event listener
   - Player quit cleanup
   - Cache management

4. **BetterHudUtil** - Utility class
   - Variable map creation
   - Number formatting (K, M, B suffixes)
   - Health bar generation
   - Text truncation and formatting

## Usage Examples

### Initialize the Manager

```java
// In your main plugin class
private QuantumBetterHudManager betterHudManager;
private QuantumCompassManager compassManager;

@Override
public void onEnable() {
    // Initialize BetterHud integration
    betterHudManager = new QuantumBetterHudManager(this);
    betterHudManager.initialize();
    
    // Initialize compass manager
    compassManager = new QuantumCompassManager(betterHudManager, getLogger());
    
    // Register listener
    getServer().getPluginManager().registerEvents(
        new BetterHudListener(betterHudManager, compassManager), 
        this
    );
}
```

### Show Popups

```java
// Simple popup
betterHudManager.showPopup(player, "welcome_popup");

// Popup with variables
Map<String, String> vars = BetterHudUtil.createVariables(
    "player_name", player.getName(),
    "coins", BetterHudUtil.formatNumber(playerCoins),
    "health", BetterHudUtil.formatPercentage(player.getHealth(), 20)
);
betterHudManager.showPopup(player, "stats_popup", vars);

// Single variable
betterHudManager.showPopup(
    player, 
    "level_up", 
    BetterHudUtil.singleVariable("level", "5")
);
```

### Manage Waypoints

```java
// Add a waypoint
Location targetLoc = new Location(world, 100, 64, 200);
compassManager.addWaypoint(player, "treasure", targetLoc);

// Add waypoint with custom icon
compassManager.addWaypoint(player, "home", homeLocation, "house_icon");

// Remove a waypoint
compassManager.removeWaypoint(player, "treasure");

// Clear all waypoints for a player
compassManager.clearWaypoints(player);
```

### Utility Functions

```java
// Format numbers
String formatted = BetterHudUtil.formatNumber(1500000); // "1.5M"

// Create health bar
String healthBar = BetterHudUtil.getColoredHealthBar(
    player.getHealth(), 
    player.getMaxHealth(), 
    10
);

// Format percentages
String percentage = BetterHudUtil.formatPercentage(75, 100); // "75%"

// Truncate text
String shortened = BetterHudUtil.truncate("Very Long Text Here", 10); // "Very Lo..."
```

## Performance Considerations

### Caching Strategy
- Player lookups are cached to avoid repeated API calls
- Waypoints are tracked locally to prevent unnecessary operations
- Popup cooldowns prevent client spam

### Memory Management
- Automatic cleanup on player quit events
- Cache clearing available for reload operations
- Thread-safe collections (ConcurrentHashMap)

### Best Practices
1. Check `betterHudManager.isAvailable()` before operations
2. Use the utility methods to reduce code duplication
3. Batch waypoint operations when possible
4. Leverage popup cooldowns to prevent spam

## Dependencies

This module requires:
- BetterHud 1.14.1 or later
- Paper API 1.21.11 or later
- Kotlin stdlib 2.1.0 (shaded)
- Java 21+

All dependencies are automatically included via Maven shade plugin.

## Building

```bash
# From the betterhud directory
mvn clean package

# From the root Quantum directory
mvn clean package -pl betterhud
```

## Integration with Main Plugin

The BetterHud module is automatically included when building the main Quantum plugin. The shaded JAR contains all necessary dependencies with relocated packages to prevent conflicts.

## API Reference

For complete BetterHud API documentation, see:
- [BetterHud GitHub](https://github.com/toxicity188/BetterHud)
- [BetterHud Documentation](https://deepwiki.com/toxicity188/BetterHud)
- [API Usage Guide](https://deepwiki.com/toxicity188/BetterHud/12.1-using-the-api)

## License

This integration module follows the same license as the Quantum plugin. BetterHud itself is licensed under MIT License.
