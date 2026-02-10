# Quick Start - ModelEngine Health Bar Configuration

## TL;DR

To fix health bar positioning for ModelEngine mobs, add this to `mob_healthbar.yml`:

```yaml
"Your Mob Name":
  enabled: true
  modelengine_offset: 1.2  # Height in blocks above the mob
  # ... other settings ...
```

Then reload: `/quantum reload`

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
  modelengine_offset: 1.0  # 1 block above

# Medium guardian
"Gardien de l'Eau":
  modelengine_offset: 1.2  # 1.2 blocks above

# Large servant
"Serviteur d'Eau":
  modelengine_offset: 1.5  # 1.5 blocks above

# Boss knight
"⚔ Chevalier de l'Eau ⚔":
  modelengine_offset: 2.0  # 2 blocks above
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

Set a default for all ModelEngine mobs:

```yaml
global:
  default_modelengine_offset: 0.5
```

Individual mob settings override this global default.

## How It Works

The system uses **TextDisplay entities** (introduced in Minecraft 1.19.4) instead of custom names with newlines, because:
- Newlines in custom names stopped working in Minecraft 1.20.5+
- TextDisplay provides precise positioning
- The healthbar automatically follows the mob as it moves
- Updates 4 times per second for smooth tracking

## Troubleshooting

**Health bar doesn't move?**
1. Check that ModelEngine is installed
2. Verify mob has a ModelEngine model
3. Confirm mob name matches exactly (without color codes)
4. Run `/quantum reload` after config changes

**Health bar still wrong?**
- Adjust in increments of 0.1-0.2 blocks
- Remember: offset is the Y-coordinate above the entity
- Test with actual players to see final result

**Health bar doesn't follow mob?**
- Normal - it updates every 5 ticks (0.25 seconds)
- If it never follows, restart the server

## Need More Help?

See `MODELENGINE_HEALTHBAR.md` for complete documentation.
