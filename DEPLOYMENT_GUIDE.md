# Quick Deployment Guide - TAB Integration v5.5.0

## 📋 Prerequisites

- Java 21+
- Maven 3.6+
- Internet connection
- Minecraft 1.21.11 Server (Paper/Spigot)

## 🔨 Building the Plugin

```bash
# Clone the repository (if not already done)
git clone https://github.com/kazotaruumc72/Quantum.git
cd Quantum

# Build the plugin
mvn clean package

# The compiled JAR will be in: target/Quantum-1.0.1.jar
```

## 📥 Download TAB Plugin

Get TAB v5.5.0 from one of these sources:
- **GitHub**: https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
- **SpigotMC**: https://www.spigotmc.org/resources/57806/
- **Modrinth**: https://modrinth.com/plugin/tab-was-taken

## 📂 Installation

1. Copy both JARs to your server's plugins folder:
   ```
   server/plugins/
   ├── TAB-v5.5.0.jar
   └── Quantum-1.0.1.jar
   ```

2. Start your server

3. Look for this message in console:
   ```
   [Quantum] ✓ TAB integration enabled! (v5.5.0, MiniMessage support available)
   ```

## 🎮 Using Custom Placeholders

The Quantum plugin registers these placeholders with TAB:

| Placeholder | Description |
|-------------|-------------|
| `%quantum_level%` | Player's Quantum level |
| `%quantum_job%` | Player's current job name |
| `%quantum_job_level%` | Player's job level |
| `%quantum_tower%` | Current tower ID |
| `%quantum_tower_floor%` | Tower floor progress |

### Example TAB Configuration

Edit `plugins/TAB/config.yml`:

```yaml
tablist-name-formatting:
  enabled: true
  prefix: "&7[Lv.%quantum_level%] "
  suffix: " &8| &e%quantum_job%"

nametags:
  enabled: true
  prefix: "&7[Lv.%quantum_level%] "

scoreboard:
  enabled: true
  lines:
    - "&eLevel: &f%quantum_level%"
    - "&eJob: &f%quantum_job% (Lv.%quantum_job_level%)"
    - "&eTower: &f%quantum_tower% - Floor %quantum_tower_floor%"
```

## ❓ Troubleshooting

### TAB Integration Not Working

1. **Check TAB is installed:**
   ```
   /plugins
   ```
   Look for TAB in green

2. **Check Quantum logs:**
   ```
   [Quantum] TAB plugin not found - TAB integration disabled
   ```
   → Install TAB plugin

3. **Verify version compatibility:**
   - TAB: v5.5.0 or compatible
   - Minecraft: 1.21.11
   - Paper API: 1.21.11-R0.1-SNAPSHOT

### Build Errors

**Error: Cannot resolve dependencies**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository/com/github/NEZNAMY

# Force update
mvn clean install -U
```

**Error: Network issues**
- Ensure internet connection
- Check firewall/proxy settings
- Maven needs access to: jitpack.io, repo.papermc.io

## 📚 Additional Resources

- **TAB Wiki**: https://github.com/NEZNAMY/TAB/wiki
- **TAB API Docs**: https://github.com/NEZNAMY/TAB/wiki/Developer-API
- **Full Integration Guide**: See `TAB_INTEGRATION.md`

## ✅ Verification Checklist

After installation:
- [ ] Both plugins show in `/plugins` as green
- [ ] Console shows TAB integration message
- [ ] Custom placeholders work in `/tab parse` command
- [ ] Placeholders update in TAB displays
- [ ] No errors in console

## 🆘 Support

If you encounter issues:
1. Check server logs for errors
2. Verify all versions are correct
3. Review `TAB_INTEGRATION.md` for detailed troubleshooting
4. Check TAB Discord: https://discord.gg/YPqXt63YQj

---

**Version**: TAB API 5.5.0 | Minecraft 1.21.11  
**Last Updated**: 2026-02-12
