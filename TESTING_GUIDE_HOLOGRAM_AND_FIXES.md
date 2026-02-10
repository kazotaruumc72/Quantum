# Testing Guide: Hologram Offset & Tower Fixes

This guide provides comprehensive testing instructions for the changes made in this PR.

## Summary of Changes

### 1. ✅ Hologram Offset (renamed from modelengine_offset)
- **What changed**: Configuration field renamed from `modelengine_offset` to `hologram_offset`
- **Backward compatibility**: Old `modelengine_offset` still works
- **Files affected**: `mob_healthbar.yml`, `HealthBarManager.java`, `MODELENGINE_HEALTHBAR.md`

### 2. ✅ Healthbar Percentage Format
- **What changed**: Percentage display changed from "100.0%" to "100%"
- **Files affected**: `HealthBarManager.java`

### 3. ✅ Door Block Despawn Fix
- **What changed**: Door blocks now properly preserve their state when despawning/respawning
- **Files affected**: `TowerDoorManager.java`

### 4. ✅ Mob Kill Counter Fix
- **What changed**: Tower scoreboard now shows real-time kill counts during tower gameplay
- **Files affected**: `TowerManager.java`

---

## Testing Instructions

### Test 1: Hologram Offset Configuration

#### Objective
Verify that healthbars can be positioned using the new `hologram_offset` field and that backward compatibility with `modelengine_offset` works.

#### Steps
1. **Stop the server** if running
2. **Edit `mob_healthbar.yml`**:
   - Find a mob configuration (e.g., "Slime d'Eau")
   - Change `hologram_offset: 1.0` to `hologram_offset: 2.0`
3. **Start the server**
4. **Spawn the mob** in-game (or enter a tower floor with that mob)
5. **Verify** the healthbar appears **higher** above the mob

#### Expected Result
✅ Healthbar should be positioned 2.0 blocks above the mob (higher than before)

#### Test Backward Compatibility
1. **Edit `mob_healthbar.yml`**:
   - Change `hologram_offset: 2.0` back to `modelengine_offset: 1.5`
2. **Reload** with `/quantum reload`
3. **Verify** the healthbar still works and is positioned at 1.5 blocks

#### Expected Result
✅ Old configuration field still works

---

### Test 2: Percentage Format

#### Objective
Verify that healthbar percentages display as "100%" instead of "100.0%"

#### Steps
1. **Spawn any mob** with a healthbar enabled
2. **Look at the mob** to see the healthbar
3. **Check the percentage display**

#### Expected Result
✅ Percentage should show "100%" not "100.0%"
✅ At 50% health, should show "50%" not "50.0%"

---

### Test 3: Door Block Despawn (CRITICAL FIX)

#### Objective
Verify that tower doors properly despawn for 30 seconds after completing a floor objective, then respawn with correct orientation.

#### Setup
1. **Ensure you have a tower configured** in `towers.yml`
2. **Ensure doors are set up** for at least one floor (use `/quantum door pos1` and `/quantum door pos2` to select, then `/quantum door create <tower_id> <floor>`)
3. **The door should have complex blocks** like doors, gates, or trapdoors with specific orientations

#### Steps
1. **Enter the tower floor** with a configured door
2. **Complete the floor objective** (kill all required mobs)
3. **Observe the door blocks**:
   - They should **disappear** immediately
   - A message should appear: "§a§l✓ La porte s'ouvre!"
4. **Wait 30 seconds**
5. **Observe the door blocks**:
   - They should **reappear** with the **exact same orientation/state** as before

#### Expected Result
✅ Door blocks despawn immediately upon floor completion
✅ Door blocks respawn after exactly 30 seconds
✅ **IMPORTANT**: Door orientation/rotation/hinge position is preserved (this was the bug - doors were losing their BlockData)
✅ Complex blocks like double doors, iron doors, gates maintain all properties

#### What Was Fixed
- **Before**: Doors would respawn but lose their orientation/rotation due to using deprecated `getData()` method
- **After**: Doors preserve full BlockData including orientation, rotation, hinge position, etc.

---

### Test 4: Mob Kill Counter (CRITICAL FIX)

#### Objective
Verify that the kill counter updates in real-time during tower gameplay.

#### Prerequisites
1. **Ensure `towers_scoreboard.yml` exists** and is configured
2. **Check that PlaceholderAPI is installed**
3. **Verify tower scoreboard is enabled** in the configuration

#### Steps
1. **Enter a tower floor**
   - Use `/quantum tower <tower_id> <floor>` or use the tower entrance NPC
2. **Check your scoreboard**:
   - You should see a **tower-specific scoreboard** appear
   - It should show lines like:
     ```
     Kills: 0/5
     Floor: 1
     ```
3. **Kill one mob**
4. **Immediately check the scoreboard**:
   - The kill count should update within 1 second
   - Example: "Kills: 1/5"
5. **Kill more mobs** and verify the counter updates each time
6. **Complete the floor** (kill all required mobs)
7. **Check that the door opens** and scoreboard resets for next floor

#### Expected Result
✅ Tower scoreboard appears when entering a floor
✅ Kill counter updates within 1 second of each kill
✅ Counter shows current/required kills (e.g., "3/10")
✅ Scoreboard disappears when leaving the tower
✅ Main scoreboard returns when exiting the tower

#### What Was Fixed
- **Before**: Default scoreboard was disabled when entering tower, but no tower scoreboard was shown, so kill counts were invisible
- **After**: `TowerScoreboardHandler.enableTowerScoreboard()` is now called, showing a real-time updating scoreboard

#### Troubleshooting
If scoreboard doesn't show:
1. Check console for errors
2. Verify `towers_scoreboard.yml` exists in plugins/Quantum/
3. Check that PlaceholderAPI is installed
4. Try `/quantum reload`

---

## Configuration Examples

### Example 1: Custom Hologram Offsets

```yaml
# mob_healthbar.yml

"Small Spider":
  enabled: true
  hologram_offset: 0.8  # Lower healthbar for small mob
  
"Medium Guardian":
  enabled: true
  hologram_offset: 1.2  # Standard height
  
"Large Boss":
  enabled: true
  hologram_offset: 2.5  # Much higher for large boss
```

### Example 2: Tower Scoreboard Configuration

```yaml
# towers_scoreboard.yml

update_interval: 5  # Update every 5 ticks (4 times/second)

default:
  title: "<gold><bold>TOUR</bold></gold>"
  lines:
    - ""
    - "<gray>Floor:</gray> <white>%quantum_tower_floor%</white>"
    - "<gray>Kills:</gray> <white>%quantum_tower_kills_progress%</white>"
    - ""
    
tower_water:
  title: "<aqua><bold>Tour de l'Eau</bold></aqua>"
  lines:
    - ""
    - "<gray>Étage:</gray> <white>%quantum_tower_floor%</white>"
    - "<gray>Mobs tués:</gray> <white>%quantum_tower_kills_progress%</white>
    - "<gray>Progression:</gray> <white>%quantum_tower_percentage%%</white>"
    - ""
```

---

## Verification Checklist

After testing, verify:

- [ ] Healthbars position correctly with `hologram_offset`
- [ ] Old `modelengine_offset` configurations still work (backward compatibility)
- [ ] Percentages show as "100%" not "100.0%"
- [ ] Door blocks despawn after floor completion
- [ ] Door blocks respawn after 30 seconds
- [ ] **Door orientation/rotation is preserved** (check doors, gates, trapdoors)
- [ ] Tower scoreboard appears when entering a floor
- [ ] Kill counter updates in real-time
- [ ] Tower scoreboard disappears when leaving
- [ ] Main scoreboard returns when exiting tower

---

## Known Issues & Notes

### Hologram Offset
- Only applies to mobs with healthbars enabled
- Requires `/quantum reload` after config changes
- Offset is always positive (healthbar above mob)

### Door Despawn
- Doors must be configured using `/quantum door` commands
- 30-second timer is hardcoded (not configurable)
- Doors only despawn after completing floor objectives

### Kill Counter
- Requires PlaceholderAPI to be installed
- Scoreboard update interval configurable in `towers_scoreboard.yml`
- Only tracks kills of mobs with `tower_mob` metadata
- Kill counter resets when entering a new floor

---

## Performance Notes

All changes are optimized for performance:
- **Hologram offset**: No performance impact (configuration change only)
- **Percentage format**: Slightly faster (no decimal formatting)
- **Door despawn**: More efficient (uses modern BlockData API)
- **Kill counter**: Minimal impact (scoreboard updates on configurable interval)

---

## Rollback Instructions

If issues occur, rollback by:

1. **Revert to previous version**:
   ```bash
   git checkout ec78d8b  # Commit before changes
   mvn clean package
   ```

2. **Or manually revert specific changes**:
   - `mob_healthbar.yml`: Change `hologram_offset` back to `modelengine_offset`
   - Remove tower scoreboard activation in `TowerManager.java`

---

## Support

For issues or questions:
1. Check console logs for errors
2. Use `/quantum debug` for verbose logging
3. Report issues with full error logs

---

**Last Updated**: 2026-02-10
**PR Branch**: `copilot/add-hologram-offset-healthbar`
