# PlaceholderAPI Integration for Quantum Plugin

## Overview

Quantum plugin provides full integration with PlaceholderAPI for Minecraft 1.21.11. This integration allows server administrators to use custom Quantum placeholders in any plugin that supports PlaceholderAPI, including scoreboards, chat plugins, holograms, and more.

## Requirements

- **Minecraft Version**: 1.21.11
- **PlaceholderAPI Plugin**: Version 2.11.6 or higher
- **Quantum Plugin**: Version 1.0.1 or higher

## Features

### Automatic Registration
When PlaceholderAPI is installed, Quantum automatically registers its custom placeholders on startup.

### Wide Compatibility
Use Quantum placeholders in any plugin that supports PlaceholderAPI:
- **Scoreboards**: DeluxeScoreboard, FeatherBoard, etc.
- **Chat**: EssentialsX Chat, ChatControl, etc.
- **Tab Lists**: TAB, Nametagedit, etc.
- **Holograms**: DecentHolograms, HolographicDisplays, etc.
- **And many more!**

## Installation

### Server Setup

1. Download PlaceholderAPI v2.11.6 or later from:
   - SpigotMC: https://www.spigotmc.org/resources/6245/
   - GitHub: https://github.com/PlaceholderAPI/PlaceholderAPI/releases

2. Place `PlaceholderAPI-2.11.6.jar` in your server's `plugins/` folder

3. Place the compiled `Quantum-1.0.1.jar` in your server's `plugins/` folder

4. Start your server

5. Look for this message in console:
   ```
   [Quantum] ✓ PlaceholderAPI integration enabled! (v2.11.6)
   [Quantum] Available placeholders: %quantum_level%, %quantum_job%, %quantum_tower%, and more
   ```

## Available Placeholders

### Player Level Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_level%` | Player's Quantum level | `42` |
| `%quantum_exp%` | Player's current experience | `1500` |
| `%quantum_exp_required%` | Experience required for next level | `2000` |
| `%quantum_exp_progress%` | Experience progress as percentage | `75%` |

### Job System Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_job%` | Player's current job name | `Miner` |
| `%quantum_job_level%` | Player's job level | `15` |
| `%quantum_job_exp%` | Player's job experience | `3200` |

### Tower System Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_tower%` | Current tower ID | `tower_fire` |
| `%quantum_tower_name%` | Current tower display name | `Fire Tower` |
| `%quantum_tower_floor%` | Current floor in tower | `7` |

### Storage System Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_storage_items%` | Total items in storage | `1250` |
| `%quantum_storage_capacity%` | Storage capacity | `2000` |
| `%quantum_storage_used_percent%` | Storage usage percentage | `62%` |

### Economy Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_eco_balance%` | Player's current balance (raw number) | `1234.56` |
| `%quantum_eco_balance_formatted%` | Player's balance formatted with currency | `1234.56 Dollars` |
| `%quantum_eco_currency%` | Currency name (singular) | `Dollar` |
| `%quantum_eco_currency_plural%` | Currency name (plural) | `Dollars` |
| `%quantum_eco_total_buy%` | Total amount spent on purchases | `5000.00` |
| `%quantum_eco_total_sell%` | Total amount earned from sales | `7500.00` |
| `%quantum_eco_net_profit%` | Net profit (sales - purchases) | `2500.00` |
| `%quantum_eco_transactions%` | Total number of transactions | `42` |

### Statistics Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_orders_created%` | Total orders created | `45` |
| `%quantum_orders_filled%` | Total orders filled | `32` |
| `%quantum_items_sold%` | Total items sold | `8500` |
| `%quantum_items_bought%` | Total items bought | `6200` |

### Home System Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_homes%` | Number of homes set | `3` |
| `%quantum_homes_max%` | Maximum homes allowed | `5` |

## Usage Examples

### Scoreboard Configuration

**DeluxeScoreboard example:**
```yaml
scoreboard:
  title: "&6&lMY SERVER"
  lines:
    - "&7Level: &f%quantum_level%"
    - "&7EXP: &f%quantum_exp%&7/&f%quantum_exp_required%"
    - "&7Progress: &f%quantum_exp_progress%"
    - ""
    - "&7Job: &f%quantum_job% &8(Lv.%quantum_job_level%&8)"
    - "&7Tower: &f%quantum_tower_name%"
    - "&7Floor: &f%quantum_tower_floor%"
    - ""
    - "&7Storage: &f%quantum_storage_items%&7/&f%quantum_storage_capacity%"
    - ""
    - "&7Balance: &a%quantum_eco_balance_formatted%"
```

### Chat Format

**EssentialsX Chat example:**
```yaml
chat:
  format: '&7[Lv.%quantum_level%] {DISPLAYNAME}&7: {MESSAGE}'
```

### Hologram

**DecentHolograms example:**
```yaml
lines:
  1: "&6&l⭐ TOP PLAYER ⭐"
  2: "&7Level: &f%quantum_level%"
  3: "&7Job: &f%quantum_job% (Lv.%quantum_job_level%)"
  4: "&7Tower Progress: &fFloor %quantum_tower_floor%"
```

### Tab Plugin Integration

**TAB plugin example:**
```yaml
tablist-name-formatting:
  enabled: true
  prefix: "&7[Lv%quantum_level%] "
  suffix: " &8| &e%quantum_job%"
```

## Testing Placeholders

### Using PlaceholderAPI Commands

```bash
# Parse a placeholder for yourself
/papi parse me %quantum_level%

# Parse a placeholder for another player
/papi parse PlayerName %quantum_level%

# List all Quantum placeholders
/papi ecloud placeholders quantum

# Parse multiple placeholders
/papi parserel me me Level: %quantum_level% Job: %quantum_job%
```

## Building the Plugin

### Prerequisites
1. Java 21 or higher
2. Maven 3.6 or higher
3. Internet connection (for downloading dependencies)

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Build the plugin JAR
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

The compiled JAR will be in: `target/Quantum-1.0.1.jar`

## Troubleshooting

### PlaceholderAPI Integration Not Working

**Problem**: Console shows "PlaceholderAPI not found"
**Solution**: 
1. Ensure PlaceholderAPI plugin is installed
2. Check that PlaceholderAPI loads before Quantum
3. Verify PlaceholderAPI version is 2.11.6 or compatible

### Placeholders Not Updating

**Problem**: Placeholders show old values
**Solution**:
1. Check PlaceholderAPI's refresh interval in its config
2. Try `/papi reload` to reload all expansions
3. Restart the server if issues persist

### Placeholders Show as Text

**Problem**: Placeholder text appears literally (e.g., `%quantum_level%`)
**Solution**:
1. Verify the other plugin supports PlaceholderAPI
2. Check the other plugin's documentation for placeholder syntax
3. Some plugins require enabling PlaceholderAPI support in their config

### Build Failures

**Problem**: Maven cannot resolve PlaceholderAPI dependency
**Solution**:
```bash
# Clear Maven cache
rm -rf ~/.m2/repository/me/clip/placeholderapi

# Force update dependencies
mvn clean install -U
```

## API Integration (For Developers)

### Maven Dependency

```xml
<repository>
    <id>placeholderapi</id>
    <url>https://repo.extendedclip.com/content/repositories/placeholderapi/</url>
</repository>

<dependency>
    <groupId>me.clip</groupId>
    <artifactId>placeholderapi</artifactId>
    <version>2.11.6</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml

```yaml
softdepend:
  - PlaceholderAPI
```

### Creating a Custom Expansion

```java
public class QuantumExpansion extends PlaceholderExpansion {
    
    @Override
    public String getIdentifier() {
        return "quantum";
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(getPlayerLevel(player));
        }
        return null;
    }
}
```

### Registering the Expansion

```java
public class PlaceholderAPIManager {
    private QuantumExpansion expansion;
    
    public void initialize() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new QuantumExpansion(plugin);
            expansion.register();
        }
    }
}
```

## Architecture

### Class Structure

```
com.wynvers.quantum.placeholderapi/
├── PlaceholderAPIManager.java     - Main integration manager
└── QuantumExpansion.java          - PlaceholderAPI expansion implementation
```

### Initialization Flow

1. Quantum plugin starts
2. Checks if PlaceholderAPI plugin is installed
3. Creates `PlaceholderAPIManager`
4. Manager creates and registers `QuantumExpansion`
5. Placeholders are now available to all plugins
6. On shutdown, expansion is unregistered

## Performance Considerations

### Caching
- Player data is cached by managers to reduce database queries
- Placeholder values are computed on-demand when requested
- No scheduled tasks or background updates

### Update Frequency
- PlaceholderAPI controls how often placeholders are refreshed
- Default refresh interval is configurable in PlaceholderAPI's config
- Recommended: 1-5 seconds for most use cases

### Best Practices
- Don't use too many placeholders in high-frequency updates (e.g., action bars)
- Consider using TAB plugin's built-in placeholder system for tab lists
- Cache placeholder results in your own plugins when possible

## Compatibility

This integration is compatible with:
- **PlaceholderAPI**: 2.11.6+
- **Minecraft**: 1.21.11
- **Paper API**: 1.21.11-R0.1-SNAPSHOT
- **Java**: 21+

### Works With
- ✅ All PlaceholderAPI-compatible plugins
- ✅ TAB Plugin (can use both TAB and PlaceholderAPI)
- ✅ Vault-compatible economy plugins
- ✅ LuckPerms permission groups

## Related Integrations

Quantum also provides direct integration with:
- **TAB Plugin**: See `TAB_INTEGRATION.md`

This direct integration may offer better performance or additional features compared to using PlaceholderAPI.

## References

- **PlaceholderAPI Plugin**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **PlaceholderAPI Wiki**: https://wiki.placeholderapi.com/
- **PlaceholderAPI Expansions**: https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders
- **Quantum Repository**: https://github.com/kazotaruumc72/Quantum

## Support

For issues related to:
- **Quantum Integration**: Create an issue on Quantum's GitHub
- **PlaceholderAPI Plugin**: Visit PlaceholderAPI's GitHub or Discord
- **Other Plugins**: Consult their respective documentation

## Credits

- **PlaceholderAPI**: Clip, extended_clip, and contributors
- **Quantum Integration**: kazotaruumc72/Quantum development team

## License

This integration follows Quantum plugin's license. PlaceholderAPI usage follows PlaceholderAPI's license terms.

---

**Version**: PlaceholderAPI 2.11.6 | Minecraft 1.21.11  
**Last Updated**: 2026-02-12
