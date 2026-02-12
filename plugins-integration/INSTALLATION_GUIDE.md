# Complete Plugin Installation Guide

This guide provides step-by-step instructions for installing and configuring BetterHud, TAB, and PlaceholderAPI with the Quantum plugin.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Download Links](#download-links)
3. [Installation Steps](#installation-steps)
4. [Configuration](#configuration)
5. [Verification](#verification)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

Before installing the plugin integrations, ensure you have:

### Server Requirements
- ✅ **Minecraft Server**: 1.21.11 (Paper recommended, Spigot compatible)
- ✅ **Java**: Version 21 or higher
- ✅ **Quantum Plugin**: Version 1.0.1 or higher
- ✅ **Nexo Plugin**: Required dependency for Quantum

### Optional But Recommended
- Server admin/operator permissions
- FTP/SFTP access to server files
- Basic knowledge of YAML configuration

## Download Links

### Required Plugin: Quantum
- **GitHub**: https://github.com/kazotaruumc72/Quantum
- **Version**: 1.0.1+

### Integration Plugins (All Optional)

#### BetterHud
- **Version**: 1.14.1+
- **SpigotMC**: https://www.spigotmc.org/resources/115559/
- **GitHub**: https://github.com/toxicity188/BetterHud
- **Author**: toxicity188

#### TAB
- **Version**: 5.5.0+
- **GitHub Releases**: https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
- **SpigotMC**: https://www.spigotmc.org/resources/57806/
- **Author**: NEZNAMY

#### PlaceholderAPI
- **Version**: 2.11.6+
- **SpigotMC**: https://www.spigotmc.org/resources/6245/
- **GitHub**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **Authors**: Clip, extended_clip, and contributors

## Installation Steps

### Step 1: Download Plugin JARs

Download the following files:

```
Required:
├── Quantum-1.0.1.jar
└── Nexo-1.18.jar (or compatible version)

Optional (any or all):
├── BetterHud-1.14.1.jar
├── TAB-v5.5.0.jar
└── PlaceholderAPI-2.11.6.jar
```

### Step 2: Stop Your Server

Before installing plugins, stop your Minecraft server:
```bash
/stop
```

Or use your server control panel's stop function.

### Step 3: Install Plugin Files

Upload the JAR files to your server's `plugins/` folder:

```
server/
└── plugins/
    ├── Nexo-1.18.jar (required)
    ├── Quantum-1.0.1.jar (required)
    ├── BetterHud-1.14.1.jar (optional)
    ├── TAB-v5.5.0.jar (optional)
    └── PlaceholderAPI-2.11.6.jar (optional)
```

**Using FTP/SFTP:**
1. Connect to your server
2. Navigate to the `plugins/` folder
3. Upload the JAR files
4. Ensure file permissions are correct (usually 644 or 755)

**Using Server Panel:**
1. Access your hosting control panel
2. Navigate to File Manager
3. Go to `plugins/` folder
4. Upload the JAR files

### Step 4: Start Your Server

Start your Minecraft server. The plugins will:
1. Load and initialize
2. Create configuration folders
3. Generate default configuration files

Watch the console for messages like:
```
[Quantum] Enabling Quantum v1.0.1
[Quantum] ✓ BetterHud integration initialized!
[Quantum] ✓ TAB integration enabled! (v5.5.0)
[Quantum] ✓ PlaceholderAPI integration enabled! (v2.11.6)
```

### Step 5: Verify Installation

After server startup, check that all plugins loaded:

```bash
/plugins
```

You should see (in green):
- Quantum
- Nexo
- BetterHud (if installed)
- TAB (if installed)
- PlaceholderAPI (if installed)

## Configuration

### BetterHud Configuration

After first run, BetterHud creates:
```
plugins/BetterHud/
├── config.yml
├── huds.yml
├── popups.yml
└── compass.yml
```

**Apply Quantum Examples:**
```bash
# Copy from Quantum integration folder
cp plugins-integration/betterhud/examples/* plugins/BetterHud/
```

Or manually copy files from:
- `plugins-integration/betterhud/examples/`

### TAB Configuration

After first run, TAB creates:
```
plugins/TAB/
├── config.yml
├── animations.yml
├── scoreboard.yml
└── (other files)
```

**Apply Quantum Examples:**
```bash
# Use examples as reference
# Copy specific configurations you want from:
cp plugins-integration/tab/examples/config_example.yml plugins/TAB/config.yml
cp plugins-integration/tab/examples/scoreboard_example.yml plugins/TAB/scoreboard.yml
```

**Or manually edit** TAB's config files using the examples in:
- `plugins-integration/tab/examples/`

### PlaceholderAPI Configuration

**No configuration needed!** Quantum automatically registers its expansion with PlaceholderAPI.

**Verify it works:**
```bash
/papi parse me %quantum_level%
```

Should return your current level (e.g., "42").

### Example Configurations for Other Plugins

If you use other plugins that support PlaceholderAPI, see examples in:
- `plugins-integration/placeholderapi/examples/chat_example.yml` (EssentialsX)
- `plugins-integration/placeholderapi/examples/hologram_example.yml` (DecentHolograms)
- `plugins-integration/placeholderapi/examples/scoreboard_example.yml` (DeluxeScoreboard)
- `plugins-integration/placeholderapi/examples/tab_example.yml` (TAB with PAPI)

## Verification

### Test BetterHud Integration

1. **Join the server** as a player
2. **Check for HUD elements** (if configured)
3. **Test a popup** by performing an action that triggers one (e.g., level up)
4. **Check console** for any BetterHud errors

**Admin Commands:**
```bash
/betterhud reload  # Reload BetterHud configurations
```

### Test TAB Integration

1. **Open tablist** (press Tab key)
2. **Check for Quantum placeholders** displaying correctly
3. **Look at nametags** above players
4. **View scoreboard** (if enabled)

**Admin Commands:**
```bash
/tab reload      # Reload TAB
/tab debug       # Debug information
```

### Test PlaceholderAPI Integration

**Test individual placeholders:**
```bash
/papi parse me %quantum_level%
/papi parse me %quantum_job%
/papi parse me %quantum_tower_floor%
/papi parse me %quantum_storage_items%
```

**Check registered expansion:**
```bash
/papi ecloud info Quantum
```

**Reload PlaceholderAPI:**
```bash
/papi reload
```

## Complete Installation Example

Here's a complete installation walkthrough:

### Scenario: Fresh Server Setup

```bash
# 1. Stop server
/stop

# 2. Upload plugins (via FTP or panel)
# Upload to: /server/plugins/
- Nexo-1.18.jar
- Quantum-1.0.1.jar
- BetterHud-1.14.1.jar
- TAB-v5.5.0.jar
- PlaceholderAPI-2.11.6.jar

# 3. Start server
# (Server generates default configs)

# 4. Stop server again
/stop

# 5. Apply Quantum configurations
# Copy from plugins-integration/ to respective plugin folders

# 6. Start server
# Integrations are now active!

# 7. Verify
/plugins
/papi parse me %quantum_level%
/tab reload
```

## Troubleshooting

### Plugins Not Loading

**Problem:** Plugins show as red in `/plugins` command

**Solutions:**
1. Check Java version: `java -version` (must be 21+)
2. Verify JAR file integrity (re-download if needed)
3. Check server logs for specific errors
4. Ensure server is Paper/Spigot 1.21.11

### Integration Not Detected

**Problem:** Console doesn't show integration messages

**Solutions:**
1. Verify plugin load order (use `/plugins` command)
2. Check that both Quantum and integration plugin loaded successfully
3. Ensure compatible versions are installed
4. Review server startup logs for errors
5. Try restarting the server

### Placeholders Not Working

**Problem:** Placeholders show as literal text (e.g., "%quantum_level%")

**Solutions:**
1. For PlaceholderAPI: Run `/papi reload`
2. For TAB: Run `/tab reload`
3. For BetterHud: Run `/betterhud reload`
4. Verify the plugin supports placeholders
5. Check placeholder syntax (must be exact)

### Configuration Errors

**Problem:** Server won't start or shows config errors

**Solutions:**
1. Validate YAML syntax (use https://www.yamllint.com/)
2. Check for tab/space mixing (use spaces only)
3. Ensure file encoding is UTF-8
4. Review specific error messages in console
5. Restore default configs and reapply changes

### Performance Issues

**Problem:** Server lag after installing plugins

**Solutions:**
1. Increase refresh intervals in TAB config
2. Reduce number of active BetterHud elements
3. Optimize PlaceholderAPI refresh rates
4. Check server resources (CPU, RAM)
5. Review timings report: `/timings paste`

### Permission Issues

**Problem:** Players can't see HUD elements or scoreboards

**Solutions:**
1. Check plugin-specific permissions
2. Verify LuckPerms/permission plugin is configured
3. Test with operator status first
4. Review each plugin's permission nodes

## Getting Help

### Documentation Resources
- **Quantum**: [README.md](../README.md)
- **Integrations**: [PLUGIN_INTEGRATIONS.md](../PLUGIN_INTEGRATIONS.md)
- **BetterHud**: [BETTERHUD_INTEGRATION.md](../BETTERHUD_INTEGRATION.md)
- **TAB**: [TAB_INTEGRATION.md](../TAB_INTEGRATION.md)
- **PlaceholderAPI**: [PLACEHOLDERAPI_INTEGRATION.md](../PLACEHOLDERAPI_INTEGRATION.md)

### Support Channels
- **Quantum Issues**: https://github.com/kazotaruumc72/Quantum/issues
- **BetterHud**: SpigotMC resource discussion
- **TAB**: GitHub Issues and Discord
- **PlaceholderAPI**: Discord and GitHub

### Useful Commands Reference

**Quantum:**
```bash
/quantum reload    # Reload Quantum configuration
/quantum version   # Check version
```

**BetterHud:**
```bash
/betterhud reload  # Reload BetterHud
/bh reload         # Short version
```

**TAB:**
```bash
/tab reload        # Reload TAB
/tab debug         # Debug information
```

**PlaceholderAPI:**
```bash
/papi reload       # Reload PlaceholderAPI
/papi parse <player> <placeholder>  # Test placeholder
/papi ecloud info <expansion>       # Expansion info
```

## Next Steps

After successful installation:

1. **Customize configurations** to match your server's theme
2. **Test all features** thoroughly
3. **Configure permissions** for players
4. **Set up backups** of working configurations
5. **Review documentation** for advanced features
6. **Join support communities** for updates and help

## Additional Notes

- All three integration plugins are **optional** - Quantum works without them
- You can install any combination (e.g., just TAB, or TAB + PlaceholderAPI)
- Integrations add features but don't change core Quantum functionality
- Keep plugins updated for best compatibility and security
- Always test on a development server before applying to production

---

**Installation Guide Version**: 1.0  
**Last Updated**: 2026-02-12  
**Quantum Version**: 1.0.1  
**Minecraft Version**: 1.21.11
