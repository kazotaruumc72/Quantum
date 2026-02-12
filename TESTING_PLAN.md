# Testing Plan - Internal Region System

## Prerequisites

Before testing, ensure:
- Minecraft server 1.21.11 (Paper/Spigot)
- Java 21 installed
- Nexo plugin installed (required dependency)
- Compiled Quantum plugin JAR

## Test Scenarios

### Scenario 1: Without WorldGuard (Internal Regions)

#### Setup
1. Remove WorldGuard from server plugins folder
2. Configure a test tower with internal regions in `towers.yml`:

```yaml
towers:
  test_tower:
    name: "§eTest Tower"
    worldguard_region: "test_tower_global"
    min_level: 1
    max_level: 100
    
    floors:
      1:
        worldguard_region: "test_floor_1"
        region:
          world: "world"
          min: "100,60,100"   # Adjust to your test area
          max: "150,80,150"
        
        spawn:
          world: "world"
          x: 125.5
          y: 65.0
          z: 125.5
          yaw: 0.0
          pitch: 0.0
        
        spawners:
          test_mob:
            type: ZOMBIE
            model: "test_model"
            display_name: "&cTest Mob"
            base_health: 20.0
            damage: 2
            amount: 1
            interval: 30
            max_alive: 3
```

#### Tests

1. **Server Startup**
   - [ ] Server starts without errors
   - [ ] Console shows: "Internal Region Manager loaded! (X regions registered)"
   - [ ] Console shows: "WorldGuard not found - using internal region system"

2. **Region Entry Detection**
   - [ ] Walk into the defined region area
   - [ ] Message appears: "Tu entres dans Test Tower (Etage 1)"
   - [ ] Tower scoreboard activates

3. **Level Restrictions**
   - [ ] Create a test player with level < min_level
   - [ ] Try to enter the region
   - [ ] Entry should be blocked with level message
   - [ ] Test with level in range - entry should work
   - [ ] Test with level > max_level - entry should be blocked

4. **Region Exit Detection**
   - [ ] Walk out of the region
   - [ ] Message appears: "Tu quittes la tour"
   - [ ] Tower scoreboard deactivates

5. **Bypass Permission**
   - [ ] Give player `quantum.tower.bypass` permission
   - [ ] Entry should work regardless of level
   - [ ] Message shows: "[Bypass] Tu entres dans Test Tower"

6. **Death and Respawn**
   - [ ] Enter tower
   - [ ] Die within tower region
   - [ ] Kill counter should reset
   - [ ] Respawn outside tower
   - [ ] Tower state should be cleared

### Scenario 2: With WorldGuard (Compatibility Test)

#### Setup
1. Install WorldGuard
2. Create WorldGuard regions matching the tower configuration:
   ```
   /rg define test_tower_global
   /rg define test_floor_1
   ```
3. Use the same tower configuration from Scenario 1

#### Tests

1. **Server Startup**
   - [ ] Server starts without errors
   - [ ] Console shows: "WorldGuard detected - using WorldGuard for region detection"
   - [ ] Console shows: "WorldGuard integration enabled!"

2. **WorldGuard Priority**
   - [ ] Verify all region entry/exit tests from Scenario 1 still work
   - [ ] WorldGuard regions should be used instead of internal regions
   - [ ] Zone GUI command `/zonegui` should be available

3. **Region Detection**
   - [ ] Walk into WorldGuard region
   - [ ] Tower zone features activate
   - [ ] Walk out of WorldGuard region
   - [ ] Tower zone features deactivate

### Scenario 3: Mixed Configuration

#### Setup
1. Remove WorldGuard
2. Create both inline and external region definitions:

In `towers.yml`:
```yaml
floors:
  1:
    worldguard_region: "test_floor_1"
    region:
      world: "world"
      min: "100,60,100"
      max: "150,80,150"
```

In `regions.yml`:
```yaml
regions:
  test_floor_2:
    world: "world"
    min: "160,60,160"
    max: "210,80,210"
```

In `towers.yml` add floor 2:
```yaml
  2:
    worldguard_region: "test_floor_2"
    spawn:
      world: "world"
      x: 185.5
      y: 65.0
      z: 185.5
```

#### Tests
1. **Both Region Sources**
   - [ ] Both floor 1 and floor 2 regions should work
   - [ ] Console shows correct number of regions loaded
   - [ ] Can move between floors and see floor change messages

### Scenario 4: Edge Cases

#### Tests

1. **Invalid Coordinates**
   - [ ] Configure region with invalid format: `min: "abc,def,ghi"`
   - [ ] Server should start with warning
   - [ ] Region should not be registered
   - [ ] Console should show error message

2. **Missing World**
   - [ ] Configure region without world field
   - [ ] Server should start with warning
   - [ ] Region should not be registered

3. **Overlapping Regions**
   - [ ] Create two overlapping regions
   - [ ] First matching region should be used
   - [ ] Entry/exit should work correctly

4. **Region Boundaries**
   - [ ] Stand exactly on min coordinates
   - [ ] Should be inside region (inclusive)
   - [ ] Stand exactly on max coordinates
   - [ ] Should be inside region (inclusive)
   - [ ] Stand one block outside
   - [ ] Should be outside region

5. **World Mismatch**
   - [ ] Configure region for "world"
   - [ ] Try to enter from "world_nether"
   - [ ] Region should not detect entry

### Scenario 5: Performance

#### Tests

1. **Region Cache**
   - [ ] Enter and exit region multiple times quickly
   - [ ] No performance degradation
   - [ ] Console shows no repeated warnings

2. **Multiple Players**
   - [ ] Have 5+ players enter/exit regions simultaneously
   - [ ] All players should see correct messages
   - [ ] No cross-player interference

## Expected Console Messages

### Success Messages
```
✓ Internal Regions loaded! (X regions registered)
✓ Tower system loaded! (X tours)
✓ Integrated tower scoreboard ready!
Registered region: test_floor_1
```

### Warning Messages (Expected)
```
⚠ WorldGuard not found - using internal region system
Region test_floor_1 has no world specified
Region test_floor_1 has invalid coordinate format
```

### Debug Messages (if enabled)
```
Player PlayerName entered tower region: test_floor_1
Player PlayerName left tower region: test_floor_1
```

## Rollback Plan

If issues are found:
1. Restore WorldGuard plugin
2. Remove internal region definitions from config
3. Restart server
4. System should fall back to WorldGuard-only mode

## Notes for Developers

- Check console for any Java exceptions
- Monitor server TPS during tests
- Test with different player levels
- Verify tower progression is saved correctly
- Check that spawners activate properly
- Ensure scoreboards update correctly

## Success Criteria

The implementation is successful if:
- ✅ All tests pass in Scenario 1 (without WorldGuard)
- ✅ All tests pass in Scenario 2 (with WorldGuard)
- ✅ No errors in server console
- ✅ No performance degradation
- ✅ Existing WorldGuard setups continue to work
- ✅ Migration path from WorldGuard to internal is smooth
