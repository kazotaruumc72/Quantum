# Example Health Bar Configurations

This document provides example configurations demonstrating the fixes and new features added to address the health bar display issues.

## Issue 1: Display Only Health Number (PERCENTAGE_ONLY)

**Problem:** User wanted to display just "100" without the health bar visual `[|||||||||||||||||||||||||||]`

**Solution:** Use the new `PERCENTAGE_ONLY` format:

```yaml
# Example 1: Simple percentage display
"Test Mob 1":
  enabled: true
  format: PERCENTAGE_ONLY     # Shows only "100" (no bar!)
  hologram_offset: 1.0
  color_thresholds:
    75: "&a"   # Green at full health
    50: "&e"   # Yellow at 50%
    25: "&6"   # Orange at 25%
    0: "&c"    # Red at low health

# Example 2: Zombie with percentage only
ZOMBIE:
  enabled: true
  format: PERCENTAGE_ONLY
  hologram_offset: 1.0
  color_thresholds:
    75: "&a"
    50: "&e"
    25: "&6"
    0: "&c"
```

This will display:
- At 100% health: `100` (green)
- At 75% health: `75` (yellow)
- At 50% health: `50` (orange)
- At 25% health: `25` (red)

**No bar visual, just the number!**

## Issue 2: hologram_offset Now Works for All Mobs

**Problem:** `hologram_offset: 1.5` didn't work for regular mobs, only for ModelEngine mobs

**Solution:** The setting now works for ALL mobs, not just ModelEngine ones:

```yaml
# Regular Minecraft mob with custom offset
SKELETON:
  enabled: true
  hologram_offset: 1.5        # Works now!
  format: CLASSIC
  bar_length: 20
  show_percentage: true

# Custom mob without ModelEngine
"My Custom Mob":
  enabled: true
  hologram_offset: 1.8        # Position healthbar 1.8 blocks above
  format: PERCENTAGE_ONLY
  color_thresholds:
    75: "&b"
    50: "&3"
    25: "&9"
    0: "&1"

# Large mob that needs higher positioning
RAVAGER:
  enabled: true
  hologram_offset: 2.0        # Higher offset for large mob
  format: CLASSIC
  bar_length: 25
  show_percentage: true
  show_numeric: true
```

**Key Points:**
- `hologram_offset` now works for all entity types
- Works for both vanilla Minecraft mobs and custom mobs
- Works whether or not ModelEngine is installed
- The old `modelengine_offset` still works for backward compatibility

## Issue 3: Tab Completion for /quantum reload healthbar

**Problem:** The command `/quantum reload healthbar` was not tab-completable

**Solution:** Tab completion now works for all these variations:

```
/quantum reload healthbar       ✓ (now works!)
/quantum reload healthbars      ✓ (now works!)
/quantum reload mob_healthbar   ✓ (now works!)
/quantum reload mob_healthbar.yml ✓ (now works!)
```

Just type `/quantum reload hea` and press TAB to auto-complete!

## Complete Example Configuration

Here's a complete example showing all the fixes together:

```yaml
# In mob_healthbar.yml:

global:
  enabled: true
  max_distance: 50
  update_interval: 1
  hide_at_full_health: false
  show_only_in_combat: false
  hide_delay_after_combat: 5
  override_custom_names: false
  matching_mode: EXACT
  default_hologram_offset: 0.5

# Example 1: Zombie with percentage only (no bar)
ZOMBIE:
  enabled: true
  name_color: "&2"
  format: PERCENTAGE_ONLY      # NEW: Just shows the number!
  hologram_offset: 1.0         # FIXED: Works for all mobs now!
  color_thresholds:
    75: "&a"
    50: "&e"
    25: "&6"
    0: "&c"

# Example 2: Skeleton with higher offset
SKELETON:
  enabled: true
  name_color: "&7"
  hologram_offset: 1.5         # FIXED: Now applies correctly!
  format: CLASSIC
  bar_length: 20
  show_percentage: true
  color_thresholds:
    75: "&a"
    50: "&e"
    25: "&6"
    0: "&c"

# Example 3: Custom mob with percentage only
"Boss Monster":
  enabled: true
  name_color: "&c&l"
  hologram_offset: 2.5         # FIXED: Works without ModelEngine!
  format: PERCENTAGE_ONLY      # NEW: Clean percentage display
  color_thresholds:
    90: "&a"
    75: "&e"
    50: "&6"
    25: "&c"
    10: "&4"

# Example 4: Comparison of formats
"Test All Formats":
  enabled: true
  hologram_offset: 1.2
  
  # Try different formats:
  format: CLASSIC           # [||||||||||||] 100
  # format: HEARTS          # ❤❤❤❤❤♡♡♡♡♡
  # format: NUMERIC         # 20/20 HP
  # format: PERCENTAGE_ONLY # 100
  # format: BOSS_BAR        # Uses boss bar
  
  color_thresholds:
    75: "&a"
    50: "&e"
    25: "&6"
    0: "&c"
```

## Testing Your Configuration

After updating your `mob_healthbar.yml`:

1. Save the file
2. Run `/quantum reload healthbar` (with tab completion!)
3. Spawn or find the configured mob
4. Check the health display

## Troubleshooting

**Percentage only not showing?**
- Make sure you have `format: PERCENTAGE_ONLY` (case-sensitive)
- Check that the mob section is enabled
- Reload with `/quantum reload healthbar`

**hologram_offset not working?**
- Verify the offset value is greater than 0
- Try different values (0.5, 1.0, 1.5, 2.0)
- Make sure you reloaded after changes

**Tab completion not working?**
- Make sure you're using the latest version with the fix
- Try typing `/quantum reload hea` and press TAB
- Check console for any errors

## Summary of Changes

✅ **New Format:** `PERCENTAGE_ONLY` - displays only the health number without the bar
✅ **Fixed:** `hologram_offset` now works for ALL mobs (not just ModelEngine)
✅ **Fixed:** Tab completion for `/quantum reload healthbar` and variants
✅ **Backward Compatible:** Old `modelengine_offset` settings still work

These changes make it easier to customize health displays exactly how you want them!
