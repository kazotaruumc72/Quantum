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

**Note:** As of the latest version, Quantum PlaceholderAPI integration uses a **delegation system** that automatically makes **ALL internal Quantum placeholders** available through PlaceholderAPI. This means every placeholder that works in Quantum menus and internal systems is also available for use in any PlaceholderAPI-compatible plugin. The placeholders listed below are the most commonly used ones, but many more are available (storage modes, item amounts, tower progress, order details, transaction history, etc.).

### Player Level Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_level%` | Player's Quantum level | `42` |
| `%quantum_exp%` | Player's current experience | `1500` |
| `%quantum_exp_required%` | Experience required for next level | `2000` |
| `%quantum_exp_progress%` | Experience progress (0-100) | `75` |

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

### Economy Placeholders (Primary Currency)

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_eco_balance%` | Player's current balance (raw number) | `1234.56` |
| `%quantum_eco_balance_formatted%` | Player's balance formatted with currency | `1234.56$` |
| `%quantum_eco_currency%` | Currency name (singular) | `Dollar` |
| `%quantum_eco_currency_plural%` | Currency name (plural) | `Dollars` |
| `%quantum_eco_symbol%` | Currency symbol (supports Nexo glyphs) | `$` |
| `%quantum_eco_total_buy%` | Total amount spent on purchases | `5000.00` |
| `%quantum_eco_total_sell%` | Total amount earned from sales | `7500.00` |
| `%quantum_eco_net_profit%` | Net profit (sales - purchases) | `2500.00` |
| `%quantum_eco_transactions%` | Total number of transactions | `42` |

### Per-Currency Placeholders (Multi-Currency)

Replace `<id>` with the currency identifier from `config.yml` (e.g., `dollar`, `token`, `coin`).

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_eco_<id>_balance%` | Balance for specific currency | `500.00` |
| `%quantum_eco_<id>_balance_formatted%` | Formatted balance for specific currency | `500.00⛃` |
| `%quantum_eco_<id>_symbol%` | Symbol of specific currency (Nexo glyphs supported) | `⛃` |
| `%quantum_eco_<id>_currency%` | Name of specific currency (singular) | `Token` |
| `%quantum_eco_<id>_currency_plural%` | Name of specific currency (plural) | `Tokens` |

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

### Additional Placeholders (via Delegation System)

The following placeholder categories are also available through the delegation system:

#### Storage Mode Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_mode%` | Current storage mode | `NORMAL` |
| `%quantum_mode_display%` | Storage mode with colors | `§aNormal Mode` |
| `%quantum_mode_simple%` | Simple mode display | `Normal` |
| `%quantum_storage_total%` | Total items (all stacks) | `15000` |

#### Item Amount Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_amt_<item_id>%` | Amount of specific item | `64` |
| `%quantum_amt_nexo-<nexo_id>%` | Amount of Nexo custom item | `32` |
| `%quantum_amt_minecraft-<material>%` | Amount of Minecraft item | `128` |

**Examples:**
- `%quantum_amt_minecraft-diamond%` - Amount of diamonds in storage
- `%quantum_amt_nexo-custom_sword%` - Amount of custom Nexo sword in storage

#### Extended Job Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_job_name%` | Current job display name | `Miner` |
| `%quantum_job_exp_needed%` | EXP needed for next level | `1000` |
| `%quantum_job_exp_progress%` | EXP progress display | `500/1000` |
| `%quantum_job_rank%` | Player's rank in their job | `5` |
| `%quantum_job_booster_exp%` | Active EXP booster multiplier | `2.0` |
| `%quantum_job_booster_money%` | Active money booster multiplier | `1.5` |
| `%quantum_job_boosters_active%` | Number of active boosters | `2` |

#### Extended Tower Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_tower_current%` | Current tower name | `Fire Tower` |
| `%quantum_tower_progress%` | Tower progress display | `5/10` |
| `%quantum_tower_next_boss%` | Next boss floor number | `10` |
| `%quantum_tower_status%` | Tower status message | `§aIn Progress` |
| `%quantum_total_floors_completed%` | Total floors across all towers | `25/100` |
| `%quantum_tower_<id>_progress%` | Specific tower progress | `3/10` |
| `%quantum_tower_<id>_percentage%` | Specific tower percentage | `30.0` |
| `%quantum_tower_<id>_completed%` | Whether tower is completed | `false` |

#### Apartment Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_apartment_name%` | Apartment name | `My Home` |
| `%quantum_apartment_size%` | Apartment size | `Medium` |
| `%quantum_apartment_deadline%` | Contract deadline | `30 days` |
| `%quantum_apartment_furniture_count%` | Number of furniture items | `15` |

#### Coordinate Placeholders
| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quantum_player_x%` | Player's X coordinate | `125` |
| `%quantum_player_y%` | Player's Y coordinate | `64` |
| `%quantum_player_z%` | Player's Z coordinate | `-350` |

**Note:** These placeholders work for online players through the delegation system. Some placeholders may require specific managers to be initialized (e.g., VaultManager for economy placeholders).

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
    - "&7Tokens: &e%quantum_eco_token_balance% %quantum_eco_token_symbol%"
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

com.wynvers.quantum.managers/
└── PlaceholderManager.java        - Internal placeholder resolution (used by delegation)
```

### Initialization Flow

1. Quantum plugin starts
2. Checks if PlaceholderAPI plugin is installed
3. Creates `PlaceholderAPIManager`
4. Manager creates and registers `QuantumExpansion`
5. Placeholders are now available to all plugins
6. On shutdown, expansion is unregistered

### Delegation System (New)

**How It Works:**

The PlaceholderAPI integration now uses a **delegation system** to ensure consistency between internal and external placeholder resolution:

1. When a placeholder is requested through PlaceholderAPI (e.g., `%quantum_eco_balance%`)
2. `QuantumExpansion.onRequest()` is called
3. **For online players**: The request is delegated to the internal `PlaceholderManager`
4. `PlaceholderManager` resolves the placeholder using the same logic as internal menus
5. The resolved value is returned to PlaceholderAPI
6. **For offline players**: Fallback logic in `QuantumExpansion` handles the request

**Benefits:**

- ✅ **Complete Placeholder Coverage**: ALL internal placeholders automatically work through PlaceholderAPI
- ✅ **Consistency**: Same placeholder resolution logic everywhere
- ✅ **Maintainability**: Only need to update PlaceholderManager for new placeholders
- ✅ **Backward Compatibility**: Offline player placeholders still work
- ✅ **Economy Placeholders**: Fully functional through delegation

**Code Example:**

```java
@Override
public String onRequest(OfflinePlayer offlinePlayer, String params) {
    Player player = offlinePlayer.getPlayer();

    // Delegate to internal PlaceholderManager for online players
    if (plugin.getPlaceholderManager() != null && player != null) {
        String fullPlaceholder = "%" + params + "%";
        String result = plugin.getPlaceholderManager().parse(player, fullPlaceholder);

        // If resolved, return the result
        if (!result.equals(fullPlaceholder)) {
            return result;
        }
    }

    // Fallback for offline players or unresolved placeholders
    // ... (existing offline player logic)
}
```

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
