# Plugin Integrations Overview

## Summary

The Quantum plugin now includes full integration support for two major Minecraft server plugins:

1. **TAB** (https://github.com/NEZNAMY/TAB) - Advanced tablist, nametags, and scoreboard
2. **PlaceholderAPI** (@PlaceholderAPI/PlaceholderAPI) - Universal placeholder system

Both plugins are **soft dependencies**, meaning Quantum will work with or without them installed. When installed, they provide enhanced functionality and display capabilities.

## Integration Status

| Plugin | Status | Version | Documentation |
|--------|--------|---------|---------------|
| TAB | ✅ Fully Integrated | 5.5.0 | [TAB_INTEGRATION.md](TAB_INTEGRATION.md) |
| PlaceholderAPI | ✅ Fully Integrated | 2.11.6 | [PLACEHOLDERAPI_INTEGRATION.md](PLACEHOLDERAPI_INTEGRATION.md) |

## Quick Start

### Installing All Integrations

1. **Download the plugins:**
   - TAB v5.5.0: https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
   - PlaceholderAPI v2.11.6: https://www.spigotmc.org/resources/6245/

2. **Install Quantum and the optional plugins:**
   ```
   server/plugins/
   ├── Quantum-1.0.1.jar
   ├── TAB-v5.5.0.jar (optional)
   └── PlaceholderAPI-2.11.6.jar (optional)
   ```

3. **Start your server**

4. **Verify integration in console:**
   ```
   [Quantum] ✓ TAB integration enabled! (v5.5.0, MiniMessage support available)
   [Quantum] ✓ PlaceholderAPI integration enabled! (v2.11.6)
   ```

## Feature Comparison

### TAB Integration
**Best for:** Tablist, nametags, scoreboards
- ✨ Custom placeholders for TAB displays
- ✨ MiniMessage support for modern text formatting
- ✨ Player sorting by level/job/tower
- ✨ Conditional formatting based on Quantum stats
- 📝 See: [TAB_INTEGRATION.md](TAB_INTEGRATION.md)

**Example Use Cases:**
- Show player level in tab list
- Display job and level in nametags
- Tower progress in scoreboard
- Custom prefix/suffix with Quantum data

### PlaceholderAPI Integration
**Best for:** Universal compatibility with other plugins
- ✨ 20+ custom placeholders for Quantum data
- ✨ Works with ANY PlaceholderAPI-compatible plugin
- ✨ Offline player support for some placeholders
- ✨ Compatible with scoreboards, chat, holograms, and more
- 📝 See: [PLACEHOLDERAPI_INTEGRATION.md](PLACEHOLDERAPI_INTEGRATION.md)

**Example Use Cases:**
- EssentialsX chat format with Quantum level
- DecentHolograms showing player statistics
- DeluxeScoreboard with Quantum data
- Any plugin that supports PlaceholderAPI

## Available Quantum Placeholders

All three integrations provide access to Quantum data through placeholders:

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

## Which Integration Should I Use?

### Use TAB when:
- ✅ You want to customize tab list
- ✅ You need nametags above players
- ✅ You want a built-in scoreboard
- ✅ You need advanced sorting/grouping

### Use PlaceholderAPI when:
- ✅ You use many other plugins
- ✅ You want maximum compatibility
- ✅ You already have PlaceholderAPI
- ✅ You need placeholders in chat, holograms, etc.

### Use Multiple Integrations:
You can use both together! They complement each other:
- **TAB** for tablist and nametags
- **PlaceholderAPI** for everything else (chat, holograms, etc.)

## Configuration Examples

### Example 1: Complete Server Setup
```
# Use both integrations together
- TAB: Show level in tab list and nametags
- PlaceholderAPI: Level in chat via EssentialsX
```

### Example 2: Minimal Setup
```
# Use only PlaceholderAPI for broad compatibility
- PlaceholderAPI: Works with TAB, EssentialsX, Holograms, etc.
```

### Example 3: Visual Focus
```
# Use TAB for rich visual experience
- TAB: Enhanced tablist and nametags
```

## Example Configurations Included

### PlaceholderAPI Examples
Located in `src/main/resources/placeholderapi-examples/`:
- `scoreboard_example.yml` - DeluxeScoreboard config
- `chat_example.yml` - EssentialsX Chat config
- `hologram_example.yml` - DecentHolograms config
- `tab_example.yml` - TAB plugin config

## Performance Considerations

### All Integrations are Optimized:
- ✅ Player data caching to reduce database queries
- ✅ Soft dependencies - no performance impact when not installed
- ✅ Efficient placeholder resolution
- ✅ No scheduled tasks (update on-demand only)

### Recommended Settings:
- **TAB**: 1-5 second refresh interval
- **PlaceholderAPI**: Controlled by individual plugins

## Troubleshooting

### Plugin Not Detected
**Problem**: Console shows "Plugin not found"
**Solution**: 
1. Verify plugin is installed in `/plugins/`
2. Check that it loads before Quantum (use `/plugins` command)
3. Ensure correct version is installed

### Placeholders Not Working
**Problem**: Placeholders show as literal text
**Solution**:
1. Check syntax: `%quantum_level%` (not `{quantum_level}`)
2. Verify Quantum integration is enabled (check console logs)
3. Ensure other plugin supports PlaceholderAPI/TAB
4. Try `/papi parse me %quantum_level%` (PlaceholderAPI)

### Build Errors
**Problem**: Maven build fails
**Solution**:
1. Ensure Java 21+ is installed
2. Check internet connection (Maven downloads dependencies)
3. Clear Maven cache: `mvn clean install -U`
4. See individual integration docs for detailed troubleshooting

## Dependencies

### Maven (pom.xml)
All three integrations are configured as provided dependencies:

```xml
<!-- TAB API -->
<dependency>
    <groupId>com.github.NEZNAMY</groupId>
    <artifactId>TAB-API</artifactId>
    <version>5.5.0</version>
    <scope>provided</scope>
</dependency>

<!-- PlaceholderAPI -->
<dependency>
    <groupId>me.clip</groupId>
    <artifactId>placeholderapi</artifactId>
    <version>2.11.6</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml
Both are configured as soft dependencies:

```yaml
softdepend:
  - TAB
  - PlaceholderAPI
```

## API for Developers

### Accessing Integrations from Code

```java
Quantum plugin = Quantum.getInstance();

// TAB
TABManager tabManager = plugin.getTabManager();
if (tabManager != null && tabManager.isEnabled()) {
    // TAB placeholders are registered automatically
}

// PlaceholderAPI
PlaceholderAPIManager papiManager = plugin.getPlaceholderAPIManager();
if (papiManager != null && papiManager.isEnabled()) {
    // PlaceholderAPI expansion is registered automatically
}
```

## Version Compatibility

| Component | Required Version | Notes |
|-----------|-----------------|-------|
| Minecraft | 1.21.11 | All integrations tested on 1.21.11 |
| Paper API | 1.21.11-R0.1-SNAPSHOT | Or compatible Spigot/Paper fork |
| Java | 21+ | Required for Quantum |
| TAB | 5.5.0+ | Optional |
| PlaceholderAPI | 2.11.6+ | Optional |

## Support & Resources

### Documentation
- [TAB Integration](TAB_INTEGRATION.md)
- [PlaceholderAPI Integration](PLACEHOLDERAPI_INTEGRATION.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)

### External Resources
- **TAB**: https://github.com/NEZNAMY/TAB
- **PlaceholderAPI**: https://github.com/PlaceholderAPI/PlaceholderAPI

### Getting Help
1. Check the integration documentation for your specific plugin
2. Review example configurations in `src/main/resources/`
3. Check console logs for initialization messages
4. Create an issue on Quantum's GitHub repository

## Credits

- **TAB Plugin**: NEZNAMY
- **PlaceholderAPI**: Clip, extended_clip, and contributors
- **Quantum Integrations**: kazotaruumc72/Quantum development team

## License

These integrations follow Quantum plugin's license. Each integrated plugin follows its own respective license terms.

---

**Last Updated**: 2026-02-12  
**Quantum Version**: 1.0.1  
**Minecraft Version**: 1.21.11
