# Quick Start - Health Bar Configuration

## TL;DR

To adjust health bar positioning for any mob, add this to `mob_healthbar.yml`:

```yaml
"Your Mob Name":
  enabled: true
  hologram_offset: 1.2  # Height in blocks above the mob
  # ... other settings ...
```

Then reload: `/quantum reload healthbar`

## Display Formats

Choose how the health bar looks:

```yaml
format: CLASSIC            # [||||||||||||] 100
format: HEARTS            # ❤❤❤❤❤♡♡♡♡♡
format: NUMERIC           # 20/20 HP
format: PERCENTAGE_ONLY   # 100 (just the number, no bar!)
format: BOSS_BAR          # Uses Minecraft's boss bar
```

**New in this update:** `PERCENTAGE_ONLY` displays only the health number without the bar visual!

## Visual Indicator

Mobs with ModelEngine models automatically display a ⚙ symbol at the start of their health bar, making them easy to identify.

**Customize the indicator in `mob_healthbar.yml`:**
```yaml
symbols:
  modelengine:
    indicator: "⚙"  # Change to any symbol: ⚡, ✦, ⬟, ●, ◆
    color: "&7"     # Color code
```

## Common Values

| Mob Size | Recommended Offset | Description |
|----------|-------------------|-------------|
| Tiny (slime) | 0.6 - 0.9 | Small models |
| Small | 0.9 - 1.2 | Standard small mobs |
| Medium | 1.2 - 1.5 | Human-sized mobs |
| Large | 1.5 - 2.1 | Large creatures |
| Boss | 2.1 - 2.7 | Very large bosses |

## Examples from Config

```yaml
# Small water slime
"Slime d'Eau":
  hologram_offset: 1.0  # 1 block above

# Medium guardian
"Gardien de l'Eau":
  hologram_offset: 1.2  # 1.2 blocks above

# Large servant
"Serviteur d'Eau":
  hologram_offset: 1.5  # 1.5 blocks above

# Boss knight
"⚔ Chevalier de l'Eau ⚔":
  hologram_offset: 2.0  # 2 blocks above
  
# Example with percentage-only display
"Simple Zombie":
  hologram_offset: 1.0
  format: PERCENTAGE_ONLY  # Shows only "100" without the bar
```

## Adjustment Formula

```
New offset = Current offset ± 0.1 to 0.2
```

- Too low? Add 0.1-0.2
- Too high? Subtract 0.1-0.2
- Way off? Adjust by larger increments (0.5)

**Note:** The offset is in Minecraft blocks. 1.0 = 1 block above the entity.

## Global Default

Set a default for all mobs (including ModelEngine mobs):

```yaml
global:
  default_hologram_offset: 0.5
```

Individual mob settings override this global default.

**Note:** The old `modelengine_offset` settings still work for backward compatibility, but `hologram_offset` is now the recommended setting and works for all mobs.

## How It Works

The system uses **TextDisplay entities** (introduced in Minecraft 1.19.4) instead of custom names with newlines, because:
- Newlines in custom names stopped working in Minecraft 1.20.5+
- TextDisplay provides precise positioning
- The healthbar automatically follows the mob as it moves
- Updates 4 times per second for smooth tracking

## Troubleshooting

**Health bar doesn't move?**
1. Check configuration in `mob_healthbar.yml`
2. Verify mob name matches exactly (without color codes)
3. Run `/quantum reload healthbar` after config changes (now with tab completion!)
4. If using ModelEngine, check that ModelEngine is installed

**Health bar still wrong?**
- Adjust in increments of 0.1-0.2 blocks
- Remember: offset is the Y-coordinate above the entity
- Test with actual players to see final result

**Health bar doesn't follow mob?**
- Normal - it updates every 5 ticks (0.25 seconds)
- If it never follows, restart the server

## Need More Help?

See `MODELENGINE_HEALTHBAR.md` for complete documentation.
