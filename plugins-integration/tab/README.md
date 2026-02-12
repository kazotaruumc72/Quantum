# TAB Integration for Quantum

## 📦 Plugin Information

- **Plugin Name**: TAB
- **Author**: NEZNAMY
- **Version**: 5.5.0+
- **Minecraft**: 1.21.11
- **Purpose**: Advanced tablist, nametags, and scoreboard

## 🔗 Download Links

- **GitHub**: https://github.com/NEZNAMY/TAB
- **GitHub Releases**: https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
- **SpigotMC**: https://www.spigotmc.org/resources/57806/
- **Wiki**: https://github.com/NEZNAMY/TAB/wiki

## 📥 Installation

### 1. Download TAB
Download TAB v5.5.0 or later from GitHub releases or SpigotMC.

### 2. Install Plugin
Place `TAB-v5.5.0.jar` in your `server/plugins/` folder:
```
server/plugins/TAB-v5.5.0.jar
```

### 3. Start Server
Start your server to generate default configurations.

### 4. Configure TAB
TAB creates its configuration in `/plugins/TAB/`. You can use Quantum placeholders in any TAB configuration file.

### 5. Apply Quantum Examples
Review the example configurations in the `examples/` directory for ideas on using Quantum placeholders with TAB.

## ✨ Integration Features

### Automatic Placeholder Registration
Quantum automatically registers all its placeholders with TAB when both plugins are loaded. No additional configuration needed!

### MiniMessage Support
TAB 5.5.0+ includes MiniMessage support, allowing modern text formatting:
```
<gradient:blue:aqua>%quantum_level%</gradient>
```

### Real-time Updates
TAB automatically updates when Quantum data changes:
- Player levels up → tablist updates
- Job changes → nametag updates
- Tower progress → scoreboard updates

## 🔖 Available Quantum Placeholders

All Quantum placeholders work in TAB configurations:

### Player Level & Experience
```
%quantum_level%
%quantum_exp%
%quantum_exp_required%
%quantum_exp_progress%
```

### Job System
```
%quantum_job%
%quantum_job_level%
%quantum_job_exp%
```

### Tower System
```
%quantum_tower%
%quantum_tower_name%
%quantum_tower_floor%
```

### Storage System
```
%quantum_storage_items%
%quantum_storage_capacity%
%quantum_storage_used_percent%
```

### Statistics
```
%quantum_orders_created%
%quantum_orders_filled%
%quantum_items_sold%
%quantum_items_bought%
```

### Home System
```
%quantum_homes%
%quantum_homes_max%
```

## 📝 Configuration Examples

### Tablist Header/Footer
Edit `/plugins/TAB/config.yml`:

```yaml
header-footer:
  enabled: true
  header:
    - "&7&m--------------&r &6Quantum Server &7&m--------------"
    - "&7Level: &e%quantum_level% &7| Job: &e%quantum_job%"
  footer:
    - "&7Tower Floor: &e%quantum_tower_floor%"
    - "&7&m----------------------------------------"
```

### Player Tablist Name
```yaml
tablist-name-formatting:
  enabled: true
  format: "&e[%quantum_level%] &f%player%"
```

### Nametags
Edit `/plugins/TAB/config.yml`:

```yaml
nametags:
  enabled: true
  prefix: "&7[Lvl %quantum_level%] "
  suffix: " &e%quantum_job%"
```

### Scoreboard
Create `/plugins/TAB/scoreboard.yml`:

```yaml
scoreboard:
  enabled: true
  title: "&6&lQuantum Stats"
  lines:
    - "&7Level: &e%quantum_level%"
    - "&7EXP: &e%quantum_exp%/%quantum_exp_required%"
    - "&7Job: &e%quantum_job% &7(Lv.%quantum_job_level%)"
    - "&7Tower: &e%quantum_tower_name%"
    - "&7Floor: &e%quantum_tower_floor%"
    - "&7Storage: &e%quantum_storage_items%/%quantum_storage_capacity%"
```

### Player Sorting
Sort players by level in tablist:

```yaml
tablist-sorting:
  enabled: true
  sorting-rules:
    - "placeholder:%quantum_level%"
    - "player_name"
```

## 🎨 Advanced Formatting

### MiniMessage Examples
```yaml
# Gradient text
format: "<gradient:blue:aqua>%quantum_level%</gradient>"

# Hover text
format: "<hover:show_text:'Level %quantum_level%'>%player%</hover>"

# Click actions
format: "<click:run_command:/quantum stats>Click for stats</click>"

# Combined
format: "<hover:show_text:'&7Job: %quantum_job%\n&7Level: %quantum_job_level%'><gradient:gold:yellow>[%quantum_level%]</gradient></hover> %player%"
```

### Conditional Formatting
Use TAB's conditional display feature:

```yaml
tablist-name-formatting:
  format: "%condition:quantum_level>50%&6&lPRO &r%player%%else%&7%player%%end%"
```

### Per-World Configurations
Show different data based on world:

```yaml
per-world-playerlist:
  enabled: true
  world_nether:
    format: "&c[%quantum_tower_floor%] &f%player%"
  world_the_end:
    format: "&d[%quantum_level%] &f%player%"
```

## 🔧 Customization Tips

### Performance Optimization
Set appropriate refresh intervals in TAB config:

```yaml
placeholderapi-refresh-intervals:
  quantum_level: 5000  # Update every 5 seconds
  quantum_exp: 5000
  quantum_job: -1      # Update only on change
```

### Color Schemes
Use consistent colors for Quantum data:
- Level: `&e` (yellow)
- Job: `&b` (aqua)
- Tower: `&d` (light purple)
- Storage: `&a` (green)

### Layout Best Practices
- Keep tablist clean and readable
- Use gradients sparingly
- Test on different screen sizes
- Consider colorblind players

## 🎯 Usage Examples

### Example 1: Ranked Tablist
```yaml
tablist-name-formatting:
  format: "&e[%quantum_level%] &7%player% &8| &b%quantum_job%"
```

Result: `[42] Steve | Miner`

### Example 2: Job-Based Prefix
```yaml
nametags:
  prefix: "&7[%quantum_job%] "
```

Result: `[Farmer] Alex`

### Example 3: Stats Sidebar
```yaml
scoreboard:
  title: "&6&l⚡ Quantum"
  lines:
    - "&7━━━━━━━━━━━━━━"
    - "&eLevel: &f%quantum_level%"
    - "&eEXP: &f%quantum_exp_progress%&7%"
    - "&7━━━━━━━━━━━━━━"
    - "&bJob: &f%quantum_job%"
    - "&bLevel: &f%quantum_job_level%"
    - "&7━━━━━━━━━━━━━━"
    - "&dTower: &f%quantum_tower_name%"
    - "&dFloor: &f%quantum_tower_floor%"
    - "&7━━━━━━━━━━━━━━"
```

## 🔄 Integration with Other Features

### LuckPerms Integration
Combine with permission group prefixes:

```yaml
nametags:
  prefix: "%luckperms-prefix%&7[Lv.%quantum_level%] "
```

### World Names
Show world with Quantum data:

```yaml
tablist-name-formatting:
  format: "%world% &7[%quantum_level%] &f%player%"
```

### Player Count
Use TAB's placeholders with Quantum's:

```yaml
header:
  - "&6&lQuantum Server &7(%online%/%max%)"
  - "&7Average Level: &e%avg_level%" # Use custom calculation
```

## 🐛 Troubleshooting

### Placeholders Not Working
1. Verify both TAB and Quantum are loaded
2. Check `/tab debug` output
3. Ensure placeholder syntax is correct
4. Check console for integration messages

### Formatting Issues
1. Validate YAML syntax
2. Check color code compatibility (& vs §)
3. Test MiniMessage syntax
4. Review TAB wiki for format specs

### Performance Issues
1. Increase refresh intervals for static data
2. Disable unused TAB features
3. Optimize placeholder complexity
4. Check server TPS

### Display Problems
1. Clear client resource packs
2. Restart client
3. Check TAB's client-side features
4. Verify Minecraft version compatibility

## 📚 Additional Resources

### Documentation
- **Main Integration Guide**: [../../TAB_INTEGRATION.md](../../TAB_INTEGRATION.md)
- **Plugin Integrations Overview**: [../../PLUGIN_INTEGRATIONS.md](../../PLUGIN_INTEGRATIONS.md)
- **TAB Wiki**: https://github.com/NEZNAMY/TAB/wiki

### Example Configurations
All example files are in the `examples/` directory:
- Complete TAB configurations
- Quantum-optimized layouts
- Color schemes
- Format templates

### Support
- **Quantum Issues**: https://github.com/kazotaruumc72/Quantum/issues
- **TAB Discord**: Check GitHub for invite link
- **TAB Issues**: https://github.com/NEZNAMY/TAB/issues

## 📝 Version History

### Quantum 1.0.1 - TAB 5.5.0
- ✅ Full placeholder integration
- ✅ MiniMessage support
- ✅ 20+ Quantum placeholders
- ✅ Automatic registration
- ✅ Real-time updates

## 👥 Credits

- **TAB Plugin**: NEZNAMY
- **Quantum Integration**: kazotaruumc72/Quantum team
- **Example Configurations**: Quantum development team

---

**For complete integration documentation, see [TAB_INTEGRATION.md](../../TAB_INTEGRATION.md)**
