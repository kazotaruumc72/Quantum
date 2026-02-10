# Testing Guide - HealthBar Fix for Custom Model Mobs

## What Was Fixed

The healthbar system for ModelEngine custom model mobs was completely broken in Minecraft 1.21 because it used newlines in custom names, which no longer work.

**Old System (Broken in MC 1.21+):**
- Used `\n` characters in `setCustomName()` to create vertical offset
- Newlines stopped working in Minecraft 1.20.5+
- Healthbars appeared at wrong positions or showed literal "\n" text

**New System (Working in MC 1.21+):**
- Uses TextDisplay entities (introduced in MC 1.19.4)
- Precise Y-coordinate positioning (1.0 = 1 block above mob)
- Automatic position tracking (updates 4 times per second)
- Clean automatic cleanup on mob death

## Testing Checklist

### Basic Functionality
- [ ] Healthbars appear above custom ModelEngine mobs
- [ ] Healthbars show correct health percentage/bars
- [ ] ModelEngine indicator symbol (⚙) appears for custom model mobs
- [ ] Healthbars update when mob takes damage
- [ ] Healthbars update when mob heals
- [ ] Healthbars disappear when mob dies

### Position Testing
Test with various mob sizes configured in `mob_healthbar.yml`:

- [ ] **Small mobs** (offset 0.6-1.0): Healthbar positioned above model
- [ ] **Medium mobs** (offset 1.0-1.5): Healthbar clearly visible above
- [ ] **Large mobs** (offset 1.5-2.1): Healthbar well above large models
- [ ] **Boss mobs** (offset 2.0-2.7): Healthbar at appropriate height

### Movement Testing
- [ ] Healthbar follows mob when it walks
- [ ] Healthbar follows mob when it jumps
- [ ] Healthbar follows flying mobs smoothly
- [ ] Healthbar stays positioned correctly during combat
- [ ] No visual lag or stuttering in healthbar movement

### Configuration Testing
- [ ] `/quantum reload` updates healthbar configurations
- [ ] Changing `modelengine_offset` values takes effect after reload
- [ ] Global `default_modelengine_offset` works for unconfigured mobs
- [ ] Per-mob offsets override global default correctly

### Performance Testing
- [ ] Multiple mobs (10-20) with healthbars: No lag
- [ ] Mob spawning/despawning: No performance issues
- [ ] Server restart: Healthbars cleanup properly
- [ ] `/quantum reload`: No memory leaks or orphaned displays

### Edge Cases
- [ ] Mob teleportation: Healthbar follows
- [ ] Chunk loading/unloading: Healthbars handle correctly
- [ ] Mob death: TextDisplay entity removed immediately
- [ ] Plugin reload: Old displays cleaned up, new ones created
- [ ] ModelEngine model changes: Healthbar adjusts

### Visual Quality
- [ ] Healthbar always faces player (Billboard.CENTER working)
- [ ] Text is readable at various distances
- [ ] Colors display correctly
- [ ] Symbols (⚙, ❤, etc.) render properly
- [ ] No Z-fighting or visual glitches

## Known Limitations

1. **Update Frequency**: Healthbars update position every 5 ticks (0.25 seconds). Very fast movements may show slight lag, but this is intentional to balance performance.

2. **Client Rendering**: Some visual aspects depend on client settings (GUI scale, render distance). Test with different settings if possible.

3. **Backward Compatibility**: Old mobs with newlines in their names will have the newlines stripped. This is expected behavior.

## Configuration Examples for Testing

### Test Mob 1: Small Slime
```yaml
"Test Slime":
  enabled: true
  modelengine_offset: 0.8
  bar_length: 15
  show_percentage: true
```

### Test Mob 2: Medium Guardian
```yaml
"Test Guardian":
  enabled: true
  modelengine_offset: 1.3
  bar_length: 20
  show_percentage: true
  show_numeric: true
```

### Test Mob 3: Large Boss
```yaml
"Test Boss":
  enabled: true
  modelengine_offset: 2.2
  bar_length: 30
  show_percentage: true
  show_numeric: true
  format: CLASSIC
```

## Troubleshooting

### Healthbar not appearing
1. Check `mob_healthbar.yml` has `enabled: true` for the mob
2. Verify ModelEngine is installed and the mob has a model
3. Check server console for errors
4. Try `/quantum reload`

### Healthbar at wrong height
1. Adjust `modelengine_offset` in `mob_healthbar.yml`
2. Increase/decrease by 0.1-0.2 blocks at a time
3. Reload with `/quantum reload` after each change
4. Test with the actual model in-game

### Healthbar not following mob
1. Check if the mob is moving at all (some mobs are stationary)
2. Restart the server (update task may not have started)
3. Check server TPS (low TPS can delay updates)

### Performance issues
1. Check number of active mobs with healthbars
2. Reduce update frequency if needed (contact developer)
3. Monitor server TPS and RAM usage

## Success Criteria

The fix is successful if:
1. ✅ Healthbars appear correctly positioned for all ModelEngine mobs
2. ✅ Healthbars follow mobs smoothly during movement
3. ✅ No errors in server console related to healthbars
4. ✅ Configuration changes take effect after reload
5. ✅ No performance degradation with multiple mobs
6. ✅ Clean cleanup on mob death and server shutdown

## Reporting Issues

If you encounter any problems:
1. Note the exact mob name and configuration
2. Check server console for errors
3. Test with a simple configuration first
4. Provide screenshots if visual issues occur
5. Note server version and ModelEngine version
