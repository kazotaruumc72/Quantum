# BetterHud Integration for Quantum

## 📦 Plugin Information

- **Plugin Name**: BetterHud
- **Author**: @toxicity188
- **Version**: 1.14.1+
- **Minecraft**: 1.21.11
- **Purpose**: Custom HUD elements, popups, and waypoints

## 🔗 Download Links

- **SpigotMC**: https://www.spigotmc.org/resources/115559/
- **GitHub**: https://github.com/toxicity188/BetterHud
- **Documentation**: https://www.spigotmc.org/resources/115559/

## 📥 Installation

### 1. Download BetterHud
Download BetterHud v1.14.1 or later from SpigotMC.

### 2. Install Plugin
Place `BetterHud-1.14.1.jar` in your `server/plugins/` folder:
```
server/plugins/BetterHud-1.14.1.jar
```

### 3. Start Server
Start your server to generate default configurations.

### 4. Apply Quantum Configurations
Copy the example configurations from this directory:
```bash
cp examples/* /path/to/server/plugins/BetterHud/
```

Or manually copy each file:
- `config.yml` - Main BetterHud configuration
- `huds.yml` - HUD element definitions
- `popups.yml` - Popup configurations
- `compass.yml` - Waypoint/compass settings

### 5. Restart Server
Restart your server to apply the Quantum-specific configurations.

## 🎨 Available HUD Elements

The example configurations include the following HUD elements optimized for Quantum:

### Level Display
- Shows player level prominently
- Updates in real-time
- Configurable position and style

### Experience Bar
- Visual progress bar for experience
- Shows current EXP and EXP required
- Percentage display

### Job Information
- Current job name and level
- Job experience progress
- Color-coded by job type

### Storage Status
- Items in storage
- Storage capacity
- Usage percentage indicator

### Tower Progress
- Current tower and floor
- Tower name display
- Progress indicators

## 💬 Available Popups

The example configurations include these popup notifications:

### Level Up Popup
- Triggered when player levels up
- Shows new level with animation
- Congratulatory message

### Job Change Popup
- Displayed when changing jobs
- Shows new job name
- Welcome message

### Tower Floor Popup
- Shown when reaching a new floor
- Floor number display
- Encouragement message

### Storage Full Popup
- Warning when storage is full
- Current capacity shown
- Action prompt

## 🧭 Compass/Waypoint Features

### Quest Waypoints
- Dynamic waypoint markers
- Distance calculation
- Direction indicators

### Home Markers
- Show home locations
- Multiple home support
- Custom icons per home

### Tower Markers
- Tower entrance locations
- Floor markers
- Progress waypoints

## 📝 Configuration Files

### config.yml
Main BetterHud configuration optimized for Quantum integration.

**Key Settings**:
- HUD refresh rate: 1000ms (1 second)
- Popup cooldown: 100ms
- Resource pack: Auto-detect
- Language: en_US

### huds.yml
Defines all HUD elements that appear on player screens.

**Included Elements**:
- `quantum_level` - Player level display
- `quantum_exp` - Experience bar
- `quantum_job` - Job information
- `quantum_storage` - Storage status
- `quantum_tower` - Tower progress

### popups.yml
Configures popup messages for various events.

**Included Popups**:
- `level_up` - Level advancement
- `job_change` - Job selection
- `tower_floor` - Tower progression
- `storage_full` - Storage warning

### compass.yml
Sets up waypoint and compass functionality.

**Features**:
- Quest waypoints
- Home markers
- Tower locations
- Custom icons and colors

## 🔧 Customization

### Editing Positions
Modify HUD element positions in `huds.yml`:
```yaml
huds:
  quantum_level:
    x: 10
    y: 10
    anchor: TOP_LEFT
```

### Changing Colors
Customize colors in popup configurations:
```yaml
popups:
  level_up:
    color: "#FFD700"  # Gold
```

### Adding New Elements
Add custom HUD elements by following the BetterHud documentation and using Quantum placeholders.

## 🔖 Quantum Placeholders

All Quantum placeholders work in BetterHud configurations:

### Player Data
- `%quantum_level%`
- `%quantum_exp%`
- `%quantum_exp_required%`
- `%quantum_exp_progress%`

### Job System
- `%quantum_job%`
- `%quantum_job_level%`
- `%quantum_job_exp%`

### Tower System
- `%quantum_tower%`
- `%quantum_tower_name%`
- `%quantum_tower_floor%`

### Storage System
- `%quantum_storage_items%`
- `%quantum_storage_capacity%`
- `%quantum_storage_used_percent%`

## 🎯 Usage in Code

If you're developing with the Quantum API:

```java
// Get BetterHud manager
QuantumBetterHudManager hudManager = Quantum.getInstance().getBetterHudManager();

// Check if available
if (hudManager != null && hudManager.isAvailable()) {
    // Show a popup
    Map<String, String> variables = BetterHudUtil.createVariables(
        "player", player.getName(),
        "level", String.valueOf(playerLevel)
    );
    hudManager.showPopup(player, "level_up", variables);
    
    // Update HUD
    hudManager.updateHud(player, null, UpdateEvent.EMPTY);
}
```

## 🐛 Troubleshooting

### HUD Not Appearing
1. Verify BetterHud is installed and loaded
2. Check resource pack is enabled (BetterHud may require it)
3. Review console for BetterHud errors
4. Try `/betterhud reload` command

### Popups Not Showing
1. Check popup cooldown settings
2. Verify popup names match configuration
3. Test with simple popup first
4. Check player permissions

### Placeholders Not Working
1. Ensure Quantum is loaded before BetterHud
2. Verify placeholder syntax
3. Check console for integration messages
4. Try updating both plugins

### Configuration Errors
1. Validate YAML syntax (use online validator)
2. Check for tab/space mixing
3. Ensure UTF-8 encoding
4. Review BetterHud logs

## 📚 Additional Resources

### Documentation
- **Main Integration Guide**: [../../BETTERHUD_INTEGRATION.md](../../BETTERHUD_INTEGRATION.md)
- **Plugin Integrations Overview**: [../../PLUGIN_INTEGRATIONS.md](../../PLUGIN_INTEGRATIONS.md)
- **BetterHud Official Docs**: Check SpigotMC resource page

### Example Configurations
All example files are in the `examples/` directory:
- Full HUD layouts
- Popup templates
- Compass configurations
- Color schemes

### Support
- **Quantum Issues**: https://github.com/kazotaruumc72/Quantum/issues
- **BetterHud Support**: SpigotMC resource discussion

## 📝 Version History

### Quantum 1.0.1 - BetterHud 1.14.1
- ✅ Complete HUD integration
- ✅ Popup system with variables
- ✅ Compass/waypoint support
- ✅ 20+ Quantum placeholders
- ✅ Optimized caching system

## 👥 Credits

- **BetterHud Plugin**: toxicity188
- **Quantum Integration**: kazotaruumc72/Quantum team
- **Example Configurations**: Quantum development team

---

**For complete integration documentation, see [BETTERHUD_INTEGRATION.md](../../BETTERHUD_INTEGRATION.md)**
