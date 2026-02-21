# Internal Region System Documentation

## Overview

The Quantum plugin now supports tower zones without requiring WorldGuard as a dependency. The internal region system provides the same functionality for tower zone management while maintaining compatibility with WorldGuard if it's installed.

## How It Works

### Automatic Detection

The plugin automatically detects whether WorldGuard is available:

- **WorldGuard Available**: Uses WorldGuard's region system via reflection
- **WorldGuard Not Available**: Uses the internal region system

This allows seamless operation in both scenarios without any configuration changes.

### Internal Region System

The internal region system uses cuboid (rectangular box) regions defined by two corner points. Regions are checked when players move to determine if they've entered or exited a tower zone.

## Configuration

### Method 1: Define Regions in towers.yml

You can define regions directly in your tower floor configurations:

```yaml
towers:
  tower_water:
    name: "§b§lTour de l'Eau"
    min_level: 1
    max_level: 26
    
    floors:
      1:
        worldguard_region: "donjon_eau_1"  # Region name
        
        # Internal region definition (optional, only if not using WorldGuard)
        region:
          world: "world"
          min: "50,60,150"    # x,y,z coordinates of minimum corner
          max: "150,80,250"   # x,y,z coordinates of maximum corner
        
        spawn:
          world: "world"
          x: 100.5
          y: 65.0
          z: 200.5
          yaw: 90.0
          pitch: 0.0
        
        spawners:
          # ... spawner configuration
```

### Method 2: Define Regions in regions.yml

Alternatively, create a separate `regions.yml` file in your plugin data folder:

```yaml
regions:
  donjon_eau_1:
    world: "world"
    min: "50,60,150"
    max: "150,80,250"
  
  donjon_eau_2:
    world: "world"
    min: "160,70,160"
    max: "260,90,260"
  
  # Add more regions as needed
```

## Finding Region Coordinates

To define a region, you need to know the minimum and maximum corner coordinates:

### Option 1: Manual Calculation

1. Go to one corner of your tower zone area
2. Press F3 to see coordinates (x, y, z)
3. Note down the coordinates
4. Go to the opposite corner (diagonally)
5. Note down those coordinates
6. Use the smaller values for `min` and larger values for `max`

Example:
- Corner 1: x=50, y=60, z=150
- Corner 2: x=150, y=80, z=250
- Result: `min: "50,60,150"` and `max: "150,80,250"`

### Option 2: WorldEdit (if available)

1. Use WorldEdit's selection tools (`//wand` or `//pos1` and `//pos2`)
2. Select your region
3. Note the coordinates displayed
4. Use these coordinates in your configuration

## Region Format

### Coordinate Format

Coordinates must be in the format: `"x,y,z"` (comma-separated, in quotes)

- **x**: East-West position
- **y**: Height (vertical position)
- **z**: North-South position

### World Name

The world name must match exactly the name of your Minecraft world (usually "world", "world_nether", or "world_the_end").

## Migration from WorldGuard

If you currently use WorldGuard and want to migrate to the internal system:

1. **Keep your current configuration**: The `worldguard_region` field can stay as it is
2. **Add internal region definitions**: Add the `region` section to each floor
3. **Remove WorldGuard**: Once all regions are defined, you can safely remove WorldGuard
4. **Test**: The plugin will automatically use the internal regions

## Compatibility

### With WorldGuard Installed

- The plugin will prefer WorldGuard's region system
- Internal region definitions are ignored
- Zone GUI and other WorldGuard features remain available

### Without WorldGuard

- The plugin uses the internal region system
- All tower zone features work normally
- Zone GUI is not available (it requires WorldGuard)

## Features Supported

The internal region system fully supports:

- ✅ Zone entry/exit detection
- ✅ Level restrictions (min/max level checks)
- ✅ Tower progression tracking
- ✅ Kill tracking and spawner management
- ✅ Tower scoreboard display
- ✅ Death and respawn handling
- ✅ Region caching for performance

## Troubleshooting

### Regions Not Loading

Check the server console for messages like:
```
✓ Internal Regions loaded! (X regions registered)
```

If no regions are loaded:
1. Verify the `region` section exists in towers.yml or regions.yml
2. Check coordinate format (must be quoted: `"x,y,z"`)
3. Ensure world name matches exactly

### Players Can Enter Without Level Check

Verify that:
1. The region name in `worldguard_region` matches the region ID
2. The coordinates correctly cover the intended area
3. The tower has `min_level` and `max_level` configured

### Regions Overlapping

If regions overlap:
- The first matching region will be used
- This is the same behavior as WorldGuard
- Consider making regions non-overlapping for clarity

## Performance

The internal region system is designed for performance:

- Region checks only happen on block movement (not every tick)
- Results are cached using an LRU cache (100 entries)
- Coordinate checks use simple integer comparisons

## Example Full Configuration

```yaml
towers:
  tower_water:
    name: "§b§lTour de l'Eau"
    min_level: 1
    max_level: 26
    
    floors:
      1:
        worldguard_region: "donjon_eau_1"
        region:
          world: "world"
          min: "50,60,150"
          max: "150,80,250"
        spawn:
          world: "world"
          x: 100.5
          y: 65.0
          z: 200.5
        spawners:
          slime_pack_1:
            type: ZOMBIE
            model: "slime_basic"
            base_health: 40.0
            # ... more spawner config
      
      2:
        worldguard_region: "donjon_eau_2"
        region:
          world: "world"
          min: "160,70,160"
          max: "260,90,260"
        spawn:
          world: "world"
          x: 210.5
          y: 75.0
          z: 210.5
        spawners:
          # ... spawners for floor 2
```

## Support

For issues or questions about the internal region system:
1. Check the console logs for error messages
2. Verify your configuration format
3. Test with a simple single-region setup first
4. Consider using WorldGuard if you need advanced features like complex region shapes or inheritance
