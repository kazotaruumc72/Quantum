# Quick Start - ModelEngine Health Bar Configuration

## TL;DR

To fix health bar positioning for ModelEngine mobs, add this to `mob_healthbar.yml`:

```yaml
"Your Mob Name":
  enabled: true
  modelengine_offset: 1.2  # Adjust this value (0.3 = 1 line higher)
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

| Mob Size | Recommended Offset | Lines Added |
|----------|-------------------|-------------|
| Tiny (slime) | 0.6 - 0.9 | 2-3 lines |
| Small | 0.9 - 1.2 | 3-4 lines |
| Medium | 1.2 - 1.5 | 4-5 lines |
| Large | 1.5 - 2.1 | 5-7 lines |
| Boss | 2.1 - 2.7 | 7-9 lines |

## Examples from Config

```yaml
# Small water slime
"Slime d'Eau":
  modelengine_offset: 1.0

# Medium guardian
"Gardien de l'Eau":
  modelengine_offset: 1.2

# Large servant
"Serviteur d'Eau":
  modelengine_offset: 1.5

# Boss knight
"⚔ Chevalier de l'Eau ⚔":
  modelengine_offset: 2.0
```

## Adjustment Formula

```
New offset = Current offset ± 0.3
```

- Too low? Add 0.3
- Too high? Subtract 0.3
- Way off? Adjust by multiples of 0.3

## Global Default

Set a default for all ModelEngine mobs:

```yaml
global:
  default_modelengine_offset: 0.5
```

Individual mob settings override this global default.

## Troubleshooting

**Health bar doesn't move?**
1. Check that ModelEngine is installed
2. Verify mob has a ModelEngine model
3. Confirm mob name matches exactly (without color codes)
4. Run `/quantum reload` after config changes

**Health bar still wrong?**
- Adjust in increments of 0.3
- Client rendering varies - test with actual players
- Consider different player GUI scales

## Need More Help?

See `MODELENGINE_HEALTHBAR.md` for complete documentation.
