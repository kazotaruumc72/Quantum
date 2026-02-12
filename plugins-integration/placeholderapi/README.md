# PlaceholderAPI Integration for Quantum

## 📦 Plugin Information

- **Plugin Name**: PlaceholderAPI
- **Author**: Clip, extended_clip, and contributors
- **Version**: 2.11.6+
- **Minecraft**: 1.21.11
- **Purpose**: Universal placeholder system for Minecraft plugins

## 🔗 Download Links

- **GitHub**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **SpigotMC**: https://www.spigotmc.org/resources/6245/
- **Wiki**: https://github.com/PlaceholderAPI/PlaceholderAPI/wiki
- **Placeholders List**: https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders

## 📥 Installation

### 1. Download PlaceholderAPI
Download PlaceholderAPI v2.11.6 or later from SpigotMC.

### 2. Install Plugin
Place `PlaceholderAPI-2.11.6.jar` in your `server/plugins/` folder:
```
server/plugins/PlaceholderAPI-2.11.6.jar
```

### 3. Start Server
Start your server to load PlaceholderAPI.

### 4. Automatic Registration
Quantum automatically registers its expansion with PlaceholderAPI when both plugins are loaded. No additional configuration needed!

### 5. Verify Installation
Use the command to test placeholders:
```
/papi parse me %quantum_level%
```

## ✨ Integration Features

### Automatic Expansion Registration
The Quantum expansion (`QuantumExpansion`) is automatically registered when PlaceholderAPI is detected. No manual installation required!

### Universal Compatibility
PlaceholderAPI makes Quantum data available to **any** plugin that supports PlaceholderAPI, including:
- **Chat Plugins**: EssentialsX, ChatControl, VentureChat
- **Hologram Plugins**: DecentHolograms, HolographicDisplays
- **Scoreboard Plugins**: DeluxeScoreboard, FeatherBoard
- **Tab Plugins**: TAB, NametagEdit
- **And many more!**

### Offline Player Support
Some Quantum placeholders work with offline players when data is cached.

## 🔖 Available Quantum Placeholders

All placeholders use the format: `%quantum_<placeholder>%`

### Player Level & Experience
```
%quantum_level%              - Player's current level
%quantum_exp%                - Current experience points
%quantum_exp_required%       - Experience needed for next level
%quantum_exp_progress%       - Progress percentage (0-100)
```

### Job System
```
%quantum_job%                - Current job name (e.g., "Miner")
%quantum_job_level%          - Job level
%quantum_job_exp%            - Job experience points
```

### Tower System
```
%quantum_tower%              - Current tower ID
%quantum_tower_name%         - Tower display name
%quantum_tower_floor%        - Current floor number
```

### Storage System
```
%quantum_storage_items%      - Number of items in storage
%quantum_storage_capacity%   - Total storage capacity
%quantum_storage_used_percent% - Usage percentage
```

### Statistics & Orders
```
%quantum_orders_created%     - Total orders created
%quantum_orders_filled%      - Total orders filled
%quantum_items_sold%         - Total items sold
%quantum_items_bought%       - Total items bought
```

### Home System
```
%quantum_homes%              - Number of homes set
%quantum_homes_max%          - Maximum homes allowed
```

## 📝 Usage Examples

### Chat Plugin (EssentialsX)
Edit `plugins/Essentials/config.yml`:

```yaml
chat:
  format: '<{DISPLAYNAME}> {MESSAGE}'
  group-formats:
    Default: '&7[Lv.%quantum_level%] &f{DISPLAYNAME}&7: &f{MESSAGE}'
```

Result: `[Lv.42] Steve: Hello!`

### Hologram (DecentHolograms)
Create a hologram showing player stats:

```yaml
lines:
  - '&6&l%player%'
  - '&7Level: &e%quantum_level%'
  - '&7Job: &e%quantum_job% &7(Lv.%quantum_job_level%)'
  - '&7Tower Floor: &e%quantum_tower_floor%'
```

### Scoreboard (DeluxeScoreboard)
Edit scoreboard configuration:

```yaml
title: '&6&lQUANTUM'
lines:
  15: '&7&m---------------'
  14: '&ePlayer Stats'
  13: '&7Level: &f%quantum_level%'
  12: '&7EXP: &f%quantum_exp%&7/&f%quantum_exp_required%'
  11: '&7Progress: &f%quantum_exp_progress%&7%'
  10: ''
  9: '&bJob Information'
  8: '&7Job: &f%quantum_job%'
  7: '&7Level: &f%quantum_job_level%'
  6: '&7EXP: &f%quantum_job_exp%'
  5: ''
  4: '&dTower Progress'
  3: '&7Tower: &f%quantum_tower_name%'
  2: '&7Floor: &f%quantum_tower_floor%'
  1: '&7&m---------------'
```

### TAB Plugin
PlaceholderAPI placeholders work in TAB too:

```yaml
tablist-name-formatting:
  format: '&e[%quantum_level%] &f%player%'
```

### MOTD (Server List)
Use in `server.properties` or MOTD plugins:

```
&6Welcome to Quantum Server!
&7Average Level: %quantum_level%
```

## 🔧 Advanced Usage

### Relational Placeholders
Quantum supports comparing data between players:

```
%rel_quantum_level%          - Compare levels between two players
```

### Mathematical Operations
Use PlaceholderAPI's math feature:

```
%math_0_{quantum_level}+10%  - Add 10 to level
%math_0_{quantum_exp}/{quantum_exp_required}*100% - Calculate percentage
```

### Conditional Placeholders
Use PlaceholderAPI's conditional feature:

```
%quantum_level% > 50 ? &6PRO : &7Newbie
```

### Multiple Placeholders
Combine multiple Quantum placeholders:

```yaml
format: '&e[Lv.%quantum_level%] &b%quantum_job% &f%player%'
```

Result: `[Lv.42] Miner Steve`

## 📋 Configuration Examples

The `examples/` directory contains full configuration examples for popular plugins:

### chat_example.yml
EssentialsX chat format with Quantum placeholders.

### hologram_example.yml
DecentHolograms configuration showing player stats.

### scoreboard_example.yml
DeluxeScoreboard layout with Quantum data.

### tab_example.yml
TAB plugin configuration using PlaceholderAPI placeholders.

## 🎯 Plugin Compatibility

Quantum placeholders work with these popular plugins:

### Chat Plugins ✅
- EssentialsX Chat
- ChatControl Pro
- VentureChat
- ChatEx
- DeluxeChat

### Hologram Plugins ✅
- DecentHolograms
- HolographicDisplays
- TrHologram
- CMI Holograms

### Scoreboard Plugins ✅
- DeluxeScoreboard
- FeatherBoard
- AnimatedScoreboard
- Scoreboard

### Tab/Nametag Plugins ✅
- TAB
- NametagEdit
- TabList
- UltraTablist

### Other Compatible Plugins ✅
- DiscordSRV (Discord bot messages)
- ConditionalEvents
- DeluxeMenus
- TitleManager
- ActionBarAPI

## 🔍 Testing Placeholders

### Parse Command
Test any placeholder as yourself:
```
/papi parse me %quantum_level%
```

### Parse Another Player
Test placeholders for another player:
```
/papi parse Steve %quantum_job%
```

### List Quantum Placeholders
See all registered Quantum placeholders:
```
/papi ecloud info Quantum
```

### Reload PlaceholderAPI
Reload after changes:
```
/papi reload
```

## 🐛 Troubleshooting

### Placeholder Shows as Text
**Problem**: `%quantum_level%` appears literally instead of the value

**Solutions**:
1. Verify PlaceholderAPI is installed and loaded
2. Check Quantum integration message in console
3. Test with `/papi parse me %quantum_level%`
4. Ensure target plugin supports PlaceholderAPI
5. Try `/papi reload`

### Placeholder Shows "N/A" or Empty
**Problem**: Placeholder is recognized but shows no value

**Solutions**:
1. Verify player has Quantum data (level, job, etc.)
2. Check if player is online (some placeholders require it)
3. Review console for Quantum errors
4. Ensure database is connected

### Performance Issues
**Problem**: Server lag when using many placeholders

**Solutions**:
1. Increase refresh intervals in scoreboard/tab plugins
2. Cache placeholder values where possible
3. Reduce number of holograms with placeholders
4. Use static text where data doesn't change often

### Integration Not Detected
**Problem**: Console doesn't show PlaceholderAPI integration

**Solutions**:
1. Ensure PlaceholderAPI loads before Quantum
2. Check for plugin conflicts
3. Verify compatible versions
4. Review startup logs for errors

## 📚 Additional Resources

### Documentation
- **Main Integration Guide**: [../../PLACEHOLDERAPI_INTEGRATION.md](../../PLACEHOLDERAPI_INTEGRATION.md)
- **Plugin Integrations Overview**: [../../PLUGIN_INTEGRATIONS.md](../../PLUGIN_INTEGRATIONS.md)
- **PlaceholderAPI Wiki**: https://github.com/PlaceholderAPI/PlaceholderAPI/wiki

### Example Configurations
All example files are in the `examples/` directory:
- EssentialsX chat format
- DecentHolograms templates
- DeluxeScoreboard layouts
- TAB configurations

### Support
- **Quantum Issues**: https://github.com/kazotaruumc72/Quantum/issues
- **PlaceholderAPI Discord**: Check GitHub for invite
- **PlaceholderAPI Issues**: https://github.com/PlaceholderAPI/PlaceholderAPI/issues

## 🎓 Best Practices

### Performance
- Use appropriate refresh intervals
- Cache values when possible
- Avoid excessive placeholder usage
- Monitor server performance

### Formatting
- Keep formats readable and clean
- Use consistent color schemes
- Test on different screen sizes
- Consider colorblind accessibility

### Maintenance
- Test placeholders after Quantum updates
- Keep PlaceholderAPI updated
- Review logs for deprecation warnings
- Document custom configurations

## 📝 Version History

### Quantum 1.0.1 - PlaceholderAPI 2.11.6
- ✅ 20+ custom placeholders
- ✅ Automatic expansion registration
- ✅ Offline player support
- ✅ Universal compatibility
- ✅ Optimized caching

## 👥 Credits

- **PlaceholderAPI**: Clip, extended_clip, and contributors
- **Quantum Integration**: kazotaruumc72/Quantum team
- **Example Configurations**: Quantum development team

---

**For complete integration documentation, see [PLACEHOLDERAPI_INTEGRATION.md](../../PLACEHOLDERAPI_INTEGRATION.md)**
